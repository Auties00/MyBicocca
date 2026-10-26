package it.attendance100.mybicocca.codegen.core

import it.attendance100.mybicocca.codegen.model.ApiSpec
import it.attendance100.mybicocca.codegen.model.SpecValidator
import java.nio.file.Path
import kotlin.io.path.absolute
import kotlin.io.path.invariantSeparatorsPathString
import kotlin.io.path.relativeTo

/**
 * Runs the pipeline for a set of APIs: for each, read its specs, merge them, apply the effects
 * overlay and validate the model, then generate and sync each requested target, and its live
 * tests when the API has a `tests.yaml` and the target a test output directory, and its
 * resources when it declares some (see [ResourceData]); finally sync
 * the files the targets share in each output directory they wrote to, and the files derived from
 * the APIs' files there (see [CodeTarget.sharedFiles], [CodeTarget.indexFiles] and
 * [CodeTarget.sharedTestFiles]).
 */
class Generator(
    private val registry: Plugins = PluginRegistry,
    private val log: (String) -> Unit = ::println,
) {
    /**
     * @param specDirs Directories each holding an API's `codegen.json`.
     * @param targetIds Targets to run; all configured targets when null.
     * @param check When true, nothing is written: the returned plans describe what would change.
     * @param refreshResources When true, the data of the APIs' resources is fetched again first,
     *   with their builders, and saved in their spec directories; never when [check].
     * @return One plan per API and target run, then one per output directory with shared files.
     */
    fun run(specDirs: List<Path>, targetIds: Set<String>? = null, check: Boolean = false, refreshResources: Boolean = false): List<SyncPlan> {
        val directories = mutableMapOf<Path, Directory>()
        val plans = specDirs.flatMap { specDir ->
            runApi(specDir, targetIds, check, refreshResources && !check) { outputDir, run -> directories.getOrPut(outputDir) { Directory() }.add(run) }
        }
        return plans + directories.mapNotNull { (outputDir, directory) -> syncShared(outputDir, directory, check) }
    }

    /** What the APIs' runs left in an output directory, from which its shared files follow. */
    private class Directory {
        /** The targets that wrote to the directory, by id, with the options of one of their APIs. */
        val targets = mutableMapOf<String, TargetRun>()
        val shared = mutableMapOf<String, GeneratedFile>()
        val produced = mutableSetOf<String>()
        val deleted = mutableSetOf<String>()

        fun add(run: TargetRun) {
            targets.putIfAbsent(run.target.id, run)
            for (file in run.shared) {
                val previous = shared.putIfAbsent(file.path, file)
                require(previous == null || previous == file) { "APIs generated into one directory disagree on the shared file ${file.path}" }
            }
            produced += run.files.map { it.path }
            deleted += run.plan.deletions.map { it.relativeTo(run.plan.outputDir).invariantSeparatorsPathString }
        }
    }

    /**
     * A target's run for one API into one directory: its SDK or its tests.
     *
     * @property shared The files the target shares in the directory.
     * @property index The files derived from the APIs' files in the directory.
     */
    private class TargetRun(
        val target: CodeTarget,
        val context: TargetContext,
        val files: List<GeneratedFile>,
        val plan: SyncPlan,
        val shared: List<GeneratedFile>,
        val index: (Set<String>) -> List<GeneratedFile>,
    )

    private fun syncShared(outputDir: Path, directory: Directory, check: Boolean): SyncPlan? {
        // The APIs' files once the run is applied, including those of APIs the run skipped.
        val paths = OutputSync.generatedPaths(outputDir, exceptOwner = SHARED_FILES_OWNER) - directory.deleted + directory.produced
        val files = directory.shared.values + directory.targets.values.flatMap { it.index(paths) }
        // A directory whose targets share no files is left alone, rather than cleared of the shared
        // files of targets this run skipped.
        if (files.isEmpty()) return null
        val plan = OutputSync.plan(outputDir, files, SHARED_FILES_OWNER)
        if (!check) OutputSync.apply(plan)
        log("Shared files: ${files.size} files, ${plan.writes.size} to write, ${plan.deletions.size} to delete ($outputDir)")
        return plan
    }

    /** The live tests the `tests.yaml` of the API in [specDir] plans, or null when it has none. Writes nothing. */
    fun plan(specDir: Path): TestPlan? {
        val spec = read(specDir, CodegenConfig.load(specDir.resolve(CodegenConfig.FILE_NAME)), check = true)
        return LiveTestsConfig.load(specDir)?.let { TestPlanner(spec, it).plan() }
    }

    /** Reads and merges the API's specs, applies its effects overlay and validates the result. */
    private fun read(specDir: Path, config: CodegenConfig, check: Boolean): ApiSpec {
        val merged = ApiSpec.merge(config.specReaders.map { readerConfig ->
            registry.reader(readerConfig.type).read(ReaderContext(specDir, readerConfig.options, mayUpdateSpecFiles = !check, log))
        })
        val spec = EffectsOverlay.apply(merged, specDir)
        SpecValidator.requireValid(spec)
        log("Read API '${spec.id}': ${spec.modules.size} modules, ${spec.declarations.size} types, ${spec.modules.sumOf { it.operations.size }} operations")
        return spec
    }

    private fun runApi(
        specDir: Path,
        targetIds: Set<String>?,
        check: Boolean,
        refreshResources: Boolean,
        record: (outputDir: Path, run: TargetRun) -> Unit,
    ): List<SyncPlan> {
        val config = CodegenConfig.load(specDir.resolve(CodegenConfig.FILE_NAME))
        val unknownTargets = targetIds.orEmpty() - config.targets.keys
        require(unknownTargets.isEmpty()) { "Targets not configured in $specDir: $unknownTargets" }
        val spec = read(specDir, config, check)
        val tests = LiveTestsConfig.load(specDir)?.let { TestPlanner(spec, it).plan() }
        tests?.let { log("Planned the live tests of '${spec.id}': ${it.cases.size} cases, ${it.skips.size} operations skipped") }
        val resources = ResourceData.load(specDir, spec, refreshResources, registry, log)

        return config.targets
            .filterKeys { targetIds == null || it in targetIds }
            .flatMap { (id, targetConfig) ->
                val target = registry.target(id)
                fun resolve(dir: String) = specDir.resolve(dir).absolute().normalize()
                val resourceDir = targetConfig.resourceOutputDir ?: targetConfig.outputDir
                val context = TargetContext(targetConfig.options, resolve(targetConfig.outputDir), targetConfig.testOutputDir?.let(::resolve), resolve(resourceDir), log)
                fun sync(files: List<GeneratedFile>, dir: String, shared: List<GeneratedFile>, index: (Set<String>) -> List<GeneratedFile>, what: String): SyncPlan {
                    val outputDir = resolve(dir)
                    val plan = OutputSync.plan(outputDir, files, spec.id)
                    if (!check) OutputSync.apply(plan)
                    record(outputDir, TargetRun(target, context, files, plan, shared, index))
                    log("  $what: ${files.size} files, ${plan.writes.size} to write, ${plan.deletions.size} to delete ($outputDir)")
                    return plan
                }
                val resourceFiles = if (resources.isEmpty()) emptyList() else target.generateResources(spec, resources, context)
                // Resources written next to the code are synced with it, so that neither sync deletes the other's files.
                val besideCode = resolve(resourceDir) == resolve(targetConfig.outputDir)
                listOfNotNull(
                    sync(target.generate(spec, context) + resourceFiles.takeIf { besideCode }.orEmpty(), targetConfig.outputDir, target.sharedFiles(context), { target.indexFiles(it, context) }, id),
                    resourceFiles.takeIf { !besideCode && it.isNotEmpty() }?.let { sync(it, resourceDir, emptyList(), { emptyList() }, "$id resources") },
                    tests?.let { plan ->
                        targetConfig.testOutputDir?.let { dir -> sync(target.generateTests(plan, context), dir, target.sharedTestFiles(context), { emptyList() }, "$id tests") }
                    },
                )
            }
    }
}

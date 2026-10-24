package it.attendance100.mybicocca.codegen.core

import it.attendance100.mybicocca.codegen.model.ApiSpec
import it.attendance100.mybicocca.codegen.model.SpecValidator
import java.nio.file.Path
import kotlin.io.path.absolute
import kotlin.io.path.invariantSeparatorsPathString
import kotlin.io.path.relativeTo

/**
 * Runs the pipeline for a set of APIs: for each, read its specs, merge and validate the model,
 * then generate and sync each requested target; finally sync the files the targets share in each
 * output directory they wrote to, and the files derived from the APIs' files there (see
 * [CodeTarget.sharedFiles] and [CodeTarget.indexFiles]).
 */
class Generator(
    private val registry: Plugins = PluginRegistry,
    private val log: (String) -> Unit = ::println,
) {
    /**
     * @param specDirs Directories each holding an API's `codegen.json`.
     * @param targetIds Targets to run; all configured targets when null.
     * @param check When true, nothing is written: the returned plans describe what would change.
     * @return One plan per API and target run, then one per output directory with shared files.
     */
    fun run(specDirs: List<Path>, targetIds: Set<String>? = null, check: Boolean = false): List<SyncPlan> {
        val directories = mutableMapOf<Path, Directory>()
        val plans = specDirs.flatMap { specDir ->
            runApi(specDir, targetIds, check) { outputDir, run -> directories.getOrPut(outputDir) { Directory() }.add(run) }
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
            for (file in run.target.sharedFiles(run.context)) {
                val previous = shared.putIfAbsent(file.path, file)
                require(previous == null || previous == file) { "APIs generated into one directory disagree on the shared file ${file.path}" }
            }
            produced += run.files.map { it.path }
            deleted += run.plan.deletions.map { it.relativeTo(run.plan.outputDir).invariantSeparatorsPathString }
        }
    }

    private class TargetRun(val target: CodeTarget, val context: TargetContext, val files: List<GeneratedFile>, val plan: SyncPlan)

    private fun syncShared(outputDir: Path, directory: Directory, check: Boolean): SyncPlan? {
        // The APIs' files once the run is applied, including those of APIs the run skipped.
        val paths = OutputSync.generatedPaths(outputDir, exceptOwner = SHARED_FILES_OWNER) - directory.deleted + directory.produced
        val files = directory.shared.values + directory.targets.values.flatMap { it.target.indexFiles(paths, it.context) }
        // A directory whose targets share no files is left alone, rather than cleared of the shared
        // files of targets this run skipped.
        if (files.isEmpty()) return null
        val plan = OutputSync.plan(outputDir, files, SHARED_FILES_OWNER)
        if (!check) OutputSync.apply(plan)
        log("Shared files: ${files.size} files, ${plan.writes.size} to write, ${plan.deletions.size} to delete ($outputDir)")
        return plan
    }

    private fun runApi(
        specDir: Path,
        targetIds: Set<String>?,
        check: Boolean,
        record: (outputDir: Path, run: TargetRun) -> Unit,
    ): List<SyncPlan> {
        val config = CodegenConfig.load(specDir.resolve(CodegenConfig.FILE_NAME))
        val unknownTargets = targetIds.orEmpty() - config.targets.keys
        require(unknownTargets.isEmpty()) { "Targets not configured in $specDir: $unknownTargets" }

        val spec = ApiSpec.merge(config.specReaders.map { readerConfig ->
            registry.reader(readerConfig.type).read(ReaderContext(specDir, readerConfig.options, mayUpdateSpecFiles = !check, log))
        })
        SpecValidator.requireValid(spec)
        log("Read API '${spec.id}': ${spec.modules.size} modules, ${spec.declarations.size} types, ${spec.modules.sumOf { it.operations.size }} operations")

        return config.targets
            .filterKeys { targetIds == null || it in targetIds }
            .map { (id, targetConfig) ->
                val target = registry.target(id)
                val context = TargetContext(targetConfig.options, log)
                val files = target.generate(spec, context)
                val outputDir = specDir.resolve(targetConfig.outputDir).absolute().normalize()
                val plan = OutputSync.plan(outputDir, files, spec.id)
                if (!check) OutputSync.apply(plan)
                record(outputDir, TargetRun(target, context, files, plan))
                log("  $id: ${files.size} files, ${plan.writes.size} to write, ${plan.deletions.size} to delete ($outputDir)")
                plan
            }
    }
}

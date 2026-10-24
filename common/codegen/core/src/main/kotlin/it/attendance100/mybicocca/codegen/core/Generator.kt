package it.attendance100.mybicocca.codegen.core

import it.attendance100.mybicocca.codegen.model.ApiSpec
import it.attendance100.mybicocca.codegen.model.SpecValidator
import java.nio.file.Path
import kotlin.io.path.absolute

/**
 * Runs the pipeline for a set of APIs: for each, read its specs, merge and validate the model,
 * then generate and sync each requested target; finally sync the files the targets share in each
 * output directory they wrote to (see [CodeTarget.sharedFiles]).
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
        val shared = mutableMapOf<Path, MutableMap<String, GeneratedFile>>()
        val plans = specDirs.flatMap { specDir ->
            runApi(specDir, targetIds, check) { outputDir, files ->
                val inDir = shared.getOrPut(outputDir) { mutableMapOf() }
                for (file in files) {
                    val previous = inDir.putIfAbsent(file.path, file)
                    require(previous == null || previous == file) { "APIs generated into $outputDir disagree on the shared file ${file.path}" }
                }
            }
        }
        // A directory whose targets share no files is left alone, rather than cleared of the shared
        // files of targets this run skipped.
        return plans + shared.filterValues { it.isNotEmpty() }.map { (outputDir, files) ->
            val plan = OutputSync.plan(outputDir, files.values.toList(), SHARED_FILES_OWNER)
            if (!check) OutputSync.apply(plan)
            log("Shared files: ${files.size} files, ${plan.writes.size} to write, ${plan.deletions.size} to delete ($outputDir)")
            plan
        }
    }

    private fun runApi(
        specDir: Path,
        targetIds: Set<String>?,
        check: Boolean,
        share: (outputDir: Path, files: List<GeneratedFile>) -> Unit,
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
                share(outputDir, target.sharedFiles(context))
                val plan = OutputSync.plan(outputDir, files, spec.id)
                if (!check) OutputSync.apply(plan)
                log("  $id: ${files.size} files, ${plan.writes.size} to write, ${plan.deletions.size} to delete ($outputDir)")
                plan
            }
    }
}

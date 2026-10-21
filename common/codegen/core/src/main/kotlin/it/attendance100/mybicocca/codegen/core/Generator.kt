package it.attendance100.mybicocca.codegen.core

import it.attendance100.mybicocca.codegen.model.SpecValidator
import java.nio.file.Path
import kotlin.io.path.absolute

/**
 * Runs the pipeline for one API: read its spec, validate the model, then generate and sync
 * each requested target.
 */
class Generator(
    private val registry: PluginRegistry = PluginRegistry,
    private val log: (String) -> Unit = ::println,
) {
    /**
     * @param specDir Directory holding the API's `codegen.json`.
     * @param targetIds Targets to run; all configured targets when null.
     * @param check When true, nothing is written: the returned plans describe what would change.
     * @return One plan per target run.
     */
    fun run(specDir: Path, targetIds: Set<String>? = null, check: Boolean = false): List<SyncPlan> {
        val config = CodegenConfig.load(specDir.resolve(CodegenConfig.FILE_NAME))
        val unknownTargets = targetIds.orEmpty() - config.targets.keys
        require(unknownTargets.isEmpty()) { "Targets not configured in $specDir: $unknownTargets" }

        val reader = registry.reader(config.reader.type)
        val spec = reader.read(ReaderContext(specDir, config.reader.options, mayUpdateSpecFiles = !check, log))
        SpecValidator.requireValid(spec)
        log("Read API '${spec.id}': ${spec.modules.size} modules, ${spec.declarations.size} types, ${spec.modules.sumOf { it.operations.size }} operations")

        return config.targets
            .filterKeys { targetIds == null || it in targetIds }
            .map { (id, targetConfig) ->
                val target = registry.target(id)
                val files = target.generate(spec, TargetContext(targetConfig.options, log))
                val outputDir = specDir.resolve(targetConfig.outputDir).absolute().normalize()
                val plan = OutputSync.plan(outputDir, files, spec.id)
                if (!check) OutputSync.apply(plan)
                log("  $id: ${files.size} files, ${plan.writes.size} to write, ${plan.deletions.size} to delete ($outputDir)")
                plan
            }
    }
}

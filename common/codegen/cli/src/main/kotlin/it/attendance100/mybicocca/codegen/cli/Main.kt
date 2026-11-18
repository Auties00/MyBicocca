package it.attendance100.mybicocca.codegen.cli

import it.attendance100.mybicocca.codegen.core.CodegenConfig
import it.attendance100.mybicocca.codegen.core.Generator
import it.attendance100.mybicocca.codegen.core.SyncPlan
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlin.io.path.isDirectory
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name
import kotlin.system.exitProcess

private const val USAGE = """
Usage: codegen [--check] [--target <id>]... [<spec dir>...]

Generates the SDKs of the APIs whose spec directories (each holding a codegen.json) are
given, or of every API under ./specs when none is.

  --check         Write nothing; exit with status 1 if any generated file is out of date.
  --target <id>   Only run this target (repeatable). Default: every configured target.
"""

fun main(args: Array<String>) {
    val arguments = try {
        Arguments.parse(args.toList())
    } catch (e: IllegalArgumentException) {
        System.err.println(e.message)
        System.err.println(USAGE.trimIndent())
        exitProcess(2)
    }

    val plans = Generator().run(arguments.specDirs, arguments.targets, arguments.check)

    if (arguments.check) {
        val stale = plans.filterNot(SyncPlan::isUpToDate)
        if (stale.isNotEmpty()) {
            System.err.println("Generated code is out of date; run codegen without --check:")
            for (plan in stale) {
                plan.writes.forEach { System.err.println("  changed: ${plan.outputDir.resolve(it.path)}") }
                plan.deletions.forEach { System.err.println("  stale:   $it") }
            }
            exitProcess(1)
        }
        println("Generated code is up to date.")
    }
}

private data class Arguments(
    val specDirs: List<Path>,
    val targets: Set<String>?,
    val check: Boolean,
) {
    companion object {
        private val DEFAULT_SPECS_ROOT = Path("specs")

        fun parse(args: List<String>): Arguments {
            var check = false
            val targets = mutableSetOf<String>()
            val specDirs = mutableListOf<Path>()
            val iterator = args.iterator()
            while (iterator.hasNext()) {
                when (val arg = iterator.next()) {
                    "--check" -> check = true
                    "--target" -> {
                        require(iterator.hasNext()) { "--target needs a target id" }
                        targets += iterator.next()
                    }
                    else -> {
                        require(!arg.startsWith("--")) { "Unknown option: $arg" }
                        specDirs.add(Path(arg))
                    }
                }
            }
            return Arguments(
                specDirs = specDirs.toList().ifEmpty { allSpecDirs() },
                targets = targets.ifEmpty { null },
                check = check,
            )
        }

        private fun allSpecDirs(): List<Path> {
            require(DEFAULT_SPECS_ROOT.isDirectory()) { "No spec directories given and ./$DEFAULT_SPECS_ROOT doesn't exist" }
            return DEFAULT_SPECS_ROOT.listDirectoryEntries()
                .filter { it.resolve(CodegenConfig.FILE_NAME).exists() }
                .sortedBy { it.name }
        }
    }
}

package it.attendance100.mybicocca.codegen.cli

import com.github.ajalt.clikt.core.CoreCliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.ProgramResult
import com.github.ajalt.clikt.core.UsageError
import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.choice
import com.github.ajalt.clikt.parameters.types.path
import it.attendance100.mybicocca.codegen.core.CodegenConfig
import it.attendance100.mybicocca.codegen.core.Generator
import it.attendance100.mybicocca.codegen.core.PluginRegistry
import it.attendance100.mybicocca.codegen.core.SyncPlan
import it.attendance100.mybicocca.codegen.core.TestPlanReport
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlin.io.path.isDirectory
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name

fun main(args: Array<String>) = Codegen().main(args)

private class Codegen : CoreCliktCommand(name = "codegen") {
    init {
        // Without Mordant, Clikt leaves exiting to the caller; CI relies on the status of --check.
        context { exitProcess = { status -> kotlin.system.exitProcess(status) } }
    }

    private val check by option("--check", help = "Write nothing; exit with status 1 if any generated file is out of date.")
        .flag()

    private val plan by option("--plan", help = "Write nothing; print the live tests each API's tests.yaml plans, and the operations they skip.")
        .flag()

    private val targets by option("--target", metavar = "<id>", help = "Only run this target (repeatable). Default: every configured target.")
        .choice(*PluginRegistry.targets.keys.sorted().toTypedArray())
        .multiple()

    private val specDirs by argument(name = "spec dir", help = "Directories each holding an API's codegen.json.")
        .path(mustExist = true, canBeFile = false)
        .multiple()

    override fun help(context: Context): String =
        "Generates the SDKs of the APIs whose spec directories are given, or of every API under ./$DEFAULT_SPECS_ROOT when none is."

    override fun run() {
        if (plan) {
            for (specDir in specDirs.ifEmpty { allSpecDirs() }) {
                Generator(log = {}).plan(specDir)?.let { echo(TestPlanReport.text(it)) }
            }
            return
        }
        val plans = Generator().run(specDirs.ifEmpty { allSpecDirs() }, targets.toSet().ifEmpty { null }, check)
        if (!check) return

        val stale = plans.filterNot(SyncPlan::isUpToDate)
        if (stale.isNotEmpty()) {
            echo("Generated code is out of date; run codegen without --check:", err = true)
            for (plan in stale) {
                plan.writes.forEach { echo("  changed: ${plan.outputDir.resolve(it.path)}", err = true) }
                plan.deletions.forEach { echo("  stale:   $it", err = true) }
            }
            throw ProgramResult(1)
        }
        echo("Generated code is up to date.")
    }

    private fun allSpecDirs(): List<Path> {
        if (!DEFAULT_SPECS_ROOT.isDirectory()) throw UsageError("No spec directories given and ./$DEFAULT_SPECS_ROOT doesn't exist")
        return DEFAULT_SPECS_ROOT.listDirectoryEntries()
            .filter { it.resolve(CodegenConfig.FILE_NAME).exists() }
            .sortedBy { it.name }
    }

    private companion object {
        val DEFAULT_SPECS_ROOT = Path("specs")
    }
}

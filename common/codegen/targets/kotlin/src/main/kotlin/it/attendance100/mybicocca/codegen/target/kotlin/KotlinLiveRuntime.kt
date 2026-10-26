package it.attendance100.mybicocca.codegen.target.kotlin

import it.attendance100.mybicocca.codegen.core.GeneratedFile
import it.attendance100.mybicocca.codegen.core.resourceFiles

/**
 * The runtime the live tests [TestEmitter] writes build on: the JUnit base class running cases,
 * the run's clients, budget and sources, the checks of responses against the spec, the canonical
 * form of results, the journal of changes to undo and the report. [KotlinTarget] writes it once
 * per test output directory as its shared test files.
 *
 * The sources are this module's `kotlin-live` resources, in the `live` package next to the APIs'
 * runtime.
 */
internal object KotlinLiveRuntime {
    private val FILES = listOf("Live.kt", "LiveSupport.kt")

    fun files(runtimePackage: String): List<GeneratedFile> {
        val packageName = "$runtimePackage.live"
        return resourceFiles(javaClass, "kotlin-live", FILES, packageName.replace('.', '/')) {
            it.replace("__PACKAGE__", packageName).replace("__RUNTIME__", runtimePackage)
        }
    }
}

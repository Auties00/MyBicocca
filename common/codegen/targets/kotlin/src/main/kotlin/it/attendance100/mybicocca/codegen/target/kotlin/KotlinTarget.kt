package it.attendance100.mybicocca.codegen.target.kotlin

import it.attendance100.mybicocca.codegen.core.CodeTarget
import it.attendance100.mybicocca.codegen.core.GeneratedFile
import it.attendance100.mybicocca.codegen.core.TargetContext
import it.attendance100.mybicocca.codegen.core.TestPlan
import it.attendance100.mybicocca.codegen.core.decodeOptions
import it.attendance100.mybicocca.codegen.model.ApiSpec
import kotlinx.serialization.Serializable

/**
 * Generates a Kotlin/JVM SDK built on Ktor and kotlinx.serialization, on top of the
 * [KotlinRuntime] it writes as its shared files. See [KotlinLayout] for where declarations go.
 */
class KotlinTarget : CodeTarget {
    override val id: String = "kotlin"

    /**
     * @property packageName Root package of the API (e.g. `…data.remote.esse3`).
     * @property runtimePackage Package of the shared runtime; by default `runtime` next to the
     *   API's package (e.g. `…data.remote.runtime`).
     * @property temporalSerializers The serializer for each date format the API uses.
     */
    @Serializable
    data class Options(
        val packageName: String,
        val runtimePackage: String = packageName.substringBeforeLast('.') + ".runtime",
        val temporalSerializers: List<TemporalSerializer> = emptyList(),
    )

    override fun generate(spec: ApiSpec, context: TargetContext): List<GeneratedFile> {
        val options = context.options.decodeOptions<Options>()
        val layout = KotlinLayout(spec, options.packageName, options.runtimePackage)
        val client = ClientEmitter(spec, layout)
        val serializers = KotlinSerializers(spec, layout, TemporalSerializers(options.temporalSerializers))
        val records = RecordEmitter(spec, layout, serializers)
        val scrapers = ScrapeEmitter(spec, layout, records)
        val services = ServiceEmitter(spec, layout, scrapers, serializers)

        fun file(packageName: String, fileName: String, emit: (KotlinFile) -> Unit): GeneratedFile =
            KotlinFile(packageName, fileName, spec.id).also(emit).toGeneratedFile()

        return buildList {
            for (module in spec.modules) {
                if (module.records.isNotEmpty()) {
                    add(file(layout.dtoPackage, layout.recordsFileName(module)) { file -> file.body.separated(module.records) { records.emit(it, file) } })
                }
                if (module.operations.isNotEmpty()) {
                    add(file(layout.apiPackage, "${layout.serviceName(module)}.kt") { services.emit(module, it) })
                }
            }
            if (spec.enums.isNotEmpty()) {
                add(file(layout.dtoPackage, layout.enumsFileName) { file -> file.body.separated(spec.enums) { EnumEmitter.emit(it, file) } })
            }
            if (spec.authSchemes.isNotEmpty()) {
                add(file(layout.dtoPackage, layout.authFileName) { AuthEmitter(spec, layout).emit(it) })
            }
            if (serializers.isNeeded) {
                add(file(layout.dtoPackage, serializers.fileName) { serializers.emit(it) })
            }
            if (scrapers.isNeeded) {
                add(file(layout.dtoPackage, scrapers.fileName) { scrapers.emit(it) })
            }
            if (spec.servers.isNotEmpty()) {
                add(file(layout.apiPackage, "${layout.server.substringAfterLast('.')}.kt") { ServerEmitter(spec, layout).emit(it) })
            }
            add(file(layout.apiPackage, "${layout.facadeName}.kt") { FacadeEmitter(spec, layout).emit(it) })
            add(file(layout.apiPackage, "${layout.runtime.substringAfterLast('.')}.kt") { client.emitRuntime(it) })
            add(file(layout.exception.substringBeforeLast('.'), "${layout.exception.substringAfterLast('.')}.kt") { client.emitException(it) })
            if (spec.usesRoles) {
                add(file(layout.dtoPackage, layout.rolesFileName) { client.emitRoles(it) })
            }
        }
    }

    /** The live tests of the API, in the `live` package next to its own (see [TestEmitter]). */
    override fun generateTests(plan: TestPlan, context: TargetContext): List<GeneratedFile> {
        val options = context.options.decodeOptions<Options>()
        val layout = KotlinLayout(plan.spec, options.packageName, options.runtimePackage)
        return TestEmitter(plan, layout, "${options.packageName}.live", "${options.runtimePackage}.live").files()
    }

    /** The [KotlinLiveRuntime], in the `live` package next to the APIs' runtime. */
    override fun sharedTestFiles(context: TargetContext): List<GeneratedFile> =
        KotlinLiveRuntime.files(context.options.decodeOptions<Options>().runtimePackage)

    /** The [KotlinRuntime], in the APIs' runtime package. */
    override fun sharedFiles(context: TargetContext): List<GeneratedFile> =
        KotlinRuntime.files(context.options.decodeOptions<Options>().runtimePackage)
}

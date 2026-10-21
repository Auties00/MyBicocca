package it.attendance100.mybicocca.codegen.target.kotlin

import it.attendance100.mybicocca.codegen.core.CodeTarget
import it.attendance100.mybicocca.codegen.core.GeneratedFile
import it.attendance100.mybicocca.codegen.core.TargetContext
import it.attendance100.mybicocca.codegen.core.decodeOptions
import it.attendance100.mybicocca.codegen.model.ApiSpec
import kotlinx.serialization.Serializable

/**
 * Generates a Kotlin/JVM SDK built on Ktor and kotlinx.serialization. See [KotlinLayout] for
 * where declarations go and which runtime declarations the API must provide by hand.
 */
class KotlinTarget : CodeTarget {
    override val id: String = "kotlin"

    /**
     * @property packageName Root package of the API (e.g. `…data.remote.esse3`).
     * @property temporalSerializers The runtime's serializer for each date format the API uses.
     */
    @Serializable
    data class Options(
        val packageName: String,
        val temporalSerializers: List<TemporalSerializer> = emptyList(),
    )

    override fun generate(spec: ApiSpec, context: TargetContext): List<GeneratedFile> {
        val options = context.options.decodeOptions<Options>()
        val layout = KotlinLayout(spec, options.packageName)
        val serializers = KotlinSerializers(spec, layout, TemporalSerializers(options.temporalSerializers))
        val records = RecordEmitter(spec, layout, serializers)
        val scrapers = ScrapeEmitter(spec, layout, records)
        val services = ServiceEmitter(spec, layout, scrapers)

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
        }
    }

    private fun file(packageName: String, fileName: String, emit: (KotlinFile) -> Unit): GeneratedFile =
        KotlinFile(packageName, fileName).also(emit).toGeneratedFile()
}

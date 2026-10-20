package it.attendance100.mybicocca.codegen.reader.moodle

import it.attendance100.mybicocca.codegen.core.ReaderContext
import it.attendance100.mybicocca.codegen.core.SpecReader
import it.attendance100.mybicocca.codegen.core.decodeOptions
import it.attendance100.mybicocca.codegen.model.ApiSpec
import kotlinx.serialization.Serializable
import kotlin.io.path.name

/**
 * Reads Moodle web service functions from a dump of a Moodle source tree (made by
 * `common/specs/moodle/tools/dump-functions.php`), plus the overlay files naming modules,
 * types and members (see [Overlay]). Runs that may update spec files sync the overlays with
 * the dump.
 */
class MoodleReader : SpecReader {
    override val id: String = "moodle"

    /**
     * @property dump The function dump, relative to the spec directory.
     * @property id The API's id.
     * @property prefix The prefix of the API's declarations.
     */
    @Serializable
    data class Options(
        val dump: String = "functions.json",
        val modules: String = "modules.json",
        val types: String = "types.json",
        val names: String = "names.json",
        val id: String = "moodle",
        val prefix: String = "Moodle",
    )

    override fun read(context: ReaderContext): ApiSpec {
        val options = context.options.decodeOptions<Options>()
        val dump = MoodleDump.load(context.specDir.resolve(options.dump))
        val modulesFile = context.specDir.resolve(options.modules)
        val typesFile = context.specDir.resolve(options.types)
        val namesFile = context.specDir.resolve(options.names)
        val modules = Overlay.load(modulesFile)
        val types = Overlay.load(typesFile)
        val names = Overlay.load(namesFile)

        val builder = MoodleModelBuilder(dump, options.id, options.prefix, modules, types, names)
        val spec = builder.build()
        context.log("Read Moodle ${dump.moodle}: ${dump.functions.size} functions")

        if (context.mayUpdateSpecFiles) {
            for ((file, count) in listOf(
                modulesFile to modules.sync(modulesFile, builder.components),
                typesFile to types.sync(typesFile, builder.structurePaths),
                namesFile to names.sync(namesFile, builder.wireNames),
            )) {
                context.log("Synced ${file.name}: $count entries")
            }
        }
        return spec
    }
}

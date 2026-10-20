package it.attendance100.mybicocca.codegen.target.web

import it.attendance100.mybicocca.codegen.model.EnumType
import it.attendance100.mybicocca.codegen.target.web.TsSyntax.doc

/**
 * Emits an enum as a union of its wire values (plus any string when open) and a same-named
 * `const` naming each value, so callers write `Esse3CareerStatus.Active` and compare plain
 * strings.
 */
internal class TsEnumEmitter(private val layout: TsLayout) {

    fun emit(enum: EnumType, file: TsFile) {
        val out = file.body
        val literals = enum.values.joinToString(" | ") { TsSyntax.string(it.wireValue) }
        out.doc(enum.description)
        out.line("export type ${enum.name} = $literals${if (enum.open) " | (string & {})" else ""};")
        out.line()
        out.block("export const ${enum.name} = {", "} as const;") {
            for (value in enum.values) {
                doc(value.description)
                line("${TsSyntax.propertyKey(value.name)}: ${TsSyntax.string(value.wireValue)},")
            }
        }
        out.line()
        layout.importRuntime(file, "decodeWireEnum")
        out.block("export function ${layout.decoderName(enum.name)}(json: unknown): ${enum.name} | undefined {") {
            line("return decodeWireEnum(json, Object.values(${enum.name}), { open: ${enum.open}, acceptsWrappedValue: ${enum.acceptsWrappedValue} });")
        }
    }
}

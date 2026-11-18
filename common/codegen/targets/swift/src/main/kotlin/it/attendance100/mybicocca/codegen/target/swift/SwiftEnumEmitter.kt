package it.attendance100.mybicocca.codegen.target.swift

import it.attendance100.mybicocca.codegen.core.CodeWriter
import it.attendance100.mybicocca.codegen.model.EnumType
import it.attendance100.mybicocca.codegen.target.swift.SwiftSyntax.doc

/**
 * Emits enums conforming to the runtime's `WireEnum`, which provides `Codable` from
 * `init(wireValue:)` and `wireValue`. An open enum keeps values it doesn't know in
 * `unknown(String)`; a closed one fails to decode them.
 */
internal class SwiftEnumEmitter(private val layout: SwiftLayout) {

    fun emit(enum: EnumType, out: CodeWriter) {
        val cases = layout.caseNames(enum)
        // A declared value named "unknown" takes the place of the catch-all case.
        val declaresUnknown = UNKNOWN in cases.values
        val hasCatchAll = enum.open && !declaresUnknown

        out.doc(enum.description)
        out.block("public enum ${enum.name}: WireEnum {") {
            for (value in enum.values) {
                doc(value.description)
                line("case ${SwiftSyntax.identifier(cases.getValue(value.name))}")
            }
            if (hasCatchAll) {
                line("/// A value this SDK doesn't know yet.")
                line("case $UNKNOWN(String)")
            }
            if (enum.acceptsWrappedValue) {
                line()
                line("public static let acceptsWrappedValue = true")
            }
            if (enum.integer) {
                line()
                line("public static let isInteger = true")
            }
            line()
            block(if (enum.open) "public init(wireValue: String) {" else "public init?(wireValue: String) {") {
                // Swift aligns `case` labels with their `switch`.
                line("switch wireValue {")
                for (value in enum.values) {
                    line("case ${SwiftSyntax.string(value.wireValue)}: self = .${SwiftSyntax.identifier(cases.getValue(value.name))}")
                }
                line(
                    when {
                        !enum.open -> "default: return nil"
                        declaresUnknown -> "default: self = .$UNKNOWN"
                        else -> "default: self = .$UNKNOWN(wireValue)"
                    }
                )
                line("}")
            }
            line()
            block("public var wireValue: String {") {
                line("switch self {")
                for (value in enum.values) {
                    line("case .${SwiftSyntax.identifier(cases.getValue(value.name))}: return ${SwiftSyntax.string(value.wireValue)}")
                }
                if (hasCatchAll) line("case .$UNKNOWN(let value): return value")
                line("}")
            }
        }
    }

    private companion object {
        const val UNKNOWN = "unknown"
    }
}

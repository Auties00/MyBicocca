package it.attendance100.mybicocca.codegen.target.kotlin

import it.attendance100.mybicocca.codegen.core.CodeWriter
import it.attendance100.mybicocca.codegen.model.ApiSpec
import it.attendance100.mybicocca.codegen.model.AuthScheme
import it.attendance100.mybicocca.codegen.target.kotlin.KotlinSyntax.doc

/**
 * Emits the auth schemes: a sealed interface whose implementations hold each scheme's
 * credentials and turn them into the headers (and form fields) to send. Operations accepting
 * several schemes take the interface, so callers can only pass one set of credentials.
 */
internal class AuthEmitter(private val spec: ApiSpec, private val layout: KotlinLayout) {

    fun emit(file: KotlinFile) {
        file.body.apply {
            line("/** Credentials accepted by ${spec.prefix} operations. */")
            block("sealed interface ${layout.authBase} {") {
                line("/** The headers that authenticate a request. */")
                line("val headers: Map<String, String>")
                if (spec.authSchemes.any { it.form.isNotEmpty() }) {
                    line()
                    line("/** The form fields that authenticate a request. */")
                    line("val form: Map<String, String> get() = emptyMap()")
                }
            }
            for (scheme in spec.authSchemes) {
                line()
                scheme(scheme)
            }
        }
    }

    private fun CodeWriter.scheme(scheme: AuthScheme) {
        val name = layout.authClass(scheme.name)
        doc(scheme.description)
        val parameters = scheme.parameters.map { "val ${KotlinSyntax.identifier(it.name)}: String" }
        commaList("data class $name(", parameters, ") : ${layout.authBase} {")
        indent {
            commaList("override val headers: Map<String, String> get() = mapOf(", scheme.headers.map { (header, template) ->
                "${KotlinSyntax.string(header)} to ${template(template)}"
            }, ")")
            if (scheme.form.isNotEmpty()) {
                commaList("override val form: Map<String, String> get() = mapOf(", scheme.form.map { (field, template) ->
                    "${KotlinSyntax.string(field)} to ${template(template)}"
                }, ")")
            }
        }
        line("}")
    }

    /** A header value template (`Bearer {token}`) as a Kotlin string template. */
    private fun template(template: String): String {
        val literals = PLACEHOLDER.split(template).map(KotlinSyntax::stringContent)
        val placeholders = PLACEHOLDER.findAll(template).map { "\${${KotlinSyntax.identifier(it.groupValues[1])}}" }.toList()
        return "\"" + literals.zip(placeholders + "").joinToString("") { (literal, placeholder) -> literal + placeholder } + "\""
    }

    private companion object {
        val PLACEHOLDER = Regex("""\{([^}]+)}""")
    }
}

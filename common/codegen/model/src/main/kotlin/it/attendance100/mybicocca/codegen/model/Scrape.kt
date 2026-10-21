package it.attendance100.mybicocca.codegen.model

/**
 * How a value is read out of an HTML document: a pipeline of steps, each taking the previous
 * step's output, which targets compile to native code over their HTML parser.
 *
 * A step turns a missing input into a missing output, so a pipeline yields no value as soon as
 * something isn't found; only [Exists] turns a missing element into `false`. Reads yield no value
 * for blank text, after trimming.
 *
 * Selectors must stay within the CSS that Jsoup, SwiftSoup and browsers all support: no
 * `:contains`, `:has` or other Jsoup-only pseudo-classes, and attribute values with characters
 * other than letters, digits, `-` and `_` quoted (`a[href*="export.php"]`).
 */
sealed interface ScrapeStep {
    /** The kind of value the step reads; null when it reads any kind (see [FirstOf]). */
    val input: ScrapeKind?

    /** The kind of value the step yields. */
    val output: ScrapeKind

    /** The first descendant matching [selector]. */
    data class Select(val selector: String) : Navigation

    /** Every descendant matching [selector]. */
    data class SelectAll(val selector: String) : ScrapeStep {
        override val input get() = ScrapeKind.ELEMENT
        override val output get() = ScrapeKind.ELEMENTS
    }

    /** The document the element belongs to. */
    data object Root : Navigation

    /** The element's next sibling element. */
    data object Next : Navigation

    /** The element's last sibling node, which may be text. */
    data object LastSibling : Navigation

    /**
     * The element of the whole document matched by the selector in the element's [attribute],
     * as a link to a section of the same page (`href="#equipment-12"`) names it.
     */
    data class Follow(val attribute: String) : Navigation

    /**
     * Groups the element's children into sections: each child matching [header] with the
     * siblings that follow it up to the next header, wrapped together in one element. Children
     * before the first header belong to no section.
     */
    data class Sections(val header: String) : ScrapeStep {
        override val input get() = ScrapeKind.ELEMENT
        override val output get() = ScrapeKind.ELEMENTS
    }

    /** The first of the elements. */
    data object First : ScrapeStep {
        override val input get() = ScrapeKind.ELEMENTS
        override val output get() = ScrapeKind.ELEMENT
    }

    /** The elements for which [test], a pipeline from an element to a flag, yields `true`. */
    data class Where(val test: List<ScrapeStep>) : ScrapeStep {
        override val input get() = ScrapeKind.ELEMENTS
        override val output get() = ScrapeKind.ELEMENTS
    }

    /**
     * The element's text with whitespace collapsed, as a browser renders it, leaving out the
     * descendants matching [exclude] (such as screen-reader-only labels).
     */
    data class Text(val exclude: String? = null) : Read

    /** The text of the element itself, without its descendants'. */
    data object OwnText : Read

    /** The element's inner HTML. */
    data object Html : Read

    /** The value of the element's [name] attribute. */
    data class Attribute(val name: String) : Read

    /** The value of the element's [attribute] as an absolute URL, resolved against the page's. */
    data class Url(val attribute: String) : Read

    /**
     * The element's siblings rendered as text, keeping their line breaks: text as written,
     * `<br>` as a new line, and each item of a list as a `• ` line. Blank lines are collapsed.
     */
    data object Lines : Read

    /** Whether the element was found. */
    data object Exists : ScrapeStep {
        override val input get() = ScrapeKind.ELEMENT
        override val output get() = ScrapeKind.FLAG
    }

    /** The text matched by [pattern]: its first group, or the whole match without groups. */
    data class Regex(val pattern: String) : Transform

    data class RemovePrefix(val prefix: String) : Transform

    data class RemoveSuffix(val suffix: String) : Transform

    /** The text without [prefix] and [suffix], when it has both. */
    data class Unwrap(val prefix: String, val suffix: String) : Transform

    /** No value when the text is any of [values], ignoring case. */
    data class NullIf(val values: List<String>) : Transform

    /** The value of the query parameter [name] of the URL the text holds. */
    data class QueryParameter(val name: String) : Transform

    /** The text replaced by the value [replacements] gives it, ignoring case; other texts are kept. */
    data class Replace(val replacements: Map<String, String>) : Transform

    /** The text with every occurrence of each key of [replacements] replaced by its value, as `,` by `.` in `87,5`. */
    data class ReplaceAll(val replacements: Map<String, String>) : Transform

    /** Whether the text is [value], ignoring case. */
    data class Equals(val value: String) : Test

    /** Whether the text contains [value], ignoring case. */
    data class Contains(val value: String) : Test

    data object Not : ScrapeStep {
        override val input get() = ScrapeKind.FLAG
        override val output get() = ScrapeKind.FLAG
    }

    /**
     * The value of the first of the [alternatives] that yields one. They take the same kind
     * and yield the same kind.
     */
    data class FirstOf(val alternatives: List<List<ScrapeStep>>) : ScrapeStep {
        override val input: ScrapeKind? get() = null
        override val output: ScrapeKind get() = error("The output of first-of is the output of its alternatives")
    }

    /** A step from an element to an element. */
    sealed interface Navigation : ScrapeStep {
        override val input get() = ScrapeKind.ELEMENT
        override val output get() = ScrapeKind.ELEMENT
    }

    /** A step from an element to its text, or a part of it. */
    sealed interface Read : ScrapeStep {
        override val input get() = ScrapeKind.ELEMENT
        override val output get() = ScrapeKind.TEXT
    }

    /** A step from text to text. */
    sealed interface Transform : ScrapeStep {
        override val input get() = ScrapeKind.TEXT
        override val output get() = ScrapeKind.TEXT
    }

    /** A step from text to a flag. */
    sealed interface Test : ScrapeStep {
        override val input get() = ScrapeKind.TEXT
        override val output get() = ScrapeKind.FLAG
    }
}

/** The kinds of value flowing through a scrape pipeline. */
enum class ScrapeKind {
    /** One node of the document: an element, or a text node. */
    ELEMENT,

    /** Nodes in document order. */
    ELEMENTS,
    TEXT,
    FLAG,
}

/**
 * How a [RecordType] is read out of an HTML element. Each field reads its value with its own
 * [Field.scrape], starting from the element; a field without one takes its default.
 *
 * A record whose non-null field without a default yields no value isn't read at all: a list
 * leaves it out, and an operation fails.
 *
 * @property missing Texts the page shows for a missing value, such as `Dato mancante`. Reads
 *   yielding one of them, ignoring case, yield no value.
 */
data class RecordScrape(val missing: List<String> = emptyList())

/** Scrape pipelines and the kinds they flow through. */
object Scrapes {
    /**
     * The kind [steps] yield when given [input], or null when a step can't read what the step
     * before it yields; [problem] receives the reason.
     */
    fun output(steps: List<ScrapeStep>, input: ScrapeKind, problem: (String) -> Unit = {}): ScrapeKind? {
        var kind = input
        for (step in steps) {
            kind = when (step) {
                is ScrapeStep.FirstOf -> {
                    if (step.alternatives.isEmpty()) return null.also { problem("first-of has no alternatives") }
                    val outputs = step.alternatives.map { output(it, kind, problem) ?: return null }
                    if (outputs.toSet().size != 1) return null.also { problem("the alternatives of first-of yield different kinds: $outputs") }
                    outputs.first()
                }
                else -> {
                    if (step.input != kind) return null.also { problem("${step.name} reads ${step.input}, not $kind") }
                    if (step is ScrapeStep.Where && output(step.test, ScrapeKind.ELEMENT, problem) != ScrapeKind.FLAG) {
                        return null.also { problem("the test of where must yield a flag") }
                    }
                    step.output
                }
            }
        }
        return kind
    }

    /** The kind a scraped value of [type] needs. */
    fun kindFor(type: TypeRef, spec: ApiSpec): ScrapeKind? = when (type) {
        is TypeRef.ListOf -> ScrapeKind.ELEMENTS
        is TypeRef.MapOf -> null
        is TypeRef.Named -> when (val declaration = spec.declarations[type.name]) {
            is EnumType -> ScrapeKind.TEXT
            is RecordType -> if (declaration.scrape != null) ScrapeKind.ELEMENT else null
            null -> null
        }
        is TypeRef.Primitive -> when (type.kind) {
            PrimitiveKind.BOOLEAN -> ScrapeKind.FLAG
            PrimitiveKind.STRING, PrimitiveKind.INT32, PrimitiveKind.INT64, PrimitiveKind.FLOAT32, PrimitiveKind.FLOAT64 -> ScrapeKind.TEXT
            else -> null
        }
    }

    /**
     * Whether a scraped list of [element]s reads a record from each element. Other lists read
     * each element's text, as [ScrapeStep.Text] does, and leave out the elements yielding no
     * value.
     */
    fun isRecordList(element: TypeRef, spec: ApiSpec): Boolean =
        element is TypeRef.Named && (spec.declarations[element.name] as? RecordType)?.scrape != null

    private val ScrapeStep.name: String get() = this::class.simpleName ?: toString()
}

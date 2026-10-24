package it.attendance100.mybicocca.codegen.reader.openapi

import it.attendance100.mybicocca.codegen.model.ScrapeStep

/**
 * Reads a scrape pipeline written as a YAML list of steps (see [ScrapeStep]). A step without an
 * argument is a plain string; a step with one is a single-key object:
 *
 * ```yaml
 * x-mb-scrape:
 *   - select-all: .custom-color-bold
 *   - where: [text, { equals: Capacità }]
 *   - first
 *   - last-sibling
 *   - text
 *   - remove-suffix: " posti"
 * ```
 *
 * | Step                       | Argument                          |
 * |----------------------------|-----------------------------------|
 * | `select`, `select-all`     | a CSS selector                    |
 * | `root`, `next`, `last-sibling`, `first` | none                 |
 * | `follow`                   | the attribute holding a selector  |
 * | `sections`                 | the CSS selector of the headers   |
 * | `where`                    | a pipeline yielding a flag        |
 * | `text`                     | none, or `{exclude: selector}`    |
 * | `own-text`, `html`, `lines`, `exists`, `not` | none            |
 * | `attribute`, `url`         | the attribute name                |
 * | `regex`                    | a pattern                         |
 * | `remove-prefix`, `remove-suffix` | the text to remove          |
 * | `unwrap`                   | `[prefix, suffix]`                |
 * | `null-if`                  | a text, or a list of them         |
 * | `query-parameter`          | the parameter name                |
 * | `replace`                  | `{text: replacement}`             |
 * | `equals`, `contains`       | a text                            |
 * | `first-of`                 | a list of pipelines               |
 */
internal object ScrapeSyntax {
    fun pipeline(raw: Any?, where: String): List<ScrapeStep> {
        val steps = raw as? List<*> ?: error("$where: a scrape must be a list of steps")
        return steps.map { step(it, where) }
    }

    private fun step(raw: Any?, where: String): ScrapeStep = when (raw) {
        is String -> bare(raw, where)
        is Map<*, *> -> {
            val (name, argument) = raw.entries.singleOrNull() ?: error("$where: a scrape step must have a single key, not ${raw.keys}")
            withArgument(name.toString(), argument, where)
        }
        else -> error("$where: unsupported scrape step '$raw'")
    }

    private fun bare(name: String, where: String): ScrapeStep = when (name) {
        "root" -> ScrapeStep.Root
        "next" -> ScrapeStep.Next
        "last-sibling" -> ScrapeStep.LastSibling
        "first" -> ScrapeStep.First
        "text" -> ScrapeStep.Text()
        "own-text" -> ScrapeStep.OwnText
        "html" -> ScrapeStep.Html
        "lines" -> ScrapeStep.Lines
        "exists" -> ScrapeStep.Exists
        "not" -> ScrapeStep.Not
        else -> error("$where: unknown scrape step '$name', or it needs an argument")
    }

    private fun withArgument(name: String, argument: Any?, where: String): ScrapeStep {
        val step = "$where step '$name'"
        return when (name) {
            "select" -> ScrapeStep.Select(text(argument, step))
            "select-all" -> ScrapeStep.SelectAll(text(argument, step))
            "follow" -> ScrapeStep.Follow(text(argument, step))
            "sections" -> ScrapeStep.Sections(text(argument, step))
            "where" -> ScrapeStep.Where(pipeline(argument, step))
            "text" -> ScrapeStep.Text(exclude = (argument as? Map<*, *>)?.get("exclude")?.toString() ?: error("$step takes {exclude: selector}"))
            "attribute" -> ScrapeStep.Attribute(text(argument, step))
            "url" -> ScrapeStep.Url(text(argument, step))
            "regex" -> ScrapeStep.Regex(text(argument, step))
            "remove-prefix" -> ScrapeStep.RemovePrefix(text(argument, step))
            "remove-suffix" -> ScrapeStep.RemoveSuffix(text(argument, step))
            "unwrap" -> {
                val parts = (argument as? List<*>)?.map { it.toString() }.orEmpty()
                require(parts.size == 2) { "$step takes [prefix, suffix]" }
                ScrapeStep.Unwrap(parts[0], parts[1])
            }
            "null-if" -> ScrapeStep.NullIf(if (argument is List<*>) argument.map { it.toString() } else listOf(text(argument, step)))
            "query-parameter" -> ScrapeStep.QueryParameter(text(argument, step))
            "replace" -> ScrapeStep.Replace(Extensions.stringMap(argument, step))
            "equals" -> ScrapeStep.Equals(text(argument, step))
            "contains" -> ScrapeStep.Contains(text(argument, step))
            "first-of" -> ScrapeStep.FirstOf(
                (argument as? List<*> ?: error("$step takes a list of pipelines")).mapIndexed { index, alternative -> pipeline(alternative, "$step alternative ${index + 1}") },
            )
            else -> error("$where: unknown scrape step '$name'")
        }
    }

    private fun text(argument: Any?, where: String): String = when (argument) {
        is String -> argument
        is Number, is Boolean -> argument.toString()
        else -> error("$where takes a text")
    }
}

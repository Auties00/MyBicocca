package it.attendance100.mybicocca.data.common.util

import it.attendance100.mybicocca.data.common.exception.HtmlParsingException
import org.jsoup.nodes.Element

/**
 * A wrapper around a table/grid row's cell map that provides convenient methods
 * for extracting cell values by column name.
 *
 * This class supports fuzzy matching via [getElementOrNull]/[getElementOrThrow] methods,
 * which search for keys containing any of the provided names.
 *
 * Fuzzy matching is useful when table column headers may vary slightly
 * across different pages or when multiple possible header names should
 * be accepted (e.g., "descrizione", "nome", "title").
 *
 * @property headers The set of all column names (keys) available in this row.
 *
 * @sample
 * ```kotlin
 * table.parseTable().map { row ->
 *     MyData(
 *         id = row.getTextOrThrow("id"),
 *         name = row.getTextOrNull("name", "nome", "description"),
 *         link = row.getElementOrNull("link")?.selectFirst("a")?.attr("href")
 *     )
 * }
 * ```
 */
class DataRowElement(private val cellMap: Map<String, Element>) {

    /**
     * The set of all column names (keys) available in this row.
     */
    val headers: Set<String> get() = cellMap.keys

    /**
     * The collection of all row cells (values) available in this row.
     */
    val cells: Collection<Element> get() = cellMap.values

    /**
     * Returns a map of all keys to their cleaned text values.
     *
     * This is useful when you need to iterate over all entries or when
     * the keys themselves are meaningful data (e.g., language names as keys
     * with proficiency levels as values).
     *
     * @return A map where keys are the column names and values are the cleaned text content.
     *         Entries with blank text values are excluded.
     *
     * @sample
     * ```kotlin
     * val languages = dl.parseDefinitionList().toTextMap().map { (lang, level) ->
     *     LanguageRequirement(language = lang, level = level)
     * }
     * ```
     */
    fun toTextMap(): Map<String, String> =
        cellMap.mapValues { it.value.cleanText() }.filterValues { it.isNotBlank() }

    /**
     * Returns the first [Element] whose column name contains any of the given [names],
     * or `null` if no match is found.
     *
     * This performs a fuzzy match by checking if any key in the row contains
     * any of the provided names as a substring. The search stops at the first match.
     *
     * @param names One or more substrings to search for in column names.
     * @return The first matching cell [Element], or `null` if no key contains any of the names.
     *
     * @sample
     * ```kotlin
     * // Returns the element for "descrizione_corso" if it exists
     * val cell = row.getElementOrNull("descrizione", "description", "nome")
     * val href = cell?.selectFirst("a")?.attr("href")
     * ```
     */
    fun getElementOrNull(vararg names: String): Element? {
        return names.firstNotNullOfOrNull { cellMap[it.lowercase()] }
    }

    /**
     * Returns the first [Element] whose column name contains any of the given [names],
     * or throws [IllegalArgumentException] if no match is found.
     *
     * This performs a fuzzy match by checking if any key in the row contains
     * any of the provided names as a substring. The search stops at the first match.
     *
     * @param names One or more substrings to search for in column names.
     * @return The first matching cell [Element].
     * @throws IllegalArgumentException If no key contains any of the provided names.
     *         The exception message includes the searched names and all available keys.
     *
     * @sample
     * ```kotlin
     * val linkCell = row.getElementOrThrow("link", "url")
     * val href = linkCell.selectFirst("a")?.attr("href")
     * ```
     */
    fun getElementOrThrow(vararg names: String): Element {
        return getElementOrNull(*names)
            ?: throw IllegalArgumentException(
                "Key not found: searched for ${names.toList()}, available keys: $headers"
            )
    }

    /**
     * Returns the result of applying [transform] to the first cell whose column name
     * contains any of the given [names], or `null` if no match is found or the
     * transform returns `null`.
     *
     * This performs a fuzzy match and applies the transform function to the matching
     * element, allowing custom extraction logic.
     *
     * @param T The type returned by the transform function.
     * @param names One or more substrings to search for in column names.
     * @param transform A function that converts the [Element] to the desired type,
     *        returning `null` if conversion fails.
     * @return The transformed value, or `null` if no match is found or transform returns `null`.
     *
     * @sample
     * ```kotlin
     * val link = row.getElementAsOrNull("link") { it.selectFirst("a")?.attr("href") }
     * val dataId = row.getElementAsOrNull("id") { it.attr("data-id").toLongOrNull() }
     * ```
     */
    inline fun <T> getElementAsOrNull(vararg names: String, transform: (Element) -> T?): T? {
        val element = getElementOrNull(*names) ?: return null
        return transform(element)
    }

    /**
     * Returns the result of applying [transform] to the first cell whose column name
     * contains any of the given [names], or throws an exception if no match is found
     * or the transform returns `null`.
     *
     * This performs a fuzzy match and applies the transform function to the matching
     * element, allowing custom extraction logic.
     *
     * @param T The type returned by the transform function.
     * @param names One or more substrings to search for in column names.
     * @param transform A function that converts the [Element] to the desired type,
     *        returning `null` if conversion fails.
     * @return The transformed value.
     * @throws IllegalArgumentException If no key contains any of the provided names.
     * @throws HtmlParsingException If the transform function returns `null`.
     *
     * @sample
     * ```kotlin
     * val link = row.getElementAsOrThrow("link") { it.selectFirst("a")?.attr("href") }
     * val dataId = row.getElementAsOrThrow("id") { it.attr("data-id").toLongOrNull() }
     * ```
     */
    inline fun <T> getElementAsOrThrow(vararg names: String, transform: (Element) -> T?): T {
        val element = getElementOrNull(*names)
            ?: throw IllegalArgumentException(
                "Key not found: searched for ${names.toList()}, available keys: $headers"
            )
        return transform(element)
            ?: throw HtmlParsingException(
                "Transform returned null for key ${names.toList()}, element: $element, available keys: $headers"
            )
    }

    /**
     * Returns the cleaned text content of the first cell whose column name contains
     * any of the given [names], or `null` if no match is found.
     *
     * This performs a fuzzy match and extracts the text content from the matching
     * element. The text is cleaned by removing zero-width spaces, normalizing
     * non-breaking spaces, and trimming whitespace.
     *
     * @param names One or more substrings to search for in column names.
     * @return The cleaned text content, or `null` if no match is found.
     *
     * @sample
     * ```kotlin
     * val description = row.getTextOrNull("descrizione", "description", "nome")
     * val credits = row.getTextOrNull("crediti", "cfu")?.toIntOrNull() ?: 0
     * ```
     */
    fun getTextOrNull(vararg names: String): String? {
        return getElementAsOrNull(*names) { it.cleanText() }
    }

    /**
     * Returns the cleaned text content of the first cell whose column name contains
     * any of the given [names], or throws an exception if no match is found.
     *
     * This performs a fuzzy match and extracts the text content from the matching
     * element. The text is cleaned by removing zero-width spaces, normalizing
     * non-breaking spaces, and trimming whitespace.
     *
     * @param names One or more substrings to search for in column names.
     * @return The cleaned text content.
     * @throws IllegalArgumentException If no key contains any of the provided names.
     *
     * @sample
     * ```kotlin
     * val code = row.getTextOrThrow("codice", "code")
     * val name = row.getTextOrThrow("nome", "name", "descrizione")
     * ```
     */
    fun getTextOrThrow(vararg names: String): String {
        return getElementAsOrThrow(*names) { it.cleanText() }
    }

    /**
     * Returns the result of applying [transform] to the cleaned text content of the
     * first cell whose column name contains any of the given [names], or `null` if
     * no match is found or the transform returns `null`.
     *
     * This performs a fuzzy match, extracts and cleans the text content, then applies
     * the transform function.
     *
     * @param T The type returned by the transform function.
     * @param names One or more substrings to search for in column names.
     * @param transform A function that converts the text [String] to the desired type,
     *        returning `null` if conversion fails.
     * @return The transformed value, or `null` if no match or transform returns `null`.
     *
     * @sample
     * ```kotlin
     * val date = row.getTextAsOrNull("data", "date") { parseDate(it) }
     * val credits = row.getTextAsOrNull("crediti", "cfu") { it.toIntOrNull() }
     * ```
     */
    inline fun <T> getTextAsOrNull(vararg names: String, transform: (String) -> T?): T? {
        val text = getTextOrNull(*names) ?: return null
        return transform(text)
    }

    /**
     * Returns the result of applying [transform] to the cleaned text content of the
     * first cell whose column name contains any of the given [names], or throws an
     * exception if no match is found or the transform returns `null`.
     *
     * This performs a fuzzy match, extracts and cleans the text content, then applies
     * the transform function.
     *
     * @param T The type returned by the transform function.
     * @param names One or more substrings to search for in column names.
     * @param transform A function that converts the text [String] to the desired type,
     *        returning `null` if conversion fails.
     * @return The transformed value.
     * @throws IllegalArgumentException If no key contains any of the provided names.
     * @throws HtmlParsingException If the transform returns `null`.
     *
     * @sample
     * ```kotlin
     * val date = row.getTextAsOrThrow("data", "date") { parseDate(it) }
     * val credits = row.getTextAsOrThrow("crediti", "cfu") { it.toIntOrNull() }
     * ```
     */
    inline fun <T> getTextAsOrThrow(vararg names: String, transform: (String) -> T?): T {
        val text = getTextOrNull(*names)
            ?: throw IllegalArgumentException(
                "Key not found: searched for ${names.toList()}, available keys: $headers"
            )
        return transform(text)
            ?: throw HtmlParsingException(
                "Transform returned null for key ${names.toList()}, text: '$text', available keys: $headers"
            )
    }
}
package it.attendance100.mybicocca.data.remote.common.util

import io.ktor.client.statement.HttpResponse
import it.attendance100.mybicocca.data.remote.common.exception.HtmlParsingException
import io.ktor.client.statement.bodyAsChannel
import io.ktor.client.statement.request
import io.ktor.utils.io.jvm.javaio.toInputStream
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements

/**
 * Parses an HTTP response as an HTML document using Jsoup
 *
 * @return the HTML document
 **/
suspend fun HttpResponse.toHtml(): Document {
    val inputStream = bodyAsChannel().toInputStream()
    val url = request.url.toString()
    return Jsoup.parse(inputStream, null, url)
}

/**
 * Parses an HTML table element into a sequence of [DataRowElement] objects.
 *
 * This extension function extracts headers and data rows from a table,
 * automatically detecting where headers are located:
 *
 * 1. **Thead headers**: If the table has a `<thead>` element, headers are
 *    extracted from `<th>` or `<td>` elements within it, and data rows
 *    are taken from `<tbody>`.
 *
 * 2. **First row headers**: If no `<thead>` exists, the first `<tr>` is
 *    treated as the header row, and subsequent rows are treated as data.
 *
 * Header text is normalized to lowercase and cleaned (whitespace trimmed,
 * special characters removed) to enable consistent key lookups.
 *
 * @return A [Sequence] of [DataRowElement] objects, one for each data row in the table.
 *         Returns an empty sequence if the table has no headers or no data rows.
 *
 * @sample
 * ```kotlin
 * // Basic usage with standard table structure
 * val courses = doc.selectFirst("table.courses")
 *     ?.parseTable()
 *     ?.map { row ->
 *         Course(
 *             code = row.getTextOrThrow("codice"),
 *             name = row.getTextOrThrow("nome", "descrizione"),
 *             credits = row.getTextOrNull("cfu")?.toIntOrNull() ?: 0
 *         )
 *     }
 *     ?.toList()
 *     ?: emptyList()
 * ```
 *
 * @sample
 * ```kotlin
 * // Filtering and transforming with null safety
 * val exams = table.parseTable()
 *     .mapNotNull { row ->
 *         val date = row.getTextAsOrNull("data") { parseDate(it) }
 *             ?: return@mapNotNull null
 *         Exam(
 *             name = row.getTextOrNull("nome") ?: return@mapNotNull null,
 *             date = date,
 *             room = row.getTextOrNull("aula")
 *         )
 *     }
 *     .toList()
 * ```
 */
fun Element.parseTable(): Sequence<DataRowElement> {
    val theadHeaders = select("thead tr th, thead tr td")

    val (headers, dataRows) = if (theadHeaders.isNotEmpty()) {
        val headers = theadHeaders.map { it.cleanText().lowercase() }
        val rows = select("tbody tr")
        headers to rows
    } else {
        val allRows = select("tr")
        if (allRows.isEmpty()) return emptySequence()

        val headerRow = allRows.first()!!
        val headers = headerRow.select("th, td").map { it.cleanText().lowercase() }
        val rows = Elements(allRows.drop(1))
        headers to rows
    }

    if (headers.isEmpty()) return emptySequence()

    return dataRows.asSequence().map { row ->
        val cells = row.select("td")
        if(headers.size != cells.size) {
            throw HtmlParsingException("Mismatch between headers and rows")
        }

        DataRowElement(headers.zip(cells).toMap())
    }
}

/**
 * Parses an HTML definition list (`<dl>`) element into a [DataRowElement].
 *
 * This extension function extracts key-value pairs from `<dt>`/`<dd>` elements,
 * where each `<dt>` becomes a key and its next sibling `<dd>` becomes the value.
 *
 * Keys are normalized by:
 * - Cleaning the text (removing special characters, trimming)
 * - Removing trailing colons
 * - Converting to lowercase
 *
 * The resulting [DataRowElement] can be used with all the fuzzy matching methods
 * like [DataRowElement.getTextOrNull], [DataRowElement.getElementOrNull], etc.
 *
 * @return A [DataRowElement] containing the definition list entries.
 *         Returns a [TableRow] with empty keys if no `<dt>` elements are found.
 *
 * @sample
 * ```kotlin
 * val dl = doc.selectFirst("dl.record-riga")
 *     ?: throw IllegalStateException("Missing definition list")
 * val data = dl.parseGrid()
 *
 * val name = data.getTextOrThrow("nome", "name")
 * val date = data.getTextAsOrNull("data") { parseDate(it) }
 * val link = data.getElementOrNull("link")?.selectFirst("a")?.attr("href")
 * ```
 */
fun Element.parseGrid(): DataRowElement {
    val map = select("dt")
        .associate { dt ->
            val key = dt.cleanText().removeSuffix(":").lowercase()
            val value = dt.nextElementSibling()
            key to value
        }.filterValues {
            it != null
        }.mapValues {
            it.value!!
        }
    return DataRowElement(map)
}

/**
 * Extracts and cleans the text content of this element.
 *
 * This is a convenience method equivalent to `element.text().cleanText()`.
 *
 * @return The cleaned text content of the element.
 * @see String.cleanText
 */
fun Element.cleanText(): String = text().cleanText()
package it.attendance100.mybicocca.codegen.resources

import org.jsoup.Jsoup
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CourseRatingsBuilderTest {
    /** A teacher's page, with its chart's series as the site writes them. */
    private fun page(vararg series: String) = """
        var plot2 = $.jqplot('chart', [
            ${series.joinToString(", ")}
                ],
                    horizontalGraph
                );
        var plot3 = $.jqplot('chart', [ [ [9.0, 'Soddisfazione Complessiva'] ] ], verticalGraph);
    """.trimIndent()

    private val rated = "[ [2.1, 'Soddisfazione Complessiva'] , [2.2, 'Efficacia Didattica'] , [2.3, 'Aspetti organizzativi'] , ]"

    @Test
    fun `reads the series of the first chart`() {
        val series = CourseRatingsBuilder.chartSeries(page(rated, rated, rated, rated, rated))
        assertEquals(5, series.size)
        assertEquals(listOf("Soddisfazione Complessiva" to 2.1, "Efficacia Didattica" to 2.2, "Aspetti organizzativi" to 2.3), series.last())
    }

    @Test
    fun `keeps a series served blank in its place`() {
        val series = CourseRatingsBuilder.chartSeries(page(rated, rated, "[ ]", rated, "[ ]"))
        assertEquals(listOf(3, 3, 0, 3, 0), series.map { it.size })
    }

    @Test
    fun `reads no series from a chart served blank`() {
        assertEquals(emptyList(), CourseRatingsBuilder.chartSeries(page()))
    }

    /** A listing as the site writes it: its questionnaires in the header, each row's written by a script. */
    private fun listing(total: String?, vararg rows: Pair<String, Int>) = Jsoup.parse(
        """
        <div class="jumbotron"><p><strong>Anno Accademico</strong> : 2024/2025 </p>
        ${total?.let { "<p><strong>Numero di questionari</strong> : $it\n</p>" }.orEmpty()}</div>
        <table><tbody>
        ${rows.withIndex().joinToString("\n") { (i, row) ->
            "<tr data-href='opinioniCDS.vm?idCDS=${row.first}&amp;idAA=2024'><td>${row.first}</td>" +
                "<td><span id='id_[$i][14]'></span><script>document.getElementById(\"id_[$i][14]\").innerHTML = formatta(${row.second});</script></td>" +
                "<td><script>document.getElementById(\"id_[$i][15]\").innerHTML = formatta(7.5);</script></td></tr>"
        }}
        </tbody></table>
        """.trimIndent(),
        CourseRatingsBuilder.BASE_URL,
    )

    @Test
    fun `reads each row's questionnaires from its script`() {
        val rows = CourseRatingsBuilder.rows(listing("1,500", "A" to 1000, "B" to 500))
        assertEquals(listOf(1000, 500), rows.map { it.questionnaires })
        assertEquals("${CourseRatingsBuilder.BASE_URL}opinioniCDS.vm?idCDS=A&idAA=2024", rows.first().url)
    }

    @Test
    fun `takes a listing whose rows add up to its header and its parent's row`() {
        assertTrue(CourseRatingsBuilder.isWhole(listing("1,500", "A" to 1000, "B" to 500), 1500))
        assertTrue(CourseRatingsBuilder.isWhole(listing("1,500", "A" to 1000, "B" to 500), null))
    }

    @Test
    fun `counts a row listed twice once`() {
        assertTrue(CourseRatingsBuilder.isWhole(listing("1,500", "A" to 1000, "B" to 500, "B" to 500), 1500))
    }

    @Test
    fun `refuses a listing served with parts missing`() {
        // No rows, a row missing, no header, or a header that isn't its parent's.
        assertFalse(CourseRatingsBuilder.isWhole(listing("1,500"), 1500))
        assertFalse(CourseRatingsBuilder.isWhole(listing("1,500", "A" to 1000), 1500))
        assertFalse(CourseRatingsBuilder.isWhole(listing(null, "A" to 1000, "B" to 500), 1500))
        assertFalse(CourseRatingsBuilder.isWhole(listing("1,400", "A" to 1000, "B" to 400), 1500))
    }

    @Test
    fun `takes an empty listing that says it covers nothing`() {
        assertTrue(CourseRatingsBuilder.isWhole(listing("0"), 0))
    }

    @Test
    fun `reads the years the site links to`() {
        val document = Jsoup.parse("""<a href="opinioniAA.vm?idAA=2013">2013/2014</a><a href="opinioniAA.vm?idAA=2024">2024/2025</a><a href="index.vm">Home</a>""")
        assertEquals(setOf(2013, 2024), CourseRatingsBuilder.years(document))
    }
}

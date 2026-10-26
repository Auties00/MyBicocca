package it.attendance100.mybicocca.codegen.resources

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

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
}

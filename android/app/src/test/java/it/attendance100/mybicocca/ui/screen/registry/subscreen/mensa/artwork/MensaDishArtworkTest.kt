package it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.artwork

import org.junit.Assert.assertEquals
import org.junit.Test

class MensaDishArtworkTest {

    private fun art(name: String) = MensaDishArtwork.forName(name)

    @Test
    fun `pasta wins over the fish or vegetable it is served with`() {
        assertEquals(MensaArt.PASTA_TOMATO, art("SPAGHETTI TONNO, CAPPERI E OLIVE S/G"))
        assertEquals(MensaArt.PASTA_PESTO, art("PASTA CON CREMA DI ZUCCHINE E PANCETTA"))
        assertEquals(MensaArt.PASTA_PESTO, art("PASTA S/GLUT AL PESTO"))
    }

    @Test
    fun `soups and stews are recognised by their opening word`() {
        assertEquals(MensaArt.SOUP, art("CREMA DI CAROTE E ORIGANO"))
        assertEquals(MensaArt.SOUP, art("PASSATO DI VERDURA"))
        assertEquals(MensaArt.SOUP, art("Pasta S/G e fagioli"))
        assertEquals(MensaArt.SOUP, art("VERZA E CECI IN UMIDO"))
    }

    @Test
    fun `rice dishes and baked dishes`() {
        assertEquals(MensaArt.RISOTTO, art("RISOTTO ALLA MONZESE"))
        assertEquals(MensaArt.RISOTTO, art("RISO IN BIANCO"))
        assertEquals(MensaArt.LASAGNA, art("TORTINO AGLI ASPARAGI"))
    }

    @Test
    fun `pizzas sandwiches and salads`() {
        assertEquals(MensaArt.PIZZA, art("PIZZA CON TONNO"))
        assertEquals(MensaArt.SANDWICH, art("PANINO AL PROSCIUTTO COTTO"))
        assertEquals(MensaArt.SANDWICH, art("PIADINA ROMAGNOLA CON HUMUS DI CECI"))
        assertEquals(MensaArt.SALAD, art("INSALATA, CAVOLO ROSSO, MAIS, TONNO"))
        assertEquals(MensaArt.SALAD, art("POKE RISO VENERE, SALMONE, ZUCCHINE JU"))
    }

    @Test
    fun `mains sides and extras`() {
        assertEquals(MensaArt.FISH, art("FILETTO DI MERLUZZO DORATO"))
        assertEquals(MensaArt.POULTRY, art("COSCE DI POLLO ALLA PAPRIKA"))
        assertEquals(MensaArt.MEAT, art("ARROSTO DI MAIALE CON CASTAGNE"))
        assertEquals(MensaArt.VEGETABLES, art("GRIGLIA VEGETARIANA (SOLO IN MENSA U12)"))
        assertEquals(MensaArt.POTATOES, art("PATATE AL ROSMARINO"))
        assertEquals(MensaArt.VEGETABLES, art("FAGIOLINI AL VAPORE"))
        assertEquals(MensaArt.FRUIT, art("MACEDONIA DI FRUTTA 150G"))
        assertEquals(MensaArt.DESSERT, art("YOGURT BIO"))
        assertEquals(MensaArt.BREAD, art("GRISSINI BIO"))
        assertEquals(MensaArt.DRINK, art("ACQUA NATURALE 0,5 L"))
        assertEquals(MensaArt.EGGS_CHEESE, art("FORMAGGIO"))
    }

    @Test
    fun `unknown names fall back to the course`() {
        assertEquals(null, art("PIATTO DELLO CHEF"))
        assertEquals(MensaArt.MEAT, MensaDishArtwork.forCourse(2))
        assertEquals(MensaArt.RISOTTO, MensaDishArtwork.forCourse(null))
    }
}

package it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.artwork

import androidx.annotation.DrawableRes
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.domain.model.mensa.MensaDish
import java.text.Normalizer

/** The illustrations the canteen screens draw dishes and courses with. */
enum class MensaArt(@DrawableRes val drawable: Int) {
    PASTA_TOMATO(R.drawable.mensa_art_pasta_tomato),
    PASTA_PESTO(R.drawable.mensa_art_pasta_pesto),
    RISOTTO(R.drawable.mensa_art_risotto),
    SOUP(R.drawable.mensa_art_soup),
    LASAGNA(R.drawable.mensa_art_lasagna),
    MEAT(R.drawable.mensa_art_meat),
    POULTRY(R.drawable.mensa_art_poultry),
    FISH(R.drawable.mensa_art_fish),
    EGGS_CHEESE(R.drawable.mensa_art_eggs_cheese),
    PIZZA(R.drawable.mensa_art_pizza),
    SANDWICH(R.drawable.mensa_art_sandwich),
    SALAD(R.drawable.mensa_art_salad),
    VEGETABLES(R.drawable.mensa_art_vegetables),
    POTATOES(R.drawable.mensa_art_potatoes),
    BREAD(R.drawable.mensa_art_bread),
    FRUIT(R.drawable.mensa_art_fruit),
    DESSERT(R.drawable.mensa_art_dessert),
    DRINK(R.drawable.mensa_art_drink),
}

/**
 * Picks the illustration for a dish. The Italian name is matched against keyword rules, most
 * specific first, and the course is the fallback, so every dish resolves to some illustration.
 */
object MensaDishArtwork {

    fun forDish(dish: MensaDish): MensaArt = forName(dish.name) ?: forCourse(dish.courseId)

    fun forCourse(courseId: Int?): MensaArt = when (courseId) {
        1 -> MensaArt.PASTA_TOMATO
        2 -> MensaArt.MEAT
        4 -> MensaArt.VEGETABLES
        6 -> MensaArt.DESSERT
        7 -> MensaArt.BREAD
        9 -> MensaArt.DRINK
        12 -> MensaArt.PIZZA
        13 -> MensaArt.SANDWICH
        14 -> MensaArt.RISOTTO
        17 -> MensaArt.SALAD
        else -> MensaArt.RISOTTO
    }

    internal fun forName(name: String): MensaArt? {
        val normalized = normalize(name)
        return RULES.firstOrNull { (pattern, _) -> pattern.containsMatchIn(normalized) }?.second
    }

    private fun normalize(name: String): String =
        Normalizer.normalize(name.lowercase(), Normalizer.Form.NFD)
            .replace(DIACRITICS, "")
            .replace(NON_ALPHANUMERIC, " ")
            .trim()
            .replace(WHITESPACE, " ")

    private val DIACRITICS = Regex("\\p{InCombiningDiacriticalMarks}+")
    private val NON_ALPHANUMERIC = Regex("[^a-z0-9]+")
    private val WHITESPACE = Regex("\\s+")

    private fun rule(pattern: String, art: MensaArt) = Regex(pattern) to art

    /** Order matters: a name takes the first rule it matches (`spaghetti tonno` is pasta, not fish). */
    private val RULES = listOf(
        rule("\\bpoke\\b", MensaArt.SALAD),
        rule("\\bpizza\\b", MensaArt.PIZZA),
        rule("\\b(panin|toast|tramezzin|piadina|schiacciata|focaccia|rotolo)", MensaArt.SANDWICH),
        rule("\\b(insalat|verdura cruda)", MensaArt.SALAD),
        rule("^(crema|minestr|passato|vellutata|zuppa)|\\be fagioli\\b|\\bin umido\\b", MensaArt.SOUP),
        rule("\\b(pesto|portofino|noci)\\b|\\bpasta\\b.*\\bzucchine\\b", MensaArt.PASTA_PESTO),
        rule("\\b(pasta|spaghett|penne|fusilli|rigatoni|tagliatell|pizzoccheri)", MensaArt.PASTA_TOMATO),
        rule("\\brisott|\\briso\\b", MensaArt.RISOTTO),
        rule("\\b(lasagn|gnocch|cannelloni|sformato|flan|tortino|sfoglia|polenta)\\b", MensaArt.LASAGNA),
        rule("\\bvegetarian", MensaArt.VEGETABLES),
        rule("\\b(pesce|salmone|merluzzo|tonno|orata|branzino|calamar|scorfano|sgombro|trota|gamber)", MensaArt.FISH),
        rule("\\b(pollo|tacchino|fusi|cosce|fesa)\\b", MensaArt.POULTRY),
        rule("\\b(manzo|vitello|maiale|arrosto|polpett|cotoletta|bresaola|affettato|salsiccia|griglia|prosciutto|speck|salame|porchetta|mortadella|wurstel)", MensaArt.MEAT),
        rule("\\b(formaggio|uova|frittata|tofu|mozzarella|ricotta|zola|feta|fontina|farinata)\\b", MensaArt.EGGS_CHEESE),
        rule("\\bpatat|\\brosticciata\\b", MensaArt.POTATOES),
        rule("\\b(verdur|broccol|carot|cavol|cima di rapa|fagiolini|finocch|funghi|piselli|spinaci|melanzan|biete|verza|asparag|carciof|peperon|legumi)", MensaArt.VEGETABLES),
        rule("\\b(frutta|macedonia|mela|banana|arancia|pera)\\b", MensaArt.FRUIT),
        rule("\\b(yogurt|budino|torta|dolce|muffin|dessert|tiramisu|gelato)\\b", MensaArt.DESSERT),
        rule("\\b(pane|grissini|cracker|crackers|bocconcino)\\b", MensaArt.BREAD),
        rule("\\b(acqua|bibita|caff|succo|birra|vino|cola)", MensaArt.DRINK),
    )
}

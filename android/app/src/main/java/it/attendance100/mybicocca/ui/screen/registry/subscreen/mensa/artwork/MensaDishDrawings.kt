package it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.artwork

import androidx.annotation.DrawableRes
import it.attendance100.mybicocca.R

/**
 * The illustration drawn for each dish the canteen serves, by Compass dish id. Gluten-free
 * variants and repeated listings share their dish's drawing. Generated from the drawing set;
 * dishes not listed fall back to [MensaDishArtwork]'s keyword rules.
 */
internal object MensaDishDrawings {
    private val byDish: Map<Int, Int> = mapOf(
        38577 to R.drawable.mensa_dish_38577, // Vellutata Con Carote E Zenzero
        38609 to R.drawable.mensa_dish_38609, // Riso In Bianco
        38610 to R.drawable.mensa_dish_38610, // Pasta In Bianco
        38611 to R.drawable.mensa_dish_38611, // Riso Al Pomodoro
        38612 to R.drawable.mensa_dish_38612, // Pasta Al Pomodoro
        38613 to R.drawable.mensa_dish_38613, // Pasta Integrale Al Pomodoro
        38614 to R.drawable.mensa_dish_38614, // Pasta Integrale In Bianco
        38615 to R.drawable.mensa_dish_38615, // Riso Integrale In Bianco
        38616 to R.drawable.mensa_dish_38616, // Riso Integrale Al Pomodoro
        38622 to R.drawable.mensa_dish_38622, // Grissini
        38623 to R.drawable.mensa_dish_69399, // Frutta Fresca
        38624 to R.drawable.mensa_dish_38624, // Yogurt Bio
        38625 to R.drawable.mensa_dish_38625, // Dessert 100G
        38626 to R.drawable.mensa_dish_38626, // Acqua Naturale 0,5 L
        38627 to R.drawable.mensa_dish_38627, // Acqua Gassata 0,5 L
        38628 to R.drawable.mensa_dish_38628, // Macedonia Di Frutta 150G
        38661 to R.drawable.mensa_dish_38661, // Broccoli All'Olio
        38673 to R.drawable.mensa_dish_38673, // Pasta All'Amatriciana
        38684 to R.drawable.mensa_dish_38684, // Pasta Integrale Alla Boscaiola
        38715 to R.drawable.mensa_dish_38715, // Broccoli Al Vapore
        38755 to R.drawable.mensa_dish_38755, // Finocchi Al Vapore
        38772 to R.drawable.mensa_dish_38772, // Farinata Di Ceci
        38782 to R.drawable.mensa_dish_38782, // Pane Integrale Bio
        38799 to R.drawable.mensa_dish_38799, // Pasta Alla Carbonara
        38817 to R.drawable.mensa_dish_81675, // Cavolfiore Gratinato S/G
        39133 to R.drawable.mensa_dish_39133, // Passato Di Verdura
        42236 to R.drawable.mensa_dish_42236, // Lenticchie In Umido
        42239 to R.drawable.mensa_dish_42239, // Cosce Di Pollo Alla Paprika
        42290 to R.drawable.mensa_dish_42290, // Verdura Cruda Di Stagione 200G
        42455 to R.drawable.mensa_dish_42455, // Pasta Speck E Zafferano
        42494 to R.drawable.mensa_dish_42494, // Biete All'Olio
        42536 to R.drawable.mensa_dish_42536, // Spinaci All'Olio
        50855 to R.drawable.mensa_dish_50855, // Crema Di Cavolfiore
        51280 to R.drawable.mensa_dish_51280, // Bresaola (Solo In Mensa U6-1)
        61106 to R.drawable.mensa_dish_61106, // Macedonia Di Verdure
        61283 to R.drawable.mensa_dish_61283, // Risotto Alla Zucca
        61290 to R.drawable.mensa_dish_61290, // Pasta E Fagioli
        62283 to R.drawable.mensa_dish_62283, // Brasato Di Manzo
        69397 to R.drawable.mensa_dish_69397, // Piselli Stufati
        69398 to R.drawable.mensa_dish_69398, // Patate Sabbiose
        69399 to R.drawable.mensa_dish_69399, // Frutta Fresca 150G
        69406 to R.drawable.mensa_dish_69406, // Burger Di Ceci E Farro
        69408 to R.drawable.mensa_dish_69408, // Pasta All'Arrabbiata
        69409 to R.drawable.mensa_dish_69409, // Minestrone Di Verdura
        69415 to R.drawable.mensa_dish_69415, // Spinaci Al Vapore
        69427 to R.drawable.mensa_dish_69427, // Frittata Ai Carciofi
        69429 to R.drawable.mensa_dish_69429, // Fagiolini Al Vapore
        70780 to R.drawable.mensa_dish_70780, // Cosce Di Pollo Alla Diavola
        73460 to R.drawable.mensa_dish_73460, // Pasta Integrale Con Sugo Di Giornata (Su Richiesta)
        73461 to R.drawable.mensa_dish_73461, // Riso Integrale Con Sugo Di Giornata (Su Richiesta)
        73925 to R.drawable.mensa_dish_73925, // Pasta S/Glut Alla Portofino
        73927 to R.drawable.mensa_dish_38673, // Pasta S/Glut All'Amatriciana
        73952 to R.drawable.mensa_dish_69408, // Pasta S/Glut All'Arrabbiata
        73968 to R.drawable.mensa_dish_73968, // Pane S/Glut
        74031 to R.drawable.mensa_dish_74031, // Gnocchi S/G Burro E Salvia
        74038 to R.drawable.mensa_dish_74038, // Pasta S/G Alla Boscaiola
        74054 to R.drawable.mensa_dish_74054, // Pasta S/G Provola E Zucchine
        74196 to R.drawable.mensa_dish_74196, // Insalata, Pomodoro, Olive, Porchetta
        74197 to R.drawable.mensa_dish_74197, // Insalata, Pomodoro, Olive, Mozzarella
        77064 to R.drawable.mensa_dish_77064, // Legumi Misti In Umido
        78320 to R.drawable.mensa_dish_91617, // Crackers Bio
        78890 to R.drawable.mensa_dish_78890, // Pane Multicereali
        78891 to R.drawable.mensa_dish_78891, // Pane Bocconcino
        80516 to R.drawable.mensa_dish_80516, // Crema Di Patate E Spinaci
        80522 to R.drawable.mensa_dish_80522, // Filetto Di Sgombro Al Limone E Prezzemolo
        80525 to R.drawable.mensa_dish_80525, // Filetto Di Trota In Umido
        80540 to R.drawable.mensa_dish_80540, // Insalata, Cavolo Rosso, Mais, Fontina Dop
        80541 to R.drawable.mensa_dish_80541, // Poke Di Riso Integrale, Tonno, Broccoli, Carote
        80550 to R.drawable.mensa_dish_80550, // Tortino Di Mais Con Cipolle E Formaggio
        80927 to R.drawable.mensa_dish_80927, // Griglia (Solo In Mensa U12)
        81675 to R.drawable.mensa_dish_81675, // Cavolfiore Gratinato
        81793 to R.drawable.mensa_dish_81793, // Rosticciata Con Patate E Peperoni
        82891 to R.drawable.mensa_dish_82891, // Spaghetti Tonno, Capperi E Olive
        88256 to R.drawable.mensa_dish_88256, // Burger Di Ceci Sg
        88258 to R.drawable.mensa_dish_77064, // Legumi Misti In Umido Sg
        88374 to R.drawable.mensa_dish_88374, // Tofu
        88375 to R.drawable.mensa_dish_88375, // Formaggio
        88376 to R.drawable.mensa_dish_88376, // Affettato
        88465 to R.drawable.mensa_dish_88762, // Pizza Margherita
        88466 to R.drawable.mensa_dish_88771, // Pizza Salame Piccante
        88467 to R.drawable.mensa_dish_88467, // Pizza Con Tonno
        88468 to R.drawable.mensa_dish_88468, // Pizza Con Prosciutto Cotto
        88469 to R.drawable.mensa_dish_88469, // Pizza Con Olive
        88470 to R.drawable.mensa_dish_88470, // Pizza Con Speck
        88471 to R.drawable.mensa_dish_88471, // Pizza Con Zola
        88472 to R.drawable.mensa_dish_88472, // Pizza Con Carciofi
        88473 to R.drawable.mensa_dish_88473, // Pizza Con Carciofi E Cotto
        88474 to R.drawable.mensa_dish_92871, // Pizza Con Funghi
        88475 to R.drawable.mensa_dish_88475, // Pizza Con Funghi E Cotto
        88477 to R.drawable.mensa_dish_88477, // Pizza Con Wurstel
        88478 to R.drawable.mensa_dish_88478, // Pizza Vegetariana
        88480 to R.drawable.mensa_dish_88480, // Pizza Capricciosa
        88481 to R.drawable.mensa_dish_88481, // Pizza 4 Stagioni
        88483 to R.drawable.mensa_dish_88483, // Panino Al Salame
        88484 to R.drawable.mensa_dish_88484, // Panino Al Prosciutto Cotto
        88485 to R.drawable.mensa_dish_88485, // Panino Al Prosciutto Crudo
        88486 to R.drawable.mensa_dish_88486, // Panino Con Porchetta Igp
        88487 to R.drawable.mensa_dish_88487, // Panino Al Speck
        88488 to R.drawable.mensa_dish_88488, // Panino Con Cotoletta
        88489 to R.drawable.mensa_dish_88489, // Panino Con Melanzane, Zucchine Robiola E Pomodori Secchi
        88490 to R.drawable.mensa_dish_88490, // Panino Speck Brie E Insalata
        88491 to R.drawable.mensa_dish_88491, // Panino Crudo Mozzarella Pomodoro E Insalata
        88492 to R.drawable.mensa_dish_88492, // Panino Con Porchetta, Pomodoro E Insalata
        88493 to R.drawable.mensa_dish_88493, // Panino Con Bresaola Insalata E Asiago
        88494 to R.drawable.mensa_dish_88494, // Maxi Toast
        88495 to R.drawable.mensa_dish_88495, // Piadina Crudo Robiola E Insalata
        88496 to R.drawable.mensa_dish_88496, // Piadina Cotto Robiola E Insalata
        88497 to R.drawable.mensa_dish_88497, // Panino Con Cotoletta Pomodoro E Insalata
        88498 to R.drawable.mensa_dish_88498, // Panino Alla Caprese
        88499 to R.drawable.mensa_dish_88499, // Panino Porchetta E Brie
        88500 to R.drawable.mensa_dish_88500, // Focaccia Cotto Asiago E Pomodoro
        88501 to R.drawable.mensa_dish_88501, // Focaccia Salame E Asiago
        88502 to R.drawable.mensa_dish_88502, // Focaccia Barese
        88503 to R.drawable.mensa_dish_88503, // Schiacciata Romana Con Prosciutto Cotto E Mozzarella
        88504 to R.drawable.mensa_dish_88504, // Schiacciata Romana Con Prosciutto Crudo
        88505 to R.drawable.mensa_dish_88505, // Schiacciata Romana Con Mortadella
        88506 to R.drawable.mensa_dish_88506, // Focaccia Alle Verdure
        88507 to R.drawable.mensa_dish_88507, // Focaccia Quadrata
        88508 to R.drawable.mensa_dish_88508, // Bibita
        88510 to R.drawable.mensa_dish_88510, // Caffè
        88543 to R.drawable.mensa_dish_61290, // Pasta S/G E Fagioli
        88600 to R.drawable.mensa_dish_90989, // Zuppa Di Legumi S/G
        88762 to R.drawable.mensa_dish_88762, // Pizza Margherita
        88768 to R.drawable.mensa_dish_88768, // Pizza Cotto
        88771 to R.drawable.mensa_dish_88771, // Pizza Salame Piccante
        88779 to R.drawable.mensa_dish_88779, // Pizza Con Le Verdure
        88893 to R.drawable.mensa_dish_38612, // Pasta Sg Al Pomodoro
        88894 to R.drawable.mensa_dish_38622, // Grissini Sg
        88895 to R.drawable.mensa_dish_91617, // Crackers Sg
        90660 to R.drawable.mensa_dish_90660, // Cuori Di Merluzzo Al Timo
        90748 to R.drawable.mensa_dish_38622, // Grissini Bio
        90989 to R.drawable.mensa_dish_90989, // Zuppa Di Legumi
        90998 to R.drawable.mensa_dish_90998, // Filetto Di Scorfano Alla Siciliana
        91031 to R.drawable.mensa_dish_91031, // Insalata, Cetriolo, Pomodoro, Feta Dop
        91057 to R.drawable.mensa_dish_91057, // Filetto Di Merluzzo Dorato
        91065 to R.drawable.mensa_dish_91065, // Pasta In Salsa Di Noci
        91616 to R.drawable.mensa_dish_91616, // Griglia Vegetariana (Solo In Mensa U12)
        91617 to R.drawable.mensa_dish_91617, // Crackers
        91704 to R.drawable.mensa_dish_91704, // Rotolo Romagnolo Con Verdure Grill E Stracchino
        91812 to R.drawable.mensa_dish_91812, // Pasta S/G Con Crema Di Carciofi
        91825 to R.drawable.mensa_dish_42455, // Pasta S/G Speck E Zafferano
        91826 to R.drawable.mensa_dish_91057, // Filetto Di Merluzzo Dorato S/G
        91827 to R.drawable.mensa_dish_91065, // Pasta S/G In Salsa Di Noci
        92337 to R.drawable.mensa_dish_91704, // Rotolo Romagnolo S/G Con Verdure Grill E Stracchino
        92871 to R.drawable.mensa_dish_92871, // Pizza Con Funghi
        92875 to R.drawable.mensa_dish_92875, // Pizza Bianca Ai Formaggi
        92887 to R.drawable.mensa_dish_92887, // Sformato Di Lenticchie
        95424 to R.drawable.mensa_dish_95424, // Patate Al Rosmarino (Solo In Combinazione Con Pizza In U12)
        101004 to R.drawable.mensa_dish_101004, // Insalata, Cavolo Rosso, Mais, Tonno
        101006 to R.drawable.mensa_dish_101006, // Insalata, Carote Julienne, Peperoni, Prosciutto Cotto
        101007 to R.drawable.mensa_dish_101007, // Insalata, Carote Julienne, Peperoni, Fagioli Borlotti
        101010 to R.drawable.mensa_dish_101010, // Insalata, Cetriolo, Pomodoro, Frittata
        101013 to R.drawable.mensa_dish_101013, // Poke Riso Basmati, Frittata, Pomodoro, Semi Di Sesamo, Maionese
        101015 to R.drawable.mensa_dish_101015, // Poke Riso Venere, Salmone, Zucchine Julienne, Pomodorini
        101018 to R.drawable.mensa_dish_101018, // Poke Riso, Lenticchie, Cavolfiore, Salsa Di Soia
        101027 to R.drawable.mensa_dish_101027, // Patate Rustiche
        101105 to R.drawable.mensa_dish_101105, // Pasta Portofino Con Basilico
        101106 to R.drawable.mensa_dish_101106, // Spatzle Di Spinaci Con Burro E Salvia
        101110 to R.drawable.mensa_dish_101110, // Pasta Al Sugo Di Melanzane
        101124 to R.drawable.mensa_dish_101124, // Spezzatino Di Manzo Con Funghi E Patate
        101335 to R.drawable.mensa_dish_101335, // Pasta In Crema Di Carciofi
        101413 to R.drawable.mensa_dish_101413, // Zuppa Di Cavoletti Di Bruxelles
        101419 to R.drawable.mensa_dish_101419, // Pasta Al Pomodoro Con Zucchine E Provola
        101631 to R.drawable.mensa_dish_101631, // Piadina Romagnola Con Humus Di Ceci E Peperoni
        101632 to R.drawable.mensa_dish_101632, // Arrosto Di Maiale Alla Birra E Senape
        101633 to R.drawable.mensa_dish_101633, // Filetto Di Sgombro Agli Agrumi
        101634 to R.drawable.mensa_dish_101634, // Flan Di Asparagi
        102038 to R.drawable.mensa_dish_102038, // Flan Di Zucchine
        102044 to R.drawable.mensa_dish_102044, // Patate Al Curry
        102193 to R.drawable.mensa_dish_102193, // Patate Gratinate Alla Senape
        102203 to R.drawable.mensa_dish_102203, // Spaghetti Alla Crema Di Pomodoro E Pan Grattato
        102204 to R.drawable.mensa_dish_102204, // Risotto Pere E Taleggio
        102205 to R.drawable.mensa_dish_102205, // Tortino Agli Asparagi
        102206 to R.drawable.mensa_dish_102206, // Sformato Di Melanzane
        102719 to R.drawable.mensa_dish_101007, // Insalata, Carote Julienne, Peperoni, Fagioli Borlotti S/G
        102720 to R.drawable.mensa_dish_92887, // Sformato Di Lenticchie S/G
        102721 to R.drawable.mensa_dish_101110, // Pasta Al Sugo Di Melanzane S/G
        102722 to R.drawable.mensa_dish_61283, // Risotto Alla Zucca S/G
        102726 to R.drawable.mensa_dish_102726, // Piadina S/G Con Humus Di Ceci E Peperoni
        102727 to R.drawable.mensa_dish_69398, // Patate Sabbiose S/G
        102728 to R.drawable.mensa_dish_38799, // Pasta Alla Carbonara S/G
        102739 to R.drawable.mensa_dish_102204, // Risotto Pere E Taleggio S/G
        102740 to R.drawable.mensa_dish_102193, // Patate Gratinate Alla Senape S/G
        102741 to R.drawable.mensa_dish_102205, // Tortino Agli Asparagi S/G
        102742 to R.drawable.mensa_dish_82891, // Spaghetti Tonno, Capperi E Olive S/G
        104731 to R.drawable.mensa_dish_70780, // Cosce Di Pollo Alla Diavola S/G
        104733 to R.drawable.mensa_dish_104733, // Arrosto Di Maiale Alla Senape S/G
        104794 to R.drawable.mensa_dish_91616, // Griglia Vegetariana S/G (Solo In Mensa U12)
        104888 to R.drawable.mensa_dish_104888, // Poke Riso, Lenticchie, Cavolfiore S/G
    )

    @DrawableRes
    fun of(dishId: Int): Int? = byDish[dishId]
}

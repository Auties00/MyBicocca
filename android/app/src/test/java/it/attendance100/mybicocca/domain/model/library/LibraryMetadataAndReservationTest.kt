package it.attendance100.mybicocca.domain.model.library

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.LocalDateTime

/**
 * Coverage for static library metadata, building/address/phone formatting, campus sorting,
 * proximity matching, Monza career-programme matching, and reservation-to-zone resolution.
 */
class LibraryMetadataAndReservationTest {

    @Test
    fun `catalogue looks up Centrale and Medicina and returns null for unknown ids`() {
        val centrale = LibraryCatalog.byId(LibraryCatalog.CENTRALE_ID)
        assertThat(centrale).isNotNull()
        assertThat(centrale!!.campus).isEqualTo(LibraryCampus.Milano)
        assertThat(centrale.floorIt).isEqualTo("II piano")
        assertThat(centrale.floorEn).isEqualTo("2nd floor")
        assertThat(centrale.mapBuildingCode).isEqualTo("U06")
        assertThat(centrale.photoUrl).contains("biblio.unimib.it")

        val medicina = LibraryCatalog.byId(LibraryCatalog.MEDICINA_ID)
        assertThat(medicina).isNotNull()
        assertThat(medicina!!.campus).isEqualTo(LibraryCampus.Monza)
        assertThat(medicina.floorIt).isEqualTo("piano terra")
        assertThat(medicina.floorEn).isEqualTo("ground floor")
        assertThat(medicina.mapBuildingCode).isEqualTo("U08")
        assertThat(medicina.photoUrl).contains("affluences.media")

        assertThat(LibraryCatalog.byId("unknown-site-id")).isNull()
    }

    @Test
    fun `buildingLine strips Unimib suffix and appends localized floor`() {
        assertThat(LibraryCatalog.buildingLine("Ed. U6 Agorà (Unimib)", "II piano"))
            .isEqualTo("Ed. U6 Agorà · II piano")
        assertThat(LibraryCatalog.buildingLine("Ed. U6 Agorà (Unimib)", "2nd floor"))
            .isEqualTo("Ed. U6 Agorà · 2nd floor")
        assertThat(LibraryCatalog.buildingLine("Ed. U8 Asclepio (Unimib)", "piano terra"))
            .isEqualTo("Ed. U8 Asclepio · piano terra")
        assertThat(LibraryCatalog.buildingLine("Ed. U9 (UNIMIB)", null))
            .isEqualTo("Ed. U9")
        assertThat(LibraryCatalog.buildingLine(null, null)).isNull()
    }

    @Test
    fun `cleanAddress removes trailing Italie or Italia`() {
        assertThat(LibraryCatalog.cleanAddress("Piazza dell'Ateneo Nuovo, 1, 20126 Milano MI, Italie"))
            .isEqualTo("Piazza dell'Ateneo Nuovo, 1, 20126 Milano MI")
        assertThat(LibraryCatalog.cleanAddress("Via Cadore 48, 20900 Monza MB, Italia"))
            .isEqualTo("Via Cadore 48, 20900 Monza MB")
        assertThat(LibraryCatalog.cleanAddress("Via Cadore 48, 20900 Monza MB"))
            .isEqualTo("Via Cadore 48, 20900 Monza MB")
        assertThat(LibraryCatalog.cleanAddress("   ")).isNull()
    }

    @Test
    fun `formatPhone formats Italian Milan numbers and preserves unrecognized inputs`() {
        assertThat(LibraryCatalog.formatPhone("+390264486251")).isEqualTo("+39 02 6448 6251")
        assertThat(LibraryCatalog.formatPhone("0264486251")).isEqualTo("+39 02 6448 6251")
        assertThat(LibraryCatalog.formatPhone("02 6448 6251")).isEqualTo("+39 02 6448 6251")
        assertThat(LibraryCatalog.formatPhone("00390264486251")).isEqualTo("+39 02 6448 6251")
        assertThat(LibraryCatalog.formatPhone("+390264488001")).isEqualTo("+39 02 6448 8001")
        assertThat(LibraryCatalog.formatPhone("+44 20 7946 0958")).isEqualTo("+44 20 7946 0958")
        assertThat(LibraryCatalog.formatPhone("   ")).isNull()
    }

    @Test
    fun `sortForCampus orders preferred campus first and defaults to Centrale first`() {
        val centrale = library(LibraryCatalog.CENTRALE_ID)
        val medicina = library(LibraryCatalog.MEDICINA_ID)

        assertThat(LibraryCatalog.sortForCampus(listOf(medicina, centrale), null).map { it.id })
            .containsExactly(LibraryCatalog.CENTRALE_ID, LibraryCatalog.MEDICINA_ID).inOrder()
        assertThat(LibraryCatalog.sortForCampus(listOf(medicina, centrale), LibraryCampus.Milano).map { it.id })
            .containsExactly(LibraryCatalog.CENTRALE_ID, LibraryCatalog.MEDICINA_ID).inOrder()
        assertThat(LibraryCatalog.sortForCampus(listOf(centrale, medicina), LibraryCampus.Monza).map { it.id })
            .containsExactly(LibraryCatalog.MEDICINA_ID, LibraryCatalog.CENTRALE_ID).inOrder()
    }

    @Test
    fun `nearestWithinKm matches within 5 km and returns null outside 5 km`() {
        assertThat(LibraryCatalog.nearestWithinKm(45.5182, 9.21392)?.id).isEqualTo(LibraryCatalog.CENTRALE_ID)
        assertThat(LibraryCatalog.nearestWithinKm(45.6039, 9.26105)?.id).isEqualTo(LibraryCatalog.MEDICINA_ID)
        // Roughly 2 km south of Centrale.
        assertThat(LibraryCatalog.nearestWithinKm(45.5000, 9.21392)?.id).isEqualTo(LibraryCatalog.CENTRALE_ID)
        // Midpoint between Centrale and Medicina (~5.1 km from each) -> outside 5 km.
        assertThat(LibraryCatalog.nearestWithinKm(45.56105, 9.237485)).isNull()
        // Far away (Bergamo).
        assertThat(LibraryCatalog.nearestWithinKm(45.6983, 9.6773)).isNull()
    }

    @Test
    fun `campusForProgramme matches every Monza programme entry including uppercase, accents and suffixes`() {
        val monzaInputs = listOf(
            "medicina e chirurgia",
            "MEDICINA E CHIRURGIA (ABILITANTE ALL'ESERCIZIO DELLA PROFESSIONE DI MEDICO CHIRURGO)",
            "medicine and surgery",
            "MEDICINE AND SURGERY (SINGLE-CYCLE DEGREE)",
            "odontoiatria e protesi dentaria",
            "ODONTOIATRIA E PROTESI DENTARIA (ABILITANTE)",
            "fisioterapia",
            "FISIOTERAPIA (ABILITANTE ALLA PROFESSIONE SANITARIA DI FISIOTERAPISTA)",
            "igiene dentale",
            "IGIENE DENTALE (ABILITANTE)",
            "infermieristica",
            "INFERMIERISTICA (ABILITANTE ALLA PROFESSIONE SANITARIA DI INFERMIERE)",
            "ostetricia",
            "OSTETRICIA (ABILITANTE)",
            "tecniche di laboratorio biomedico",
            "TECNICHE DI LABORATORIO BIOMEDICO (ABILITANTE)",
            "tecniche di radiologia medica",
            "TECNICHE DI RADIOLOGIA MEDICA, PER IMMAGINI E RADIOTERAPIA",
            "terapia della neuro e psicomotricita",
            "TERAPIA DELLA NEURO E PSICOMOTRICITÀ DELL'ETÀ EVOLUTIVA (ABILITANTE)",
            "biotecnologie mediche",
            "BIOTECNOLOGIE MEDICHE",
            "scienze infermieristiche e ostetriche",
            "SCIENZE INFERMIERISTICHE E OSTETRICHE",
        )
        for (input in monzaInputs) {
            assertThat(LibraryCatalog.campusForProgramme(input)).isEqualTo(LibraryCampus.Monza)
        }
    }

    @Test
    fun `campusForProgramme routes near-misses and Milano degrees to Milano and skips blank`() {
        val milanoInputs = listOf(
            "biotecnologie",
            "BIOTECNOLOGIE",
            "BIOTECNOLOGIE INDUSTRIALI",
            "INFORMATICA",
            "MEDICINA",
            "SCIENZE BIOLOGICHE",
            "TECNICHE DI LABORATORIO",
            "TECNICHE DI RADIOLOGIA",
            "PREFISIOTERAPIA",
            "FISIOTERAPIAX",
            "IGIENE",
            "PSICOLOGIA CLINICA",
            "SCIENZE MEDICO-BIOLOGICHE",
        )
        for (input in milanoInputs) {
            assertThat(LibraryCatalog.campusForProgramme(input)).isEqualTo(LibraryCampus.Milano)
        }
        assertThat(LibraryCatalog.campusForProgramme(null)).isNull()
        assertThat(LibraryCatalog.campusForProgramme("   ")).isNull()
    }

    @Test
    fun `reservation maps Centrale seat prefixes to zone colors and everything else to desk`() {
        val cases = listOf(
            "R 1" to LibraryZoneColor.Rossa,
            "G 11 - Posto con presa elettrica" to LibraryZoneColor.Gialla,
            "V 17" to LibraryZoneColor.Verde,
            "L 4" to LibraryZoneColor.Lilla,
            "B 9" to LibraryZoneColor.Blu,
            "A 2" to LibraryZoneColor.Arancio,
            "Carrel 12" to LibraryZoneColor.Carrels,
            "Posto 101" to LibraryZoneColor.Other,
            "Aula 3" to LibraryZoneColor.Other,
        )
        for ((seat, expectedColor) in cases) {
            val res = reservation("Biblioteca Centrale", "Ed. U6 Agorà (Unimib)", seat)
            assertThat(res.zoneColor()).isEqualTo(expectedColor)
            assertThat(res.compactLibraryName()).isEqualTo("Centrale")
            assertThat(res.mapBuildingCode()).isEqualTo("U06")
        }

        // All Medicina seats use the desk drawing regardless of prefix.
        for (seat in listOf("R 1", "Carrel 12", "Posto 101")) {
            val res = reservation("Biblioteca di Medicina", "Ed. U8 Asclepio (Unimib)", seat)
            assertThat(res.zoneColor()).isEqualTo(LibraryZoneColor.Other)
            assertThat(res.compactLibraryName()).isEqualTo("Medicina")
            assertThat(res.mapBuildingCode()).isEqualTo("U08")
        }

        // Unmatched library uses desk, full library name, and null building code.
        val unmatched = reservation("Biblioteca Esterna", "Ed. U9", "R 1 - Posto con presa elettrica")
        assertThat(unmatched.zoneColor()).isEqualTo(LibraryZoneColor.Other)
        assertThat(unmatched.compactLibraryName()).isEqualTo("Biblioteca Esterna")
        assertThat(unmatched.mapBuildingCode()).isNull()
        assertThat(unmatched.shortSeatName()).isEqualTo("R 1")
    }

    @Test
    fun `reservation lifecycle helpers handle upcoming, check-in window, in progress and ended`() {
        val start = LocalDateTime.of(2026, 6, 18, 16, 0)
        val end = LocalDateTime.of(2026, 6, 18, 18, 30)
        val res = reservation("Biblioteca Centrale", null, "R 42", start = start, end = end)

        // 15 minutes before: not in check-in window yet.
        val beforeWindow = start.minusMinutes(15)
        assertThat(res.isCheckInOpenAt(beforeWindow)).isFalse()
        assertThat(res.isUpcomingSoonAt(beforeWindow)).isFalse()
        assertThat(res.isInProgressAt(beforeWindow)).isFalse()
        assertThat(res.isEndedAt(beforeWindow)).isFalse()

        // 10 minutes before: check-in open, "Tra poco".
        val atWindow = start.minusMinutes(10)
        assertThat(res.isCheckInOpenAt(atWindow)).isTrue()
        assertThat(res.isUpcomingSoonAt(atWindow)).isTrue()
        assertThat(res.isInProgressAt(atWindow)).isFalse()

        // At start: in progress ("In corso").
        assertThat(res.isCheckInOpenAt(start)).isTrue()
        assertThat(res.isUpcomingSoonAt(start)).isFalse()
        assertThat(res.isInProgressAt(start)).isTrue()

        // At end: ended.
        assertThat(res.isEndedAt(end)).isTrue()
        assertThat(res.isCheckInOpenAt(end)).isFalse()
        assertThat(res.isInProgressAt(end)).isFalse()
    }

    private fun library(id: String) = Library(
        id = id,
        slug = id,
        name = id,
        secondaryName = null,
        address = null,
        latitude = null,
        longitude = null,
        pictureUrl = null,
        phone = null,
        email = null,
        websiteUrl = null,
        bookable = true,
        liveStatus = null,
    )

    private fun reservation(
        libraryName: String,
        secondaryName: String?,
        seatName: String,
        start: LocalDateTime = LocalDateTime.of(2026, 6, 18, 16, 0),
        end: LocalDateTime = LocalDateTime.of(2026, 6, 18, 18, 30),
    ) = LibraryReservation(
        reservationId = 1,
        libraryName = libraryName,
        librarySecondaryName = secondaryName,
        seatName = seatName,
        start = start,
        end = end,
        note = null,
        reservationCode = "A7K2",
        cancellationToken = "tok",
        state = LibraryReservationState.Upcoming,
    )
}

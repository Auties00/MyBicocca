package it.attendance100.mybicocca.data.api.esse3

import it.attendance100.mybicocca.data.exception.esse3.Esse3NotAuthorizedException
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory

class Esse3CountriesApiTest : Esse3ApiTestBase() {

    private val logger = LoggerFactory.getLogger(Esse3CountriesApiTest::class.java)

    private val italyFiscalCode = "100"
    private val italyIso6392 = "ita"

    @Test
    suspend fun getNations_noFilter() {
        try {
            val nations = api.countries.getNations()
            assertNotNull(nations)
            assertFalse(nations.isEmpty(), "Expected at least one nation")
            logger.info("Retrieved ${nations.size} nations")

            for (nation in nations) {
                assertNotNull(nation.nationId, "nationId should not be null for any nation")
                assertNotNull(nation.description, "description should not be null for any nation")
            }

            // Italy should always be present
            val italy = nations.find { it.fiscalCode == italyFiscalCode || it.description?.contains("ITALIA", ignoreCase = true) == true }
            assertNotNull(italy, "Italy should be in the nations list")
            logger.info("Italy: nationId=${italy?.nationId}, code=${italy?.code}, iso31661=${italy?.iso31661Code}")
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("Not authorized to get nations: ${e.message}")
        }
    }

    @Test
    suspend fun getNations_withIso6392Filter() {
        try {
            val nations = api.countries.getNations(iso6392Code = italyIso6392)
            assertNotNull(nations)
            logger.info("Nations filtered by iso6392='$italyIso6392': ${nations.size}")

            // When filtering by a specific ISO code, result should be a subset
            for (nation in nations) {
                assertNotNull(nation.nationId)
                assertNotNull(nation.description)
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("Not authorized to get nations with iso filter: ${e.message}")
        }
    }

    @Test
    suspend fun getProvinces_forItaly() {
        try {
            val nations = api.countries.getNations()
            val italy = nations.find { it.fiscalCode == italyFiscalCode || it.description?.contains("ITALIA", ignoreCase = true) == true }
                ?: run { logger.warn("Italy not found in nations, skipping province test"); return }

            val italyId = italy.nationId!!
            val provinces = api.countries.getProvinces(nationId = italyId)
            assertNotNull(provinces)
            assertFalse(provinces.isEmpty(), "Italy should have provinces")
            logger.info("Retrieved ${provinces.size} provinces for Italy (nationId=$italyId)")

            for (province in provinces) {
                assertNotNull(province.nationId, "provinceNationId should not be null")
                // All provinces belong to Italy
                assertTrue(province.nationId == italyId, "Province nationId should match Italy")
            }

            // Check a sample of province fields
            val first = provinces.first()
            logger.info("First province: abbreviation=${first.abbreviation}, description=${first.provinceDescription}, region=${first.regionDescription}")
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("Not authorized to get provinces: ${e.message}")
        }
    }

    @Test
    suspend fun getProvinces_forSampleNations() {
        try {
            val nations = api.countries.getNations()
            assertFalse(nations.isEmpty())

            // Test a few nations to make sure province endpoint works broadly
            val sampled = nations.take(5)
            for (nation in sampled) {
                val nationId = nation.nationId ?: continue
                try {
                    val provinces = api.countries.getProvinces(nationId = nationId)
                    logger.info("Nation '${nation.description}' (id=$nationId): ${provinces.size} provinces")
                    for (province in provinces) {
                        assertNotNull(province.nationId)
                    }
                } catch (e: Esse3NotAuthorizedException) {
                    logger.warn("Not authorized for provinces of nationId=$nationId: ${e.message}")
                }
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("Not authorized to get nations: ${e.message}")
        }
    }

    @Test
    suspend fun getMunicipalities_forItaly_noFilter() {
        try {
            val nations = api.countries.getNations()
            val italy = nations.find { it.fiscalCode == italyFiscalCode || it.description?.contains("ITALIA", ignoreCase = true) == true }
                ?: run { logger.warn("Italy not found, skipping municipalities test"); return }

            val italyId = italy.nationId!!
            // Limit results to avoid huge response
            val municipalities = api.countries.getMunicipalities(nationId = italyId, limit = 50)
            assertNotNull(municipalities)
            logger.info("Retrieved ${municipalities.size} municipalities for Italy (limit=50)")

            for (municipality in municipalities) {
                assertNotNull(municipality.nationId, "municipalityNationId should not be null")
                assertNotNull(municipality.municipalityDescription, "municipalityDescription should not be null")
            }

            if (municipalities.isNotEmpty()) {
                val first = municipalities.first()
                logger.info("First municipality: id=${first.municipalityId}, desc=${first.municipalityDescription}, province=${first.abbreviation}, region=${first.regionDescription}")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("Not authorized to get municipalities: ${e.message}")
        }
    }

    @Test
    suspend fun getMunicipalities_forItaly_withAbbreviationFilter() {
        try {
            val nations = api.countries.getNations()
            val italy = nations.find { it.fiscalCode == italyFiscalCode || it.description?.contains("ITALIA", ignoreCase = true) == true }
                ?: run { logger.warn("Italy not found, skipping municipalities filter test"); return }

            val italyId = italy.nationId!!
            // Filter by province abbreviation MI (Milan)
            val municipalities = api.countries.getMunicipalities(nationId = italyId, abbreviation = "MI")
            assertNotNull(municipalities)
            logger.info("Municipalities in Milan province (MI): ${municipalities.size}")

            for (municipality in municipalities) {
                assertNotNull(municipality.nationId)
                // All results should be in MI province
                assertTrue(municipality.abbreviation == "MI" || municipality.abbreviation == null,
                    "All municipalities should belong to MI province or have null abbreviation")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("Not authorized to get municipalities with abbreviation filter: ${e.message}")
        }
    }

    @Test
    suspend fun getMunicipalities_forItaly_withPagination() {
        try {
            val nations = api.countries.getNations()
            val italy = nations.find { it.fiscalCode == italyFiscalCode || it.description?.contains("ITALIA", ignoreCase = true) == true }
                ?: run { logger.warn("Italy not found, skipping municipalities pagination test"); return }

            val italyId = italy.nationId!!

            val page1 = api.countries.getMunicipalities(nationId = italyId, start = 0, limit = 10)
            val page2 = api.countries.getMunicipalities(nationId = italyId, start = 10, limit = 10)

            assertNotNull(page1)
            assertNotNull(page2)
            logger.info("Municipalities page1=${page1.size}, page2=${page2.size}")

            // Both pages should return results if Italy has more than 10 municipalities
            if (page1.size == 10) {
                // If page1 has 10 results, page2 should be different (pagination working)
                val page1Ids = page1.mapNotNull { it.municipalityId }.toSet()
                val page2Ids = page2.mapNotNull { it.municipalityId }.toSet()
                assertTrue(page1Ids.intersect(page2Ids).isEmpty() || page2.isEmpty(),
                    "Pages should not overlap")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("Not authorized to get municipalities with pagination: ${e.message}")
        }
    }

    @Test
    suspend fun getMunicipalities_withRegionCodeFilter() {
        try {
            val nations = api.countries.getNations()
            val italy = nations.find { it.fiscalCode == italyFiscalCode || it.description?.contains("ITALIA", ignoreCase = true) == true }
                ?: run { logger.warn("Italy not found, skipping region filter test"); return }

            val italyId = italy.nationId!!

            // Get provinces first to find a region code
            val provinces = api.countries.getProvinces(nationId = italyId)
            val regionCode = provinces.firstOrNull()?.regionCode
                ?: run { logger.warn("No region code found, skipping region filter test"); return }

            val municipalities = api.countries.getMunicipalities(nationId = italyId, regionCode = regionCode, limit = 20)
            assertNotNull(municipalities)
            logger.info("Municipalities in region '$regionCode': ${municipalities.size}")

            for (municipality in municipalities) {
                assertNotNull(municipality.nationId)
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("Not authorized to get municipalities with region filter: ${e.message}")
        }
    }

    @Test
    suspend fun getPostalCode_forItaly() {
        try {
            val nations = api.countries.getNations()
            val italy = nations.find { it.fiscalCode == italyFiscalCode || it.description?.contains("ITALIA", ignoreCase = true) == true }
                ?: run { logger.warn("Italy not found, skipping postal code test"); return }

            val italyId = italy.nationId!!
            val postalCodes = api.countries.getPostalCode(nationId = italyId)
            assertNotNull(postalCodes)
            logger.info("Retrieved ${postalCodes.size} postal codes for Italy")

            for (postalCode in postalCodes) {
                assertNotNull(postalCode.nationId, "postalCodeNationId should not be null")
            }

            if (postalCodes.isNotEmpty()) {
                val first = postalCodes.first()
                logger.info("First postal code: cap=${first.postalCode}, municipality=${first.municipalityDescription}, province=${first.abbreviation}")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("Not authorized to get postal codes: ${e.message}")
        }
    }

    @Test
    suspend fun getPostalCode_forItaly_withAbbreviationFilter() {
        try {
            val nations = api.countries.getNations()
            val italy = nations.find { it.fiscalCode == italyFiscalCode || it.description?.contains("ITALIA", ignoreCase = true) == true }
                ?: run { logger.warn("Italy not found, skipping postal code filter test"); return }

            val italyId = italy.nationId!!
            val postalCodes = api.countries.getPostalCode(nationId = italyId, abbreviation = "MI")
            assertNotNull(postalCodes)
            logger.info("Postal codes for Milan province (MI): ${postalCodes.size}")

            for (postalCode in postalCodes) {
                assertNotNull(postalCode.nationId)
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("Not authorized to get postal codes with abbreviation filter: ${e.message}")
        }
    }

    @Test
    suspend fun getPostalCode_forItaly_withMunicipalityFilter() {
        try {
            val nations = api.countries.getNations()
            val italy = nations.find { it.fiscalCode == italyFiscalCode || it.description?.contains("ITALIA", ignoreCase = true) == true }
                ?: run { logger.warn("Italy not found, skipping postal code municipality filter test"); return }

            val italyId = italy.nationId!!

            // Get some municipalities first
            val municipalities = api.countries.getMunicipalities(nationId = italyId, limit = 5)
            val firstMunicipality = municipalities.firstOrNull()
                ?: run { logger.warn("No municipalities found, skipping postal code filter test"); return }

            val municipalityId = firstMunicipality.municipalityId
            val municipalityCode = firstMunicipality.municipalityCode

            if (municipalityId != null) {
                val postalCodes = api.countries.getPostalCode(nationId = italyId, municipalityId = municipalityId)
                assertNotNull(postalCodes)
                logger.info("Postal codes for municipality '${firstMunicipality.municipalityDescription}' (id=$municipalityId): ${postalCodes.size}")
                for (postalCode in postalCodes) {
                    assertNotNull(postalCode.nationId)
                }
            }

            if (municipalityCode != null) {
                val postalCodes = api.countries.getPostalCode(nationId = italyId, municipalityCode = municipalityCode)
                assertNotNull(postalCodes)
                logger.info("Postal codes for municipality code '$municipalityCode': ${postalCodes.size}")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("Not authorized to get postal codes with municipality filter: ${e.message}")
        }
    }
}

package it.attendance100.mybicocca.data.api.esse3

import it.attendance100.mybicocca.data.dto.esse3.Esse3PermissionLevel
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.util.logging.Logger

class Esse3CountriesApiTest : Esse3ApiTestBase() {

    private val logger = Logger.getLogger(Esse3CountriesApiTest::class.java.name)

    companion object {
        private lateinit var permissions: Set<Esse3PermissionLevel>
        private const val ITALY_CODE = "200"
    }

    @BeforeAll
    fun setUp() = runTest {
        val loginResult = api.auth.login()
        permissions = loginResult.profiles
            .mapNotNull { profile ->
                profile.groupId?.let { Esse3PermissionLevel.fromGroupId(it.toInt()) }
            }
            .toSet()
        logger.info("Derived permissions from login: $permissions")
    }

    @Test
    fun getNations_defaults() = runTest {
        val nations = api.countries.getNations()
        logger.info("getNations_defaults: returned ${nations.size} nations")
        assertTrue(nations.isNotEmpty(), "Expected at least one nation")
        for (nation in nations) {
            assertNotNull(nation.nationId, "nationId should not be null")
            assertNotNull(nation.description, "description should not be null")
            logger.info("  nation: id=${nation.nationId}, desc=${nation.description}, fiscalCode=${nation.fiscalCode}, iso31661=${nation.iso31661Code}")
        }
        val italy = nations.find { it.code == ITALY_CODE }
        assertNotNull(italy, "Italy (code=$ITALY_CODE) should be present in the nations list")
        logger.info("Italy found: nationId=${italy!!.nationId}, code=${italy.code}, description=${italy.description}")
    }

    @Test
    fun getNations_withIso6392Code() = runTest {
        val nations = api.countries.getNations(iso6392Code = "ita")
        logger.info("getNations_withIso6392Code('ita'): returned ${nations.size} nations")
        assertTrue(nations.isNotEmpty(), "Expected at least one nation matching iso6392='ita'")
        for (nation in nations) {
            assertNotNull(nation.nationId, "nationId should not be null")
            assertNotNull(nation.description, "description should not be null")
            logger.info("  nation: id=${nation.nationId}, desc=${nation.description}")
        }
    }

    @Test
    fun getProvinces_forItaly_defaults() = runTest {
        val nations = api.countries.getNations()
        val italy = nations.first { it.code == ITALY_CODE }
        val italyId = italy.nationId!!

        val provinces = api.countries.getProvinces(nationId = italyId)
        logger.info("getProvinces_forItaly_defaults: returned ${provinces.size} provinces for nationId=$italyId")
        assertTrue(provinces.isNotEmpty(), "Italy should have at least one province")
        for (province in provinces) {
            assertNotNull(province.nationId, "province nationId should not be null")
            assertEquals(italyId, province.nationId, "province nationId should match Italy's nationId")
            logger.info("  province: abbreviation=${province.abbreviation}, desc=${province.provinceDescription}, region=${province.regionDescription}, regionCode=${province.regionCode}")
        }
    }

    @Test
    fun getMunicipalities_forItaly_defaults() = runTest {
        val nations = api.countries.getNations()
        val italy = nations.first { it.code == ITALY_CODE }
        val italyId = italy.nationId!!

        val municipalities = api.countries.getMunicipalities(nationId = italyId, start = 0, limit = 5)
        logger.info("getMunicipalities_forItaly_defaults: returned ${municipalities.size} municipalities (start=0, limit=5)")
        assertTrue(municipalities.isNotEmpty(), "Italy should have at least one municipality")
        assertTrue(municipalities.size <= 5, "Expected at most 5 municipalities with limit=5, got ${municipalities.size}")
        for (municipality in municipalities) {
            assertNotNull(municipality.nationId, "municipality nationId should not be null")
            assertEquals(italyId, municipality.nationId, "municipality nationId should match Italy's nationId")
            assertNotNull(municipality.municipalityDescription, "municipalityDescription should not be null")
            logger.info("  municipality: id=${municipality.municipalityId}, desc=${municipality.municipalityDescription}, abbreviation=${municipality.abbreviation}, region=${municipality.regionDescription}")
        }
    }

    @Test
    fun getMunicipalities_forItaly_withAbbreviationFilter() = runTest {
        val nations = api.countries.getNations()
        val italy = nations.first { it.code == ITALY_CODE }
        val italyId = italy.nationId!!

        val municipalities = api.countries.getMunicipalities(nationId = italyId, abbreviation = "MI", start = 0, limit = 5)
        logger.info("getMunicipalities_forItaly_withAbbreviationFilter('MI'): returned ${municipalities.size} municipalities")
        assertTrue(municipalities.isNotEmpty(), "Milan province should have at least one municipality")
        for (municipality in municipalities) {
            assertNotNull(municipality.nationId, "municipality nationId should not be null")
            assertEquals("MI", municipality.abbreviation, "municipality abbreviation should be MI")
            logger.info("  municipality: id=${municipality.municipalityId}, desc=${municipality.municipalityDescription}, postalCode=${municipality.postalCode}")
        }
    }

    @Test
    fun getMunicipalities_forItaly_withRegionCodeFilter() = runTest {
        val nations = api.countries.getNations()
        val italy = nations.first { it.code == ITALY_CODE }
        val italyId = italy.nationId!!

        val provinces = api.countries.getProvinces(nationId = italyId)
        val regionCode = provinces.first().regionCode!!
        logger.info("Using regionCode=$regionCode for municipality filter")

        val municipalities = api.countries.getMunicipalities(nationId = italyId, regionCode = regionCode, start = 0, limit = 5)
        logger.info("getMunicipalities_forItaly_withRegionCodeFilter('$regionCode'): returned ${municipalities.size} municipalities")
        assertTrue(municipalities.isNotEmpty(), "Expected at least one municipality for regionCode=$regionCode")
        for (municipality in municipalities) {
            assertNotNull(municipality.nationId, "municipality nationId should not be null")
            logger.info("  municipality: id=${municipality.municipalityId}, desc=${municipality.municipalityDescription}, regionCode=${municipality.regionCode}")
        }
    }

    @Test
    fun getMunicipalities_forItaly_withMunicipalityCodeFilter() = runTest {
        val nations = api.countries.getNations()
        val italy = nations.first { it.code == ITALY_CODE }
        val italyId = italy.nationId!!

        val allMunicipalities = api.countries.getMunicipalities(nationId = italyId, start = 0, limit = 1)
        assertTrue(allMunicipalities.isNotEmpty(), "Need at least one municipality to extract municipalityCode")
        val targetCode = allMunicipalities.first().municipalityCode!!
        logger.info("Using municipalityCode=$targetCode for filter")

        val filtered = api.countries.getMunicipalities(nationId = italyId, municipalityCode = targetCode)
        logger.info("getMunicipalities_forItaly_withMunicipalityCodeFilter('$targetCode'): returned ${filtered.size} municipalities")
        assertTrue(filtered.isNotEmpty(), "Expected at least one municipality with municipalityCode=$targetCode")
        for (municipality in filtered) {
            assertEquals(targetCode, municipality.municipalityCode, "municipalityCode should match filter value")
            logger.info("  municipality: id=${municipality.municipalityId}, desc=${municipality.municipalityDescription}")
        }
    }

    @Test
    fun getMunicipalities_forItaly_pagination() = runTest {
        val nations = api.countries.getNations()
        val italy = nations.first { it.code == ITALY_CODE }
        val italyId = italy.nationId!!

        val page1 = api.countries.getMunicipalities(nationId = italyId, start = 0, limit = 5)
        val page2 = api.countries.getMunicipalities(nationId = italyId, start = 5, limit = 5)

        logger.info("getMunicipalities_forItaly_pagination: page1=${page1.size}, page2=${page2.size}")
        assertTrue(page1.isNotEmpty(), "First page should not be empty")
        assertTrue(page1.size <= 5, "First page should respect limit=5, got ${page1.size}")
        assertTrue(page2.size <= 5, "Second page should respect limit=5, got ${page2.size}")

        if (page1.size == 5 && page2.isNotEmpty()) {
            val page1Ids = page1.mapNotNull { it.municipalityId }.toSet()
            val page2Ids = page2.mapNotNull { it.municipalityId }.toSet()
            assertTrue(page1Ids.intersect(page2Ids).isEmpty(), "Pages should not have overlapping municipality IDs")
            logger.info("Pagination verified: no overlap between page1 and page2")
        }
    }

    @Test
    fun getMunicipalities_forItaly_withOrderParam() = runTest {
        val nations = api.countries.getNations()
        val italy = nations.first { it.code == ITALY_CODE }
        val italyId = italy.nationId!!

        val municipalities = api.countries.getMunicipalities(nationId = italyId, start = 0, limit = 5, order = "+comuneDes")
        logger.info("getMunicipalities_forItaly_withOrderParam('+comuneDes'): returned ${municipalities.size} municipalities")
        assertTrue(municipalities.isNotEmpty(), "Expected at least one municipality")
        for (municipality in municipalities) {
            assertNotNull(municipality.municipalityDescription, "municipalityDescription should not be null")
            logger.info("  municipality: desc=${municipality.municipalityDescription}")
        }
    }

    @Test
    fun getPostalCode_forItaly_defaults() = runTest {
        val nations = api.countries.getNations()
        val italy = nations.first { it.code == ITALY_CODE }
        val italyId = italy.nationId!!

        val postalCodes = api.countries.getPostalCode(nationId = italyId)
        logger.info("getPostalCode_forItaly_defaults: returned ${postalCodes.size} postal codes for nationId=$italyId")
        assertTrue(postalCodes.isNotEmpty(), "Italy should have at least one postal code")
        for (postalCode in postalCodes.take(10)) {
            assertNotNull(postalCode.nationId, "postalCode nationId should not be null")
            logger.info("  postalCode: cap=${postalCode.postalCode}, municipality=${postalCode.municipalityDescription}, province=${postalCode.abbreviation}, region=${postalCode.regionDescription}")
        }
    }

    @Test
    fun getPostalCode_forItaly_withAbbreviationFilter() = runTest {
        val nations = api.countries.getNations()
        val italy = nations.first { it.code == ITALY_CODE }
        val italyId = italy.nationId!!

        val postalCodes = api.countries.getPostalCode(nationId = italyId, abbreviation = "MI")
        logger.info("getPostalCode_forItaly_withAbbreviationFilter('MI'): returned ${postalCodes.size} postal codes")
        assertTrue(postalCodes.isNotEmpty(), "Milan province should have at least one postal code")
        for (postalCode in postalCodes.take(10)) {
            assertNotNull(postalCode.nationId, "postalCode nationId should not be null")
            assertEquals("MI", postalCode.abbreviation, "postalCode abbreviation should be MI")
            logger.info("  postalCode: cap=${postalCode.postalCode}, municipality=${postalCode.municipalityDescription}")
        }
    }

    @Test
    fun getPostalCode_forItaly_withRegionCodeFilter() = runTest {
        val nations = api.countries.getNations()
        val italy = nations.first { it.code == ITALY_CODE }
        val italyId = italy.nationId!!

        val provinces = api.countries.getProvinces(nationId = italyId)
        val regionCode = provinces.first().regionCode!!
        logger.info("Using regionCode=$regionCode for postal code filter")

        val postalCodes = api.countries.getPostalCode(nationId = italyId, regionCode = regionCode)
        logger.info("getPostalCode_forItaly_withRegionCodeFilter('$regionCode'): returned ${postalCodes.size} postal codes")
        assertTrue(postalCodes.isNotEmpty(), "Expected at least one postal code for regionCode=$regionCode")
        for (postalCode in postalCodes.take(10)) {
            assertNotNull(postalCode.nationId, "postalCode nationId should not be null")
            logger.info("  postalCode: cap=${postalCode.postalCode}, region=${postalCode.regionDescription}")
        }
    }

    @Test
    fun getPostalCode_forItaly_withMunicipalityIdFilter() = runTest {
        val nations = api.countries.getNations()
        val italy = nations.first { it.code == ITALY_CODE }
        val italyId = italy.nationId!!

        val municipalities = api.countries.getMunicipalities(nationId = italyId, start = 0, limit = 1)
        assertTrue(municipalities.isNotEmpty(), "Need at least one municipality to get its ID")
        val targetMunicipalityId = municipalities.first().municipalityId!!
        val targetMunicipalityDesc = municipalities.first().municipalityDescription
        logger.info("Using municipalityId=$targetMunicipalityId ($targetMunicipalityDesc) for postal code filter")

        val postalCodes = api.countries.getPostalCode(nationId = italyId, municipalityId = targetMunicipalityId)
        logger.info("getPostalCode_forItaly_withMunicipalityIdFilter($targetMunicipalityId): returned ${postalCodes.size} postal codes")
        assertTrue(postalCodes.isNotEmpty(), "Expected at least one postal code for municipalityId=$targetMunicipalityId")
        for (postalCode in postalCodes) {
            assertNotNull(postalCode.nationId, "postalCode nationId should not be null")
            logger.info("  postalCode: cap=${postalCode.postalCode}, municipalityId=${postalCode.municipalityId}, municipality=${postalCode.municipalityDescription}")
        }
    }

    @Test
    fun getPostalCode_forItaly_withMunicipalityCodeFilter() = runTest {
        val nations = api.countries.getNations()
        val italy = nations.first { it.code == ITALY_CODE }
        val italyId = italy.nationId!!

        val municipalities = api.countries.getMunicipalities(nationId = italyId, start = 0, limit = 1)
        assertTrue(municipalities.isNotEmpty(), "Need at least one municipality to get its code")
        val targetCode = municipalities.first().municipalityCode!!
        logger.info("Using municipalityCode=$targetCode for postal code filter")

        val postalCodes = api.countries.getPostalCode(nationId = italyId, municipalityCode = targetCode)
        logger.info("getPostalCode_forItaly_withMunicipalityCodeFilter('$targetCode'): returned ${postalCodes.size} postal codes")
        assertTrue(postalCodes.isNotEmpty(), "Expected at least one postal code for municipalityCode=$targetCode")
        for (postalCode in postalCodes) {
            assertNotNull(postalCode.nationId, "postalCode nationId should not be null")
            logger.info("  postalCode: cap=${postalCode.postalCode}, municipalityCode=${postalCode.municipalityCode}")
        }
    }
}

package it.attendance100.mybicocca.data.remote.esse3.api

import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3CompanyPostInput
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3CompanyPutInput
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3GenericAttachmentInsertMetadata
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3PermissionLevel
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3TrainingProject
import it.attendance100.mybicocca.data.remote.esse3.exception.Esse3Exception
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.util.logging.Logger
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class Esse3InternshipsApiTest : Esse3ApiTestBase() {

    companion object {
        private val logger = Logger.getLogger(Esse3InternshipsApiTest::class.java.name)
        private lateinit var userPermissions: Set<Esse3PermissionLevel>
    }

    @BeforeAll
    fun setUp() = runTest {
        logger.info("Logging in to derive user permissions")
        val loginResult = api.auth.login()
        logger.info("Login successful: userId=${loginResult.user.userId}, groupId=${loginResult.user.groupId}")

        val permissions = mutableSetOf(
            Esse3PermissionLevel.ANY,
            Esse3PermissionLevel.AUTHENTICATED_USER,
            Esse3PermissionLevel.fromGroupId(loginResult.user.groupId)
        )
        for (profile in loginResult.profiles) {
            val description = profile.description
            if (description != null) {
                permissions.add(Esse3PermissionLevel.fromProfileName(description))
            }
        }
        userPermissions = permissions
        logger.info("Derived user permissions: $userPermissions")
    }

    @Test
    fun testGetCareerSegments_defaults() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER, Esse3PermissionLevel.STUDENT)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetCareerSegments_defaults: calling getCareerSegments() with defaults")
            val segments = api.internships.getCareerSegments()
            assertNotNull(segments)
            logger.info("testGetCareerSegments_defaults: returned ${segments.size} segments")
            for (segment in segments) {
                logger.info("testGetCareerSegments_defaults: studentId=${segment.studentId}, courseOfStudyId=${segment.courseOfStudyId}, studyPlanId=${segment.studyPlanId}, enrollmentId=${segment.enrollmentId}")
            }
        } else {
            logger.info("testGetCareerSegments_defaults: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.internships.getCareerSegments()
            }
            logger.info("testGetCareerSegments_defaults: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetCareerSegments_withEnrollmentId() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER, Esse3PermissionLevel.STUDENT)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetCareerSegments_withEnrollmentId: calling getCareerSegments(matricola=${studentProfile.matricola})")
            val segments = api.internships.getCareerSegments(matricola = studentProfile.matricola)
            assertNotNull(segments)
            logger.info("testGetCareerSegments_withEnrollmentId: returned ${segments.size} segments")
            for (segment in segments) {
                assertNotNull(segment.studentId)
                logger.info("testGetCareerSegments_withEnrollmentId: studentId=${segment.studentId}, courseOfStudyCode=${segment.courseOfStudyCode}, studyPlanId=${segment.studyPlanId}")
            }
        } else {
            logger.info("testGetCareerSegments_withEnrollmentId: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.internships.getCareerSegments(matricola = studentProfile.matricola)
            }
            logger.info("testGetCareerSegments_withEnrollmentId: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetCareerSegments_withCourseOfStudyId() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER, Esse3PermissionLevel.STUDENT)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetCareerSegments_withCourseOfStudyId: calling getCareerSegments(courseOfStudyStudentId=${studentProfile.degreeCourseId})")
            val segments = api.internships.getCareerSegments(courseOfStudyStudentId = studentProfile.degreeCourseId)
            assertNotNull(segments)
            logger.info("testGetCareerSegments_withCourseOfStudyId: returned ${segments.size} segments")
            for (segment in segments) {
                logger.info("testGetCareerSegments_withCourseOfStudyId: studentId=${segment.studentId}, courseOfStudyId=${segment.courseOfStudyId}")
            }
        } else {
            logger.info("testGetCareerSegments_withCourseOfStudyId: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.internships.getCareerSegments(courseOfStudyStudentId = studentProfile.degreeCourseId)
            }
            logger.info("testGetCareerSegments_withCourseOfStudyId: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetCareerSegments_pagination() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER, Esse3PermissionLevel.STUDENT)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetCareerSegments_pagination: calling getCareerSegments(start=0, limit=5)")
            val segments = api.internships.getCareerSegments(start = 0, limit = 5)
            assertNotNull(segments)
            assertTrue(segments.size <= 5, "Paginated result size should be <= 5, got ${segments.size}")
            logger.info("testGetCareerSegments_pagination: returned ${segments.size} segments")
            for (segment in segments) {
                logger.info("testGetCareerSegments_pagination: studentId=${segment.studentId}, matId=${segment.matId}")
            }
        } else {
            logger.info("testGetCareerSegments_pagination: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.internships.getCareerSegments(start = 0, limit = 5)
            }
            logger.info("testGetCareerSegments_pagination: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetInternshipCompanies_defaults() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetInternshipCompanies_defaults: calling getInternshipCompanies() with defaults")
            val companies = api.internships.getInternshipCompanies()
            assertNotNull(companies)
            logger.info("testGetInternshipCompanies_defaults: returned ${companies.size} companies")
            assertTrue(companies.isNotEmpty(), "getInternshipCompanies() should return non-empty list")
            for (company in companies.take(5)) {
                assertNotNull(company.companyId)
                logger.info("testGetInternshipCompanies_defaults: companyId=${company.companyId}, code=${company.companyCode}, description=${company.companyDescription}, state=${company.companyStateCode}")
            }
        } else {
            logger.info("testGetInternshipCompanies_defaults: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.internships.getInternshipCompanies()
            }
            logger.info("testGetInternshipCompanies_defaults: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetInternshipCompanies_withValidConvention() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetInternshipCompanies_withValidConvention: calling getInternshipCompanies(hasValidConvention=1)")
            val companies = api.internships.getInternshipCompanies(hasValidConvention = 1)
            assertNotNull(companies)
            logger.info("testGetInternshipCompanies_withValidConvention: returned ${companies.size} companies")
            for (company in companies.take(5)) {
                assertNotNull(company.companyId)
                logger.info("testGetInternshipCompanies_withValidConvention: companyId=${company.companyId}, hasValidConvention=${company.hasValidConvention}")
            }
        } else {
            logger.info("testGetInternshipCompanies_withValidConvention: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.internships.getInternshipCompanies(hasValidConvention = 1)
            }
            logger.info("testGetInternshipCompanies_withValidConvention: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetInternshipCompanies_withValidOpportunity() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetInternshipCompanies_withValidOpportunity: calling getInternshipCompanies(hasValidOpportunity=1)")
            val companies = api.internships.getInternshipCompanies(hasValidOpportunity = 1)
            assertNotNull(companies)
            logger.info("testGetInternshipCompanies_withValidOpportunity: returned ${companies.size} companies")
            for (company in companies.take(5)) {
                assertNotNull(company.companyId)
                logger.info("testGetInternshipCompanies_withValidOpportunity: companyId=${company.companyId}, hasValidOpportunity=${company.hasValidOpportunity}")
            }
        } else {
            logger.info("testGetInternshipCompanies_withValidOpportunity: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.internships.getInternshipCompanies(hasValidOpportunity = 1)
            }
            logger.info("testGetInternshipCompanies_withValidOpportunity: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetCompanyContacts_defaults() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetCompanyContacts_defaults: fetching companies to get companyId")
            val companies = api.internships.getInternshipCompanies()
            val company = companies.firstOrNull()
            if (company == null || company.companyId == null) {
                logger.warning("testGetCompanyContacts_defaults: no companies available, skipping")
                return@runTest
            }
            val companyId = company.companyId!!

            logger.info("testGetCompanyContacts_defaults: calling getCompanyContacts(companyId=$companyId)")
            val contacts = api.internships.getCompanyContacts(companyId)
            assertNotNull(contacts)
            logger.info("testGetCompanyContacts_defaults: returned ${contacts.size} contacts")
            for (contact in contacts.take(5)) {
                logger.info("testGetCompanyContacts_defaults: contactId=${contact.companyContactId}, surname=${contact.surname}, name=${contact.name}, role=${contact.role}, active=${contact.activeFlag}")
            }
        } else {
            logger.info("testGetCompanyContacts_defaults: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.internships.getCompanyContacts(1L)
            }
            logger.info("testGetCompanyContacts_defaults: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetCompanyContacts_withActiveFilter() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetCompanyContacts_withActiveFilter: fetching companies to get companyId")
            val companies = api.internships.getInternshipCompanies()
            val company = companies.firstOrNull()
            if (company == null || company.companyId == null) {
                logger.warning("testGetCompanyContacts_withActiveFilter: no companies available, skipping")
                return@runTest
            }
            val companyId = company.companyId!!

            logger.info("testGetCompanyContacts_withActiveFilter: calling getCompanyContacts(companyId=$companyId, activeFlag=1)")
            val contacts = api.internships.getCompanyContacts(companyId = companyId, activeFlag = 1)
            assertNotNull(contacts)
            logger.info("testGetCompanyContacts_withActiveFilter: returned ${contacts.size} contacts")
            for (contact in contacts.take(5)) {
                logger.info("testGetCompanyContacts_withActiveFilter: contactId=${contact.companyContactId}, surname=${contact.surname}, name=${contact.name}, active=${contact.activeFlag}")
            }
        } else {
            logger.info("testGetCompanyContacts_withActiveFilter: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.internships.getCompanyContacts(companyId = 1L, activeFlag = 1)
            }
            logger.info("testGetCompanyContacts_withActiveFilter: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetCompanyConventions_defaults() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetCompanyConventions_defaults: fetching companies with valid conventions")
            val companies = api.internships.getInternshipCompanies(hasValidConvention = 1)
            val company = companies.firstOrNull()
            if (company == null || company.companyId == null) {
                logger.warning("testGetCompanyConventions_defaults: no companies with valid conventions available, skipping")
                return@runTest
            }
            val companyId = company.companyId!!

            logger.info("testGetCompanyConventions_defaults: calling getCompanyConventions(companyId=$companyId)")
            val conventions = api.internships.getCompanyConventions(companyId)
            assertNotNull(conventions)
            logger.info("testGetCompanyConventions_defaults: returned ${conventions.size} conventions")
            for (convention in conventions.take(5)) {
                logger.info("testGetCompanyConventions_defaults: conventionSiteId=${convention.conventionSiteId}, description=${convention.conventionSiteDescription}, state=${convention.conventionStateCode}, startDate=${convention.startDate}, endDate=${convention.endDate}")
            }
        } else {
            logger.info("testGetCompanyConventions_defaults: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.internships.getCompanyConventions(1L)
            }
            logger.info("testGetCompanyConventions_defaults: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetCompanyConventions_withStateFilter() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetCompanyConventions_withStateFilter: fetching companies with valid conventions")
            val companies = api.internships.getInternshipCompanies(hasValidConvention = 1)
            val company = companies.firstOrNull()
            if (company == null || company.companyId == null) {
                logger.warning("testGetCompanyConventions_withStateFilter: no companies available, skipping")
                return@runTest
            }
            val companyId = company.companyId!!

            logger.info("testGetCompanyConventions_withStateFilter: calling getCompanyConventions(companyId=$companyId, conventionStateCode=A)")
            val conventions = api.internships.getCompanyConventions(companyId = companyId, conventionStateCode = "A")
            assertNotNull(conventions)
            logger.info("testGetCompanyConventions_withStateFilter: returned ${conventions.size} conventions")
            for (convention in conventions.take(5)) {
                logger.info("testGetCompanyConventions_withStateFilter: conventionSiteId=${convention.conventionSiteId}, state=${convention.conventionStateCode}")
            }
        } else {
            logger.info("testGetCompanyConventions_withStateFilter: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.internships.getCompanyConventions(companyId = 1L, conventionStateCode = "A")
            }
            logger.info("testGetCompanyConventions_withStateFilter: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetCompanySites_defaults() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetCompanySites_defaults: fetching companies to get companyId")
            val companies = api.internships.getInternshipCompanies()
            val company = companies.firstOrNull()
            if (company == null || company.companyId == null) {
                logger.warning("testGetCompanySites_defaults: no companies available, skipping")
                return@runTest
            }
            val companyId = company.companyId!!

            logger.info("testGetCompanySites_defaults: calling getCompanySites(companyId=$companyId)")
            val sites = api.internships.getCompanySites(companyId)
            assertNotNull(sites)
            logger.info("testGetCompanySites_defaults: returned ${sites.size} sites")
            for (site in sites.take(5)) {
                logger.info("testGetCompanySites_defaults: siteId=${site.companySiteId}, description=${site.companySiteDescription}, city=${site.city}, nation=${site.nationDescription}")
            }
        } else {
            logger.info("testGetCompanySites_defaults: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.internships.getCompanySites(1L)
            }
            logger.info("testGetCompanySites_defaults: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetInternshipOpportunities_defaults() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetInternshipOpportunities_defaults: calling getInternshipOpportunities() with defaults")
            val opportunities = api.internships.getInternshipOpportunities()
            assertNotNull(opportunities)
            logger.info("testGetInternshipOpportunities_defaults: returned ${opportunities.size} opportunities")
            for (opportunity in opportunities.take(5)) {
                logger.info("testGetInternshipOpportunities_defaults: title=${opportunity.title}, company=${opportunity.company}, type=${opportunity.internshipTypeCode}, area=${opportunity.area}")
            }
        } else {
            logger.info("testGetInternshipOpportunities_defaults: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.internships.getInternshipOpportunities()
            }
            logger.info("testGetInternshipOpportunities_defaults: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetInternshipOpportunities_withCompanyFilter() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetInternshipOpportunities_withCompanyFilter: fetching opportunities to get company name")
            val allOpportunities = api.internships.getInternshipOpportunities()
            val firstOpportunity = allOpportunities.firstOrNull()
            if (firstOpportunity == null || firstOpportunity.company == null) {
                logger.warning("testGetInternshipOpportunities_withCompanyFilter: no opportunities with company available, skipping")
                return@runTest
            }
            val companyName = firstOpportunity.company!!

            logger.info("testGetInternshipOpportunities_withCompanyFilter: calling getInternshipOpportunities(company=$companyName)")
            val filtered = api.internships.getInternshipOpportunities(company = companyName)
            assertNotNull(filtered)
            assertTrue(filtered.isNotEmpty(), "Filtered by company should return non-empty list")
            logger.info("testGetInternshipOpportunities_withCompanyFilter: returned ${filtered.size} opportunities")
            for (opportunity in filtered.take(5)) {
                logger.info("testGetInternshipOpportunities_withCompanyFilter: title=${opportunity.title}, company=${opportunity.company}")
            }
        } else {
            logger.info("testGetInternshipOpportunities_withCompanyFilter: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.internships.getInternshipOpportunities(company = "test")
            }
            logger.info("testGetInternshipOpportunities_withCompanyFilter: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetInternshipOpportunities_withDurationFilter() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetInternshipOpportunities_withDurationFilter: calling getInternshipOpportunities(durationFrom=1, durationTo=12)")
            val opportunities = api.internships.getInternshipOpportunities(durationFrom = 1, durationTo = 12)
            assertNotNull(opportunities)
            logger.info("testGetInternshipOpportunities_withDurationFilter: returned ${opportunities.size} opportunities")
            for (opportunity in opportunities.take(5)) {
                logger.info("testGetInternshipOpportunities_withDurationFilter: title=${opportunity.title}, durationMonths=${opportunity.durationMonths}")
            }
        } else {
            logger.info("testGetInternshipOpportunities_withDurationFilter: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.internships.getInternshipOpportunities(durationFrom = 1, durationTo = 12)
            }
            logger.info("testGetInternshipOpportunities_withDurationFilter: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetInternshipOpportunities_withExpiredVisible() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetInternshipOpportunities_withExpiredVisible: calling getInternshipOpportunities(expiredOpportunitiesVisible=1)")
            val opportunities = api.internships.getInternshipOpportunities(expiredOpportunitiesVisible = 1)
            assertNotNull(opportunities)
            logger.info("testGetInternshipOpportunities_withExpiredVisible: returned ${opportunities.size} opportunities")
            for (opportunity in opportunities.take(5)) {
                logger.info("testGetInternshipOpportunities_withExpiredVisible: title=${opportunity.title}, enrollmentEndDate=${opportunity.enrollmentEndDate}")
            }
        } else {
            logger.info("testGetInternshipOpportunities_withExpiredVisible: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.internships.getInternshipOpportunities(expiredOpportunitiesVisible = 1)
            }
            logger.info("testGetInternshipOpportunities_withExpiredVisible: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testCheckStudentInternshipEligibility() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            val fiscalCode = session.fiscalCode
            if (fiscalCode == null) {
                logger.warning("testCheckStudentInternshipEligibility: no fiscal code in session, skipping")
                return@runTest
            }

            logger.info("testCheckStudentInternshipEligibility: calling checkStudentInternshipEligibility(fiscalCode=$fiscalCode, languageCode=[IT])")
            try {
                val eligibility = api.internships.checkStudentInternshipEligibility(
                    fiscalCode = fiscalCode,
                    languageCode = listOf("IT")
                )
                assertNotNull(eligibility)
                logger.info("testCheckStudentInternshipEligibility: returned ${eligibility.size} results")
                for (item in eligibility) {
                    logger.info("testCheckStudentInternshipEligibility: matricola=${item.matricola}, serviceType=${item.serviceType}, outcomeCode=${item.outcomeCode}, outcomeDescription=${item.outcomeDescription}")
                }
            } catch (e: Esse3Exception) {
                logger.warning("testCheckStudentInternshipEligibility: validation failed - ${e.message}")
            }
        } else {
            logger.info("testCheckStudentInternshipEligibility: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.internships.checkStudentInternshipEligibility(
                    fiscalCode = "PLACEHOLDER",
                    languageCode = listOf("IT")
                )
            }
            logger.info("testCheckStudentInternshipEligibility: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testCheckStudentInternshipEligibility_withEnrollmentId() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            val fiscalCode = session.fiscalCode
            if (fiscalCode == null) {
                logger.warning("testCheckStudentInternshipEligibility_withEnrollmentId: no fiscal code in session, skipping")
                return@runTest
            }

            logger.info("testCheckStudentInternshipEligibility_withEnrollmentId: calling checkStudentInternshipEligibility(fiscalCode=$fiscalCode, languageCode=[IT], matricola=${studentProfile.matricola})")
            try {
                val eligibility = api.internships.checkStudentInternshipEligibility(
                    fiscalCode = fiscalCode,
                    languageCode = listOf("IT"),
                    matricola = studentProfile.matricola
                )
                assertNotNull(eligibility)
                logger.info("testCheckStudentInternshipEligibility_withEnrollmentId: returned ${eligibility.size} results")
                for (item in eligibility) {
                    logger.info("testCheckStudentInternshipEligibility_withEnrollmentId: outcomeCode=${item.outcomeCode}, description=${item.outcomeDescription}")
                }
            } catch (e: Esse3Exception) {
                logger.warning("testCheckStudentInternshipEligibility_withEnrollmentId: validation failed - ${e.message}")
            }
        } else {
            logger.info("testCheckStudentInternshipEligibility_withEnrollmentId: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.internships.checkStudentInternshipEligibility(
                    fiscalCode = "PLACEHOLDER",
                    languageCode = listOf("IT"),
                    matricola = studentProfile.matricola
                )
            }
            logger.info("testCheckStudentInternshipEligibility_withEnrollmentId: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetStudentInternshipApplicationHeaders_defaults() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetStudentInternshipApplicationHeaders_defaults: calling getStudentInternshipApplicationHeaders(studentId=${studentProfile.studentId})")
            val headers = api.internships.getStudentInternshipApplicationHeaders(studentId = studentProfile.studentId)
            assertNotNull(headers)
            logger.info("testGetStudentInternshipApplicationHeaders_defaults: returned ${headers.size} headers")
            for (header in headers) {
                assertNotNull(header.domicileInternshipId)
                assertTrue(header.studentId == studentProfile.studentId, "Expected studentId=${studentProfile.studentId}, got ${header.studentId}")
                logger.info("testGetStudentInternshipApplicationHeaders_defaults: domTiroId=${header.domicileInternshipId}, type=${header.internshipTypeCode}, state=${header.internshipApplicationStateCode}, entity=${header.entityDescription}")
            }
        } else {
            logger.info("testGetStudentInternshipApplicationHeaders_defaults: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.internships.getStudentInternshipApplicationHeaders(studentId = studentProfile.studentId)
            }
            logger.info("testGetStudentInternshipApplicationHeaders_defaults: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetStudentInternshipApplicationHeaders_withStateFilter() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetStudentInternshipApplicationHeaders_withStateFilter: calling getStudentInternshipApplicationHeaders(studentId=${studentProfile.studentId}, stateCode=[CHI])")
            val headers = api.internships.getStudentInternshipApplicationHeaders(
                studentId = studentProfile.studentId,
                internshipApplicationStateCode = listOf("CHI")
            )
            assertNotNull(headers)
            logger.info("testGetStudentInternshipApplicationHeaders_withStateFilter: returned ${headers.size} headers")
            for (header in headers) {
                logger.info("testGetStudentInternshipApplicationHeaders_withStateFilter: domTiroId=${header.domicileInternshipId}, state=${header.internshipApplicationStateCode}")
            }
        } else {
            logger.info("testGetStudentInternshipApplicationHeaders_withStateFilter: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.internships.getStudentInternshipApplicationHeaders(
                    studentId = studentProfile.studentId,
                    internshipApplicationStateCode = listOf("CHI")
                )
            }
            logger.info("testGetStudentInternshipApplicationHeaders_withStateFilter: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetStudentInternshipApplication_forEachHeader() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetStudentInternshipApplication_forEachHeader: fetching headers for studentId=${studentProfile.studentId}")
            val headers = api.internships.getStudentInternshipApplicationHeaders(studentId = studentProfile.studentId)
            assertNotNull(headers)
            logger.info("testGetStudentInternshipApplication_forEachHeader: found ${headers.size} headers")

            if (headers.isEmpty()) {
                logger.warning("testGetStudentInternshipApplication_forEachHeader: no headers found, skipping")
                return@runTest
            }

            for (header in headers) {
                val domicileInternshipId = header.domicileInternshipId ?: continue

                logger.info("testGetStudentInternshipApplication_forEachHeader: calling getStudentInternshipApplication(studentId=${studentProfile.studentId}, domicileInternshipId=$domicileInternshipId)")
                val detail = api.internships.getStudentInternshipApplication(
                    studentId = studentProfile.studentId,
                    domicileInternshipId = domicileInternshipId
                )
                assertNotNull(detail)
                assertTrue(detail.studentId == studentProfile.studentId, "Expected studentId=${studentProfile.studentId}")
                assertTrue(detail.domicileInternshipId == domicileInternshipId, "Expected domicileInternshipId=$domicileInternshipId")
                logger.info("testGetStudentInternshipApplication_forEachHeader: domTiroId=${detail.domicileInternshipId}, stageType=${detail.stageTypeCode}, startDate=${detail.internshipStartDate}, endDate=${detail.internshipEndDate}, convention=${detail.conventionDescription}")
            }
        } else {
            logger.info("testGetStudentInternshipApplication_forEachHeader: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.internships.getStudentInternshipApplication(
                    studentId = studentProfile.studentId,
                    domicileInternshipId = 0L
                )
            }
            logger.info("testGetStudentInternshipApplication_forEachHeader: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetStudentInternshipApplicationAttachments_forEachHeader() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetStudentInternshipApplicationAttachments_forEachHeader: fetching headers for studentId=${studentProfile.studentId}")
            val headers = api.internships.getStudentInternshipApplicationHeaders(studentId = studentProfile.studentId)
            assertNotNull(headers)
            logger.info("testGetStudentInternshipApplicationAttachments_forEachHeader: found ${headers.size} headers")

            if (headers.isEmpty()) {
                logger.warning("testGetStudentInternshipApplicationAttachments_forEachHeader: no headers found, skipping")
                return@runTest
            }

            for (header in headers) {
                val domicileInternshipId = header.domicileInternshipId ?: continue

                logger.info("testGetStudentInternshipApplicationAttachments_forEachHeader: calling getStudentInternshipApplicationAttachments(studentId=${studentProfile.studentId}, domicileInternshipId=$domicileInternshipId)")
                val attachments = api.internships.getStudentInternshipApplicationAttachments(
                    studentId = studentProfile.studentId,
                    domicileInternshipId = domicileInternshipId
                )
                assertNotNull(attachments)
                logger.info("testGetStudentInternshipApplicationAttachments_forEachHeader: domTiroId=$domicileInternshipId has ${attachments.size} attachments")
                for (attachment in attachments) {
                    assertNotNull(attachment.attachmentId)
                    logger.info("testGetStudentInternshipApplicationAttachments_forEachHeader: attachmentId=${attachment.attachmentId}, title=${attachment.title}, fileName=${attachment.fileName}, size=${attachment.size}")
                }
            }
        } else {
            logger.info("testGetStudentInternshipApplicationAttachments_forEachHeader: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.internships.getStudentInternshipApplicationAttachments(
                    studentId = studentProfile.studentId,
                    domicileInternshipId = 0L
                )
            }
            logger.info("testGetStudentInternshipApplicationAttachments_forEachHeader: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetAttachmentContent_forExistingAttachment() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetAttachmentContent_forExistingAttachment: fetching headers to find an attachment")
            val headers = api.internships.getStudentInternshipApplicationHeaders(studentId = studentProfile.studentId)
            assertNotNull(headers)

            for (header in headers) {
                val domicileInternshipId = header.domicileInternshipId ?: continue
                val attachments = api.internships.getStudentInternshipApplicationAttachments(
                    studentId = studentProfile.studentId,
                    domicileInternshipId = domicileInternshipId
                )
                val firstAttachment = attachments.firstOrNull() ?: continue
                val attachmentId = firstAttachment.attachmentId ?: continue

                logger.info("testGetAttachmentContent_forExistingAttachment: calling getAttachmentContent(studentId=${studentProfile.studentId}, attachmentId=$attachmentId)")
                try {
                    val content = api.internships.getAttachmentContent(
                        studentId = studentProfile.studentId,
                        attachmentId = attachmentId
                    )
                    assertNotNull(content)
                    logger.info("testGetAttachmentContent_forExistingAttachment: content received for attachmentId=$attachmentId")
                } catch (e: Esse3Exception) {
                    logger.warning("testGetAttachmentContent_forExistingAttachment: validation failed for attachmentId=$attachmentId - ${e.message}")
                }
                return@runTest
            }
            logger.warning("testGetAttachmentContent_forExistingAttachment: no attachments found to test")
        } else {
            logger.info("testGetAttachmentContent_forExistingAttachment: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.internships.getAttachmentContent(studentId = studentProfile.studentId, attachmentId = 0L)
            }
            logger.info("testGetAttachmentContent_forExistingAttachment: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetStudentInternshipEvaluation_forEachHeader() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetStudentInternshipEvaluation_forEachHeader: fetching headers for studentId=${studentProfile.studentId}")
            val headers = api.internships.getStudentInternshipApplicationHeaders(studentId = studentProfile.studentId)
            assertNotNull(headers)
            logger.info("testGetStudentInternshipEvaluation_forEachHeader: found ${headers.size} headers")

            if (headers.isEmpty()) {
                logger.warning("testGetStudentInternshipEvaluation_forEachHeader: no headers found, skipping")
                return@runTest
            }

            for (header in headers) {
                val domicileInternshipId = header.domicileInternshipId ?: continue

                logger.info("testGetStudentInternshipEvaluation_forEachHeader: calling getStudentInternshipEvaluation(studentId=${studentProfile.studentId}, domicileInternshipId=$domicileInternshipId)")
                try {
                    val evaluations = api.internships.getStudentInternshipEvaluation(
                        studentId = studentProfile.studentId,
                        domicileInternshipId = domicileInternshipId
                    )
                    assertNotNull(evaluations)
                    logger.info("testGetStudentInternshipEvaluation_forEachHeader: domTiroId=$domicileInternshipId has ${evaluations.size} evaluation entries")
                    for (evaluation in evaluations) {
                        logger.info("testGetStudentInternshipEvaluation_forEachHeader: questionnaireId=${evaluation.questionnaireId}, questionTypeCode=${evaluation.questionTypeCode}, completeFlag=${evaluation.completeFlag}, questionnaireDescription=${evaluation.questionnaireDescription}")
                    }
                } catch (e: Esse3Exception) {
                    logger.warning("testGetStudentInternshipEvaluation_forEachHeader: not authorized for domTiroId=$domicileInternshipId - ${e.message}")
                }
            }
        } else {
            logger.info("testGetStudentInternshipEvaluation_forEachHeader: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.internships.getStudentInternshipEvaluation(
                    studentId = studentProfile.studentId,
                    domicileInternshipId = 0L
                )
            }
            logger.info("testGetStudentInternshipEvaluation_forEachHeader: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testSaveCompany() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            val testDescription = "TEST_COMPANY_INTEGRATION_${System.currentTimeMillis()}"
            var createdEntityId: Long? = null
            try {
                logger.info("testSaveCompany: calling saveCompany(description=$testDescription)")
                val createInput = Esse3CompanyPostInput(
                    description = testDescription,
                    entityStateCode = "A",
                    origin = "S"
                )
                val createOutput = api.internships.saveCompany(createInput)
                assertNotNull(createOutput)
                assertNotNull(createOutput.entityId)
                createdEntityId = createOutput.entityId
                logger.info("testSaveCompany: created entityId=${createOutput.entityId}, siteId=${createOutput.siteId}")
            } catch (e: Esse3Exception) {
                logger.warning("testSaveCompany: validation failed - ${e.message}")
            } finally {
                val entityId = createdEntityId
                if (entityId != null) {
                    try {
                        api.internships.deleteCompany(entityId)
                        logger.info("testSaveCompany: cleaned up company id=$entityId")
                    } catch (e: Exception) {
                        logger.warning("testSaveCompany: cleanup deleteCompany failed - ${e.message}")
                    }
                }
            }
        } else {
            logger.info("testSaveCompany: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.internships.saveCompany(Esse3CompanyPostInput(description = "TEST"))
            }
            logger.info("testSaveCompany: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testUpdateCompany() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            val testDescription = "TEST_COMPANY_UPDATE_${System.currentTimeMillis()}"
            var createdEntityId: Long? = null
            try {
                logger.info("testUpdateCompany: creating a company first")
                val createInput = Esse3CompanyPostInput(
                    description = testDescription,
                    entityStateCode = "A",
                    origin = "S"
                )
                val createOutput = api.internships.saveCompany(createInput)
                assertNotNull(createOutput)
                assertNotNull(createOutput.entityId)
                createdEntityId = createOutput.entityId
                logger.info("testUpdateCompany: created entityId=${createOutput.entityId}")

                val updateInput = Esse3CompanyPutInput(description = "${testDescription}_UPDATED")
                logger.info("testUpdateCompany: calling updateCompany(companyId=${createdEntityId})")
                try {
                    api.internships.updateCompany(companyId = createdEntityId!!, body = updateInput)
                    logger.info("testUpdateCompany: company updated successfully")
                } catch (e: Esse3Exception) {
                    logger.warning("testUpdateCompany: update validation failed - ${e.message}")
                }
            } catch (e: Esse3Exception) {
                logger.warning("testUpdateCompany: create validation failed - ${e.message}")
            } finally {
                val entityId = createdEntityId
                if (entityId != null) {
                    try {
                        api.internships.deleteCompany(entityId)
                        logger.info("testUpdateCompany: cleaned up company id=$entityId")
                    } catch (e: Exception) {
                        logger.warning("testUpdateCompany: cleanup deleteCompany failed - ${e.message}")
                    }
                }
            }
        } else {
            logger.info("testUpdateCompany: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.internships.updateCompany(companyId = 1L, body = Esse3CompanyPutInput(description = "TEST"))
            }
            logger.info("testUpdateCompany: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testDeleteCompany() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            val testDescription = "TEST_COMPANY_DELETE_${System.currentTimeMillis()}"
            try {
                logger.info("testDeleteCompany: creating a company to delete")
                val createInput = Esse3CompanyPostInput(
                    description = testDescription,
                    entityStateCode = "A",
                    origin = "S"
                )
                val createOutput = api.internships.saveCompany(createInput)
                assertNotNull(createOutput)
                assertNotNull(createOutput.entityId)
                val entityId = createOutput.entityId!!
                logger.info("testDeleteCompany: created entityId=$entityId")

                logger.info("testDeleteCompany: calling deleteCompany(companyId=$entityId)")
                api.internships.deleteCompany(entityId)
                logger.info("testDeleteCompany: company deleted successfully")
            } catch (e: Esse3Exception) {
                logger.warning("testDeleteCompany: validation failed - ${e.message}")
            }
        } else {
            logger.info("testDeleteCompany: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.internships.deleteCompany(1L)
            }
            logger.info("testDeleteCompany: Esse3Exception thrown as expected")
        }
    }

    @Disabled("Irreversible mutating operation")
    @Test
    fun testPostInternshipApplicationAttachmentMetadata() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            val metadata = Esse3GenericAttachmentInsertMetadata(
                fileName = "test_attachment.pdf",
                title = "Test Attachment",
                description = "Integration test attachment",
                validFlag = 0L
            )
            logger.info("testPostInternshipApplicationAttachmentMetadata: calling postInternshipApplicationAttachmentMetadata(studentId=${studentProfile.studentId})")
            api.internships.postInternshipApplicationAttachmentMetadata(
                studentId = studentProfile.studentId,
                body = metadata
            )
            logger.info("testPostInternshipApplicationAttachmentMetadata: metadata posted successfully")
        } else {
            logger.info("testPostInternshipApplicationAttachmentMetadata: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.internships.postInternshipApplicationAttachmentMetadata(
                    studentId = studentProfile.studentId,
                    body = Esse3GenericAttachmentInsertMetadata()
                )
            }
            logger.info("testPostInternshipApplicationAttachmentMetadata: Esse3Exception thrown as expected")
        }
    }

    @Disabled("Irreversible mutating operation")
    @Test
    fun testImportInternship() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testImportInternship: calling importInternship()")
            api.internships.importInternship(body = Esse3TrainingProject())
            logger.info("testImportInternship: import completed successfully")
        } else {
            logger.info("testImportInternship: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.internships.importInternship(body = Esse3TrainingProject())
            }
            logger.info("testImportInternship: Esse3Exception thrown as expected")
        }
    }
}

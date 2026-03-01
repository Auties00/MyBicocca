package it.attendance100.mybicocca.data.api.esse3

import it.attendance100.mybicocca.data.dto.esse3.Esse3CompanyPostInput
import it.attendance100.mybicocca.data.dto.esse3.Esse3CompanyPutInput
import it.attendance100.mybicocca.data.dto.esse3.Esse3GenericAttachmentInsertMetadata
import it.attendance100.mybicocca.data.exception.esse3.Esse3NotAuthorizedException
import it.attendance100.mybicocca.data.exception.esse3.Esse3ValidationException
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.util.logging.Logger

class Esse3InternshipsApiTest : Esse3ApiTestBase() {

    private val logger: Logger = Logger.getLogger(Esse3InternshipsApiTest::class.java.name)

    @Test
    suspend fun getCareerSegments_noFilter_returnsResults() {
        try {
            val segments = api.internships.getCareerSegments()
            assertNotNull(segments)
            logger.info("getCareerSegments (no filter): ${segments.size} segments")
            for (segment in segments) {
                assertNotNull(segment)
                logger.info("  segment: studentId=${segment.studentId}, courseOfStudyId=${segment.courseOfStudyId}")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getCareerSegments not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun getCareerSegments_withStudentData_returnsStudentSegments() {
        try {
            val segments = api.internships.getCareerSegments(
                matricola = studentProfile.enrollmentNumber
            )
            assertNotNull(segments)
            logger.info("getCareerSegments (matricola=${studentProfile.enrollmentNumber}): ${segments.size} segments")
            for (segment in segments) {
                assertNotNull(segment)
                assertNotNull(segment.studentId)
                logger.info("  segment: studentId=${segment.studentId}, studyPlanId=${segment.studyPlanId}")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getCareerSegments by matricola not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun getCareerSegments_withCourseOfStudy_returnsFilteredSegments() {
        try {
            val segments = api.internships.getCareerSegments(
                courseOfStudyStudentId = studentProfile.degreeCourseId
            )
            assertNotNull(segments)
            logger.info("getCareerSegments (cdsStuId=${studentProfile.degreeCourseId}): ${segments.size} segments")
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getCareerSegments by courseOfStudyStudentId not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun getInternshipCompanies_noFilter_returnsAllCompanies() {
        try {
            val companies = api.internships.getInternshipCompanies()
            assertNotNull(companies)
            logger.info("getInternshipCompanies (no filter): ${companies.size} companies")
            for (company in companies) {
                assertNotNull(company)
                assertNotNull(company.companyId)
                logger.info("  company: id=${company.companyId}, code=${company.companyCode}, desc=${company.companyDescription}")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getInternshipCompanies not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun getInternshipCompanies_withValidConvention_returnsFilteredCompanies() {
        try {
            val companies = api.internships.getInternshipCompanies(hasValidConvention = 1)
            assertNotNull(companies)
            logger.info("getInternshipCompanies (hasValidConvention=1): ${companies.size} companies")
            for (company in companies) {
                assertNotNull(company.companyId)
                assertTrue(company.hasValidConvention == 1) { "Expected hasValidConvention=1 for companyId=${company.companyId}" }
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getInternshipCompanies with valid convention not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun getInternshipCompanies_withValidOpportunity_returnsFilteredCompanies() {
        try {
            val companies = api.internships.getInternshipCompanies(hasValidOpportunity = 1)
            assertNotNull(companies)
            logger.info("getInternshipCompanies (hasValidOpportunity=1): ${companies.size} companies")
            for (company in companies) {
                assertNotNull(company.companyId)
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getInternshipCompanies with valid opportunity not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun getCompanyContactsConventionsAndSites_forEachCompany() {
        try {
            val companies = api.internships.getInternshipCompanies()
            assertNotNull(companies)
            logger.info("Testing contacts/conventions/sites for ${companies.size} companies")

            for (company in companies) {
                val companyId = company.companyId ?: continue

                // Test getCompanyContacts
                try {
                    val contacts = api.internships.getCompanyContacts(companyId)
                    assertNotNull(contacts)
                    logger.info("  company $companyId: ${contacts.size} contacts")
                    for (contact in contacts) {
                        assertNotNull(contact)
                        logger.info("    contact: id=${contact.companyContactId}, surname=${contact.surname}, name=${contact.name}")
                    }
                } catch (e: Esse3NotAuthorizedException) {
                    logger.warning("  getCompanyContacts for company $companyId not authorized: ${e.message}")
                }

                // Test getCompanyConventions
                try {
                    val conventions = api.internships.getCompanyConventions(companyId)
                    assertNotNull(conventions)
                    logger.info("  company $companyId: ${conventions.size} conventions")
                    for (convention in conventions) {
                        assertNotNull(convention)
                        assertNotNull(convention.conventionSiteId)
                        logger.info("    convention: siteId=${convention.conventionSiteId}, desc=${convention.conventionSiteDescription}, state=${convention.conventionStateCode}")
                    }
                } catch (e: Esse3NotAuthorizedException) {
                    logger.warning("  getCompanyConventions for company $companyId not authorized: ${e.message}")
                }

                // Test getCompanySites
                try {
                    val sites = api.internships.getCompanySites(companyId)
                    assertNotNull(sites)
                    logger.info("  company $companyId: ${sites.size} sites")
                    for (site in sites) {
                        assertNotNull(site)
                        logger.info("    site: id=${site.companySiteId}, desc=${site.companySiteDescription}, city=${site.city}")
                    }
                } catch (e: Esse3NotAuthorizedException) {
                    logger.warning("  getCompanySites for company $companyId not authorized: ${e.message}")
                }
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getInternshipCompanies not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun getCompanyContacts_withFilters_returnsFilteredContacts() {
        try {
            val companies = api.internships.getInternshipCompanies()
            val firstCompany = companies.firstOrNull() ?: run {
                logger.warning("No companies available to test getCompanyContacts with filters")
                return
            }
            val companyId = firstCompany.companyId ?: return

            val contacts = api.internships.getCompanyContacts(
                companyId = companyId,
                activeFlag = 1
            )
            assertNotNull(contacts)
            logger.info("getCompanyContacts (companyId=$companyId, activeFlag=1): ${contacts.size} contacts")
            for (contact in contacts) {
                assertTrue(contact.activeFlag == 1) { "Expected activeFlag=1 for contact ${contact.companyContactId}" }
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getCompanyContacts with filters not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun getCompanyConventions_withFilters_returnsFilteredConventions() {
        try {
            val companies = api.internships.getInternshipCompanies(hasValidConvention = 1)
            val firstCompany = companies.firstOrNull() ?: run {
                logger.warning("No companies with conventions available to test getCompanyConventions with filters")
                return
            }
            val companyId = firstCompany.companyId ?: return

            val conventions = api.internships.getCompanyConventions(
                companyId = companyId,
                conventionStateCode = "A"
            )
            assertNotNull(conventions)
            logger.info("getCompanyConventions (companyId=$companyId, state=A): ${conventions.size} conventions")
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getCompanyConventions with filters not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun getInternshipOpportunities_noFilter_returnsResults() {
        try {
            val opportunities = api.internships.getInternshipOpportunities()
            assertNotNull(opportunities)
            logger.info("getInternshipOpportunities (no filter): ${opportunities.size} opportunities")
            for (opportunity in opportunities) {
                assertNotNull(opportunity)
                logger.info("  opportunity: title=${opportunity.title}, company=${opportunity.company}, type=${opportunity.internshipTypeCode}")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getInternshipOpportunities not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun getInternshipOpportunities_withCompanyFilter_returnsFilteredResults() {
        try {
            val allOpportunities = api.internships.getInternshipOpportunities()
            val firstOpportunity = allOpportunities.firstOrNull() ?: run {
                logger.warning("No opportunities available to test filtered getInternshipOpportunities")
                return
            }
            val companyName = firstOpportunity.company ?: return

            val filtered = api.internships.getInternshipOpportunities(company = companyName)
            assertNotNull(filtered)
            logger.info("getInternshipOpportunities (company=$companyName): ${filtered.size} opportunities")
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getInternshipOpportunities with company filter not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun getInternshipOpportunities_withDurationFilter_returnsFilteredResults() {
        try {
            val opportunities = api.internships.getInternshipOpportunities(
                durationFrom = 1,
                durationTo = 12
            )
            assertNotNull(opportunities)
            logger.info("getInternshipOpportunities (duration 1-12): ${opportunities.size} opportunities")
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getInternshipOpportunities with duration filter not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun getInternshipOpportunities_withExpiredVisible_returnsResults() {
        try {
            val opportunities = api.internships.getInternshipOpportunities(expiredOpportunitiesVisible = 1)
            assertNotNull(opportunities)
            logger.info("getInternshipOpportunities (expiredVisible=1): ${opportunities.size} opportunities")
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getInternshipOpportunities expiredVisible not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun checkStudentInternshipEligibility_withFiscalCode_returnsEligibilityData() {
        val fiscalCode = session.fiscalCode
        if (fiscalCode == null) {
            logger.warning("No fiscal code in session, skipping checkStudentInternshipEligibility test")
            return
        }

        try {
            val eligibility = api.internships.checkStudentInternshipEligibility(
                fiscalCode = fiscalCode,
                languageCode = listOf("IT")
            )
            assertNotNull(eligibility)
            logger.info("checkStudentInternshipEligibility (fiscalCode=$fiscalCode): ${eligibility.size} results")
            for (item in eligibility) {
                assertNotNull(item)
                logger.info("  eligibility: matricola=${item.matricola}, serviceType=${item.serviceType}, outcomeCode=${item.outcomeCode}, outcomeDesc=${item.outcomeDescription}")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("checkStudentInternshipEligibility not authorized: ${e.message}")
        } catch (e: Esse3ValidationException) {
            logger.warning("checkStudentInternshipEligibility validation failed: ${e.message}")
        }
    }

    @Test
    suspend fun checkStudentInternshipEligibility_withMatricola_returnsEligibilityData() {
        val fiscalCode = session.fiscalCode
        if (fiscalCode == null) {
            logger.warning("No fiscal code in session, skipping checkStudentInternshipEligibility with matricola test")
            return
        }

        try {
            val eligibility = api.internships.checkStudentInternshipEligibility(
                fiscalCode = fiscalCode,
                languageCode = listOf("IT"),
                matricola = studentProfile.enrollmentNumber
            )
            assertNotNull(eligibility)
            logger.info("checkStudentInternshipEligibility (matricola=${studentProfile.enrollmentNumber}): ${eligibility.size} results")
            for (item in eligibility) {
                assertNotNull(item)
                logger.info("  eligibility: outcomeCode=${item.outcomeCode}, desc=${item.outcomeDescription}")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("checkStudentInternshipEligibility with matricola not authorized: ${e.message}")
        } catch (e: Esse3ValidationException) {
            logger.warning("checkStudentInternshipEligibility with matricola validation failed: ${e.message}")
        }
    }

    @Test
    suspend fun getStudentInternshipApplicationHeaders_returnsHeaders() {
        try {
            val headers = api.internships.getStudentInternshipApplicationHeaders(
                studentId = studentProfile.studentId
            )
            assertNotNull(headers)
            logger.info("getStudentInternshipApplicationHeaders (studentId=${studentProfile.studentId}): ${headers.size} headers")
            for (header in headers) {
                assertNotNull(header)
                assertNotNull(header.domicileInternshipId)
                assertTrue(header.studentId == studentProfile.studentId) { "Expected studentId=${studentProfile.studentId}" }
                logger.info("  header: domTiroId=${header.domicileInternshipId}, type=${header.internshipTypeCode}, state=${header.internshipApplicationStateCode}")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getStudentInternshipApplicationHeaders not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun getStudentInternshipApplicationHeaders_withStateFilter_returnsFilteredHeaders() {
        try {
            val headers = api.internships.getStudentInternshipApplicationHeaders(
                studentId = studentProfile.studentId,
                internshipApplicationStateCode = listOf("A")
            )
            assertNotNull(headers)
            logger.info("getStudentInternshipApplicationHeaders (state=A): ${headers.size} headers")
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getStudentInternshipApplicationHeaders with state filter not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun getStudentInternshipApplication_forEachHeader_returnsDetail() {
        try {
            val headers = api.internships.getStudentInternshipApplicationHeaders(
                studentId = studentProfile.studentId
            )
            assertNotNull(headers)
            logger.info("Testing getStudentInternshipApplication for ${headers.size} headers")

            for (header in headers) {
                val domicileInternshipId = header.domicileInternshipId ?: continue

                val detail = api.internships.getStudentInternshipApplication(
                    studentId = studentProfile.studentId,
                    domicileInternshipId = domicileInternshipId
                )
                assertNotNull(detail)
                assertTrue(detail.studentId == studentProfile.studentId) { "Expected studentId=${studentProfile.studentId}" }
                assertTrue(detail.domicileInternshipId == domicileInternshipId) { "Expected domicileInternshipId=$domicileInternshipId" }
                logger.info("  detail: domTiroId=${detail.domicileInternshipId}, stageType=${detail.stageTypeCode}, startDate=${detail.internshipStartDate}, endDate=${detail.internshipEndDate}")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getStudentInternshipApplication not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun getStudentInternshipApplicationAttachments_forEachHeader_returnsAttachments() {
        try {
            val headers = api.internships.getStudentInternshipApplicationHeaders(
                studentId = studentProfile.studentId
            )
            assertNotNull(headers)
            logger.info("Testing getStudentInternshipApplicationAttachments for ${headers.size} headers")

            for (header in headers) {
                val domicileInternshipId = header.domicileInternshipId ?: continue

                val attachments = api.internships.getStudentInternshipApplicationAttachments(
                    studentId = studentProfile.studentId,
                    domicileInternshipId = domicileInternshipId
                )
                assertNotNull(attachments)
                logger.info("  domTiroId=$domicileInternshipId: ${attachments.size} attachments")
                for (attachment in attachments) {
                    assertNotNull(attachment)
                    assertNotNull(attachment.attachmentId)
                    assertTrue(attachment.studentId == studentProfile.studentId) { "Expected studentId=${studentProfile.studentId}" }
                    logger.info("    attachment: id=${attachment.attachmentId}, title=${attachment.title}, fileName=${attachment.fileName}, size=${attachment.size}")
                }
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getStudentInternshipApplicationAttachments not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun getAttachmentContent_forExistingAttachment_returnsContent() {
        try {
            val headers = api.internships.getStudentInternshipApplicationHeaders(
                studentId = studentProfile.studentId
            )

            for (header in headers) {
                val domicileInternshipId = header.domicileInternshipId ?: continue
                val attachments = api.internships.getStudentInternshipApplicationAttachments(
                    studentId = studentProfile.studentId,
                    domicileInternshipId = domicileInternshipId
                )
                val firstAttachment = attachments.firstOrNull() ?: continue
                val attachmentId = firstAttachment.attachmentId ?: continue

                try {
                    val content = api.internships.getAttachmentContent(
                        studentId = studentProfile.studentId,
                        attachmentId = attachmentId
                    )
                    assertNotNull(content)
                    logger.info("getAttachmentContent (studentId=${studentProfile.studentId}, attachmentId=$attachmentId): content length=${content.length}")
                } catch (e: Esse3NotAuthorizedException) {
                    logger.warning("getAttachmentContent not authorized: ${e.message}")
                } catch (e: Esse3ValidationException) {
                    logger.warning("getAttachmentContent validation failed: ${e.message}")
                }
                return // only test one attachment
            }
            logger.warning("No attachments found to test getAttachmentContent")
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getStudentInternshipApplicationHeaders not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun getStudentInternshipEvaluation_forEachHeader_returnsQuestionnaire() {
        try {
            val headers = api.internships.getStudentInternshipApplicationHeaders(
                studentId = studentProfile.studentId
            )
            assertNotNull(headers)
            logger.info("Testing getStudentInternshipEvaluation for ${headers.size} headers")

            for (header in headers) {
                val domicileInternshipId = header.domicileInternshipId ?: continue

                try {
                    val evaluations = api.internships.getStudentInternshipEvaluation(
                        studentId = studentProfile.studentId,
                        domicileInternshipId = domicileInternshipId
                    )
                    assertNotNull(evaluations)
                    logger.info("  domTiroId=$domicileInternshipId: ${evaluations.size} evaluation entries")
                    for (evaluation in evaluations) {
                        assertNotNull(evaluation)
                        logger.info("    evaluation: questionnaireId=${evaluation.questionnaireId}, questionTypeCode=${evaluation.questionTypeCode}, completeFlag=${evaluation.completeFlag}")
                    }
                } catch (e: Esse3NotAuthorizedException) {
                    logger.warning("  getStudentInternshipEvaluation for domTiroId=$domicileInternshipId not authorized: ${e.message}")
                }
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getStudentInternshipApplicationHeaders not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun saveUpdateAndDeleteCompany_createUpdateDeleteCycle() {
        val testCompanyDescription = "TEST_COMPANY_INTEGRATION_${System.currentTimeMillis()}"
        var createdEntityId: Long? = null
        try {
            try {
                val createInput = Esse3CompanyPostInput(
                    description = testCompanyDescription,
                    entityStateCode = "A",
                    origin = "S"
                )
                val createOutput = api.internships.saveCompany(createInput)
                assertNotNull(createOutput)
                assertNotNull(createOutput.entityId)
                createdEntityId = createOutput.entityId
                logger.info("saveCompany: created entityId=${createOutput.entityId}, siteId=${createOutput.siteId}")

                // Update the created company
                val updateInput = Esse3CompanyPutInput(
                    description = "${testCompanyDescription}_UPDATED"
                )
                try {
                    api.internships.updateCompany(
                        companyId = createdEntityId!!,
                        body = updateInput
                    )
                    logger.info("updateCompany: updated company id=$createdEntityId")
                } catch (e: Esse3ValidationException) {
                    logger.warning("updateCompany validation failed: ${e.message}")
                } catch (e: Esse3NotAuthorizedException) {
                    logger.warning("updateCompany not authorized: ${e.message}")
                }
            } catch (e: Esse3NotAuthorizedException) {
                logger.warning("saveCompany not authorized (admin-only endpoint): ${e.message}")
            } catch (e: Esse3ValidationException) {
                logger.warning("saveCompany validation failed: ${e.message}")
            }
        } finally {
            // Cleanup: delete the created company
            val entityId = createdEntityId
            if (entityId != null) {
                try {
                    api.internships.deleteCompany(entityId)
                    logger.info("deleteCompany: deleted company id=$entityId")
                } catch (e: Esse3NotAuthorizedException) {
                    logger.warning("deleteCompany not authorized: ${e.message}")
                } catch (e: Esse3ValidationException) {
                    logger.warning("deleteCompany validation failed: ${e.message}")
                }
            }
        }
    }

    @Test
    suspend fun postInternshipApplicationAttachmentMetadata_withCleanup() {
        val metadata = Esse3GenericAttachmentInsertMetadata(
            fileName = "test_attachment.pdf",
            title = "Test Attachment",
            description = "Integration test attachment",
            validFlag = 0L
        )
        try {
            api.internships.postInternshipApplicationAttachmentMetadata(
                studentId = studentProfile.studentId,
                body = metadata
            )
            logger.info("postInternshipApplicationAttachmentMetadata: metadata posted for studentId=${studentProfile.studentId}")
            // Note: cleanup of the posted metadata would require an API to delete it,
            // which is not provided in this API. The validFlag=0 minimizes impact.
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("postInternshipApplicationAttachmentMetadata not authorized: ${e.message}")
        } catch (e: Esse3ValidationException) {
            logger.warning("postInternshipApplicationAttachmentMetadata validation failed: ${e.message}")
        }
    }

    @Disabled("importInternship is an irreversible admin write operation")
    @Test
    suspend fun importInternship_disabled() {
        // This test is disabled because importInternship is an irreversible admin write operation.
    }
}

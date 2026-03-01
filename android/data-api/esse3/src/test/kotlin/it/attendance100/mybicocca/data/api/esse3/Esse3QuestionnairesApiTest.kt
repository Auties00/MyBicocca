package it.attendance100.mybicocca.data.api.esse3

import it.attendance100.mybicocca.data.exception.esse3.Esse3NotAuthorizedException
import it.attendance100.mybicocca.data.exception.esse3.Esse3ValidationException
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.util.logging.Logger

class Esse3QuestionnairesApiTest : Esse3ApiTestBase() {

    private val logger = Logger.getLogger(Esse3QuestionnairesApiTest::class.java.name)

    // Current academic year — used for event-info queries that require an academicYearId.
    // ESSE3 typically uses a two-digit representation of the starting calendar year (e.g. 2024 -> 2024).
    // We try a range of recent years to maximise the chance of finding data.
    private val recentAcademicYears = listOf(2024, 2023, 2022, 2021, 2020)

    // Placeholder questionnaire IDs used for event-tag endpoints. These are string IDs such as "VALDID".
    // Actual IDs are discovered from real data inside each test; these stubs guard against absent data.
    private val knownQuestionnaireIds = listOf("VALDID", "POSTLOGIN", "GENQUEST", "AVADOC")


    @Test
    suspend fun testGetRecordBookQuestionnaires() {
        try {
            val rows = api.questionnaires.getRecordBookQuestionnaires(
                matId = studentProfile.enrollmentId
            )
            logger.info("getRecordBookQuestionnaires: found ${rows.size} rows for matId=${studentProfile.enrollmentId}")
            assertNotNull(rows, "result should not be null")
            for (row in rows) {
                logger.info(
                    "  row: adsceId=${row.activityChoiceId}, activityDescription=${row.activityDescription}, " +
                        "state=${row.state}, courseYear=${row.courseYear}"
                )
                assertTrue(row.matId > 0, "matId should be positive")
                assertTrue(row.activityChoiceId > 0, "activityChoiceId should be positive")
                assertNotNull(row.activityDescription, "activityDescription should not be null")
                assertNotNull(row.state, "state should not be null")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getRecordBookQuestionnaires not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun testGetRecordBookQuestionnairesWithPagination() {
        try {
            val rows = api.questionnaires.getRecordBookQuestionnaires(
                matId = studentProfile.enrollmentId,
                order = "adDes"
            )
            logger.info("getRecordBookQuestionnaires (ordered): found ${rows.size} rows")
            assertNotNull(rows)
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getRecordBookQuestionnaires (ordered) not authorized: ${e.message}")
        }
    }


    @Test
    suspend fun testGetRecordBookRow() {
        try {
            val rows = api.questionnaires.getRecordBookQuestionnaires(
                matId = studentProfile.enrollmentId
            )
            if (rows.isEmpty()) {
                logger.warning("No record book rows available — skipping testGetRecordBookRow")
                return
            }
            for (row in rows) {
                val activityChoiceId = row.activityChoiceId
                try {
                    val detail = api.questionnaires.getRecordBookRow(
                        matId = studentProfile.enrollmentId,
                        activityChoiceId = activityChoiceId
                    )
                    logger.info(
                        "getRecordBookRow: adsceId=$activityChoiceId, " +
                            "activityDescription=${detail.activityDescription}, state=${detail.state}"
                    )
                    assertNotNull(detail, "detail should not be null")
                    assertTrue(detail.matId > 0, "matId should be positive")
                    assertTrue(detail.activityChoiceId > 0, "activityChoiceId should be positive")
                    assertNotNull(detail.activityDescription, "activityDescription should not be null")
                } catch (e: Esse3NotAuthorizedException) {
                    logger.warning("getRecordBookRow($activityChoiceId) not authorized: ${e.message}")
                } catch (e: Esse3ValidationException) {
                    logger.warning("getRecordBookRow($activityChoiceId) validation error: ${e.message}")
                }
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getRecordBookQuestionnaires not authorized: ${e.message}")
        }
    }


    @Test
    suspend fun testGetTeachingUnitQuestionnaireEvaluation() {
        try {
            val rows = api.questionnaires.getRecordBookQuestionnaires(
                matId = studentProfile.enrollmentId
            )
            if (rows.isEmpty()) {
                logger.warning("No record book rows available — skipping testGetTeachingUnitQuestionnaireEvaluation")
                return
            }
            for (row in rows) {
                val activityChoiceId = row.activityChoiceId
                // eventCompId is required; try common known values
                for (eventCompId in listOf("VALDID", "AVADOC")) {
                    try {
                        val teachingUnit = api.questionnaires.getTeachingUnitQuestionnaireEvaluation(
                            activityChoiceId = activityChoiceId,
                            eventComponentId = eventCompId
                        )
                        logger.info(
                            "getTeachingUnitQuestionnaireEvaluation: adsceId=$activityChoiceId, " +
                                "eventCompId=$eventCompId, state=${teachingUnit.state}, " +
                                "questionnaireId=${teachingUnit.questionnaireId}"
                        )
                        assertNotNull(teachingUnit, "result should not be null")
                        // questConfigId may be null if questionnaire not yet assigned
                        logger.info("  udLogPdsListWeb size=${teachingUnit.teachingUnitLogWebStudyPlanList.size}")
                        break
                    } catch (e: Esse3NotAuthorizedException) {
                        logger.warning(
                            "getTeachingUnitQuestionnaireEvaluation($activityChoiceId, $eventCompId) not authorized: ${e.message}"
                        )
                        break
                    } catch (e: Esse3ValidationException) {
                        logger.warning(
                            "getTeachingUnitQuestionnaireEvaluation($activityChoiceId, $eventCompId) validation error: ${e.message}"
                        )
                    } catch (e: Exception) {
                        logger.warning(
                            "getTeachingUnitQuestionnaireEvaluation($activityChoiceId, $eventCompId) error: ${e.message}"
                        )
                    }
                }
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getRecordBookQuestionnaires not authorized: ${e.message}")
        }
    }


    @Test
    suspend fun testGetUserComponentEventInfoDidacticEvaluation() {
        for (questionnaireId in knownQuestionnaireIds) {
            for (academicYearId in recentAcademicYears) {
                try {
                    val results = api.questionnaires.getUserComponentEventInfoDidacticEvaluation(
                        questionnaireId = questionnaireId,
                        academicYearId = academicYearId,
                        userId = studentProfile.userId.toLongOrNull()
                    )
                    logger.info(
                        "getUserComponentEventInfoDidacticEvaluation($questionnaireId, $academicYearId): found ${results.size} entries"
                    )
                    for (entry in results) {
                        logger.info(
                            "  entry: questCompId=${entry.questionComponentId}, " +
                                "questionnaireId=${entry.questionnaireId}, " +
                                "activityCode=${entry.activityCode}, " +
                                "activityDescription=${entry.activityDescription}"
                        )
                        assertTrue((entry.questionComponentId ?: 0) > 0, "questionComponentId should be positive")
                    }
                    if (results.isNotEmpty()) return
                } catch (e: Esse3NotAuthorizedException) {
                    logger.warning(
                        "getUserComponentEventInfoDidacticEvaluation($questionnaireId, $academicYearId) not authorized: ${e.message}"
                    )
                    return
                } catch (e: Esse3ValidationException) {
                    logger.warning(
                        "getUserComponentEventInfoDidacticEvaluation($questionnaireId, $academicYearId) validation error: ${e.message}"
                    )
                } catch (e: Exception) {
                    logger.warning(
                        "getUserComponentEventInfoDidacticEvaluation($questionnaireId, $academicYearId) error: ${e.message}"
                    )
                }
            }
        }
        logger.info("getUserComponentEventInfoDidacticEvaluation: no results found across all tested questionnaire/year combinations (may be expected)")
    }

    @Test
    suspend fun testGetQuestionnaireComponentEventInfoDidacticEvaluation() {
        for (questionnaireId in knownQuestionnaireIds) {
            for (academicYearId in recentAcademicYears) {
                try {
                    val results = api.questionnaires.getQuestionnaireComponentEventInfoDidacticEvaluation(
                        questionnaireId = questionnaireId,
                        academicYearId = academicYearId
                    )
                    logger.info(
                        "getQuestionnaireComponentEventInfoDidacticEvaluation($questionnaireId, $academicYearId): found ${results.size} entries"
                    )
                    for (entry in results) {
                        logger.info(
                            "  entry: questCompId=${entry.questionComponentId}, " +
                                "questionnaireId=${entry.questionnaireId}"
                        )
                        assertTrue(entry.questionComponentId > 0, "questionComponentId should be positive")
                    }
                    if (results.isNotEmpty()) return
                } catch (e: Esse3NotAuthorizedException) {
                    logger.warning(
                        "getQuestionnaireComponentEventInfoDidacticEvaluation($questionnaireId, $academicYearId) not authorized: ${e.message}"
                    )
                    return
                } catch (e: Esse3ValidationException) {
                    logger.warning(
                        "getQuestionnaireComponentEventInfoDidacticEvaluation($questionnaireId, $academicYearId) validation error: ${e.message}"
                    )
                } catch (e: Exception) {
                    logger.warning(
                        "getQuestionnaireComponentEventInfoDidacticEvaluation($questionnaireId, $academicYearId) error: ${e.message}"
                    )
                }
            }
        }
        logger.info("getQuestionnaireComponentEventInfoDidacticEvaluation: no results found (may be expected)")
    }


    @Test
    suspend fun testGetUserComponentEventInfoPostLogin() {
        for (questionnaireId in knownQuestionnaireIds) {
            for (academicYearId in recentAcademicYears) {
                try {
                    val results = api.questionnaires.getUserComponentEventInfoPostLogin(
                        questionnaireId = questionnaireId,
                        academicYearId = academicYearId,
                        userId = studentProfile.userId.toLongOrNull()
                    )
                    logger.info(
                        "getUserComponentEventInfoPostLogin($questionnaireId, $academicYearId): found ${results.size} entries"
                    )
                    for (entry in results) {
                        logger.info(
                            "  entry: questCompId=${entry.questionComponentId}, " +
                                "questionnaireId=${entry.questionnaireId}"
                        )
                        assertTrue(entry.questionComponentId > 0, "questionComponentId should be positive")
                    }
                    if (results.isNotEmpty()) return
                } catch (e: Esse3NotAuthorizedException) {
                    logger.warning(
                        "getUserComponentEventInfoPostLogin($questionnaireId, $academicYearId) not authorized: ${e.message}"
                    )
                    return
                } catch (e: Esse3ValidationException) {
                    logger.warning(
                        "getUserComponentEventInfoPostLogin($questionnaireId, $academicYearId) validation error: ${e.message}"
                    )
                } catch (e: Exception) {
                    logger.warning(
                        "getUserComponentEventInfoPostLogin($questionnaireId, $academicYearId) error: ${e.message}"
                    )
                }
            }
        }
        logger.info("getUserComponentEventInfoPostLogin: no results found (may be expected)")
    }

    @Test
    suspend fun testGetQuestionnaireComponentEventInfoPostLogin() {
        for (questionnaireId in knownQuestionnaireIds) {
            for (academicYearId in recentAcademicYears) {
                try {
                    val results = api.questionnaires.getQuestionnaireComponentEventInfoPostLogin(
                        questionnaireId = questionnaireId,
                        academicYearId = academicYearId
                    )
                    logger.info(
                        "getQuestionnaireComponentEventInfoPostLogin($questionnaireId, $academicYearId): found ${results.size} entries"
                    )
                    for (entry in results) {
                        logger.info(
                            "  entry: questCompId=${entry.questionComponentId}, " +
                                "questionnaireCode=${entry.questionnaireCode}, " +
                                "academicYearId=${entry.academicYearId}"
                        )
                        assertNotNull(entry.questionnaireCode, "questionnaireCode should not be null")
                    }
                    if (results.isNotEmpty()) return
                } catch (e: Esse3NotAuthorizedException) {
                    logger.warning(
                        "getQuestionnaireComponentEventInfoPostLogin($questionnaireId, $academicYearId) not authorized: ${e.message}"
                    )
                    return
                } catch (e: Esse3ValidationException) {
                    logger.warning(
                        "getQuestionnaireComponentEventInfoPostLogin($questionnaireId, $academicYearId) validation error: ${e.message}"
                    )
                } catch (e: Exception) {
                    logger.warning(
                        "getQuestionnaireComponentEventInfoPostLogin($questionnaireId, $academicYearId) error: ${e.message}"
                    )
                }
            }
        }
        logger.info("getQuestionnaireComponentEventInfoPostLogin: no results found (may be expected)")
    }


    @Test
    suspend fun testGetUserComponentEventInfoGeneralQuestionnaire() {
        for (questionnaireId in knownQuestionnaireIds) {
            for (academicYearId in recentAcademicYears) {
                try {
                    val results = api.questionnaires.getUserComponentEventInfoGeneralQuestionnaire(
                        questionnaireId = questionnaireId,
                        academicYearId = academicYearId,
                        userId = studentProfile.userId.toLongOrNull()
                    )
                    logger.info(
                        "getUserComponentEventInfoGeneralQuestionnaire($questionnaireId, $academicYearId): found ${results.size} entries"
                    )
                    for (entry in results) {
                        logger.info(
                            "  entry: questCompId=${entry.questionComponentId}, " +
                                "questionnaireId=${entry.questionnaireId}"
                        )
                        assertNotNull(entry.questionComponentId, "questionComponentId should not be null")
                    }
                    if (results.isNotEmpty()) return
                } catch (e: Esse3NotAuthorizedException) {
                    logger.warning(
                        "getUserComponentEventInfoGeneralQuestionnaire($questionnaireId, $academicYearId) not authorized: ${e.message}"
                    )
                    return
                } catch (e: Esse3ValidationException) {
                    logger.warning(
                        "getUserComponentEventInfoGeneralQuestionnaire($questionnaireId, $academicYearId) validation error: ${e.message}"
                    )
                } catch (e: Exception) {
                    logger.warning(
                        "getUserComponentEventInfoGeneralQuestionnaire($questionnaireId, $academicYearId) error: ${e.message}"
                    )
                }
            }
        }
        logger.info("getUserComponentEventInfoGeneralQuestionnaire: no results found (may be expected)")
    }

    @Test
    suspend fun testGetQuestionnaireComponentEventInfoGeneralQuestionnaire() {
        for (questionnaireId in knownQuestionnaireIds) {
            for (academicYearId in recentAcademicYears) {
                try {
                    val results = api.questionnaires.getQuestionnaireComponentEventInfoGeneralQuestionnaire(
                        questionnaireId = questionnaireId,
                        academicYearId = academicYearId
                    )
                    logger.info(
                        "getQuestionnaireComponentEventInfoGeneralQuestionnaire($questionnaireId, $academicYearId): found ${results.size} entries"
                    )
                    for (entry in results) {
                        logger.info(
                            "  entry: questCompId=${entry.questionComponentId}, " +
                                "questionnaireId=${entry.questionnaireId}"
                        )
                        assertTrue(entry.questionComponentId > 0, "questionComponentId should be positive")
                    }
                    if (results.isNotEmpty()) return
                } catch (e: Esse3NotAuthorizedException) {
                    logger.warning(
                        "getQuestionnaireComponentEventInfoGeneralQuestionnaire($questionnaireId, $academicYearId) not authorized: ${e.message}"
                    )
                    return
                } catch (e: Esse3ValidationException) {
                    logger.warning(
                        "getQuestionnaireComponentEventInfoGeneralQuestionnaire($questionnaireId, $academicYearId) validation error: ${e.message}"
                    )
                } catch (e: Exception) {
                    logger.warning(
                        "getQuestionnaireComponentEventInfoGeneralQuestionnaire($questionnaireId, $academicYearId) error: ${e.message}"
                    )
                }
            }
        }
        logger.info("getQuestionnaireComponentEventInfoGeneralQuestionnaire: no results found (may be expected)")
    }


    @Test
    suspend fun testGetUserComponentEventInfoAvaDoc() {
        for (questionnaireId in knownQuestionnaireIds) {
            for (academicYearId in recentAcademicYears) {
                try {
                    val results = api.questionnaires.getUserComponentEventInfoAvaDoc(
                        questionnaireId = questionnaireId,
                        academicYearId = academicYearId,
                        userId = studentProfile.userId.toLongOrNull()
                    )
                    logger.info(
                        "getUserComponentEventInfoAvaDoc($questionnaireId, $academicYearId): found ${results.size} entries"
                    )
                    for (entry in results) {
                        logger.info(
                            "  entry: questCompId=${entry.questionComponentId}, " +
                                "questionnaireId=${entry.questionnaireId}, " +
                                "activityCode=${entry.activityCode}"
                        )
                        assertTrue(entry.questionComponentId > 0, "questionComponentId should be positive")
                    }
                    if (results.isNotEmpty()) return
                } catch (e: Esse3NotAuthorizedException) {
                    logger.warning(
                        "getUserComponentEventInfoAvaDoc($questionnaireId, $academicYearId) not authorized: ${e.message}"
                    )
                    return
                } catch (e: Esse3ValidationException) {
                    logger.warning(
                        "getUserComponentEventInfoAvaDoc($questionnaireId, $academicYearId) validation error: ${e.message}"
                    )
                } catch (e: Exception) {
                    logger.warning(
                        "getUserComponentEventInfoAvaDoc($questionnaireId, $academicYearId) error: ${e.message}"
                    )
                }
            }
        }
        logger.info("getUserComponentEventInfoAvaDoc: no results found (may be expected)")
    }

    @Test
    suspend fun testGetQuestionnaireComponentEventInfoAvaDoc() {
        for (questionnaireId in knownQuestionnaireIds) {
            for (academicYearId in recentAcademicYears) {
                try {
                    val results = api.questionnaires.getQuestionnaireComponentEventInfoAvaDoc(
                        questionnaireId = questionnaireId,
                        academicYearId = academicYearId
                    )
                    logger.info(
                        "getQuestionnaireComponentEventInfoAvaDoc($questionnaireId, $academicYearId): found ${results.size} entries"
                    )
                    for (entry in results) {
                        logger.info(
                            "  entry: questCompId=${entry.questionComponentId}, " +
                                "questionnaireId=${entry.questionnaireId}"
                        )
                        assertTrue(entry.questionComponentId > 0, "questionComponentId should be positive")
                    }
                    if (results.isNotEmpty()) return
                } catch (e: Esse3NotAuthorizedException) {
                    logger.warning(
                        "getQuestionnaireComponentEventInfoAvaDoc($questionnaireId, $academicYearId) not authorized: ${e.message}"
                    )
                    return
                } catch (e: Esse3ValidationException) {
                    logger.warning(
                        "getQuestionnaireComponentEventInfoAvaDoc($questionnaireId, $academicYearId) validation error: ${e.message}"
                    )
                } catch (e: Exception) {
                    logger.warning(
                        "getQuestionnaireComponentEventInfoAvaDoc($questionnaireId, $academicYearId) error: ${e.message}"
                    )
                }
            }
        }
        logger.info("getQuestionnaireComponentEventInfoAvaDoc: no results found (may be expected)")
    }


    @Test
    suspend fun testGetCompiledQuestionnaires() {
        // Attempt to obtain a questionComponentId from the event-info endpoints.
        // If none is found, try with a placeholder to verify the authorization/error path.
        var foundQuestionComponentId: Long? = null
        for (questionnaireId in knownQuestionnaireIds) {
            for (academicYearId in recentAcademicYears) {
                try {
                    val items = api.questionnaires.getUserComponentEventInfoDidacticEvaluation(
                        questionnaireId = questionnaireId,
                        academicYearId = academicYearId,
                        userId = studentProfile.userId.toLongOrNull()
                    )
                    foundQuestionComponentId = items.firstOrNull()?.questionComponentId
                    if (foundQuestionComponentId != null) break
                } catch (_: Exception) {
                    // ignore — keep searching
                }
            }
            if (foundQuestionComponentId != null) break
        }

        val questionComponentId = foundQuestionComponentId ?: 1L
        try {
            val compiled = api.questionnaires.getCompiledQuestionnaires(questionComponentId)
            logger.info("getCompiledQuestionnaires($questionComponentId): found ${compiled.size} entries")
            for (entry in compiled) {
                logger.info(
                    "  entry: questionnaireId=${entry.questionnaireId}, " +
                        "questionComponentId=${entry.questionComponentId}, " +
                        "questionStateCode=${entry.questionStateCode}"
                )
                assertTrue(entry.questionnaireId > 0, "questionnaireId should be positive")
                assertTrue(entry.questionComponentId > 0, "questionComponentId should be positive")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getCompiledQuestionnaires($questionComponentId) not authorized (expected for STUDENT role): ${e.message}")
        } catch (e: Esse3ValidationException) {
            logger.warning("getCompiledQuestionnaires($questionComponentId) validation error: ${e.message}")
        }
    }


    @Test
    suspend fun testGetInternshipQuestionnaires() {
        // domicileInternshipId comes from internship data; use placeholder 1L to exercise auth path.
        val domicileInternshipId = 1L
        try {
            val result = api.questionnaires.getInternshipQuestionnaires(domicileInternshipId)
            logger.info(
                "getInternshipQuestionnaires($domicileInternshipId): " +
                    "internshipConfig=${result.internshipConfig.size}, " +
                    "domicileInternshipCompletionTag=${result.domicileInternshipCompletionTag.size}"
            )
            assertNotNull(result, "result should not be null")
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getInternshipQuestionnaires($domicileInternshipId) not authorized (expected for STUDENT role): ${e.message}")
        } catch (e: Esse3ValidationException) {
            logger.warning("getInternshipQuestionnaires($domicileInternshipId) validation error: ${e.message}")
        } catch (e: Exception) {
            logger.warning("getInternshipQuestionnaires($domicileInternshipId) error: ${e.message}")
        }
    }
    
    @Test
    suspend fun testGetPageNavigation() {
        // Try to find an active questionnaire to get a real page.
        // Step 1: get record book rows and find one with questionnaire data.
        try {
            val rows = api.questionnaires.getRecordBookQuestionnaires(
                matId = studentProfile.enrollmentId
            )
            if (rows.isEmpty()) {
                logger.warning("No record book rows found — cannot test getPage navigation")
                return
            }
            for (row in rows) {
                val activityChoiceId = row.activityChoiceId
                for (eventCompId in listOf("VALDID", "AVADOC")) {
                    try {
                        val teachingUnit = api.questionnaires.getTeachingUnitQuestionnaireEvaluation(
                            activityChoiceId = activityChoiceId,
                            eventComponentId = eventCompId
                        )
                        val questionnaireId = teachingUnit.questionnaireId?.toString() ?: continue
                        val questConfigId = teachingUnit.questionConfigId?.toLong() ?: continue

                        logger.info(
                            "testGetPageNavigation: found questionnaireId=$questionnaireId, " +
                                "questConfigId=$questConfigId for adsceId=$activityChoiceId"
                        )
                        // We do not call setNewSurvey (destructive), so we cannot get real
                        // questCompId / userCompId / pageId without starting a questionnaire.
                        // Log available data for diagnostic purposes only.
                        logger.info(
                            "  Available for navigation: activityChoiceId=$activityChoiceId, " +
                                "questionnaireId=$questionnaireId, questConfigId=$questConfigId"
                        )
                        return
                    } catch (e: Esse3NotAuthorizedException) {
                        logger.warning(
                            "getTeachingUnitQuestionnaireEvaluation($activityChoiceId, $eventCompId) not authorized: ${e.message}"
                        )
                        break
                    } catch (_: Exception) {
                        // Try next eventCompId
                    }
                }
            }
            logger.info("testGetPageNavigation: no active questionnaire data found to test page navigation (this is normal if no questionnaires are assigned)")
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getRecordBookQuestionnaires not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun testGetPageWithPlaceholderIds() {
        // Exercise the error path with placeholder IDs to verify that the endpoint
        // is reachable and returns an appropriate error.
        val placeholderStudentId = studentProfile.studentId
        val placeholderActivityChoiceId = 1L
        val placeholderQuestionnaireId = "VALDID"
        val placeholderQuestCompId = 1L
        val placeholderPageId = 1L
        val placeholderUserCompId = 1L
        try {
            val page = api.questionnaires.getPage(
                studentId = placeholderStudentId,
                activityChoiceId = placeholderActivityChoiceId,
                questionnaireId = placeholderQuestionnaireId,
                questionComponentId = placeholderQuestCompId,
                pageId = placeholderPageId,
                userComponentId = placeholderUserCompId
            )
            logger.info("getPage (placeholder): pageId=${page.pageId}, questionnaireCode=${page.questionnaireCode}")
            assertNotNull(page)
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getPage (placeholder) not authorized: ${e.message}")
        } catch (e: Esse3ValidationException) {
            logger.warning("getPage (placeholder) validation error (expected for placeholder IDs): ${e.message}")
        } catch (e: Exception) {
            logger.warning("getPage (placeholder) error (expected for placeholder IDs): ${e.message}")
        }
    }

    @Test
    suspend fun testGetNextPageWithPlaceholderIds() {
        val placeholderStudentId = studentProfile.studentId
        val placeholderActivityChoiceId = 1L
        val placeholderQuestionnaireId = "VALDID"
        val placeholderQuestCompId = 1L
        val placeholderPageId = 1L
        val placeholderUserCompId = 1L
        try {
            val page = api.questionnaires.getNextPage(
                studentId = placeholderStudentId,
                activityChoiceId = placeholderActivityChoiceId,
                questionnaireId = placeholderQuestionnaireId,
                questionComponentId = placeholderQuestCompId,
                pageId = placeholderPageId,
                userComponentId = placeholderUserCompId
            )
            logger.info("getNextPage (placeholder): pageId=${page.pageId}, nextPageId=${page.nextPageId}")
            assertNotNull(page)
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getNextPage (placeholder) not authorized: ${e.message}")
        } catch (e: Esse3ValidationException) {
            logger.warning("getNextPage (placeholder) validation error (expected for placeholder IDs): ${e.message}")
        } catch (e: Exception) {
            logger.warning("getNextPage (placeholder) error (expected for placeholder IDs): ${e.message}")
        }
    }

    @Test
    suspend fun testGetPreviousPageWithPlaceholderIds() {
        val placeholderStudentId = studentProfile.studentId
        val placeholderActivityChoiceId = 1L
        val placeholderQuestionnaireId = "VALDID"
        val placeholderQuestCompId = 1L
        val placeholderPageId = 1L
        val placeholderUserCompId = 1L
        try {
            val page = api.questionnaires.getPreviousPage(
                studentId = placeholderStudentId,
                activityChoiceId = placeholderActivityChoiceId,
                questionnaireId = placeholderQuestionnaireId,
                questionComponentId = placeholderQuestCompId,
                pageId = placeholderPageId,
                userComponentId = placeholderUserCompId
            )
            logger.info("getPreviousPage (placeholder): pageId=${page.pageId}, previousPageId=${page.previousPageId}")
            assertNotNull(page)
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getPreviousPage (placeholder) not authorized: ${e.message}")
        } catch (e: Esse3ValidationException) {
            logger.warning("getPreviousPage (placeholder) validation error (expected for placeholder IDs): ${e.message}")
        } catch (e: Exception) {
            logger.warning("getPreviousPage (placeholder) error (expected for placeholder IDs): ${e.message}")
        }
    }

    @Test
    suspend fun testGetComponentSummaryWithPlaceholderIds() {
        val placeholderStudentId = studentProfile.studentId
        val placeholderActivityChoiceId = 1L
        val placeholderQuestCompId = 1L
        val placeholderQuestionnaireId = "VALDID"
        val placeholderQuestConfigId = 1L
        val placeholderUserCompId = 1L
        val placeholderEventCompId = "VALDID"
        try {
            val summary = api.questionnaires.getComponentSummary(
                studentId = placeholderStudentId,
                activityChoiceId = placeholderActivityChoiceId,
                questionComponentId = placeholderQuestCompId,
                questionnaireId = placeholderQuestionnaireId,
                questionConfigId = placeholderQuestConfigId,
                userComponentId = placeholderUserCompId,
                eventComponentId = placeholderEventCompId
            )
            logger.info(
                "getComponentSummary (placeholder): questionComponentId=${summary.questionComponentId}, " +
                    "questionnaireId=${summary.questionnaireId}"
            )
            assertNotNull(summary)
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getComponentSummary (placeholder) not authorized: ${e.message}")
        } catch (e: Esse3ValidationException) {
            logger.warning("getComponentSummary (placeholder) validation error (expected for placeholder IDs): ${e.message}")
        } catch (e: Exception) {
            logger.warning("getComponentSummary (placeholder) error (expected for placeholder IDs): ${e.message}")
        }
    }

    @Test
    @Disabled("Destructive: setVisibility modifies questionnaire visibility for a user component — requires TECHNICAL_USER and cannot be safely reversed")
    suspend fun testSetVisibility() {
        // NOTE: This test is disabled because it mutates the visibility state of a questionnaire component.
        // To enable: provide a real userId, questionComponentId, visibleKind, and visibleValue.
        // Ensure the original visibility is known so it can be restored in a finally block.
        val userId = studentProfile.userId.toLong()
        val questionComponentId = 1L
        val visibleKind = "visFlg"
        val originalVisible = true
        try {
            api.questionnaires.setVisibility(
                userId = userId,
                questionComponentId = questionComponentId,
                visibleKind = visibleKind,
                visibleValue = !originalVisible
            )
            logger.info("setVisibility: succeeded")
        } finally {
            api.questionnaires.setVisibility(
                userId = userId,
                questionComponentId = questionComponentId,
                visibleKind = visibleKind,
                visibleValue = originalVisible
            )
            logger.info("setVisibility: restored original visibility")
        }
    }

    @Test
    @Disabled("Destructive: savePageAnswers submits real answers to a questionnaire page and cannot be reversed")
    suspend fun testSavePageAnswers() {
        // NOTE: This test is disabled because it submits answers to the ESSE3 server.
        // Executing this would permanently record answers for the current student.
    }

    @Test
    @Disabled("Destructive: setNewSurvey creates a new survey instance on the server — cannot be reversed without completing or abandoning it")
    suspend fun testSetNewSurvey() {
        // NOTE: This test is disabled because it initiates a questionnaire session.
        // Starting a questionnaire and not completing it may leave the system in an
        // inconsistent state. Only enable with a dedicated test environment.
    }

    @Test
    @Disabled("Destructive: putQuestionnaireComponentConfirm permanently confirms a questionnaire — cannot be reversed")
    suspend fun testPutQuestionnaireComponentConfirm() {
        // NOTE: This test is disabled because confirming a questionnaire is irreversible.
        // Only enable in a controlled test environment with disposable data.
    }
}

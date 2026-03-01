package it.attendance100.mybicocca.data.api.esse3

import it.attendance100.mybicocca.data.exception.esse3.Esse3NotAuthorizedException
import it.attendance100.mybicocca.data.exception.esse3.Esse3ValidationException
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory

class Esse3BadgeApiTest : Esse3ApiTestBase() {

    private val logger = LoggerFactory.getLogger(Esse3BadgeApiTest::class.java)

    @Test
    suspend fun getNewBadges_byStudentId() {
        try {
            val badges = api.badge.getNewBadges(studentId = studentProfile.studentId)
            assertNotNull(badges)
            logger.info("Retrieved ${badges.size} badges for studentId=${studentProfile.studentId}")

            for (badge in badges) {
                assertNotNull(badge.rfid, "badge rfid should not be null")
                logger.info("Badge: badgeId=${badge.badgeId}, rfid=${badge.rfid}, studentId=${badge.studentId}, matStatusCode=${badge.matStatusCode}")
                logger.info("  name=${badge.name} ${badge.surname}, fiscalCode=${badge.fiscalCode}")
                logger.info("  startDate=${badge.startDate}, endDate=${badge.endDate}, printDate=${badge.printDate}")
                logger.info("  frontImagePresent=${badge.frontImagePresent}, rearImagePresent=${badge.rearImagePresent}")
                logger.info("  badgeBlobId=${badge.badgeBlobId}, yearFlag=${badge.yearFlag}")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("Not authorized to get badges by studentId: ${e.message}")
        } catch (e: Esse3ValidationException) {
            logger.warn("Validation error getting badges by studentId: ${e.message}")
        }
    }

    @Test
    suspend fun getNewBadges_byFiscalCode() {
        val fiscalCode = session.fiscalCode
            ?: run { logger.warn("No fiscal code in session, skipping badge by fiscal code test"); return }

        try {
            val badges = api.badge.getNewBadges(fiscalCode = fiscalCode)
            assertNotNull(badges)
            logger.info("Retrieved ${badges.size} badges for fiscalCode=$fiscalCode")

            for (badge in badges) {
                assertNotNull(badge.rfid, "badge rfid should not be null")
                logger.info("Badge: badgeId=${badge.badgeId}, rfid=${badge.rfid}, fiscalCode=${badge.fiscalCode}")

                // Fiscal code in badge should match the queried one
                if (badge.fiscalCode != null) {
                    assert(badge.fiscalCode.equals(fiscalCode, ignoreCase = true)) {
                        "Badge fiscalCode '${badge.fiscalCode}' should match queried fiscalCode '$fiscalCode'"
                    }
                }
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("Not authorized to get badges by fiscal code: ${e.message}")
        } catch (e: Esse3ValidationException) {
            logger.warn("Validation error getting badges by fiscal code: ${e.message}")
        }
    }

    @Test
    suspend fun getNewBadges_noFilter() {
        // Without filters — technical users may see all badges, students see their own
        try {
            val badges = api.badge.getNewBadges()
            assertNotNull(badges)
            logger.info("Retrieved ${badges.size} badges (no filter)")

            for (badge in badges) {
                assertNotNull(badge.rfid, "badge rfid should not be null")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("Not authorized to get all badges without filter: ${e.message}")
        } catch (e: Esse3ValidationException) {
            logger.warn("Validation error getting all badges: ${e.message}")
        }
    }

    @Test
    suspend fun getNewBadges_withStatusFilter() {
        try {
            // Try common student status codes
            val badges = api.badge.getNewBadges(
                studentId = studentProfile.studentId,
                studentStatusCode = "A"
            )
            assertNotNull(badges)
            logger.info("Retrieved ${badges.size} badges with studentStatusCode=A for studentId=${studentProfile.studentId}")

            for (badge in badges) {
                assertNotNull(badge.rfid)
                logger.info("Badge (statusA): badgeId=${badge.badgeId}, staStuCod=${badge.studentStatusCode}")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("Not authorized to get badges with status filter: ${e.message}")
        } catch (e: Esse3ValidationException) {
            logger.warn("Validation error getting badges with status filter: ${e.message}")
        }
    }

    @Test
    suspend fun getNewBadges_withCourseOfStudyFilter() {
        try {
            val badges = api.badge.getNewBadges(
                studentId = studentProfile.studentId,
                courseOfStudyCode = null // will be filled from badges if any exist
            )
            assertNotNull(badges)

            val courseCode = badges.firstOrNull()?.courseOfStudyCode
            if (courseCode != null) {
                val filteredBadges = api.badge.getNewBadges(
                    courseOfStudyCode = courseCode
                )
                assertNotNull(filteredBadges)
                logger.info("Badges filtered by courseOfStudyCode=$courseCode: ${filteredBadges.size}")
                for (badge in filteredBadges) {
                    assertNotNull(badge.rfid)
                    if (badge.courseOfStudyCode != null) {
                        assert(badge.courseOfStudyCode == courseCode) {
                            "Badge courseOfStudyCode '${badge.courseOfStudyCode}' should match '$courseCode'"
                        }
                    }
                }
            } else {
                logger.info("No course code found from badges to apply filter test")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("Not authorized to get badges with course filter: ${e.message}")
        } catch (e: Esse3ValidationException) {
            logger.warn("Validation error getting badges with course filter: ${e.message}")
        }
    }

    @Test
    suspend fun getNewBadges_withOrderFilter() {
        try {
            val badges = api.badge.getNewBadges(
                studentId = studentProfile.studentId,
                order = "cognome"
            )
            assertNotNull(badges)
            logger.info("Retrieved ${badges.size} badges ordered by cognome")

            for (badge in badges) {
                assertNotNull(badge.rfid)
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("Not authorized to get badges with order: ${e.message}")
        } catch (e: Esse3ValidationException) {
            logger.warn("Validation error getting badges with order: ${e.message}")
        }
    }

    @Test
    suspend fun getBadgeBlobFrontPage_fromBadgeData() {
        try {
            val badges = api.badge.getNewBadges(studentId = studentProfile.studentId)
            logger.info("Found ${badges.size} badges for front page test")

            val badgeWithBlob = badges.firstOrNull { it.badgeBlobId != null && it.frontImagePresent == 1 }
            if (badgeWithBlob == null) {
                logger.warn("No badge with a front blob image found for studentId=${studentProfile.studentId}. Skipping front page test.")
                return
            }

            val blobId = badgeWithBlob.badgeBlobId!!
            logger.info("Testing getBadgeBlobFrontPage for badgeBlobId=$blobId")

            try {
                val frontPage = api.badge.getBadgeBlobFrontPage(badgeBlobId = blobId)
                assertNotNull(frontPage, "Front page blob should not be null")
                assertFalse(frontPage.isBlank(), "Front page blob should not be blank")
                logger.info("Got front page blob: length=${frontPage.length} chars (truncated: ${frontPage.take(50)}...)")
            } catch (e: Esse3NotAuthorizedException) {
                logger.warn("Not authorized to get badge front page blob=$blobId: ${e.message}")
            } catch (e: Esse3ValidationException) {
                logger.warn("Validation error getting badge front page blob=$blobId: ${e.message}")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("Not authorized to get badges for front page test: ${e.message}")
        } catch (e: Esse3ValidationException) {
            logger.warn("Validation error getting badges for front page test: ${e.message}")
        }
    }

    @Test
    suspend fun getBadgeBlobRearPage_fromBadgeData() {
        try {
            val badges = api.badge.getNewBadges(studentId = studentProfile.studentId)
            logger.info("Found ${badges.size} badges for rear page test")

            val badgeWithBlob = badges.firstOrNull { it.badgeBlobId != null && it.rearImagePresent == 1 }
            if (badgeWithBlob == null) {
                logger.warn("No badge with a rear blob image found for studentId=${studentProfile.studentId}. Skipping rear page test.")
                return
            }

            val blobId = badgeWithBlob.badgeBlobId!!
            logger.info("Testing getBadgeBlobRearPage for badgeBlobId=$blobId")

            try {
                val rearPage = api.badge.getBadgeBlobRearPage(badgeBlobId = blobId)
                assertNotNull(rearPage, "Rear page blob should not be null")
                assertFalse(rearPage.isBlank(), "Rear page blob should not be blank")
                logger.info("Got rear page blob: length=${rearPage.length} chars (truncated: ${rearPage.take(50)}...)")
            } catch (e: Esse3NotAuthorizedException) {
                logger.warn("Not authorized to get badge rear page blob=$blobId: ${e.message}")
            } catch (e: Esse3ValidationException) {
                logger.warn("Validation error getting badge rear page blob=$blobId: ${e.message}")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("Not authorized to get badges for rear page test: ${e.message}")
        } catch (e: Esse3ValidationException) {
            logger.warn("Validation error getting badges for rear page test: ${e.message}")
        }
    }

    @Test
    suspend fun getBadgeBlobFrontAndRearPage_forAllBadges() {
        // Iterate all badges and fetch front/rear pages for those that have blobs
        try {
            val badges = api.badge.getNewBadges(studentId = studentProfile.studentId)
            logger.info("Iterating ${badges.size} badges for blob pages")

            for (badge in badges) {
                val blobId = badge.badgeBlobId ?: continue
                logger.info("Badge badgeId=${badge.badgeId}, blobId=$blobId, front=${badge.frontImagePresent}, rear=${badge.rearImagePresent}")

                if (badge.frontImagePresent == 1) {
                    try {
                        val front = api.badge.getBadgeBlobFrontPage(badgeBlobId = blobId)
                        assertNotNull(front)
                        assertFalse(front.isBlank())
                        logger.info("  Front blob: ${front.length} chars")
                    } catch (e: Esse3NotAuthorizedException) {
                        logger.warn("  Not authorized for front blob blobId=$blobId: ${e.message}")
                    } catch (e: Esse3ValidationException) {
                        logger.warn("  Validation error front blob blobId=$blobId: ${e.message}")
                    }
                }

                if (badge.rearImagePresent == 1) {
                    try {
                        val rear = api.badge.getBadgeBlobRearPage(badgeBlobId = blobId)
                        assertNotNull(rear)
                        assertFalse(rear.isBlank())
                        logger.info("  Rear blob: ${rear.length} chars")
                    } catch (e: Esse3NotAuthorizedException) {
                        logger.warn("  Not authorized for rear blob blobId=$blobId: ${e.message}")
                    } catch (e: Esse3ValidationException) {
                        logger.warn("  Validation error rear blob blobId=$blobId: ${e.message}")
                    }
                }
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("Not authorized to get badges for blob iteration: ${e.message}")
        } catch (e: Esse3ValidationException) {
            logger.warn("Validation error getting badges for blob iteration: ${e.message}")
        }
    }
}

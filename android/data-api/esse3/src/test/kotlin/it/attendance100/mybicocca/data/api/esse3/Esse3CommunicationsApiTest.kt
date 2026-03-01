package it.attendance100.mybicocca.data.api.esse3

import it.attendance100.mybicocca.data.dto.esse3.Esse3CommunicationInsert
import it.attendance100.mybicocca.data.dto.esse3.Esse3RecipientInsert
import it.attendance100.mybicocca.data.exception.esse3.Esse3NotAuthorizedException
import it.attendance100.mybicocca.data.exception.esse3.Esse3ValidationException
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory

class Esse3CommunicationsApiTest : Esse3ApiTestBase() {

    private val logger = LoggerFactory.getLogger(Esse3CommunicationsApiTest::class.java)

    @Disabled("Creates a real communication on the server — run only when intentional")
    @Test
    suspend fun postCommunication() {
        val body = Esse3CommunicationInsert(
            recipients = listOf(
                Esse3RecipientInsert(
                    dataOrigin = "S",
                    personalDataId = studentProfile.personId,
                    contact = null,
                    carbonCopyFlag = 0,
                    blindCarbonCopyFlag = 0
                )
            ),
            municipalityTypeCode = null,
            deviceCode = "MAIL",
            sender = "test@unimib.it",
            from = "test@unimib.it",
            title = "Test Communication",
            text = "This is an automated test communication. Please ignore.",
            htmlFlag = 0,
            noticeboardFlag = 0,
            pushNotificationFlag = 0,
            initialValidityDate = null,
            finalValidityDate = null
        )

        try {
            api.communications.postCommunication(body)
            logger.info("Communication posted successfully")
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("Not authorized to post communication (expected for student role): ${e.message}")
        } catch (e: Esse3ValidationException) {
            logger.warn("Validation error posting communication: ${e.message}")
        }
    }

    @Test
    suspend fun getCommunication_withKnownId() {
        // Communications require a technical user and a known communication ID.
        // We attempt with a small range of IDs to see if any are accessible.
        var found = false
        for (candidateId in 1L..5L) {
            try {
                val communication = api.communications.getCommunication(municipalityId = candidateId)
                assertNotNull(communication)
                logger.info("Got communication id=$candidateId: title='${communication.title}', device='${communication.deviceCode}', state='${communication.municipalityStateDescription}'")

                // Validate fields that should be present
                assertNotNull(communication.municipalityId, "comId should not be null")
                assertNotNull(communication.deviceCode, "deviceCode should not be null")

                // Validate recipients list is accessible
                assertNotNull(communication.recipients, "recipients should not be null")
                logger.info("  Recipients count: ${communication.recipients.size}")
                for (recipient in communication.recipients) {
                    assertNotNull(recipient.municipalityId, "recipient comId should not be null")
                }

                found = true
                break
            } catch (e: Esse3NotAuthorizedException) {
                logger.warn("Not authorized to get communication id=$candidateId: ${e.message}")
                break
            } catch (e: Esse3ValidationException) {
                logger.warn("Validation error for communication id=$candidateId: ${e.message}")
            } catch (e: Exception) {
                // Communication not found for this ID — try next
                logger.debug("Communication id=$candidateId not found: ${e.message}")
            }
        }

        if (!found) {
            logger.warn("No communication found in range 1..5 (may require technical user or specific communication IDs)")
        }
    }

    @Test
    suspend fun getCommunication_withFields() {
        // Attempt to retrieve a communication specifying explicit fields
        for (candidateId in 1L..5L) {
            try {
                val communication = api.communications.getCommunication(
                    municipalityId = candidateId,
                    fields = "comId,titolo,testo,deviceCod",
                    optionalFields = "mittente,da"
                )
                assertNotNull(communication)
                logger.info("Got communication with fields filter: id=$candidateId, title='${communication.title}'")
                assertNotNull(communication.municipalityId)
                break
            } catch (e: Esse3NotAuthorizedException) {
                logger.warn("Not authorized to get communication with fields: ${e.message}")
                break
            } catch (e: Esse3ValidationException) {
                logger.warn("Validation error for communication with fields id=$candidateId: ${e.message}")
            } catch (e: Exception) {
                logger.debug("Communication id=$candidateId not found: ${e.message}")
            }
        }
    }

    @Test
    suspend fun getCommonRecipients_forKnownCommunication() {
        // Attempt to retrieve recipients for a known communication
        for (candidateId in 1L..5L) {
            try {
                val recipients = api.communications.getCommonRecipients(municipalityId = candidateId)
                assertNotNull(recipients)
                logger.info("Got ${recipients.size} recipients for communication id=$candidateId")

                for (recipient in recipients) {
                    assertNotNull(recipient.municipalityId, "recipient comId should not be null")
                    logger.info("  Recipient: destId=${recipient.destinationId}, dataOrigin=${recipient.dataOrigin}, outcomeCode=${recipient.outcomeCode}")
                }
                break
            } catch (e: Esse3NotAuthorizedException) {
                logger.warn("Not authorized to get recipients for communication id=$candidateId: ${e.message}")
                break
            } catch (e: Esse3ValidationException) {
                logger.warn("Validation error for recipients communication id=$candidateId: ${e.message}")
            } catch (e: Exception) {
                logger.debug("Communication id=$candidateId not found for recipients: ${e.message}")
            }
        }
    }

    @Test
    suspend fun getCommonRecipients_withFilters() {
        // Try with various filters
        for (candidateId in 1L..5L) {
            try {
                val recipients = api.communications.getCommonRecipients(
                    municipalityId = candidateId,
                    dataOrigin = "S",
                    start = 0,
                    limit = 10
                )
                assertNotNull(recipients)
                logger.info("Got ${recipients.size} recipients (filtered by origineDato=S) for communication id=$candidateId")

                for (recipient in recipients) {
                    assertNotNull(recipient.municipalityId)
                }
                break
            } catch (e: Esse3NotAuthorizedException) {
                logger.warn("Not authorized to get filtered recipients: ${e.message}")
                break
            } catch (e: Esse3ValidationException) {
                logger.warn("Validation error for filtered recipients communication id=$candidateId: ${e.message}")
            } catch (e: Exception) {
                logger.debug("Communication id=$candidateId not accessible: ${e.message}")
            }
        }
    }

    @Test
    suspend fun getCommonRecipients_withPagination() {
        for (candidateId in 1L..5L) {
            try {
                val page1 = api.communications.getCommonRecipients(
                    municipalityId = candidateId,
                    start = 0,
                    limit = 5
                )
                val page2 = api.communications.getCommonRecipients(
                    municipalityId = candidateId,
                    start = 5,
                    limit = 5
                )
                assertNotNull(page1)
                assertNotNull(page2)
                logger.info("Recipients pagination for communication id=$candidateId: page1=${page1.size}, page2=${page2.size}")

                if (page1.size == 5) {
                    val page1Ids = page1.mapNotNull { it.destinationId }.toSet()
                    val page2Ids = page2.mapNotNull { it.destinationId }.toSet()
                    // Pages should not overlap if both have results
                    if (page2.isNotEmpty()) {
                        assert(page1Ids.intersect(page2Ids).isEmpty()) { "Recipient pages should not overlap" }
                    }
                }
                break
            } catch (e: Esse3NotAuthorizedException) {
                logger.warn("Not authorized to get paginated recipients: ${e.message}")
                break
            } catch (e: Esse3ValidationException) {
                logger.warn("Validation error for paginated recipients: ${e.message}")
            } catch (e: Exception) {
                logger.debug("Communication id=$candidateId not accessible for pagination: ${e.message}")
            }
        }
    }
}

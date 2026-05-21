package it.attendance100.mybicocca.data.remote.esse3.api

import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3CommunicationInsert
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3PermissionLevel
import it.attendance100.mybicocca.data.remote.esse3.exception.Esse3Exception
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.util.logging.Logger
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class Esse3CommunicationsApiTest : Esse3ApiTestBase() {

    companion object {
        private val logger = Logger.getLogger(Esse3CommunicationsApiTest::class.java.name)
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
    @Disabled("Irreversible mutating operation")
    fun testPostCommunication() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testPostCommunication: user has TECHNICAL_USER permission, but test is disabled to prevent data mutation")
        } else {
            logger.info("testPostCommunication: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.communications.postCommunication(
                    body = Esse3CommunicationInsert(
                        deviceCode = "EMAIL",
                        title = "Test Communication",
                        text = "Test communication body"
                    )
                )
            }
        }
    }

    @Test
    fun testGetCommunication() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetCommunication: calling getCommunication() with municipalityId=1 and defaults")
            val result = api.communications.getCommunication(municipalityId = 1)
            logger.info("testGetCommunication: result municipalityId=${result.municipalityId}, title=${result.title}, deviceCode=${result.deviceCode}")
            assertNotNull(result.municipalityId, "municipalityId should not be null")

            logger.info("testGetCommunication: calling getCommunication() with fields param")
            val resultWithFields = api.communications.getCommunication(
                municipalityId = 1,
                fields = "comId,titolo,testo"
            )
            logger.info("testGetCommunication: result with fields municipalityId=${resultWithFields.municipalityId}")

            logger.info("testGetCommunication: calling getCommunication() with optionalFields param")
            val resultWithOptFields = api.communications.getCommunication(
                municipalityId = 1,
                optionalFields = "destinatari"
            )
            logger.info("testGetCommunication: result with optionalFields municipalityId=${resultWithOptFields.municipalityId}, recipients size=${resultWithOptFields.recipients.size}")
        } else {
            logger.info("testGetCommunication: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.communications.getCommunication(municipalityId = 1)
            }
            logger.info("testGetCommunication: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetCommonRecipients() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetCommonRecipients: calling getCommonRecipients() with municipalityId=1 and defaults")
            val defaultResult = api.communications.getCommonRecipients(municipalityId = 1)
            logger.info("testGetCommonRecipients: default result size=${defaultResult.size}")
            assertTrue(defaultResult.isNotEmpty(), "getCommonRecipients() default should return non-empty list")
            for (recipient in defaultResult) {
                logger.info("testGetCommonRecipients: recipient destinationId=${recipient.destinationId}, contact=${recipient.contact}, fullName=${recipient.fullName}")
            }

            logger.info("testGetCommonRecipients: calling getCommonRecipients() with pagination start=0, limit=5")
            val paginatedResult = api.communications.getCommonRecipients(
                municipalityId = 1,
                start = 0,
                limit = 5
            )
            logger.info("testGetCommonRecipients: paginated result size=${paginatedResult.size}")
            assertTrue(paginatedResult.size <= 5, "Paginated result size should be <= 5")

            if (defaultResult.isNotEmpty()) {
                val firstRecipient = defaultResult.first()

                if (firstRecipient.dataOrigin != null) {
                    logger.info("testGetCommonRecipients: calling getCommonRecipients() with dataOrigin=${firstRecipient.dataOrigin}")
                    val filteredByOrigin = api.communications.getCommonRecipients(
                        municipalityId = 1,
                        dataOrigin = firstRecipient.dataOrigin
                    )
                    logger.info("testGetCommonRecipients: filtered by dataOrigin result size=${filteredByOrigin.size}")
                    assertTrue(filteredByOrigin.isNotEmpty(), "Filtered by dataOrigin should return non-empty list")
                }

                if (firstRecipient.personalDataId != null) {
                    logger.info("testGetCommonRecipients: calling getCommonRecipients() with personalDataId=${firstRecipient.personalDataId}")
                    val filteredByPersonalData = api.communications.getCommonRecipients(
                        municipalityId = 1,
                        personalDataId = firstRecipient.personalDataId
                    )
                    logger.info("testGetCommonRecipients: filtered by personalDataId result size=${filteredByPersonalData.size}")
                    assertTrue(filteredByPersonalData.isNotEmpty(), "Filtered by personalDataId should return non-empty list")
                }

                if (firstRecipient.contact != null) {
                    logger.info("testGetCommonRecipients: calling getCommonRecipients() with contact=${firstRecipient.contact}")
                    val filteredByContact = api.communications.getCommonRecipients(
                        municipalityId = 1,
                        contact = firstRecipient.contact
                    )
                    logger.info("testGetCommonRecipients: filtered by contact result size=${filteredByContact.size}")
                    assertTrue(filteredByContact.isNotEmpty(), "Filtered by contact should return non-empty list")
                }

                if (firstRecipient.outcomeCode != null) {
                    logger.info("testGetCommonRecipients: calling getCommonRecipients() with outcomeCode=${firstRecipient.outcomeCode}")
                    val filteredByOutcome = api.communications.getCommonRecipients(
                        municipalityId = 1,
                        outcomeCode = firstRecipient.outcomeCode
                    )
                    logger.info("testGetCommonRecipients: filtered by outcomeCode result size=${filteredByOutcome.size}")
                    assertTrue(filteredByOutcome.isNotEmpty(), "Filtered by outcomeCode should return non-empty list")
                }
            }

            logger.info("testGetCommonRecipients: calling getCommonRecipients() with fields param")
            val resultWithFields = api.communications.getCommonRecipients(
                municipalityId = 1,
                fields = "comId,destId,recapito"
            )
            logger.info("testGetCommonRecipients: result with fields size=${resultWithFields.size}")

            logger.info("testGetCommonRecipients: calling getCommonRecipients() with optionalFields param")
            val resultWithOptFields = api.communications.getCommonRecipients(
                municipalityId = 1,
                optionalFields = "nominativo"
            )
            logger.info("testGetCommonRecipients: result with optionalFields size=${resultWithOptFields.size}")
        } else {
            logger.info("testGetCommonRecipients: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.communications.getCommonRecipients(municipalityId = 1)
            }
            logger.info("testGetCommonRecipients: Esse3Exception thrown as expected")
        }
    }
}

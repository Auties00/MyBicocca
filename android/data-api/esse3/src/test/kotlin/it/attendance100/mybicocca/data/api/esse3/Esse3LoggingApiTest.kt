package it.attendance100.mybicocca.data.api.esse3

import it.attendance100.mybicocca.data.dto.esse3.Esse3TransactionLogSessionBody
import it.attendance100.mybicocca.data.exception.esse3.Esse3NotAuthorizedException
import it.attendance100.mybicocca.data.exception.esse3.Esse3ValidationException
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.logging.Logger

class Esse3LoggingApiTest : Esse3ApiTestBase() {

    private val logger = Logger.getLogger(Esse3LoggingApiTest::class.java.name)

    @Test
    suspend fun testGetTlogSessions() {
        try {
            val sessions = api.logging.getTlogSessions()
            logger.info("getTlogSessions: found ${sessions.size} sessions")
            for (session in sessions) {
                logger.info("  session: sessionId=${session.sessionId}, llevel=${session.llevel}, lcode=${session.lcode}, ldesc=${session.ldesc}, addTransactionInfo=${session.addTransactionInfo}, llevelMinutesTimeout=${session.llevelMinutesTimeout}, insertionUserId=${session.insertionUserId}, insertionDate=${session.insertionDate}")
                assertNotNull(session.sessionId, "sessionId should not be null")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getTlogSessions not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun testGetTlogTransactionsNoFilter() {
        try {
            val transactions = api.logging.getTlogTransactions()
            logger.info("getTlogTransactions (no filter): found ${transactions.size} transactions")
            for (tx in transactions) {
                logger.info("  transaction: ctxParamsId=${tx.contextParamsId}, transactionId=${tx.transactionId}, sessionId=${tx.sessionId}, description=${tx.description}, insertionDate=${tx.insertionDate}, usrInsId=${tx.insertionUserId}")
                for (detail in tx.vTeachingLogParametersDetail) {
                    logger.info("    detail: paramName=${detail.parameterName}, paramValue=${detail.parameterValue}")
                }
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getTlogTransactions (no filter) not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun testGetTlogTransactionsBySession() {
        try {
            val sessions = api.logging.getTlogSessions()
            if (sessions.isEmpty()) {
                logger.warning("No sessions available to test getTlogTransactions by sessionId")
                return
            }

            for (session in sessions) {
                val sessionId = session.sessionId ?: continue
                logger.info("getTlogTransactions by sessionId=$sessionId")
                try {
                    val transactions = api.logging.getTlogTransactions(sessionId = sessionId)
                    logger.info("  found ${transactions.size} transactions for sessionId=$sessionId")
                    for (tx in transactions) {
                        logger.info("    transaction: ctxParamsId=${tx.contextParamsId}, transactionId=${tx.transactionId}")
                        assertEquals(sessionId, tx.sessionId, "sessionId in transaction should match requested sessionId")
                        for (detail in tx.vTeachingLogParametersDetail) {
                            logger.info("      detail: paramName=${detail.parameterName}, paramValue=${detail.parameterValue}")
                        }
                    }
                    break
                } catch (e: Esse3NotAuthorizedException) {
                    logger.warning("  getTlogTransactions(sessionId=$sessionId) not authorized: ${e.message}")
                } catch (e: Esse3ValidationException) {
                    logger.warning("  getTlogTransactions(sessionId=$sessionId) validation error: ${e.message}")
                }
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getTlogSessions not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun testGetTlogTransactionsByContextParamsId() {
        try {
            val allTransactions = api.logging.getTlogTransactions()
            val firstWithContextParamsId = allTransactions.firstOrNull { it.contextParamsId != null }
            if (firstWithContextParamsId == null) {
                logger.warning("No transactions with contextParamsId available to test getTlogTransactions filter")
                return
            }

            val ctxParamsId = firstWithContextParamsId.contextParamsId!!
            logger.info("getTlogTransactions by ctxParamsId=$ctxParamsId")
            val filtered = api.logging.getTlogTransactions(contextParamsId = ctxParamsId)
            logger.info("  found ${filtered.size} transactions for ctxParamsId=$ctxParamsId")
            for (tx in filtered) {
                logger.info("  transaction: ctxParamsId=${tx.contextParamsId}, transactionId=${tx.transactionId}")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getTlogTransactions by ctxParamsId not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun testGetTlogTransactionsByTransactionId() {
        try {
            val allTransactions = api.logging.getTlogTransactions()
            val firstWithTransactionId = allTransactions.firstOrNull { it.transactionId != null }
            if (firstWithTransactionId == null) {
                logger.warning("No transactions with transactionId available to test getTlogTransactions filter by transactionId")
                return
            }

            val transactionId = firstWithTransactionId.transactionId!!
            logger.info("getTlogTransactions by transactionId=$transactionId")
            val filtered = api.logging.getTlogTransactions(transactionId = transactionId)
            logger.info("  found ${filtered.size} transactions for transactionId=$transactionId")
            for (tx in filtered) {
                logger.info("  transaction: ctxParamsId=${tx.contextParamsId}, transactionId=${tx.transactionId}, sessionId=${tx.sessionId}")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getTlogTransactions by transactionId not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun testGetTlogTextNoFilter() {
        try {
            val text = api.logging.getTlogText()
            logger.info("getTlogText (no filter): length=${text.length}")
            assertNotNull(text, "tlog text should not be null")
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getTlogText (no filter) not authorized: ${e.message}")
        } catch (e: Esse3ValidationException) {
            logger.warning("getTlogText (no filter) validation error: ${e.message}")
        }
    }

    @Test
    suspend fun testGetTlogTextBySession() {
        try {
            val sessions = api.logging.getTlogSessions()
            if (sessions.isEmpty()) {
                logger.warning("No sessions available to test getTlogText by sessionId")
                return
            }

            for (session in sessions) {
                val sessionId = session.sessionId ?: continue
                logger.info("getTlogText by sessionId=$sessionId")
                try {
                    val text = api.logging.getTlogText(sessionId = sessionId)
                    logger.info("  getTlogText(sessionId=$sessionId): length=${text.length}")
                    assertNotNull(text, "tlog text should not be null")
                    break
                } catch (e: Esse3NotAuthorizedException) {
                    logger.warning("  getTlogText(sessionId=$sessionId) not authorized: ${e.message}")
                } catch (e: Esse3ValidationException) {
                    logger.warning("  getTlogText(sessionId=$sessionId) validation error: ${e.message}")
                }
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getTlogSessions not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun testGetTlogTextByContextParamsId() {
        try {
            val allTransactions = api.logging.getTlogTransactions()
            val firstWithContextParamsId = allTransactions.firstOrNull { it.contextParamsId != null }
            if (firstWithContextParamsId == null) {
                logger.warning("No transactions with contextParamsId available to test getTlogText filter by ctxParamsId")
                return
            }

            val ctxParamsId = firstWithContextParamsId.contextParamsId!!
            logger.info("getTlogText by ctxParamsId=$ctxParamsId")
            val text = api.logging.getTlogText(contextParamsId = ctxParamsId)
            logger.info("  getTlogText(ctxParamsId=$ctxParamsId): length=${text.length}")
            assertNotNull(text, "tlog text should not be null")
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getTlogText by ctxParamsId not authorized: ${e.message}")
        } catch (e: Esse3ValidationException) {
            logger.warning("getTlogText by ctxParamsId validation error: ${e.message}")
        }
    }

    @Test
    suspend fun testGetTlogTextByTransactionId() {
        try {
            val allTransactions = api.logging.getTlogTransactions()
            val firstWithTransactionId = allTransactions.firstOrNull { it.transactionId != null }
            if (firstWithTransactionId == null) {
                logger.warning("No transactions with transactionId available to test getTlogText filter by transactionId")
                return
            }

            val transactionId = firstWithTransactionId.transactionId!!
            logger.info("getTlogText by transactionId=$transactionId")
            val text = api.logging.getTlogText(transactionId = transactionId)
            logger.info("  getTlogText(transactionId=$transactionId): length=${text.length}")
            assertNotNull(text, "tlog text should not be null")
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getTlogText by transactionId not authorized: ${e.message}")
        } catch (e: Esse3ValidationException) {
            logger.warning("getTlogText by transactionId validation error: ${e.message}")
        }
    }

    @Test
    suspend fun testPutTlogSessions() {
        try {
            val sessions = api.logging.getTlogSessions()
            if (sessions.isEmpty()) {
                logger.warning("No sessions available to test putTlogSessions")
                return
            }

            val session = sessions.firstOrNull { it.sessionId != null } ?: run {
                logger.warning("No session with sessionId found to test putTlogSessions")
                return
            }

            val sessionId = session.sessionId!!
            val originalLlevel = session.llevel ?: 0L
            val originalAddTransactionInfo = session.addTransactionInfo ?: 0
            val originalTimeout = session.llevelMinutesTimeout ?: 0.0

            logger.info("putTlogSessions: sessionId=$sessionId, original llevel=$originalLlevel, addTransactionInfo=$originalAddTransactionInfo, timeout=$originalTimeout")

            val rollbackBody = Esse3TransactionLogSessionBody(
                llevel = originalLlevel,
                addTransactionInfo = originalAddTransactionInfo,
                llevelMinutesTimeout = originalTimeout
            )

            try {
                val updateBody = Esse3TransactionLogSessionBody(
                    llevel = originalLlevel,
                    addTransactionInfo = originalAddTransactionInfo,
                    llevelMinutesTimeout = originalTimeout
                )

                val result = api.logging.putTlogSessions(sessionId, updateBody)
                logger.info("putTlogSessions result: sessionId=${result.sessionId}, llevel=${result.llevel}, addTransactionInfo=${result.addTransactionInfo}, timeout=${result.llevelMinutesTimeout}")
                assertEquals(sessionId, result.sessionId, "sessionId should match after PUT")
            } finally {
                try {
                    val restored = api.logging.putTlogSessions(sessionId, rollbackBody)
                    logger.info("putTlogSessions rollback: sessionId=${restored.sessionId}, llevel=${restored.llevel}")
                } catch (e: Exception) {
                    logger.warning("putTlogSessions rollback failed: ${e.message}")
                }
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("putTlogSessions not authorized: ${e.message}")
        } catch (e: Esse3ValidationException) {
            logger.warning("putTlogSessions validation error: ${e.message}")
        }
    }
}

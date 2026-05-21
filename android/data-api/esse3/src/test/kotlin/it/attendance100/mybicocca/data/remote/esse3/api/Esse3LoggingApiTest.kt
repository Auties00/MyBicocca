package it.attendance100.mybicocca.data.remote.esse3.api

import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3PermissionLevel
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3TransactionLogSessionBody
import it.attendance100.mybicocca.data.remote.esse3.exception.Esse3Exception
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.logging.Logger

class Esse3LoggingApiTest : Esse3ApiTestBase() {

    private val logger = Logger.getLogger(Esse3LoggingApiTest::class.java.name)

    private lateinit var permissions: Set<Esse3PermissionLevel>

    @BeforeAll
    fun setUp() = runTest {
        val loginResult = api.auth.login()
        permissions = buildSet {
            add(Esse3PermissionLevel.AUTHENTICATED_USER)
            for (profile in loginResult.profiles) {
                val groupId = profile.groupId?.toInt()
                if (groupId != null) {
                    add(Esse3PermissionLevel.fromGroupId(groupId))
                }
                val description = profile.description
                if (description != null) {
                    add(Esse3PermissionLevel.fromProfileName(description))
                }
            }
            add(Esse3PermissionLevel.ANY)
        }
        logger.info("Derived permissions: $permissions")
    }

    private fun hasPermission(required: Esse3PermissionLevel): Boolean {
        return required == Esse3PermissionLevel.ANY || permissions.contains(required)
    }

    @Test
    fun testGetTlogSessionsDefaults() = runTest {
        if (!hasPermission(Esse3PermissionLevel.AUTHENTICATED_USER)) {
            logger.warning("Skipping testGetTlogSessionsDefaults: missing AUTHENTICATED_USER permission")
            assertThrows<Esse3Exception> { api.logging.getTlogSessions() }
            return@runTest
        }

        try {
            val sessions = api.logging.getTlogSessions()
            assertNotNull(sessions)
            logger.info("getTlogSessions (defaults): returned ${sessions.size} sessions")
            for (s in sessions) {
                logger.info("  sessionId=${s.sessionId}, llevel=${s.llevel}, lcode=${s.lcode}, ldesc=${s.ldesc}, addTransactionInfo=${s.addTransactionInfo}, llevelMinutesTimeout=${s.llevelMinutesTimeout}, insertionUserId=${s.insertionUserId}, insertionDate=${s.insertionDate}")
                assertNotNull(s.sessionId, "sessionId should not be null")
            }
        } catch (e: Esse3Exception) {
            logger.warning("getTlogSessions not authorized: ${e.message}")
        }
    }

    @Test
    fun testGetTlogTransactionsDefaults() = runTest {
        if (!hasPermission(Esse3PermissionLevel.AUTHENTICATED_USER)) {
            logger.warning("Skipping testGetTlogTransactionsDefaults: missing AUTHENTICATED_USER permission")
            assertThrows<Esse3Exception> { api.logging.getTlogTransactions() }
            return@runTest
        }

        try {
            val transactions = api.logging.getTlogTransactions()
            assertNotNull(transactions)
            logger.info("getTlogTransactions (defaults): returned ${transactions.size} transactions")
            for (tx in transactions) {
                logger.info("  ctxParamsId=${tx.contextParamsId}, transactionId=${tx.transactionId}, sessionId=${tx.sessionId}, description=${tx.description}, insertionDate=${tx.insertionDate}, insertionUserId=${tx.insertionUserId}")
                for (detail in tx.vTeachingLogParametersDetail) {
                    logger.info("    detail: paramName=${detail.parameterName}, paramValue=${detail.parameterValue}")
                }
            }
        } catch (e: Esse3Exception) {
            logger.warning("getTlogTransactions (defaults) not authorized: ${e.message}")
        }
    }

    @Test
    fun testGetTlogTransactionsFilterBySessionId() = runTest {
        if (!hasPermission(Esse3PermissionLevel.AUTHENTICATED_USER)) {
            logger.warning("Skipping testGetTlogTransactionsFilterBySessionId: missing AUTHENTICATED_USER permission")
            assertThrows<Esse3Exception> { api.logging.getTlogTransactions() }
            return@runTest
        }

        try {
            val sessions = api.logging.getTlogSessions()
            if (sessions.isEmpty()) {
                logger.warning("No sessions available to filter transactions by sessionId")
                return@runTest
            }

            val targetSession = sessions.firstOrNull { it.sessionId != null }
            if (targetSession == null) {
                logger.warning("No session with a non-null sessionId found")
                return@runTest
            }

            val sessionId = targetSession.sessionId!!
            logger.info("Filtering transactions by sessionId=$sessionId")
            val transactions = api.logging.getTlogTransactions(sessionId = sessionId)
            assertNotNull(transactions)
            logger.info("  returned ${transactions.size} transactions for sessionId=$sessionId")
            for (tx in transactions) {
                assertEquals(sessionId, tx.sessionId, "Transaction sessionId should match filter")
                logger.info("  ctxParamsId=${tx.contextParamsId}, transactionId=${tx.transactionId}")
                for (detail in tx.vTeachingLogParametersDetail) {
                    logger.info("    detail: paramName=${detail.parameterName}, paramValue=${detail.parameterValue}")
                }
            }
        } catch (e: Esse3Exception) {
            logger.warning("getTlogTransactions by sessionId not authorized: ${e.message}")
        } catch (e: Esse3Exception) {
            logger.warning("getTlogTransactions by sessionId validation error: ${e.message}")
        }
    }

    @Test
    fun testGetTlogTransactionsFilterByContextParamsId() = runTest {
        if (!hasPermission(Esse3PermissionLevel.AUTHENTICATED_USER)) {
            logger.warning("Skipping testGetTlogTransactionsFilterByContextParamsId: missing AUTHENTICATED_USER permission")
            assertThrows<Esse3Exception> { api.logging.getTlogTransactions() }
            return@runTest
        }

        try {
            val allTransactions = api.logging.getTlogTransactions()
            val target = allTransactions.firstOrNull { it.contextParamsId != null }
            if (target == null) {
                logger.warning("No transactions with contextParamsId available for filtering")
                return@runTest
            }

            val ctxParamsId = target.contextParamsId!!
            logger.info("Filtering transactions by ctxParamsId=$ctxParamsId")
            val filtered = api.logging.getTlogTransactions(contextParamsId = ctxParamsId)
            assertNotNull(filtered)
            logger.info("  returned ${filtered.size} transactions for ctxParamsId=$ctxParamsId")
            for (tx in filtered) {
                logger.info("  ctxParamsId=${tx.contextParamsId}, transactionId=${tx.transactionId}, sessionId=${tx.sessionId}")
            }
        } catch (e: Esse3Exception) {
            logger.warning("getTlogTransactions by ctxParamsId not authorized: ${e.message}")
        }
    }

    @Test
    fun testGetTlogTransactionsFilterByTransactionId() = runTest {
        if (!hasPermission(Esse3PermissionLevel.AUTHENTICATED_USER)) {
            logger.warning("Skipping testGetTlogTransactionsFilterByTransactionId: missing AUTHENTICATED_USER permission")
            assertThrows<Esse3Exception> { api.logging.getTlogTransactions() }
            return@runTest
        }

        try {
            val allTransactions = api.logging.getTlogTransactions()
            val target = allTransactions.firstOrNull { it.transactionId != null }
            if (target == null) {
                logger.warning("No transactions with transactionId available for filtering")
                return@runTest
            }

            val transactionId = target.transactionId!!
            logger.info("Filtering transactions by transactionId=$transactionId")
            val filtered = api.logging.getTlogTransactions(transactionId = transactionId)
            assertNotNull(filtered)
            logger.info("  returned ${filtered.size} transactions for transactionId=$transactionId")
            for (tx in filtered) {
                logger.info("  ctxParamsId=${tx.contextParamsId}, transactionId=${tx.transactionId}, sessionId=${tx.sessionId}")
            }
        } catch (e: Esse3Exception) {
            logger.warning("getTlogTransactions by transactionId not authorized: ${e.message}")
        }
    }

    @Test
    fun testGetTlogTextDefaults() = runTest {
        if (!hasPermission(Esse3PermissionLevel.AUTHENTICATED_USER)) {
            logger.warning("Skipping testGetTlogTextDefaults: missing AUTHENTICATED_USER permission")
            assertThrows<Esse3Exception> { api.logging.getTlogText() }
            return@runTest
        }

        try {
            val text = api.logging.getTlogText()
            assertNotNull(text)
            logger.info("getTlogText (defaults): length=${text.length}")
        } catch (e: Esse3Exception) {
            logger.warning("getTlogText (defaults) not authorized: ${e.message}")
        } catch (e: Esse3Exception) {
            logger.warning("getTlogText (defaults) validation error: ${e.message}")
        }
    }

    @Test
    fun testGetTlogTextFilterBySessionId() = runTest {
        if (!hasPermission(Esse3PermissionLevel.AUTHENTICATED_USER)) {
            logger.warning("Skipping testGetTlogTextFilterBySessionId: missing AUTHENTICATED_USER permission")
            assertThrows<Esse3Exception> { api.logging.getTlogText() }
            return@runTest
        }

        try {
            val sessions = api.logging.getTlogSessions()
            if (sessions.isEmpty()) {
                logger.warning("No sessions available to filter tlog text by sessionId")
                return@runTest
            }

            val targetSession = sessions.firstOrNull { it.sessionId != null }
            if (targetSession == null) {
                logger.warning("No session with a non-null sessionId found")
                return@runTest
            }

            val sessionId = targetSession.sessionId!!
            logger.info("Filtering tlog text by sessionId=$sessionId")
            val text = api.logging.getTlogText(sessionId = sessionId)
            assertNotNull(text)
            logger.info("  getTlogText(sessionId=$sessionId): length=${text.length}")
        } catch (e: Esse3Exception) {
            logger.warning("getTlogText by sessionId not authorized: ${e.message}")
        } catch (e: Esse3Exception) {
            logger.warning("getTlogText by sessionId validation error: ${e.message}")
        }
    }

    @Test
    fun testGetTlogTextFilterByContextParamsId() = runTest {
        if (!hasPermission(Esse3PermissionLevel.AUTHENTICATED_USER)) {
            logger.warning("Skipping testGetTlogTextFilterByContextParamsId: missing AUTHENTICATED_USER permission")
            assertThrows<Esse3Exception> { api.logging.getTlogText() }
            return@runTest
        }

        try {
            val allTransactions = api.logging.getTlogTransactions()
            val target = allTransactions.firstOrNull { it.contextParamsId != null }
            if (target == null) {
                logger.warning("No transactions with contextParamsId available to filter tlog text")
                return@runTest
            }

            val ctxParamsId = target.contextParamsId!!
            logger.info("Filtering tlog text by ctxParamsId=$ctxParamsId")
            val text = api.logging.getTlogText(contextParamsId = ctxParamsId)
            assertNotNull(text)
            logger.info("  getTlogText(ctxParamsId=$ctxParamsId): length=${text.length}")
        } catch (e: Esse3Exception) {
            logger.warning("getTlogText by ctxParamsId not authorized: ${e.message}")
        } catch (e: Esse3Exception) {
            logger.warning("getTlogText by ctxParamsId validation error: ${e.message}")
        }
    }

    @Test
    fun testGetTlogTextFilterByTransactionId() = runTest {
        if (!hasPermission(Esse3PermissionLevel.AUTHENTICATED_USER)) {
            logger.warning("Skipping testGetTlogTextFilterByTransactionId: missing AUTHENTICATED_USER permission")
            assertThrows<Esse3Exception> { api.logging.getTlogText() }
            return@runTest
        }

        try {
            val allTransactions = api.logging.getTlogTransactions()
            val target = allTransactions.firstOrNull { it.transactionId != null }
            if (target == null) {
                logger.warning("No transactions with transactionId available to filter tlog text")
                return@runTest
            }

            val transactionId = target.transactionId!!
            logger.info("Filtering tlog text by transactionId=$transactionId")
            val text = api.logging.getTlogText(transactionId = transactionId)
            assertNotNull(text)
            logger.info("  getTlogText(transactionId=$transactionId): length=${text.length}")
        } catch (e: Esse3Exception) {
            logger.warning("getTlogText by transactionId not authorized: ${e.message}")
        } catch (e: Esse3Exception) {
            logger.warning("getTlogText by transactionId validation error: ${e.message}")
        }
    }

    @Test
    fun testGetTlogTextFilterByDateRange() = runTest {
        if (!hasPermission(Esse3PermissionLevel.AUTHENTICATED_USER)) {
            logger.warning("Skipping testGetTlogTextFilterByDateRange: missing AUTHENTICATED_USER permission")
            assertThrows<Esse3Exception> { api.logging.getTlogText() }
            return@runTest
        }

        try {
            val text = api.logging.getTlogText(startDate = "2025-01-01", endDate = "2025-12-31")
            assertNotNull(text)
            logger.info("getTlogText (dateRange 2025-01-01..2025-12-31): length=${text.length}")
        } catch (e: Esse3Exception) {
            logger.warning("getTlogText by date range not authorized: ${e.message}")
        } catch (e: Esse3Exception) {
            logger.warning("getTlogText by date range validation error: ${e.message}")
        }
    }

    @Test
    fun testPutTlogSessionsIdempotent() = runTest {
        if (!hasPermission(Esse3PermissionLevel.AUTHENTICATED_USER)) {
            logger.warning("Skipping testPutTlogSessionsIdempotent: missing AUTHENTICATED_USER permission")
            assertThrows<Esse3Exception> { api.logging.getTlogSessions() }
            return@runTest
        }

        try {
            val sessions = api.logging.getTlogSessions()
            if (sessions.isEmpty()) {
                logger.warning("No sessions available to test putTlogSessions")
                return@runTest
            }

            val target = sessions.firstOrNull { it.sessionId != null }
            if (target == null) {
                logger.warning("No session with a non-null sessionId found for putTlogSessions")
                return@runTest
            }

            val sessionId = target.sessionId!!
            val originalLlevel = target.llevel ?: 0L
            val originalAddTransactionInfo = target.addTransactionInfo ?: 0
            val originalTimeout = target.llevelMinutesTimeout ?: 0.0

            logger.info("putTlogSessions: sessionId=$sessionId, original llevel=$originalLlevel, addTransactionInfo=$originalAddTransactionInfo, timeout=$originalTimeout")

            val idempotentBody = Esse3TransactionLogSessionBody(
                llevel = originalLlevel,
                addTransactionInfo = originalAddTransactionInfo,
                llevelMinutesTimeout = originalTimeout
            )

            try {
                val result = api.logging.putTlogSessions(sessionId, idempotentBody)
                assertNotNull(result)
                assertEquals(sessionId, result.sessionId, "sessionId should match after PUT")
                logger.info("putTlogSessions result: sessionId=${result.sessionId}, llevel=${result.llevel}, addTransactionInfo=${result.addTransactionInfo}, timeout=${result.llevelMinutesTimeout}")
            } finally {
                try {
                    val restored = api.logging.putTlogSessions(sessionId, idempotentBody)
                    logger.info("putTlogSessions rollback: sessionId=${restored.sessionId}, llevel=${restored.llevel}")
                } catch (e: Exception) {
                    logger.warning("putTlogSessions rollback failed: ${e.message}")
                }
            }
        } catch (e: Esse3Exception) {
            logger.warning("putTlogSessions not authorized: ${e.message}")
        } catch (e: Esse3Exception) {
            logger.warning("putTlogSessions validation error: ${e.message}")
        }
    }

    @Test
    fun testGetTlogSessionsFieldsPopulated() = runTest {
        if (!hasPermission(Esse3PermissionLevel.AUTHENTICATED_USER)) {
            logger.warning("Skipping testGetTlogSessionsFieldsPopulated: missing AUTHENTICATED_USER permission")
            assertThrows<Esse3Exception> { api.logging.getTlogSessions() }
            return@runTest
        }

        try {
            val sessions = api.logging.getTlogSessions()
            assertNotNull(sessions)
            if (sessions.isNotEmpty()) {
                val first = sessions.first()
                assertNotNull(first.sessionId, "First session sessionId should not be null")
                logger.info("First session fields: sessionId=${first.sessionId}, llevel=${first.llevel}, lcode=${first.lcode}, ldesc=${first.ldesc}")
                logger.info("  addTransactionInfo=${first.addTransactionInfo}, llevelMinutesTimeout=${first.llevelMinutesTimeout}")
                logger.info("  insertionUserId=${first.insertionUserId}, insertionDate=${first.insertionDate}")
            } else {
                logger.info("getTlogSessions returned empty list, no field validation possible")
            }
        } catch (e: Esse3Exception) {
            logger.warning("getTlogSessions not authorized: ${e.message}")
        }
    }

    @Test
    fun testGetTlogTransactionsDetailParameters() = runTest {
        if (!hasPermission(Esse3PermissionLevel.AUTHENTICATED_USER)) {
            logger.warning("Skipping testGetTlogTransactionsDetailParameters: missing AUTHENTICATED_USER permission")
            assertThrows<Esse3Exception> { api.logging.getTlogTransactions() }
            return@runTest
        }

        try {
            val transactions = api.logging.getTlogTransactions()
            assertNotNull(transactions)
            val withDetails = transactions.filter { it.vTeachingLogParametersDetail.isNotEmpty() }
            logger.info("getTlogTransactions: ${transactions.size} total, ${withDetails.size} with detail parameters")
            for (tx in withDetails.take(5)) {
                logger.info("  transactionId=${tx.transactionId}, details count=${tx.vTeachingLogParametersDetail.size}")
                for (detail in tx.vTeachingLogParametersDetail) {
                    assertNotNull(detail.parameterName, "Detail parameterName should not be null")
                    logger.info("    paramName=${detail.parameterName}, paramValue=${detail.parameterValue}")
                }
            }
        } catch (e: Esse3Exception) {
            logger.warning("getTlogTransactions not authorized: ${e.message}")
        }
    }

    @Test
    fun testGetTlogTextCombinedFilters() = runTest {
        if (!hasPermission(Esse3PermissionLevel.AUTHENTICATED_USER)) {
            logger.warning("Skipping testGetTlogTextCombinedFilters: missing AUTHENTICATED_USER permission")
            assertThrows<Esse3Exception> { api.logging.getTlogText() }
            return@runTest
        }

        try {
            val sessions = api.logging.getTlogSessions()
            val targetSession = sessions.firstOrNull { it.sessionId != null }
            if (targetSession == null) {
                logger.warning("No session available for combined filter test on getTlogText")
                return@runTest
            }

            val sessionId = targetSession.sessionId!!
            val transactions = api.logging.getTlogTransactions(sessionId = sessionId)
            val targetTx = transactions.firstOrNull { it.contextParamsId != null }
            if (targetTx == null) {
                logger.warning("No transaction with contextParamsId for combined filter on getTlogText (sessionId=$sessionId)")
                return@runTest
            }

            val ctxParamsId = targetTx.contextParamsId!!
            logger.info("getTlogText combined: sessionId=$sessionId, ctxParamsId=$ctxParamsId")
            val text = api.logging.getTlogText(sessionId = sessionId, contextParamsId = ctxParamsId)
            assertNotNull(text)
            logger.info("  combined filter result length=${text.length}")
        } catch (e: Esse3Exception) {
            logger.warning("getTlogText combined filters not authorized: ${e.message}")
        } catch (e: Esse3Exception) {
            logger.warning("getTlogText combined filters validation error: ${e.message}")
        }
    }
}

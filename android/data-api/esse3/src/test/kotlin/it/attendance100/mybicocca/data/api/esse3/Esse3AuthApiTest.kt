package it.attendance100.mybicocca.data.api.esse3

import it.attendance100.mybicocca.data.dto.esse3.Esse3CacheInfo
import it.attendance100.mybicocca.data.exception.esse3.Esse3NotAuthorizedException
import it.attendance100.mybicocca.data.exception.esse3.Esse3ValidationException
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.util.logging.Logger

class Esse3AuthApiTest : Esse3ApiTestBase() {

    private val logger = Logger.getLogger(this::class.java.name)

    @Test
    suspend fun testCheckLogon() {
        val result = api.auth.checkLogon()
        assertNotNull(result)
        assertTrue(result.ok == true, "checkLogon should return ok=true for authenticated user")
        logger.info("checkLogon result: ok=${result.ok}, changePassword=${result.changePassword}")
    }

    @Test
    suspend fun testGetJWT() {
        try {
            val result = api.auth.getJWT()
            assertNotNull(result)
            assertNotNull(result.jwt, "JWT token should not be null")
            assertTrue(result.jwt!!.isNotBlank(), "JWT token should not be blank")
            logger.info("getJWT result: jwt length=${result.jwt.length}")
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("Skipping testGetJWT: insufficient permissions - ${e.message}")
        }
    }

    @Test
    suspend fun testRefreshJWT() {
        try {
            val result = api.auth.refreshJWT()
            assertNotNull(result)
            logger.info("refreshJWT result: jwt=${result.jwt?.take(20)}...")
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("Skipping testRefreshJWT: insufficient permissions - ${e.message}")
        }
    }

    @Test
    suspend fun testRefreshJWTWithExplicitJwt() {
        try {
            val existingJwt = session.jwt
            if (existingJwt != null) {
                val result = api.auth.refreshJWT(jwt = existingJwt)
                assertNotNull(result)
                logger.info("refreshJWT with explicit JWT result: jwt=${result.jwt?.take(20)}...")
            } else {
                logger.warning("Skipping testRefreshJWTWithExplicitJwt: no JWT available in session")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("Skipping testRefreshJWTWithExplicitJwt: insufficient permissions - ${e.message}")
        }
    }

    @Test
    suspend fun testGetJWK() {
        val result = api.auth.getJWK()
        assertNotNull(result)
        assertNotNull(result.keys, "JWK keys should not be null")
        logger.info("getJWK result: keys count=${result.keys.size}")
        for (key in result.keys) {
            logger.info("  key: kty=${key.kty}, kid=${key.kid}, alg=${key.algorithm}")
            assertNotNull(key.kty, "JWK key type should not be null")
        }
    }

    @Test
    suspend fun testGetLanguageCode() {
        try {
            val result = api.auth.getLanguageCode()
            assertNotNull(result)
            assertNotNull(result.languageCode, "Language code should not be null")
            assertTrue(result.languageCode.isNotBlank(), "Language code should not be blank")
            logger.info("getLanguageCode result: languageCode=${result.languageCode}")
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("Skipping testGetLanguageCode: insufficient permissions - ${e.message}")
        }
    }

    @Test
    suspend fun testSetLanguageCode() {
        try {
            val original = api.auth.getLanguageCode()
            val originalCode = original.languageCode
            logger.info("Original language code: $originalCode")

            try {
                val result = api.auth.setLanguageCode(sessionLanguageCode = originalCode)
                assertNotNull(result)
                assertNotNull(result.languageCode, "Returned language code should not be null")
                logger.info("setLanguageCode result: languageCode=${result.languageCode}")
            } finally {
                // Restore original language code
                api.auth.setLanguageCode(sessionLanguageCode = originalCode)
                logger.info("Restored language code to: $originalCode")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("Skipping testSetLanguageCode: insufficient permissions - ${e.message}")
        }
    }

    @Test
    suspend fun testGetCacheParameters() {
        try {
            val result = api.auth.getCacheParameters()
            assertNotNull(result)
            logger.info("getCacheParameters: httpCacheEnable=${result.httpCacheEnable}, serverCacheEnable=${result.serverCacheEnable}")
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("Skipping testGetCacheParameters: insufficient permissions - ${e.message}")
        }
    }

    @Test
    suspend fun testSetCacheParameters() {
        try {
            val original = api.auth.getCacheParameters()
            logger.info("Original cache parameters: httpCacheEnable=${original.httpCacheEnable}, serverCacheEnable=${original.serverCacheEnable}")

            try {
                val result = api.auth.setCacheParameters(original)
                assertNotNull(result)
                logger.info("setCacheParameters result: httpCacheEnable=${result.httpCacheEnable}, serverCacheEnable=${result.serverCacheEnable}")
            } finally {
                // Restore original cache parameters
                api.auth.setCacheParameters(
                    Esse3CacheInfo(
                        httpCacheEnable = original.httpCacheEnable,
                        serverCacheEnable = original.serverCacheEnable
                    )
                )
                logger.info("Restored cache parameters")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("Skipping testSetCacheParameters: insufficient permissions - ${e.message}")
        }
    }

    @Test
    @Disabled("Requires TECHNICAL_USER permission - checkSessionId requires a valid session ID of another user")
    suspend fun testCheckSessionId() {
        try {
            val sessionId = session.authToken
            api.auth.checkSessionId(sessionId)
            logger.info("checkSessionId succeeded for sessionId=${sessionId.take(10)}...")
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("Skipping testCheckSessionId: insufficient permissions - ${e.message}")
        } catch (e: Esse3ValidationException) {
            logger.warning("Skipping testCheckSessionId: validation error - ${e.message}")
        }
    }
}

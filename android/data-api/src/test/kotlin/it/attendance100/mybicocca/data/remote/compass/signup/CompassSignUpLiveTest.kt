package it.attendance100.mybicocca.data.remote.compass.signup

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import kotlin.test.assertContains
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Drives the real sign-up pages as far as they go without creating an account: the form, a captcha,
 * the email check, and a submission Compass refuses (wrong captcha, no password).
 */
@Tag("live")
class CompassSignUpLiveTest {

    @Test
    fun `the sign-up pages still answer the way the client reads them`() = runBlocking {
        CompassSignUpClient().use { client ->
            val form = client.start()
            assertTrue(form.emailCheckKey != null, "the email check key is gone from the page")

            val png = client.captcha()
            assertTrue(png.size > 8 && png[1] == 'P'.code.toByte(), "the captcha is not a PNG")

            assertIs<CompassEmailCheck.Usable>(client.checkEmail("mybicocca.check.${System.currentTimeMillis()}@example.org"))

            val refused = assertIs<CompassSignUpReply.Refused>(
                client.submit(
                    CompassSignUpInput(
                        firstName = "Prova",
                        lastName = "Verifica",
                        email = "mybicocca.check.${System.currentTimeMillis()}@example.org",
                        password = "",
                        passwordConfirm = "",
                        captcha = "WRONG",
                    ),
                ),
            )
            assertContains(refused.reasons, "Captcha non valido.")
        }
    }
}

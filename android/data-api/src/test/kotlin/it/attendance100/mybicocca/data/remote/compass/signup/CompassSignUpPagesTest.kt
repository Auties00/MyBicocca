package it.attendance100.mybicocca.data.remote.compass.signup

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/** The sign-up page readers, on pages saved from a real sign-up (a disposable address). */
class CompassSignUpPagesTest {

    private fun page(name: String): String =
        checkNotNull(javaClass.getResource("/compass/signup/$name")) { "missing fixture $name" }.readText()

    @Test
    fun `the form page gives its action, hidden fields and email-check key`() {
        val form = CompassSignUpPages.parseForm(page("register.html"))
        assertEquals("https://unibicocca.compasscloud.it/area-riservata/registration", form.action)
        assertEquals("100", form.hiddenFields["strutture_id"])
        assertTrue("in_app_view" in form.hiddenFields)
        assertEquals("yP8ceh9D6kpM3RX", form.emailCheckKey)
    }

    @Test
    fun `a page without the form is unavailable`() {
        assertThrows<CompassSignUpUnavailable> { CompassSignUpPages.parseForm(page("activated.html")) }
    }

    @Test
    fun `a refused submission lists Compass's reasons`() {
        val reply = assertIs<CompassSignUpReply.Refused>(CompassSignUpPages.parseSubmitReply(page("refused.html")))
        assertEquals("Captcha non valido.", reply.reasons.first())
        assertTrue(reply.reasons.any { it.startsWith("La password non rispetta") })
    }

    @Test
    fun `an accepted submission waits for the emailed code`() {
        val reply = CompassSignUpPages.parseSubmitReply(page("needs_activation.html"))
        assertEquals(CompassSignUpReply.NeedsActivation("tsbhjefzloyykikfap@vtmpj.net"), reply)
    }

    @Test
    fun `activation succeeds or explains why not`() {
        assertEquals(CompassActivationReply.Activated, CompassSignUpPages.parseActivationReply(page("activated.html")))
        val failed = assertIs<CompassActivationReply.Failed>(CompassSignUpPages.parseActivationReply(page("activation_failed.html")))
        assertTrue("attivazione" in failed.message, failed.message)
    }

    @Test
    fun `an unknown activation reply is unavailable`() {
        assertThrows<CompassSignUpUnavailable> { CompassSignUpPages.parseActivationReply(page("register.html")) }
    }

    @Test
    fun `the email check reads success and Compass's refusal as text`() {
        assertEquals(
            CompassEmailCheck.Usable,
            CompassSignUpPages.parseEmailCheck("""{"status":"success","messaggio":"Email corretta."}"""),
        )
        val taken = CompassSignUpPages.parseEmailCheck(
            """{"status":"error","messaggio":"<strong style=\"color:red\"><i class=\"fa fa-warning fa-fw\"><\/i> La mail specificata è già stata utilizzata.<\/strong>"}""",
        )
        assertEquals(CompassEmailCheck.Refused("La mail specificata è già stata utilizzata."), taken)
    }
}

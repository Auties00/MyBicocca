package it.attendance100.mybicocca.demo.repository

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import it.attendance100.mybicocca.demo.DemoMensa
import it.attendance100.mybicocca.domain.model.mensa.MensaActivationResult
import it.attendance100.mybicocca.domain.model.mensa.MensaEmailCheck
import it.attendance100.mybicocca.domain.model.mensa.MensaSignUpInput
import it.attendance100.mybicocca.domain.model.mensa.MensaSignUpResult
import it.attendance100.mybicocca.domain.repository.MensaSignUpRepository
import kotlinx.coroutines.delay
import java.io.ByteArrayOutputStream
import javax.inject.Inject

/**
 * Offline sign-up for the demo: the captcha always reads [CAPTCHA], the emailed code is [CODE], and
 * the demo student's own address is taken.
 */
class DemoMensaSignUpRepository @Inject constructor() : MensaSignUpRepository {

    override suspend fun start() = delay(300)

    override suspend fun captcha(): ByteArray {
        val bitmap = Bitmap.createBitmap(200, 80, Bitmap.Config.ARGB_8888)
        Canvas(bitmap).apply {
            drawColor(Color.WHITE)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                textSize = 44f
                typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD_ITALIC)
            }
            CAPTCHA.forEachIndexed { i, c ->
                paint.color = listOf(0xFF8E24AA, 0xFF1E88E5, 0xFF43A047, 0xFFE53935, 0xFFFB8C00)[i % 5].toInt()
                drawText(c.toString(), 24f + i * 32f, 54f + (i % 2) * 8f, paint)
            }
        }
        return ByteArrayOutputStream().use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            out.toByteArray()
        }
    }

    override suspend fun checkEmail(email: String): MensaEmailCheck {
        delay(200)
        return if (email.trim().equals(DemoMensa.session.email, ignoreCase = true)) {
            MensaEmailCheck.Refused("La mail specificata è già stata utilizzata per registrare un altro account.")
        } else {
            MensaEmailCheck.Usable
        }
    }

    override suspend fun submit(input: MensaSignUpInput): MensaSignUpResult {
        delay(500)
        return if (!input.captcha.trim().equals(CAPTCHA, ignoreCase = true)) {
            MensaSignUpResult.Refused(listOf("Captcha non valido."))
        } else {
            MensaSignUpResult.NeedsCode(input.email.trim())
        }
    }

    override suspend fun activate(email: String, code: String): MensaActivationResult {
        delay(400)
        return if (code.trim() == CODE) {
            MensaActivationResult.Activated
        } else {
            MensaActivationResult.Failed("L'attivazione del tuo account non è andata a buon fine.")
        }
    }

    override fun close() = Unit

    companion object {
        const val CAPTCHA = "DEMO7"
        const val CODE = "123456"
    }
}

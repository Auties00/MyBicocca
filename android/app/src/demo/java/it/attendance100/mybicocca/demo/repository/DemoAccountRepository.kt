package it.attendance100.mybicocca.demo.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import dagger.hilt.android.qualifiers.ApplicationContext
import it.attendance100.mybicocca.demo.DemoStudent
import it.attendance100.mybicocca.domain.model.account.Account
import it.attendance100.mybicocca.domain.model.account.AccountEvent
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.account.SignInResult
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.repository.AccountRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/** Always signed in as [DemoStudent]; signing out or in again just lands back on that account. */
@Singleton
class DemoAccountRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) : AccountRepository {

    private val account = MutableStateFlow(DemoStudent.account)

    override fun observeActiveAccount(): Flow<Account?> = account

    override fun observeAccounts(): Flow<List<Account>> = account.map { listOf(it) }

    override fun observeEvents(): Flow<AccountEvent> = emptyFlow()

    override suspend fun signIn(username: String, password: String): SignInResult =
        SignInResult.Success(account.value, requiresCareerPick = false)

    override suspend fun signOut(accountId: AccountId) = Unit

    override suspend fun switchAccount(accountId: AccountId) = Unit

    override suspend fun selectCareer(accountId: AccountId, careerId: CareerId) {
        val current = account.value
        if (current.academic.careers.none { it.id == careerId }) return
        account.value = current.copy(academic = current.academic.copy(selectedCareerId = careerId))
    }

    override suspend fun refreshCareers(accountId: AccountId) = Unit

    override suspend fun getUserPhoto(accountId: AccountId): String = withContext(Dispatchers.IO) {
        val file = File(context.cacheDir, "demo_photo.png")
        if (!file.exists()) file.outputStream().use { initialsAvatar().compress(Bitmap.CompressFormat.PNG, 100, it) }
        file.absolutePath
    }

    // Stands in for the Esse3 photo: drop a real portrait in its place if the avatar looks too plain.
    private fun initialsAvatar(): Bitmap {
        val size = 512
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(0xFFA71E3B.toInt())
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFFFFFFF.toInt()
            textSize = size * 0.42f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val initials = "${DemoStudent.FIRST_NAME.first()}${DemoStudent.LAST_NAME.first()}"
        canvas.drawText(initials, size / 2f, size / 2f - (paint.descent() + paint.ascent()) / 2f, paint)
        return bitmap
    }
}

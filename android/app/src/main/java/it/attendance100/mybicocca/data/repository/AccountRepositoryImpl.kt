package it.attendance100.mybicocca.data.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import io.ktor.utils.io.jvm.javaio.toInputStream
import it.attendance100.mybicocca.data.auth.SessionManager
import it.attendance100.mybicocca.domain.model.account.Account
import it.attendance100.mybicocca.domain.model.account.AccountEvent
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.account.SignInResult
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.repository.AccountRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Thin adapter exposing SessionManager's account state and operations as the domain repository
 * contract, plus a local file cache for Esse3 profile photos.
 */
@Singleton
class AccountRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val sessionManager: SessionManager,
) : AccountRepository {

    override fun observeActiveAccount(): Flow<Account?> = sessionManager.activeAccount

    override fun observeAccounts(): Flow<List<Account>> = sessionManager.accounts

    override fun observeEvents(): Flow<AccountEvent> = sessionManager.events

    override suspend fun signIn(username: String, password: String): SignInResult =
        sessionManager.signIn(username, password)

    override suspend fun signOut(accountId: AccountId) =
        sessionManager.signOut(accountId)

    override suspend fun switchAccount(accountId: AccountId) =
        sessionManager.switchAccount(accountId)

    override suspend fun selectCareer(accountId: AccountId, careerId: CareerId) =
        sessionManager.selectCareer(accountId, careerId)

    override suspend fun refreshCareers(accountId: AccountId) =
        sessionManager.refreshCareers(accountId)

    /**
     * Streams the photo bytes directly into a cacheDir file so they never sit on the JVM heap,
     * and returns the file path; an existing non-empty file is reused without a network call.
     * Coil keys its memory cache on the path, so the decoded bitmap survives recomposition.
     */
    override suspend fun getUserPhoto(accountId: AccountId): String {
        val file = File(context.cacheDir, "$AVATAR_DIR/${accountId.value}.bin")
        if (file.exists() && file.length() > 0) return file.absolutePath

        file.parentFile?.mkdirs()
        val channel = sessionManager.streamPersonPhoto(accountId)
        withContext(Dispatchers.IO) {
            channel.toInputStream().use { input ->
                file.outputStream().use { output -> input.copyTo(output) }
            }
        }
        return file.absolutePath
    }

    private companion object {
        const val AVATAR_DIR = "avatars"
    }
}

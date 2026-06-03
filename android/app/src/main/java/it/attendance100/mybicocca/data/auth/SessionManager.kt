package it.attendance100.mybicocca.data.auth

import io.ktor.utils.io.ByteReadChannel
import it.attendance100.mybicocca.data.local.account.AccountDao
import it.attendance100.mybicocca.data.local.account.AccountWithCareers
import it.attendance100.mybicocca.data.local.credentials.AccountCredentials
import it.attendance100.mybicocca.data.local.credentials.CredentialsStore
import it.attendance100.mybicocca.data.local.settings.ActiveAccountStore
import it.attendance100.mybicocca.data.mapper.account.buildAcademicIdentity
import it.attendance100.mybicocca.data.mapper.account.composeDisplayName
import it.attendance100.mybicocca.data.mapper.account.toDomain
import it.attendance100.mybicocca.data.mapper.account.toEntity
import it.attendance100.mybicocca.data.mapper.account.toLearningIdentity
import it.attendance100.mybicocca.data.remote.elearning.api.ElearningApi
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningGetSiteInfoResponse
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningLoginResponse
import it.attendance100.mybicocca.data.remote.esse3.api.Esse3Api
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3CacheInfo
import it.attendance100.mybicocca.di.ApplicationScope
import it.attendance100.mybicocca.domain.model.account.Account
import it.attendance100.mybicocca.domain.model.account.AccountEvent
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.account.SignInFailure
import it.attendance100.mybicocca.domain.model.account.SignInResult
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.career.isSelectable
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.IOException
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@OptIn(ExperimentalCoroutinesApi::class)
@Singleton
class SessionManager @Inject constructor(
    private val accountDao: AccountDao,
    private val activeAccountStore: ActiveAccountStore,
    private val credentialsStore: CredentialsStore,
    private val sessionCache: SessionCache,
    private val esse3Factory: Esse3ApiFactory,
    private val elearningApiFactory: ElearningApiFactory,
    private val authMutexes: AccountKeyedMutexes,
    private val careerReconciler: CareerReconciler,
    private val accountWiper: AccountWiper,
    @ApplicationScope private val scope: CoroutineScope,
) {

    // MoodleSession is baked into ElearningApi at construction, so account switches
    // require a fresh instance to avoid one account's cookie leaking into another's.
    private val elearningApiMutex = Mutex()
    private var elearningApi: ElearningApi? = null
    private var elearningApiAccountId: AccountId? = null

    private val esse3Apis = mutableMapOf<AccountId, Esse3Api>()
    private val esse3Mutex = Mutex()

    private val _events = MutableSharedFlow<AccountEvent>(replay = 0, extraBufferCapacity = 16)
    val events: Flow<AccountEvent> = _events.asSharedFlow()

    val activeAccount: StateFlow<Account?> = activeAccountStore.activeAccountId
        .flatMapLatest { id ->
            if (id == null) flowOf(null)
            else accountDao.observeById(id.value).map { it?.toDomain() }
        }
        .stateIn(scope, SharingStarted.Eagerly, null)

    val accounts: StateFlow<List<Account>> = accountDao.observeAll()
        .map { rows -> rows.map(AccountWithCareers::toDomain) }
        .stateIn(scope, SharingStarted.WhileSubscribed(STATE_KEEP_ALIVE_MS), emptyList())

    init {
        scope.launch {
            activeAccount
                .filterNotNull()
                .distinctUntilChanged { a, b -> a.id == b.id }
                .collect { account -> warmupElearningSilently(account.id) }
        }
    }

    suspend fun signIn(username: String, password: String): SignInResult = coroutineScope {
        if (username.isBlank() || password.isBlank()) {
            return@coroutineScope SignInResult.Failure(SignInFailure.BadCredentials)
        }

        // Students typically know themselves by the short username (e.g. `l.lupi3`)
        // rather than the full `l.lupi3@campus.unimib.it`. Append the campus domain
        // when missing so both Esse3 (HTTP Basic) and Elearning (SAML j_username)
        // see the form they expect. An input that already contains `@` is passed
        // through verbatim — staff/PhD addresses on other unimib subdomains keep
        // working, and we never silently rewrite what the user typed.
        val normalizedUsername = normalizeUsername(username)
        val tempCredentials = AccountCredentials(normalizedUsername, password)
        // accountId comes from the SAML identity, so the retry callbacks await it through
        // this deferred — a 401 during signIn is bad credentials, not an expired session.
        val accountIdDeferred = CompletableDeferred<AccountId>()
        val esse3 = buildEsse3Api(accountIdDeferred, tempCredentials)

        val esse3Deferred = async {
            runCatching {
                val session = esse3.auth.login()
                val careersDto = esse3.careers.getCareers()
                session to careersDto
            }
        }
        // accountId is unknown until login resolves; the api is adopted afterwards.
        val freshElearningApi = elearningApiFactory.create()
        val elearningDeferred = async {
            runCatching {
                when (val response = freshElearningApi.auth.login(normalizedUsername, password)) {
                    is ElearningLoginResponse.Success -> {
                        val siteInfo = freshElearningApi.site.getSiteInfo(response.wsToken)
                        ElearningLoginOutcome(response.wsToken, response.moodleSessionCookie, siteInfo)
                    }
                    is ElearningLoginResponse.Error -> error(response.message)
                }
            }
        }

        val esse3Result = esse3Deferred.await()
        val elearningResult = elearningDeferred.await()

        if (esse3Result.isFailure || elearningResult.isFailure) {
            esse3.close()
            freshElearningApi.close()
            return@coroutineScope SignInResult.Failure(
                classifySignInFailure(
                    esse3Result.exceptionOrNull(),
                    elearningResult.exceptionOrNull(),
                ),
            )
        }

        val (esse3Session, esse3Careers) = esse3Result.getOrThrow()
        val elearning = elearningResult.getOrThrow()
        val siteInfo = elearning.siteInfo

        val academic = buildAcademicIdentity(esse3Session, esse3Careers)
        val learning = siteInfo.toLearningIdentity()
        val now = Instant.now()
        val existing = accountDao.findByRecordUserId(academic.recordUserId)
        val accountId = existing?.let { AccountId(it.account.id) } ?: AccountId(UUID.randomUUID().toString())
        val createdAt = existing?.let { Instant.ofEpochMilli(it.account.createdAtEpochMillis) } ?: now

        val account = Account(
            id = accountId,
            username = normalizedUsername,
            displayName = composeDisplayName(esse3Session),
            academic = academic,
            learning = learning,
            createdAt = createdAt,
            lastUsedAt = now,
            lastSyncedAt = now,
        )

        accountDao.upsertAccount(account.toEntity())
        accountDao.replaceCareers(
            accountId.value,
            academic.careers.map { it.toEntity(accountId) },
        )
        credentialsStore.save(accountId, tempCredentials)
        sessionCache.update(accountId) {
            copy(
                wsToken = elearning.wsToken,
                moodleSessionCookie = elearning.moodleSessionCookie,
                jwt = esse3Session.jwt,
            )
        }
        activeAccountStore.set(accountId)

        // Completes the signIn-time api's retry callbacks, which were awaiting accountId.
        accountIdDeferred.complete(accountId)
        esse3Mutex.withLock {
            esse3Apis.remove(accountId)?.close()
            esse3Apis[accountId] = esse3
        }
        adoptElearningApi(accountId, freshElearningApi)

        val requiresPick = academic.careers.count { it.status.isSelectable } > 1
        SignInResult.Success(account, requiresCareerPick = requiresPick)
    }

    suspend fun signOut(accountId: AccountId) {
        authMutexes.withLock(accountId) {
            esse3Mutex.withLock {
                esse3Apis.remove(accountId)?.close()
            }
            credentialsStore.delete(accountId)
            sessionCache.delete(accountId)
            accountWiper.wipe(accountId)
            accountDao.delete(accountId.value)
            val active = activeAccountStore.activeAccountId.first()
            if (active == accountId) {
                val remaining = accountDao.observeAll().first().firstOrNull()
                activeAccountStore.set(remaining?.let { AccountId(it.account.id) })
            }
        }
        authMutexes.forget(accountId)
    }

    suspend fun switchAccount(accountId: AccountId) {
        accountDao.getById(accountId.value) ?: error("Account ${accountId.value} not found")
        esse3Mutex.withLock {
            val toClose = esse3Apis.filterKeys { it != accountId }
            toClose.values.forEach { it.close() }
            esse3Apis.keys.removeAll(toClose.keys)
        }
        activeAccountStore.set(accountId)
        accountDao.updateLastUsed(accountId.value, System.currentTimeMillis())
    }

    suspend fun selectCareer(accountId: AccountId, careerId: CareerId) {
        val row = accountDao.getById(accountId.value) ?: error("Account ${accountId.value} not found")
        require(row.careers.any { it.id == careerId.value }) {
            "Career ${careerId.value} is not part of account ${accountId.value}."
        }
        accountDao.updateSelectedCareer(accountId.value, careerId.value, System.currentTimeMillis())
    }

    suspend fun refreshCareers(accountId: AccountId) {
        authMutexes.withLock(accountId) {
            val current = accountDao.getById(accountId.value) ?: return@withLock
            val esse3 = esse3ForAccount(accountId)
            val careersDto = esse3.careers.getCareers()
            val session = esse3.auth.login()
            val refreshed = buildAcademicIdentity(session, careersDto)
            val previous = current.toDomain().academic.careers
            val now = System.currentTimeMillis()
            accountDao.replaceCareers(
                accountId.value,
                refreshed.careers.map { it.toEntity(accountId) },
            )
            accountDao.updateLastSynced(accountId.value, now)
            careerReconciler.reconcile(
                accountId = accountId,
                previous = previous,
                current = refreshed.careers,
                currentSelectedId = CareerId(current.account.selectedCareerId),
            ).forEach(_events::tryEmit)
        }
    }

    suspend fun esse3(): Esse3Api {
        val active = activeAccount.value ?: error("No active account.")
        return esse3ForAccount(active.id)
    }

    suspend fun streamPersonPhoto(accountId: AccountId): ByteReadChannel {
        val row = accountDao.getById(accountId.value)
            ?: error("Account ${accountId.value} not found.")
        return esse3ForAccount(accountId).personalData.getPersonPhoto(row.account.personId)
    }

    suspend fun elearning(): ElearningSession {
        val active = activeAccount.value ?: error("No active account.")
        val cached = sessionCache.read(active.id)
        val tokens = if (cached.wsToken != null) {
            ElearningTokens(cached.wsToken, cached.moodleSessionCookie)
        } else {
            refreshElearningTokens(active.id)
        }
        val api = ensureElearningApiForAccount(active.id, tokens.moodleSessionCookie)
        return ElearningSession(api, tokens.wsToken)
    }

    /**
     * Forces a fresh Elearning login for the active account, updating both the cached
     * `wsToken` and the `MoodleSession` cookie. Use after an
     * [it.attendance100.mybicocca.data.remote.elearning.dto.ElearningKalturaVideoStreamResponse.RequiresReauth]
     * (or equivalent) signal — Moodle has expired the browser session and only a full
     * SAML round-trip can mint a new one.
     */
    suspend fun reauthElearning(): ElearningSession {
        val active = activeAccount.value ?: error("No active account.")
        sessionCache.update(active.id) { copy(wsToken = null, moodleSessionCookie = null) }
        discardElearningApi()
        val tokens = refreshElearningTokens(active.id)
        val api = ensureElearningApiForAccount(active.id, tokens.moodleSessionCookie)
        return ElearningSession(api, tokens.wsToken)
    }

    private suspend fun esse3ForAccount(accountId: AccountId): Esse3Api = esse3Mutex.withLock {
        esse3Apis[accountId]?.let { return@withLock it }
        val creds = credentialsStore.read(accountId)
            ?: error("No stored credentials for ${accountId.value}.")
        val api = buildEsse3Api(CompletableDeferred(accountId), creds)
        esse3Apis[accountId] = api
        scope.launch {
            runCatching {
                api.auth.setCacheParameters(
                    Esse3CacheInfo(httpCacheEnable = 1, serverCacheEnable = 1)
                )
            }
        }
        api
    }

    // `lateinit` so the retry callback can reference the api itself; the lambda fires
    // only on a real 401, by which time the field is populated. Post-signIn callers
    // pass an already-completed deferred.
    private fun buildEsse3Api(
        accountIdDeferred: Deferred<AccountId>,
        credentials: AccountCredentials,
    ): Esse3Api {
        lateinit var api: Esse3Api
        api = esse3Factory.create(
            credentials = credentials,
            refresh = {
                accountIdDeferred.takeIfCompleted()?.let { accountId ->
                    val session = api.auth.login()
                    sessionCache.update(accountId) { copy(jwt = session.jwt) }
                }
            },
            onReauthRequired = { cause ->
                accountIdDeferred.takeIfCompleted()?.let { accountId ->
                    _events.tryEmit(AccountEvent.RequireReauth(accountId, cause))
                }
            },
        )
        return api
    }

    private fun <T> Deferred<T>.takeIfCompleted(): T? = if (isCompleted) getCompleted() else null

    private suspend fun refreshElearningTokens(accountId: AccountId): ElearningTokens =
        authMutexes.withLock(accountId) {
            val cached = sessionCache.read(accountId)
            if (cached.wsToken != null) {
                return@withLock ElearningTokens(cached.wsToken, cached.moodleSessionCookie)
            }
            val creds = credentialsStore.read(accountId)
                ?: error("No stored credentials for ${accountId.value}.")
            val freshApi = elearningApiFactory.create()
            val response = try {
                freshApi.auth.login(creds.username, creds.password)
            } catch (t: Throwable) {
                freshApi.close()
                throw t
            }
            val (wsToken, cookie) = when (response) {
                is ElearningLoginResponse.Success -> response.wsToken to response.moodleSessionCookie
                is ElearningLoginResponse.Error -> {
                    freshApi.close()
                    error("Elearning re-auth failed: ${response.message}")
                }
            }
            sessionCache.update(accountId) {
                copy(wsToken = wsToken, moodleSessionCookie = cookie)
            }
            adoptElearningApi(accountId, freshApi)
            ElearningTokens(wsToken, cookie)
        }

    private suspend fun warmupElearningSilently(accountId: AccountId) {
        runCatching {
            val cached = sessionCache.read(accountId)
            if (cached.wsToken == null) {
                refreshElearningTokens(accountId)
            } else {
                ensureElearningApiForAccount(accountId, cached.moodleSessionCookie)
            }
        }
    }

    private suspend fun ensureElearningApiForAccount(
        accountId: AccountId,
        cookie: String?,
    ): ElearningApi = elearningApiMutex.withLock {
        val existing = elearningApi
        if (existing != null && elearningApiAccountId == accountId) return@withLock existing
        existing?.close()
        val newApi = elearningApiFactory.create(cookie)
        elearningApi = newApi
        elearningApiAccountId = accountId
        newApi
    }

    private suspend fun adoptElearningApi(accountId: AccountId, api: ElearningApi) =
        elearningApiMutex.withLock {
            if (elearningApi !== api) elearningApi?.close()
            elearningApi = api
            elearningApiAccountId = accountId
        }

    private suspend fun discardElearningApi() = elearningApiMutex.withLock {
        elearningApi?.close()
        elearningApi = null
        elearningApiAccountId = null
    }

    private data class ElearningLoginOutcome(
        val wsToken: String,
        val moodleSessionCookie: String?,
        val siteInfo: ElearningGetSiteInfoResponse,
    )

    private data class ElearningTokens(
        val wsToken: String,
        val moodleSessionCookie: String?,
    )

    private companion object {
        const val STATE_KEEP_ALIVE_MS = 5_000L
        const val DEFAULT_USERNAME_DOMAIN = "campus.unimib.it"

        /** Adds the campus domain when the user typed only the short form (no `@`). */
        fun normalizeUsername(raw: String): String {
            val trimmed = raw.trim()
            return if (trimmed.contains('@')) trimmed else "$trimmed@$DEFAULT_USERNAME_DOMAIN"
        }

        /**
         * Collapses raw backend throwables into a UI-friendly [SignInFailure].
         *
         * The data-api `ApiRequestException`/`Esse3Exception` types live in a module
         * that isn't on `:app`'s compile classpath, so we sniff stable text in the
         * generated messages instead of type-checking. Both probes are anchored to
         * strings emitted by code in this repository (`Esse3Exception.buildMessage`
         * and the SAML "missing form at step three" branch in `ElearningAuthApi`).
         */
        fun classifySignInFailure(esse3: Throwable?, elearning: Throwable?): SignInFailure {
            if (looksLikeBadCredentials(esse3) || looksLikeBadCredentials(elearning)) {
                return SignInFailure.BadCredentials
            }
            if (looksLikeNetworkFailure(esse3) || looksLikeNetworkFailure(elearning)) {
                return SignInFailure.NoConnection
            }
            return SignInFailure.Unknown
        }

        private fun looksLikeBadCredentials(t: Throwable?): Boolean {
            var cur: Throwable? = t
            while (cur != null) {
                val msg = cur.message
                if (msg != null) {
                    // Esse3Exception multi-line message: "Status code: 401" on its own line.
                    if ("Status code: 401" in msg) return true
                    // Esse3 retErrMsg verbatim when login is refused.
                    if ("credenziali" in msg.lowercase()) return true
                    // Elearning SAML rejects bad creds by re-rendering the login page
                    // without the auto-submit form — the auth flow then bails out here.
                    if ("missing form at step three" in msg) return true
                }
                cur = cur.cause
            }
            return false
        }

        private fun looksLikeNetworkFailure(t: Throwable?): Boolean {
            var cur: Throwable? = t
            while (cur != null) {
                if (cur is IOException) return true
                cur = cur.cause
            }
            return false
        }
    }
}

data class ElearningSession(
    val api: ElearningApi,
    val wsToken: String,
)

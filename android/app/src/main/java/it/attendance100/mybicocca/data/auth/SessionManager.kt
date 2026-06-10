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
import it.attendance100.mybicocca.data.remote.esse3.scraper.api.Esse3Api as Esse3LegacyApi
import io.ktor.http.Cookie
import io.ktor.http.CookieEncoding
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
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.IOException
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Owns every authenticated session. Persists accounts and careers at sign-in, exposes the
 * active-account and account-list streams, and builds, caches, and re-authenticates the
 * per-platform API clients: Esse3 REST (HTTP Basic plus JWT), Moodle (wstoken plus
 * `MoodleSession` cookie), and the legacy Esse3 web-SSO scrape session. Per-account auth work
 * is serialized through [AccountKeyedMutexes]; each client cache is guarded by its own mutex.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@Singleton
class SessionManager @Inject constructor(
    private val accountDao: AccountDao,
    private val activeAccountStore: ActiveAccountStore,
    private val credentialsStore: CredentialsStore,
    private val sessionCache: SessionCache,
    private val esse3Factory: Esse3ApiFactory,
    private val esse3LegacyApiFactory: Esse3LegacyApiFactory,
    private val elearningApiFactory: ElearningApiFactory,
    private val authMutexes: AccountKeyedMutexes,
    private val careerReconciler: CareerReconciler,
    private val accountWiper: AccountWiper,
    @ApplicationScope private val scope: CoroutineScope,
) {

    private val elearningApiMutex = Mutex()

    /**
     * Single cached Moodle client. The `MoodleSession` cookie is baked into ElearningApi at
     * construction, so each account switch builds a fresh instance — reusing one across
     * accounts would leak one account's cookie into another's calls.
     */
    private var elearningApi: ElearningApi? = null
    private var elearningApiAccountId: AccountId? = null

    private val esse3Apis = mutableMapOf<AccountId, Esse3Api>()
    private val esse3Mutex = Mutex()

    /**
     * Legacy Esse3 web-SSO (scrape) sessions, keyed per account. Each is established lazily on
     * the first access of a feature that needs the scrape surface and kept entirely independent
     * of the REST [esse3] / [elearning] paths, so a SAML failure here can never break sign-in.
     * Cookies are minted once via SAML and cached both in memory and in [SessionCache]; the api
     * re-logs in on expiry.
     */
    private val esse3LegacyApis = mutableMapOf<AccountId, Esse3LegacyApi>()
    private val esse3LegacyMutex = Mutex()

    /**
     * Account lifecycle events for the single UI consumer. A buffered [Channel] rather than a
     * replay-0 SharedFlow: it queues events emitted while nobody is collecting (app backgrounded,
     * UI not yet attached) so a [AccountEvent.RequireReauth] or career change reaches the consumer
     * when it subscribes instead of being silently dropped.
     */
    private val _events = Channel<AccountEvent>(Channel.BUFFERED)
    val events: Flow<AccountEvent> = _events.receiveAsFlow()

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

    /**
     * Runs the Esse3 and Moodle logins in parallel and persists the account on combined
     * success; any failure closes both throwaway clients and is classified into a
     * [SignInFailure] instead of being thrown.
     *
     * The Esse3 client is built before an account id exists, so its retry callbacks await the
     * id through a deferred completed only after the account is persisted: a 401 during
     * sign-in is bad credentials, not an expired session, and must never enter the silent
     * refresh path. The Moodle client is likewise created up front and adopted as the cached
     * instance once the account id is known.
     */
    suspend fun signIn(username: String, password: String): SignInResult = coroutineScope {
        if (username.isBlank() || password.isBlank()) {
            return@coroutineScope SignInResult.Failure(SignInFailure.BadCredentials)
        }

        val normalizedUsername = normalizeUsername(username)
        val tempCredentials = AccountCredentials(normalizedUsername, password)
        val accountIdDeferred = CompletableDeferred<AccountId>()
        val esse3 = buildEsse3Api(accountIdDeferred, tempCredentials)

        val esse3Deferred = async {
            runCatching {
                val session = esse3.auth.login()
                val careersDto = esse3.careers.getCareers()
                session to careersDto
            }
        }
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

        accountIdDeferred.complete(accountId)
        esse3Mutex.withLock {
            esse3Apis.remove(accountId)?.close()
            esse3Apis[accountId] = esse3
        }
        adoptElearningApi(accountId, freshElearningApi)

        val requiresPick = academic.careers.count { it.status.isSelectable } > 1
        SignInResult.Success(account, requiresCareerPick = requiresPick)
    }

    /**
     * Removes the account everywhere: cached clients, credentials, session tokens,
     * account-scoped Room data, and the account row itself. When the active account is the one
     * removed, activation falls back to the most recently used remaining account, or to the
     * signed-out state.
     */
    suspend fun signOut(accountId: AccountId) {
        authMutexes.withLock(accountId) {
            esse3Mutex.withLock {
                esse3Apis.remove(accountId)?.close()
            }
            esse3LegacyMutex.withLock {
                esse3LegacyApis.remove(accountId)?.close()
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

    /** Activates the given saved account and closes every other account's cached clients. */
    suspend fun switchAccount(accountId: AccountId) {
        accountDao.getById(accountId.value) ?: error("Account ${accountId.value} not found")
        esse3Mutex.withLock {
            val toClose = esse3Apis.filterKeys { it != accountId }
            toClose.values.forEach { it.close() }
            esse3Apis.keys.removeAll(toClose.keys)
        }
        esse3LegacyMutex.withLock {
            val toClose = esse3LegacyApis.filterKeys { it != accountId }
            toClose.values.forEach { it.close() }
            esse3LegacyApis.keys.removeAll(toClose.keys)
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

    /**
     * Re-fetches the careers from Esse3, replaces the cached set, stamps the last-synced time,
     * and emits the reconciliation events. Serialized per account; throws on network failure.
     */
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
            ).forEach { _events.trySend(it) }
        }
    }

    /** Esse3 REST client for the active account, built and cached on first use. */
    suspend fun esse3(): Esse3Api {
        val active = activeAccount.value ?: error("No active account.")
        return esse3ForAccount(active.id)
    }

    /**
     * Legacy Esse3 web-SSO scrape session for the active account, established lazily: the
     * first use reseeds from cached cookies when present and otherwise runs the SAML login and
     * caches the resulting cookies. Independent of [esse3] and [elearning].
     */
    suspend fun esse3Legacy(): Esse3LegacyApi {
        val active = activeAccount.value ?: error("No active account.")
        return esse3LegacyForAccount(active.id)
    }

    /**
     * Forces a fresh SAML login for the active account's legacy session, discarding the cached
     * cookies and scrape client. Intended for when the scrape layer raises its
     * AuthenticationException — the session cookie died and Esse3 bounced to the IdP.
     */
    suspend fun reauthEsse3Legacy(): Esse3LegacyApi {
        val active = activeAccount.value ?: error("No active account.")
        esse3LegacyMutex.withLock {
            esse3LegacyApis.remove(active.id)?.close()
        }
        sessionCache.update(active.id) { copy(esse3LegacyCookies = null) }
        return esse3LegacyForAccount(active.id)
    }

    suspend fun streamPersonPhoto(accountId: AccountId): ByteReadChannel {
        val row = accountDao.getById(accountId.value)
            ?: error("Account ${accountId.value} not found.")
        return esse3ForAccount(accountId).personalData.getPersonPhoto(row.account.personId)
    }

    /**
     * Moodle session for the active account: cached tokens are reused when present, otherwise
     * a fresh login mints them.
     */
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

    /**
     * Single-flighted per account under the auth mutexes, so concurrent callers share one SAML
     * round-trip instead of each kicking off their own.
     */
    private suspend fun esse3LegacyForAccount(accountId: AccountId): Esse3LegacyApi =
        authMutexes.withLock(accountId) {
            esse3LegacyMutex.withLock { esse3LegacyApis[accountId] }?.let { return@withLock it }

            val cached = sessionCache.read(accountId)
            val cookies = deserializeCookies(cached.esse3LegacyCookies)
                ?: run {
                    val creds = credentialsStore.read(accountId)
                        ?: error("No stored credentials for ${accountId.value}.")
                    val fresh = esse3LegacyApiFactory.login(creds.username, creds.password)
                    sessionCache.update(accountId) { copy(esse3LegacyCookies = serializeCookies(fresh)) }
                    fresh
                }

            val api = esse3LegacyApiFactory.create(cookies)
            esse3LegacyMutex.withLock {
                esse3LegacyApis.remove(accountId)?.close()
                esse3LegacyApis[accountId] = api
            }
            api
        }

    /**
     * Builds and caches the REST client on first use; Esse3 server-side caching is enabled in
     * the background as a best-effort optimization.
     */
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

    /**
     * The local api variable is `lateinit` so the refresh callback can call back into the very
     * client it belongs to; the lambda only fires on a real 401, by which time the field is
     * assigned. Callers that already know the account pass an already-completed deferred,
     * while sign-in completes it only once the account is persisted.
     */
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
                    _events.trySend(AccountEvent.RequireReauth(accountId, cause))
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

        /**
         * Appends the campus domain when the user typed only the short form (no `@`): students
         * know themselves by the short username, while the Esse3 HTTP Basic login and the
         * Elearning SAML `j_username` field expect the full e-mail form. Input that already
         * contains `@` passes through verbatim, so staff and PhD addresses on other unimib
         * subdomains keep working and what the user typed is never silently rewritten.
         */
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

        /**
         * Walks the cause chain for the three stable bad-credential signatures: the
         * "Status code: 401" line of an `Esse3Exception` multi-line message, the verbatim
         * Esse3 `retErrMsg` mentioning "credenziali" when the login is refused, and the
         * "missing form at step three" failure the Elearning SAML flow raises when the IdP
         * rejects bad credentials by re-rendering the login page without the auto-submit form.
         */
        private fun looksLikeBadCredentials(t: Throwable?): Boolean {
            var cur: Throwable? = t
            while (cur != null) {
                val msg = cur.message
                if (msg != null) {
                    if ("Status code: 401" in msg) return true
                    if ("credenziali" in msg.lowercase()) return true
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

        private const val COOKIE_FIELD_SEP = "\t"
        private const val COOKIE_RECORD_SEP = "\n"

        /**
         * Serializes the Esse3 web-SSO cookies one per line as
         * `name\tvalue\tdomain\tpath` — only the fields needed to re-seed the scrape client's
         * cookie jar. The persisted form is best-effort (a stale entry just triggers a fresh
         * SAML login), and the cookie names and values involved (`JSESSIONID`,
         * `_shibsession_*`) never contain tabs or newlines.
         */
        fun serializeCookies(cookies: List<Cookie>): String? {
            if (cookies.isEmpty()) return null
            return cookies.joinToString(COOKIE_RECORD_SEP) { c ->
                listOf(c.name, c.value, c.domain.orEmpty(), c.path.orEmpty())
                    .joinToString(COOKIE_FIELD_SEP)
            }
        }

        fun deserializeCookies(raw: String?): List<Cookie>? {
            if (raw.isNullOrBlank()) return null
            val cookies = raw.split(COOKIE_RECORD_SEP).mapNotNull { line ->
                val parts = line.split(COOKIE_FIELD_SEP)
                if (parts.size < 4) return@mapNotNull null
                Cookie(
                    name = parts[0],
                    value = parts[1],
                    encoding = CookieEncoding.RAW,
                    domain = parts[2].ifBlank { null },
                    path = parts[3].ifBlank { null },
                )
            }
            return cookies.ifEmpty { null }
        }
    }
}

/** A ready-to-use Moodle client paired with the wstoken its web-service calls must carry. */
data class ElearningSession(
    val api: ElearningApi,
    val wsToken: String,
)

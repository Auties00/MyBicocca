package it.attendance100.mybicocca.di

import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import it.attendance100.mybicocca.data.local.account.AccountDao
import it.attendance100.mybicocca.data.local.account.MyBicoccaDatabase
import it.attendance100.mybicocca.data.local.calendar.CalendarDao
import it.attendance100.mybicocca.data.local.exam.ExamCacheDao
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import javax.inject.Inject

/**
 * Smoke test for the Hilt dependency graph: boots the `SingletonComponent` under a
 * `HiltTestApplication` and resolves the Room database subgraph ([DatabaseModule]), proving the
 * Hilt code generation, the generated test component, and the `@ApplicationContext`-bound Room
 * providers all wire together end to end.
 *
 * Deliberately scoped to the Room subgraph: the full application graph pulls in hardware-bound
 * bindings (Keystore-backed EncryptedSharedPreferences, ConnectivityManager, MapLibre, Firebase
 * Performance) that a Robolectric unit test is the wrong place to instantiate. This guards the
 * wiring that breaks most often on a refactor — a missing or miss-scoped `@Provides`/`@Binds` —
 * without depending on device hardware.
 */
@RunWith(RobolectricTestRunner::class)
@Config(application = HiltTestApplication::class, sdk = [34])
@HiltAndroidTest
class HiltGraphSmokeTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var database: MyBicoccaDatabase

    @Inject
    lateinit var accountDao: AccountDao

    @Inject
    lateinit var calendarDao: CalendarDao

    @Inject
    lateinit var examCacheDao: ExamCacheDao

    @Before
    fun inject() {
        hiltRule.inject()
    }

    @Test
    fun `Hilt resolves the database and its DAOs`() {
        assertThat(database).isNotNull()
        assertThat(accountDao).isNotNull()
        assertThat(calendarDao).isNotNull()
        assertThat(examCacheDao).isNotNull()
    }

    @Test
    fun `the injected DAOs are backed by the single database instance`() {
        assertThat(database.accountDao()).isNotNull()
        assertThat(database.calendarDao()).isNotNull()
        assertThat(database.examCacheDao()).isNotNull()
    }
}

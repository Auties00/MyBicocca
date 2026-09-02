package it.attendance100.mybicocca.data.local.settings

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import it.attendance100.mybicocca.domain.model.update.AppRelease
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@org.robolectric.annotation.Config(sdk = [33])
class UpdateStateStoreTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher)
    private lateinit var store: UpdateStateStore
    private lateinit var dataStoreFile: File

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        dataStoreFile = context.preferencesDataStoreFile("test_settings")
        val dataStore = PreferenceDataStoreFactory.create(
            scope = testScope,
            produceFile = { dataStoreFile }
        )
        store = UpdateStateStore(dataStore)
    }

    @After
    fun teardown() {
        dataStoreFile.delete()
    }

    @Test
    fun nightlyState_togglesAndClearsCorrectly() = testScope.runTest {
        store.nightlyEnabled.test {
            assertThat(awaitItem()).isFalse()

            store.setNightlyEnabled(true)
            assertThat(awaitItem()).isTrue()

            store.setNightlyEnabled(false)
            assertThat(awaitItem()).isFalse()
        }
    }

    @Test
    fun nightlyState_updatesCorrectly() = testScope.runTest {
        val release = AppRelease(
            versionName = "Nightly",
            title = "Test",
            notes = "",
            pageUrl = "url",
            publishedAt = Instant.now(),
            isPreRelease = true,
            assets = emptyList(),
            commitSha = "abc"
        )

        store.nightlyState.test {
            val initial = awaitItem()
            assertThat(initial.available).isFalse()

            store.setNightlyUpdateAvailable(release, 1000L, "digest1", 2000L)
            
            val updated = awaitItem()
            assertThat(updated.available).isTrue()
            assertThat(updated.release?.commitSha).isEqualTo("abc")
            assertThat(updated.lastSeenPublishedAtMs).isEqualTo(1000L)
            assertThat(updated.lastSeenDigest).isEqualTo("digest1")
            assertThat(updated.lastCheckedAtMs).isEqualTo(2000L)
        }
    }
}

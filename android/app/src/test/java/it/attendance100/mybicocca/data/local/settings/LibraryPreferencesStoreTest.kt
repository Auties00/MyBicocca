package it.attendance100.mybicocca.data.local.settings

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import it.attendance100.mybicocca.domain.model.library.LibraryCatalog
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class LibraryPreferencesStoreTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher)
    private lateinit var dataStoreFile: File
    private lateinit var store: LibraryPreferencesStore

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        dataStoreFile = context.preferencesDataStoreFile("test_library_prefs_${System.nanoTime()}")
        val dataStore = PreferenceDataStoreFactory.create(
            scope = testScope,
            produceFile = { dataStoreFile },
        )
        store = LibraryPreferencesStore(dataStore)
    }

    @After
    fun tearDown() {
        dataStoreFile.delete()
    }

    @Test
    fun `lastUsedLibraryId round-trips and ignores blank values`() = testScope.runTest {
        assertThat(store.lastUsedLibraryId()).isNull()

        store.setLastUsedLibraryId(LibraryCatalog.MEDICINA_ID)
        assertThat(store.lastUsedLibraryId()).isEqualTo(LibraryCatalog.MEDICINA_ID)

        store.setLastUsedLibraryId("   ")
        assertThat(store.lastUsedLibraryId()).isEqualTo(LibraryCatalog.MEDICINA_ID)

        store.setLastUsedLibraryId(LibraryCatalog.CENTRALE_ID)
        assertThat(store.lastUsedLibraryId()).isEqualTo(LibraryCatalog.CENTRALE_ID)
    }
}

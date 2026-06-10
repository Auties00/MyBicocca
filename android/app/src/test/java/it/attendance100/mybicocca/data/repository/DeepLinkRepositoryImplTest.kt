package it.attendance100.mybicocca.data.repository

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import it.attendance100.mybicocca.domain.model.library.LibraryActionKind
import it.attendance100.mybicocca.domain.model.library.LibraryDeepLinkAction
import kotlinx.coroutines.test.runTest
import org.junit.Test

/**
 * The deep-link repository holds pending payloads in sticky [kotlinx.coroutines.flow.MutableStateFlow]s,
 * so a link submitted before its consumer subscribes is replayed on first collection, and
 * consuming clears it back to `null`.
 */
class DeepLinkRepositoryImplTest {

    @Test
    fun `presence scan submitted before collection is replayed`() = runTest {
        val repository = DeepLinkRepositoryImpl()

        repository.submitPresenceScan("https://elearning.unimib.it/attendance?sessid=42")

        repository.observePendingPresenceScan().test {
            assertThat(awaitItem()).isEqualTo("https://elearning.unimib.it/attendance?sessid=42")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `consuming presence scan clears it to null`() = runTest {
        val repository = DeepLinkRepositoryImpl()
        repository.submitPresenceScan("raw")

        repository.observePendingPresenceScan().test {
            assertThat(awaitItem()).isEqualTo("raw")
            repository.consumePresenceScan()
            assertThat(awaitItem()).isNull()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `initial presence scan state is null`() = runTest {
        val repository = DeepLinkRepositoryImpl()

        repository.observePendingPresenceScan().test {
            assertThat(awaitItem()).isNull()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `library action submitted before collection is replayed`() = runTest {
        val repository = DeepLinkRepositoryImpl()
        val action = LibraryDeepLinkAction(LibraryActionKind.Cancel, "token-uuid")

        repository.submitLibraryAction(action)

        repository.observePendingLibraryAction().test {
            assertThat(awaitItem()).isEqualTo(action)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `consuming library action clears it to null`() = runTest {
        val repository = DeepLinkRepositoryImpl()
        repository.submitLibraryAction(LibraryDeepLinkAction(LibraryActionKind.Cancel, "t"))

        repository.observePendingLibraryAction().test {
            assertThat(awaitItem()).isNotNull()
            repository.consumeLibraryAction()
            assertThat(awaitItem()).isNull()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `presence scan and library action are independent channels`() = runTest {
        val repository = DeepLinkRepositoryImpl()

        repository.submitPresenceScan("scan")

        repository.observePendingLibraryAction().test {
            assertThat(awaitItem()).isNull()
            cancelAndIgnoreRemainingEvents()
        }
    }
}

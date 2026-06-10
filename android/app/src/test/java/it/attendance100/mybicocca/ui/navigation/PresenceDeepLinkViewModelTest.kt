package it.attendance100.mybicocca.ui.navigation

import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import it.attendance100.mybicocca.domain.usecase.attendance.ObservePendingPresenceScanUseCase
import it.attendance100.mybicocca.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

/**
 * Coverage for the presence deep-link ViewModel: the pending mod_attendance link is mirrored
 * from the domain stream, defaulting to null when nothing is pending. No deep-link parsing
 * happens here, so only the stream mirror is exercised.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PresenceDeepLinkViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val observePendingPresenceScan: ObservePendingPresenceScanUseCase = mockk()

    private fun viewModel(pending: String?): PresenceDeepLinkViewModel {
        every { observePendingPresenceScan() } returns flowOf(pending)
        return PresenceDeepLinkViewModel(observePendingPresenceScan)
    }

    @Test
    fun `pending mirrors a captured attendance link`() = runTest {
        val link = "https://elearning.unimib.it/mod/attendance/attendance.php?qrpass=abc&sessid=42"
        val vm = viewModel(link)

        assertThat(vm.pending.value).isEqualTo(link)
    }

    @Test
    fun `pending is null when there is no captured scan`() = runTest {
        val vm = viewModel(null)

        assertThat(vm.pending.value).isNull()
    }
}

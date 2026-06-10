package it.attendance100.mybicocca.ui.navigation

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import it.attendance100.mybicocca.domain.model.settings.FileOpenChoice
import it.attendance100.mybicocca.domain.usecase.settings.ClearFileOpenChoiceUseCase
import it.attendance100.mybicocca.domain.usecase.settings.ObserveFileOpenChoicesUseCase
import it.attendance100.mybicocca.domain.usecase.settings.SetFileOpenChoiceUseCase
import it.attendance100.mybicocca.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

/**
 * Coverage for the file-open preference ViewModel: the remembered per-kind choice map mirrors
 * the persisted stream, [FileOpenPreferenceViewModel.remember] forwards a kind and choice to the
 * setter use case, and [FileOpenPreferenceViewModel.forget] forwards the kind to the clear use case.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class FileOpenPreferenceViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val observeFileOpenChoices: ObserveFileOpenChoicesUseCase = mockk()
    private val setFileOpenChoice: SetFileOpenChoiceUseCase = mockk(relaxed = true)
    private val clearFileOpenChoice: ClearFileOpenChoiceUseCase = mockk(relaxed = true)

    private fun viewModel(
        choices: Map<String, FileOpenChoice> = emptyMap(),
    ): FileOpenPreferenceViewModel {
        every { observeFileOpenChoices() } returns flowOf(choices)
        return FileOpenPreferenceViewModel(observeFileOpenChoices, setFileOpenChoice, clearFileOpenChoice)
    }

    @Test
    fun `choices mirrors the persisted choice map`() = runTest {
        val map = mapOf("pdf" to FileOpenChoice.InApp, "video" to FileOpenChoice.External)
        val vm = viewModel(choices = map)

        vm.choices.test {
            assertThat(awaitItem()).containsExactlyEntriesIn(map)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `choices is empty when nothing is remembered`() = runTest {
        val vm = viewModel(choices = emptyMap())

        assertThat(vm.choices.value).isEmpty()
    }

    @Test
    fun `remember forwards the kind and choice to the setter`() = runTest {
        val vm = viewModel()

        vm.remember("image", FileOpenChoice.External)

        coVerify { setFileOpenChoice("image", FileOpenChoice.External) }
    }

    @Test
    fun `forget forwards the kind to the clear use case`() = runTest {
        val vm = viewModel()

        vm.forget("zip")

        coVerify { clearFileOpenChoice("zip") }
    }
}

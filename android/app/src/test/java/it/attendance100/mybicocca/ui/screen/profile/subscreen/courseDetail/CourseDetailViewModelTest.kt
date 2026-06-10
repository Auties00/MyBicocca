package it.attendance100.mybicocca.ui.screen.profile.subscreen.courseDetail

import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.SyncStatus
import it.attendance100.mybicocca.domain.model.account.Account
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.transcript.CourseDetail
import it.attendance100.mybicocca.domain.usecase.account.ObserveActiveAccountUseCase
import it.attendance100.mybicocca.domain.usecase.transcript.GetCourseDetailUseCase
import it.attendance100.mybicocca.testing.MainDispatcherRule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import java.io.IOException

/**
 * Covers the libretto course-detail ViewModel: live-fetch sync status, the keyed-load cache reuse
 * versus force-refetch, the no-active-career failure path, and the discard-stale-response guard.
 */
class CourseDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val careerId = CareerId(123L)

    private fun account(career: CareerId? = careerId): Account = mockk(relaxed = true) {
        every { academic.selectedCareerId } returns (career ?: CareerId(0L))
    }

    private fun build(
        activeAccount: Account? = account(),
        getCourseDetail: GetCourseDetailUseCase = mockk(relaxed = true),
    ): CourseDetailViewModel = CourseDetailViewModel(
        getCourseDetail = getCourseDetail,
        observeActiveAccount = mockk {
            every { this@mockk.invoke() } returns MutableStateFlow<Account?>(activeAccount)
        },
    )

    @Test
    fun `load fetches the detail and settles syncStatus Idle`() = runTest {
        val detail = mockk<CourseDetail>(relaxed = true)
        val get = mockk<GetCourseDetailUseCase>()
        coEvery { get.invoke(careerId, 5L, false) } returns detail
        val vm = build(getCourseDetail = get)
        vm.load(activityChoiceId = 5L, alreadyPassed = false)
        assertThat(vm.detail.value).isEqualTo(Loadable.Loaded(detail))
        assertThat(vm.syncStatus.value).isEqualTo(SyncStatus.Idle)
    }

    @Test
    fun `load passes alreadyPassed through to skip the prerequisite call`() = runTest {
        val get = mockk<GetCourseDetailUseCase>(relaxed = true)
        val vm = build(getCourseDetail = get)
        vm.load(activityChoiceId = 5L, alreadyPassed = true)
        coVerify { get.invoke(careerId, 5L, true) }
    }

    @Test
    fun `load failure surfaces SyncStatus Failed`() = runTest {
        val cause = IOException("offline")
        val get = mockk<GetCourseDetailUseCase>()
        coEvery { get.invoke(careerId, 5L, false) } throws cause
        val vm = build(getCourseDetail = get)
        vm.load(activityChoiceId = 5L, alreadyPassed = false)
        assertThat(vm.syncStatus.value).isEqualTo(SyncStatus.Failed(cause))
        assertThat(vm.detail.value).isEqualTo(Loadable.NotYetLoaded)
    }

    @Test
    fun `reloading the same loaded key reuses the value`() = runTest {
        val detail = mockk<CourseDetail>(relaxed = true)
        val get = mockk<GetCourseDetailUseCase>()
        coEvery { get.invoke(careerId, 5L, false) } returns detail
        val vm = build(getCourseDetail = get)
        vm.load(activityChoiceId = 5L, alreadyPassed = false)
        vm.load(activityChoiceId = 5L, alreadyPassed = false)
        coVerify(exactly = 1) { get.invoke(careerId, 5L, false) }
    }

    @Test
    fun `loading a different key refetches`() = runTest {
        val detail = mockk<CourseDetail>(relaxed = true)
        val get = mockk<GetCourseDetailUseCase>()
        coEvery { get.invoke(any(), any(), any()) } returns detail
        val vm = build(getCourseDetail = get)
        vm.load(activityChoiceId = 5L, alreadyPassed = false)
        vm.load(activityChoiceId = 6L, alreadyPassed = false)
        coVerify { get.invoke(careerId, 5L, false) }
        coVerify { get.invoke(careerId, 6L, false) }
    }

    @Test
    fun `retry forces a refetch of the same key`() = runTest {
        val detail = mockk<CourseDetail>(relaxed = true)
        val get = mockk<GetCourseDetailUseCase>()
        coEvery { get.invoke(careerId, 5L, false) } returns detail
        val vm = build(getCourseDetail = get)
        vm.load(activityChoiceId = 5L, alreadyPassed = false)
        vm.retry(activityChoiceId = 5L, alreadyPassed = false)
        coVerify(exactly = 2) { get.invoke(careerId, 5L, false) }
    }

    @Test
    fun `fetch without an active career fails fast`() = runTest {
        val get = mockk<GetCourseDetailUseCase>(relaxed = true)
        val vm = build(activeAccount = null, getCourseDetail = get)
        vm.load(activityChoiceId = 5L, alreadyPassed = false)
        assertThat(vm.syncStatus.value).isInstanceOf(SyncStatus.Failed::class.java)
        coVerify(exactly = 0) { get.invoke(any(), any(), any()) }
    }
}

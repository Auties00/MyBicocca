package it.attendance100.mybicocca.ui.screen.elearning.subscreen.addCourse

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.domain.model.account.Account
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.elearning.catalog.ElearningCatalog
import it.attendance100.mybicocca.domain.model.elearning.course.CourseId
import it.attendance100.mybicocca.domain.model.elearning.course.EnrolledCourse
import it.attendance100.mybicocca.domain.usecase.account.ObserveActiveAccountUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.catalog.LoadElearningCatalogUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.course.EnrolIntoCourseUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.course.ObserveEnrolledCoursesUseCase
import it.attendance100.mybicocca.testing.MainDispatcherRule
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.addCourse.state.AddCourseOneShotEvent
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.addCourse.state.CatalogStackEntry
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.addCourse.state.EnrolmentStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import java.io.IOException

/**
 * Covers the add-course sheet ViewModel: lazy catalog load with its separate failure flag, the
 * browse-stack push/pop/reset operations, and the self-enrolment guard logic with its one-shot
 * outcomes.
 */
class AddCourseViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val accountId = AccountId("acc-1")

    private val account: Account = mockk(relaxed = true) {
        every { id } returns accountId
    }

    private fun enrolledCourse(id: Int): EnrolledCourse = EnrolledCourse(
        id = CourseId(id),
        shortName = "S$id",
        fullName = "Course $id",
        displayName = "Course $id",
        idNumber = null,
        summary = null,
        courseImageUrl = null,
        format = null,
        language = null,
        categoryId = null,
        progress = null,
        completed = false,
        completionEnabled = false,
        startDate = null,
        endDate = null,
        lastAccessDate = null,
        isFavourite = false,
        hidden = false,
    )

    private fun observeActiveAccount(value: Account? = account): ObserveActiveAccountUseCase =
        mockk { every { this@mockk.invoke() } returns MutableStateFlow<Account?>(value) }

    private fun observeEnrolledCourses(
        loadable: Loadable<List<EnrolledCourse>> = Loadable.Loaded(emptyList()),
    ): ObserveEnrolledCoursesUseCase =
        mockk { every { this@mockk.invoke(any()) } returns flowOf(loadable) }

    private fun build(
        activeAccount: ObserveActiveAccountUseCase = observeActiveAccount(),
        enrolled: ObserveEnrolledCoursesUseCase = observeEnrolledCourses(),
        loadCatalog: LoadElearningCatalogUseCase = mockk(relaxed = true),
        enrol: EnrolIntoCourseUseCase = mockk(relaxed = true),
    ): AddCourseViewModel = AddCourseViewModel(
        observeActiveAccount = activeAccount,
        observeEnrolledCourses = enrolled,
        loadCatalog = loadCatalog,
        enrolIntoCourse = enrol,
    )

    @Test
    fun `ensureCatalogLoaded loads the catalog into Loaded`() = runTest {
        val catalog = ElearningCatalog(sections = emptyList())
        val loader = mockk<LoadElearningCatalogUseCase>()
        coEvery { loader.invoke() } returns catalog
        val vm = build(loadCatalog = loader)
        vm.ensureCatalogLoaded()
        assertThat(vm.catalog.value).isEqualTo(Loadable.Loaded(catalog))
        assertThat(vm.catalogFailed.value).isFalse()
    }

    @Test
    fun `ensureCatalogLoaded sets catalogFailed when the loader throws`() = runTest {
        val loader = mockk<LoadElearningCatalogUseCase>()
        coEvery { loader.invoke() } throws IOException("missing index")
        val vm = build(loadCatalog = loader)
        vm.ensureCatalogLoaded()
        assertThat(vm.catalog.value).isEqualTo(Loadable.NotYetLoaded)
        assertThat(vm.catalogFailed.value).isTrue()
    }

    @Test
    fun `ensureCatalogLoaded does not reload an already loaded catalog`() = runTest {
        val catalog = ElearningCatalog(sections = emptyList())
        val loader = mockk<LoadElearningCatalogUseCase>()
        coEvery { loader.invoke() } returns catalog
        val vm = build(loadCatalog = loader)
        vm.ensureCatalogLoaded()
        vm.ensureCatalogLoaded()
        coVerify(exactly = 1) { loader.invoke() }
    }

    @Test
    fun `retryCatalog clears the failure flag and retries successfully`() = runTest {
        val catalog = ElearningCatalog(sections = emptyList())
        val loader = mockk<LoadElearningCatalogUseCase>()
        coEvery { loader.invoke() } throws IOException("first") andThen catalog
        val vm = build(loadCatalog = loader)
        vm.ensureCatalogLoaded()
        assertThat(vm.catalogFailed.value).isTrue()
        vm.retryCatalog()
        assertThat(vm.catalogFailed.value).isFalse()
        assertThat(vm.catalog.value).isEqualTo(Loadable.Loaded(catalog))
    }

    @Test
    fun `open pushes an entry and clears the search query`() = runTest {
        val vm = build()
        vm.setSearch("query")
        val entry = mockk<CatalogStackEntry>()
        vm.open(entry)
        assertThat(vm.stack.value).containsExactly(entry)
        assertThat(vm.searchQuery.value).isEmpty()
    }

    @Test
    fun `openPath pushes the whole chain at once`() = runTest {
        val vm = build()
        val a = mockk<CatalogStackEntry>()
        val b = mockk<CatalogStackEntry>()
        vm.openPath(listOf(a, b))
        assertThat(vm.stack.value).containsExactly(a, b).inOrder()
    }

    @Test
    fun `openPath with an empty chain is a no-op`() = runTest {
        val vm = build()
        vm.openPath(emptyList())
        assertThat(vm.stack.value).isEmpty()
    }

    @Test
    fun `back pops one level`() = runTest {
        val vm = build()
        val a = mockk<CatalogStackEntry>()
        val b = mockk<CatalogStackEntry>()
        vm.openPath(listOf(a, b))
        vm.back()
        assertThat(vm.stack.value).containsExactly(a)
    }

    @Test
    fun `back on an empty stack is a no-op`() = runTest {
        val vm = build()
        vm.back()
        assertThat(vm.stack.value).isEmpty()
    }

    @Test
    fun `resetStack empties the stack and clears the query`() = runTest {
        val vm = build()
        vm.openPath(listOf(mockk(), mockk()))
        vm.setSearch("typed")
        vm.resetStack()
        assertThat(vm.stack.value).isEmpty()
        assertThat(vm.searchQuery.value).isEmpty()
    }

    @Test
    fun `setSearch updates the query flow`() = runTest {
        val vm = build()
        vm.setSearch("matematica")
        assertThat(vm.searchQuery.value).isEqualTo("matematica")
    }

    @Test
    fun `enrol succeeds and emits EnrolSucceeded`() = runTest {
        val enrol = mockk<EnrolIntoCourseUseCase>()
        coEvery { enrol.invoke(accountId, CourseId(5), any()) } returns Unit
        val vm = build(enrol = enrol)
        vm.oneShotEvents.test {
            vm.enrol(CourseId(5), "Algebra")
            assertThat(awaitItem())
                .isEqualTo(AddCourseOneShotEvent.EnrolSucceeded(CourseId(5), "Algebra"))
            cancelAndIgnoreRemainingEvents()
        }
        assertThat(terminalStatus(vm, CourseId(5))).isEqualTo(EnrolmentStatus.Enrolled)
    }

    @Test
    fun `enrol failure emits EnrolFailed and marks status Failed`() = runTest {
        val cause = IOException("rejected")
        val enrol = mockk<EnrolIntoCourseUseCase>()
        coEvery { enrol.invoke(accountId, CourseId(5), any()) } throws cause
        val vm = build(enrol = enrol)
        vm.oneShotEvents.test {
            vm.enrol(CourseId(5), "Algebra")
            val event = awaitItem()
            assertThat(event).isInstanceOf(AddCourseOneShotEvent.EnrolFailed::class.java)
            assertThat((event as AddCourseOneShotEvent.EnrolFailed).cause).isEqualTo(cause)
            cancelAndIgnoreRemainingEvents()
        }
        assertThat(terminalStatus(vm, CourseId(5))).isEqualTo(EnrolmentStatus.Failed)
    }

    /**
     * The combined enrolment flow is WhileSubscribed, so its `.value` is stale unless actively
     * collected; subscribe via Turbine and drain to the most recent map for the assertion.
     */
    private suspend fun terminalStatus(
        vm: AddCourseViewModel,
        courseId: CourseId,
    ): EnrolmentStatus? {
        var status: EnrolmentStatus? = null
        vm.enrolment.test {
            status = expectMostRecentItem()[courseId]
            cancelAndIgnoreRemainingEvents()
        }
        return status
    }

    @Test
    fun `enrol without an account emits RequireSignIn`() = runTest {
        val enrol = mockk<EnrolIntoCourseUseCase>(relaxed = true)
        val vm = build(activeAccount = observeActiveAccount(value = null), enrol = enrol)
        vm.oneShotEvents.test {
            vm.enrol(CourseId(5), "Algebra")
            assertThat(awaitItem()).isEqualTo(AddCourseOneShotEvent.RequireSignIn)
            cancelAndIgnoreRemainingEvents()
        }
        coVerify(exactly = 0) { enrol.invoke(any(), any(), any()) }
    }

    @Test
    fun `enrol short-circuits when the course is already enrolled on the server`() = runTest {
        val enrol = mockk<EnrolIntoCourseUseCase>(relaxed = true)
        val vm = build(
            enrolled = observeEnrolledCourses(Loadable.Loaded(listOf(enrolledCourse(5)))),
            enrol = enrol,
        )
        vm.enrol(CourseId(5), "Algebra")
        coVerify(exactly = 0) { enrol.invoke(any(), any(), any()) }
    }

    @Test
    fun `server enrolment wins over a stale local Failed status`() = runTest {
        val cause = IOException("rejected")
        val enrol = mockk<EnrolIntoCourseUseCase>()
        coEvery { enrol.invoke(accountId, CourseId(5), any()) } throws cause
        val enrolledStream = MutableStateFlow<Loadable<List<EnrolledCourse>>>(
            Loadable.Loaded(emptyList()),
        )
        val observe = mockk<ObserveEnrolledCoursesUseCase> {
            every { this@mockk.invoke(any()) } returns enrolledStream
        }
        val vm = build(enrolled = observe, enrol = enrol)
        vm.oneShotEvents.test {
            vm.enrol(CourseId(5), "Algebra")
            awaitItem()
            cancelAndIgnoreRemainingEvents()
        }
        vm.enrolment.test {
            assertThat(awaitItem()[CourseId(5)]).isEqualTo(EnrolmentStatus.Failed)
            enrolledStream.value = Loadable.Loaded(listOf(enrolledCourse(5)))
            assertThat(awaitItem()[CourseId(5)]).isEqualTo(EnrolmentStatus.Enrolled)
            cancelAndIgnoreRemainingEvents()
        }
    }
}

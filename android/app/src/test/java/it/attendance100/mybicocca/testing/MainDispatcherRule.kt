package it.attendance100.mybicocca.testing

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * JUnit 4 rule that swaps `Dispatchers.Main` for a [TestDispatcher] for the duration of a test,
 * then restores it. Required by any test that drives a ViewModel: `viewModelScope` and the
 * `stateIn(... SharingStarted.Eagerly/WhileSubscribed ...)` collectors run on the main
 * dispatcher, which has no real implementation in a plain JVM unit test.
 *
 * Defaults to [UnconfinedTestDispatcher] so `StateFlow`s seeded eagerly emit their first value
 * synchronously at construction; pass a `StandardTestDispatcher` when a test needs to control
 * virtual-time advancement explicitly. The backing [dispatcher] is exposed so tests can share it
 * with the scheduler they pass to `runTest`.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    val dispatcher: TestDispatcher = UnconfinedTestDispatcher(),
) : TestWatcher() {

    override fun starting(description: Description) {
        Dispatchers.setMain(dispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}

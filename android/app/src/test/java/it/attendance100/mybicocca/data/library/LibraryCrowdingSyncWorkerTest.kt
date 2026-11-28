package it.attendance100.mybicocca.data.library

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.mockk
import it.attendance100.mybicocca.domain.usecase.library.CollectLibraryCrowdingUseCase
import it.attendance100.mybicocca.domain.usecase.library.LibraryCrowdingCollectionOutcome
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Verifies the thin [LibraryCrowdingSyncWorker] maps every use-case decision to WorkManager's Result. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class LibraryCrowdingSyncWorkerTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val params: WorkerParameters = mockk(relaxed = true)
    private val collect: CollectLibraryCrowdingUseCase = mockk()

    @Test
    fun `Continue and Completed map to success and Retry maps to retry`() = runTest {
        val worker = LibraryCrowdingSyncWorker(context, params, collect)

        coEvery { collect() } returns LibraryCrowdingCollectionOutcome.Continue
        assertThat(worker.doWork()).isEqualTo(ListenableWorker.Result.success())

        coEvery { collect() } returns LibraryCrowdingCollectionOutcome.Completed
        assertThat(worker.doWork()).isEqualTo(ListenableWorker.Result.success())

        coEvery { collect() } returns LibraryCrowdingCollectionOutcome.Retry
        assertThat(worker.doWork()).isEqualTo(ListenableWorker.Result.retry())
    }
}

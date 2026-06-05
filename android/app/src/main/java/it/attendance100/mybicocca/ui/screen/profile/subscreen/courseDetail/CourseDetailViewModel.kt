package it.attendance100.mybicocca.ui.screen.profile.subscreen.courseDetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.SyncStatus
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.transcript.CourseDetail
import it.attendance100.mybicocca.domain.usecase.account.ObserveActiveAccountUseCase
import it.attendance100.mybicocca.domain.usecase.transcript.GetCourseDetailUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

// Drives the libretto course-detail sheet. The detail (attempt history + propedeuticità)
// is fetched lazily when a course is opened and is NOT cached — it follows the no-cache
// pattern (live fetch, throws → SyncStatus.Failed). Keyed by activityChoiceId so reopening
// a different course re-fetches; reopening the same one reuses the loaded value.
@HiltViewModel
class CourseDetailViewModel @Inject constructor(
    private val getCourseDetail: GetCourseDetailUseCase,
    observeActiveAccount: ObserveActiveAccountUseCase,
) : ViewModel() {

    private val activeCareerId: StateFlow<CareerId?> = observeActiveAccount()
        .map { it?.academic?.selectedCareerId }
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private var loadedKey: Long? = null

    private val _detail = MutableStateFlow<Loadable<CourseDetail>>(Loadable.NotYetLoaded)
    val detail: StateFlow<Loadable<CourseDetail>> = _detail.asStateFlow()

    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    fun load(activityChoiceId: Long, alreadyPassed: Boolean, force: Boolean = false) {
        if (!force && loadedKey == activityChoiceId && _detail.value is Loadable.Loaded) return
        loadedKey = activityChoiceId
        _detail.value = Loadable.NotYetLoaded
        fetch(activityChoiceId, alreadyPassed)
    }

    fun retry(activityChoiceId: Long, alreadyPassed: Boolean) =
        load(activityChoiceId, alreadyPassed, force = true)

    private fun fetch(activityChoiceId: Long, alreadyPassed: Boolean) {
        val careerId = activeCareerId.value ?: run {
            _syncStatus.value = SyncStatus.Failed(IllegalStateException("Nessuna carriera attiva"))
            return
        }
        viewModelScope.launch {
            _syncStatus.value = SyncStatus.Refreshing
            runCatching { getCourseDetail(careerId, activityChoiceId, alreadyPassed) }.fold(
                onSuccess = { result ->
                    // Guard against a stale response if the user reopened a different course.
                    if (loadedKey == activityChoiceId) {
                        _detail.value = Loadable.Loaded(result)
                        _syncStatus.value = SyncStatus.Idle
                    }
                },
                onFailure = { cause ->
                    if (loadedKey == activityChoiceId) _syncStatus.value = SyncStatus.Failed(cause)
                },
            )
        }
    }
}

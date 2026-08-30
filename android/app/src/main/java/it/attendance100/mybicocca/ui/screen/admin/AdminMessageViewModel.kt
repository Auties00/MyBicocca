package it.attendance100.mybicocca.ui.screen.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.attendance100.mybicocca.domain.model.admin.AdminMessage
import it.attendance100.mybicocca.domain.repository.admin.AdminMessageRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminMessageViewModel @Inject constructor(
    private val repository: AdminMessageRepository
) : ViewModel() {

    val message: StateFlow<AdminMessage?> = repository.observeMessage()
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    init {
        viewModelScope.launch {
            repository.fetch()
        }
    }

    fun dismiss(id: String) {
        viewModelScope.launch {
            repository.dismissMessage(id)
        }
    }
}

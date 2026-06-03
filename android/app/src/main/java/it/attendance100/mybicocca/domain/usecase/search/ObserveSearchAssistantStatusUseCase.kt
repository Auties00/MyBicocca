package it.attendance100.mybicocca.domain.usecase.search

import it.attendance100.mybicocca.domain.model.search.SearchAssistantStatus
import it.attendance100.mybicocca.domain.repository.SearchAssistant
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveSearchAssistantStatusUseCase @Inject constructor(
    private val assistant: SearchAssistant,
) {
    operator fun invoke(): Flow<SearchAssistantStatus> = assistant.observeStatus()
}

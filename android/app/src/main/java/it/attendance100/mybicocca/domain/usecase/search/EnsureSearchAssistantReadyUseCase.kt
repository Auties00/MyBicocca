package it.attendance100.mybicocca.domain.usecase.search

import it.attendance100.mybicocca.domain.repository.SearchAssistant
import javax.inject.Inject

class EnsureSearchAssistantReadyUseCase @Inject constructor(
    private val assistant: SearchAssistant,
) {
    suspend operator fun invoke(): Boolean = assistant.ensureReady()
}

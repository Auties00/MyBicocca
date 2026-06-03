package it.attendance100.mybicocca.domain.usecase.search

import it.attendance100.mybicocca.domain.model.search.SearchSuggestion
import it.attendance100.mybicocca.domain.repository.SearchAssistant
import javax.inject.Inject

// Fired on query submit only — never per keystroke (on-device inference is ~1s and quota'd).
class InterpretQueryUseCase @Inject constructor(
    private val assistant: SearchAssistant,
) {
    suspend operator fun invoke(query: String): SearchSuggestion? = assistant.interpret(query)
}

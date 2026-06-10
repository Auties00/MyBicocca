package it.attendance100.mybicocca.domain.usecase.settings

import it.attendance100.mybicocca.domain.repository.FileOpenPreferenceRepository
import javax.inject.Inject

/** Forgets the remembered choice for [kind] so the chooser sheet is shown again next time. */
class ClearFileOpenChoiceUseCase @Inject constructor(
    private val repository: FileOpenPreferenceRepository,
) {
    suspend operator fun invoke(kind: String) = repository.clearChoice(kind)
}

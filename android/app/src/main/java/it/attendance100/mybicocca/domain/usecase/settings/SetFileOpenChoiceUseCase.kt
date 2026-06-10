package it.attendance100.mybicocca.domain.usecase.settings

import it.attendance100.mybicocca.domain.model.settings.FileOpenChoice
import it.attendance100.mybicocca.domain.repository.FileOpenPreferenceRepository
import javax.inject.Inject

/** Remembers how files of [kind] should open from now on ("Ricorda la scelta" in the chooser sheet). */
class SetFileOpenChoiceUseCase @Inject constructor(
    private val repository: FileOpenPreferenceRepository,
) {
    suspend operator fun invoke(kind: String, choice: FileOpenChoice) = repository.setChoice(kind, choice)
}

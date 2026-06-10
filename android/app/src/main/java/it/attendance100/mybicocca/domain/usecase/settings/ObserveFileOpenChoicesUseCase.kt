package it.attendance100.mybicocca.domain.usecase.settings

import it.attendance100.mybicocca.domain.model.settings.FileOpenChoice
import it.attendance100.mybicocca.domain.repository.FileOpenPreferenceRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Streams the remembered per-file-kind open choices. The shell consults the map before opening
 * an e-learning file: a missing kind means the chooser sheet must be shown.
 */
class ObserveFileOpenChoicesUseCase @Inject constructor(
    private val repository: FileOpenPreferenceRepository,
) {
    operator fun invoke(): Flow<Map<String, FileOpenChoice>> = repository.observeChoices()
}

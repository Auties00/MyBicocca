package it.attendance100.mybicocca.domain.usecase.settings

import it.attendance100.mybicocca.domain.model.settings.PdfThemeMode
import it.attendance100.mybicocca.domain.repository.PdfViewerSettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** Streams the PDF viewer's rendering theme so an open viewer restyles live. */
class ObservePdfThemeModeUseCase @Inject constructor(
    private val repository: PdfViewerSettingsRepository,
) {
    operator fun invoke(): Flow<PdfThemeMode> = repository.observeThemeMode()
}

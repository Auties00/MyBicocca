package it.attendance100.mybicocca.domain.usecase.settings

import it.attendance100.mybicocca.domain.model.settings.PdfThemeMode
import it.attendance100.mybicocca.domain.repository.PdfViewerSettingsRepository
import javax.inject.Inject

/** Persists the PDF viewer's rendering theme picked from the viewer toolbar. */
class SetPdfThemeModeUseCase @Inject constructor(
    private val repository: PdfViewerSettingsRepository,
) {
    suspend operator fun invoke(mode: PdfThemeMode) = repository.setThemeMode(mode)
}

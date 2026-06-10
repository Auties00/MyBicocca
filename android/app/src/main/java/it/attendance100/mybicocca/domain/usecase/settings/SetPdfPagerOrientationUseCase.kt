package it.attendance100.mybicocca.domain.usecase.settings

import it.attendance100.mybicocca.domain.model.settings.PdfPagerOrientation
import it.attendance100.mybicocca.domain.repository.PdfViewerSettingsRepository
import javax.inject.Inject

/** Persists the PDF viewer's scroll axis picked from the viewer toolbar. */
class SetPdfPagerOrientationUseCase @Inject constructor(
    private val repository: PdfViewerSettingsRepository,
) {
    suspend operator fun invoke(orientation: PdfPagerOrientation) = repository.setOrientation(orientation)
}

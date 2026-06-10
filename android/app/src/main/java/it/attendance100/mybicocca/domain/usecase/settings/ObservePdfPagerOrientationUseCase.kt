package it.attendance100.mybicocca.domain.usecase.settings

import it.attendance100.mybicocca.domain.model.settings.PdfPagerOrientation
import it.attendance100.mybicocca.domain.repository.PdfViewerSettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** Streams the PDF viewer's scroll axis so an open viewer re-lays-out live. */
class ObservePdfPagerOrientationUseCase @Inject constructor(
    private val repository: PdfViewerSettingsRepository,
) {
    operator fun invoke(): Flow<PdfPagerOrientation> = repository.observeOrientation()
}

package it.attendance100.mybicocca.domain.usecase.elearning.video

import it.attendance100.mybicocca.domain.repository.VideoPlaybackRepository
import javax.inject.Inject

class GetVideoThumbnailUrlUseCase @Inject constructor(
    private val repository: VideoPlaybackRepository,
) {
    suspend operator fun invoke(cmId: Int): String? = repository.thumbnailUrl(cmId)
}

package it.attendance100.mybicocca.domain.usecase.elearning.video

import it.attendance100.mybicocca.domain.model.elearning.video.VideoStream
import it.attendance100.mybicocca.domain.repository.VideoPlaybackRepository
import javax.inject.Inject

class ResolveVideoStreamUseCase @Inject constructor(
    private val repository: VideoPlaybackRepository,
) {
    suspend operator fun invoke(cmId: Int): VideoStream = repository.resolveStream(cmId)
}

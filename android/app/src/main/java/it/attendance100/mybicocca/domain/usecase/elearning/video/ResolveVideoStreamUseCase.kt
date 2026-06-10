package it.attendance100.mybicocca.domain.usecase.elearning.video

import it.attendance100.mybicocca.domain.model.elearning.video.VideoStream
import it.attendance100.mybicocca.domain.repository.VideoPlaybackRepository
import javax.inject.Inject

/**
 * Resolves the playable Kaltura stream for a video course module when the video player opens.
 * Stream URLs are session-scoped, so resolution hits the e-learning platform every time and is
 * never cached; throws when the stream cannot be resolved.
 */
class ResolveVideoStreamUseCase @Inject constructor(
    private val repository: VideoPlaybackRepository,
) {
    suspend operator fun invoke(cmId: Int): VideoStream = repository.resolveStream(cmId)
}

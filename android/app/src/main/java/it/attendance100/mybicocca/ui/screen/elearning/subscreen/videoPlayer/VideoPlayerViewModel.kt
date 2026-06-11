package it.attendance100.mybicocca.ui.screen.elearning.subscreen.videoPlayer

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import androidx.annotation.OptIn
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.MoreExecutors
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.SyncStatus
import it.attendance100.mybicocca.core.state.valueOrNull
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.elearning.course.CourseDetails
import it.attendance100.mybicocca.domain.model.elearning.course.CourseId
import it.attendance100.mybicocca.domain.model.elearning.course.kalvidresCmIdOrNull
import it.attendance100.mybicocca.domain.model.elearning.video.VideoProgress
import it.attendance100.mybicocca.domain.model.elearning.video.VideoStream
import it.attendance100.mybicocca.domain.model.elearning.video.VideoVariant
import it.attendance100.mybicocca.domain.usecase.account.ObserveActiveAccountUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.course.ObserveCourseDetailsUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.video.ObserveCourseVideoProgressUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.video.ResolveVideoStreamUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.video.SaveVideoProgressUseCase
import it.attendance100.mybicocca.ui.navigation.route.AppRoute
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.videoPlayer.player.VideoPlaybackService
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.videoPlayer.state.VideoPlayerOneShotEvent
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.videoPlayer.state.VideoPlayerPlaylistItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Drives course video playback through a [MediaController] bound to [VideoPlaybackService], so
 * the actual ExoPlayer lives in a foreground media-session service and audio keeps running when
 * the app is backgrounded, with a system media notification.
 *
 * Data streams: [player] (the connected controller, null until the session binds),
 * [currentStream] (resolved Kaltura stream for the current video), [currentCmId], [playlist]
 * (every visible kalvidres module of the course annotated with per-video progress), and
 * [progressByCmId] (saved positions from Room). Sync status: [syncStatus] tracks stream
 * resolution. One-shot: [oneShotEvents] for resolve and playback failures.
 *
 * Actions: [selectVideo], [selectQuality], [retry].
 *
 * Playback progress is persisted on a fixed tick while playing and flushed on pause, switch,
 * end (marked completed) and [onCleared]; a video that ends auto-advances to the next playlist
 * entry. Media items carry their notification metadata directly from the resolved stream model,
 * whose thumbnail URL becomes the artwork.
 */
@OptIn(ExperimentalCoroutinesApi::class, UnstableApi::class)
@HiltViewModel(assistedFactory = VideoPlayerViewModel.Factory::class)
class VideoPlayerViewModel @AssistedInject constructor(
    @Assisted private val key: AppRoute.VideoPlayback,
    @ApplicationContext private val context: Context,
    savedState: SavedStateHandle,
    observeActiveAccount: ObserveActiveAccountUseCase,
    private val observeCourseDetails: ObserveCourseDetailsUseCase,
    private val observeCourseVideoProgress: ObserveCourseVideoProgressUseCase,
    private val resolveVideoStream: ResolveVideoStreamUseCase,
    private val saveVideoProgress: SaveVideoProgressUseCase,
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(key: AppRoute.VideoPlayback): VideoPlayerViewModel
    }

    private val courseId: CourseId = CourseId(key.courseId)

    private val initialCmId: Int = key.cmId

    private val activeAccountId: Flow<AccountId?> = observeActiveAccount()
        .map { it?.id }
        .distinctUntilChanged()

    private val _currentCmId = MutableStateFlow(initialCmId)
    val currentCmId: StateFlow<Int> = _currentCmId.asStateFlow()

    private val _currentStream = MutableStateFlow<Loadable<VideoStream>>(Loadable.NotYetLoaded)
    val currentStream: StateFlow<Loadable<VideoStream>> = _currentStream.asStateFlow()

    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    private val _player = MutableStateFlow<Player?>(null)
    val player: StateFlow<Player?> = _player.asStateFlow()

    @kotlin.OptIn(ExperimentalCoroutinesApi::class)
    private val courseDetails: StateFlow<Loadable<CourseDetails>> = activeAccountId
        .flatMapLatest { id ->
            if (id == null) flowOf(Loadable.NotYetLoaded) else observeCourseDetails(id, courseId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_KEEP_ALIVE_MS), Loadable.NotYetLoaded)

    @kotlin.OptIn(ExperimentalCoroutinesApi::class)
    val progressByCmId: StateFlow<Map<Int, VideoProgress>> = activeAccountId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyMap()) else observeCourseVideoProgress(id, courseId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_KEEP_ALIVE_MS), emptyMap())

    val playlist: StateFlow<List<VideoPlayerPlaylistItem>> = combine(
        courseDetails,
        progressByCmId,
        currentCmId,
    ) { details, progress, current ->
        val courseDetailsValue = details.valueOrNull() ?: return@combine emptyList()
        buildPlaylist(courseDetailsValue, progress, current)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_KEEP_ALIVE_MS), emptyList())

    private val oneShotChannel = Channel<VideoPlayerOneShotEvent>(Channel.BUFFERED)
    val oneShotEvents: Flow<VideoPlayerOneShotEvent> = oneShotChannel.receiveAsFlow()

    private var mediaControllerFuture: com.google.common.util.concurrent.ListenableFuture<MediaController>? = null
    private var playerListener: Player.Listener? = null
    private var progressTickerJob: Job? = null

    /**
     * Bumped each time [loadAndPlay] starts a fresh playback; the background queue expansion
     * checks it after every suspend point so a user-driven navigation aborts stale work.
     */
    private var queueGeneration: Long = 0L
    private var queueExpansionJob: Job? = null
    private val resolvedStreamCache = mutableMapOf<Int, VideoStream>()

    init {
        bindMediaController()
    }

    /** Switches playback to another playlist video, flushing the outgoing video's progress first. */
    fun selectVideo(cmId: Int) {
        if (cmId == _currentCmId.value && _currentStream.value is Loadable.Loaded) return
        flushCurrentProgress()
        _currentCmId.value = cmId
        loadAndPlay(cmId)
    }

    /**
     * Constrains track selection to the picked variant's resolution; a null variant clears the
     * constraint and restores adaptive quality.
     */
    fun selectQuality(variant: VideoVariant?) {
        val controller = _player.value ?: return
        val params = controller.trackSelectionParameters
        val updated = if (variant?.heightPx != null && variant.widthPx != null) {
            params.buildUpon().setMaxVideoSize(variant.widthPx, variant.heightPx).build()
        } else {
            params.buildUpon().clearVideoSizeConstraints().build()
        }
        controller.trackSelectionParameters = updated
    }

    fun retry() {
        loadAndPlay(_currentCmId.value)
    }

    private fun bindMediaController() {
        val sessionToken = SessionToken(
            context,
            ComponentName(context, VideoPlaybackService::class.java),
        )
        val future = MediaController.Builder(context, sessionToken).buildAsync()
        mediaControllerFuture = future
        future.addListener(
            {
                val controller = runCatching { future.get() }.getOrNull() ?: return@addListener
                _player.value = controller
                attachListener(controller)
                loadAndPlay(_currentCmId.value)
            },
            MoreExecutors.directExecutor(),
        )
    }

    /**
     * Wires player callbacks to the ViewModel: an ended video is marked completed and playback
     * auto-advances, play/pause toggles the progress ticker, media-item transitions that
     * originate outside the screen (auto-advance, notification prev/next) realign [currentCmId]
     * and [currentStream] with whatever the player moved to, and playback errors surface as
     * one-shot events.
     */
    private fun attachListener(controller: MediaController) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) {
                    flushCurrentProgress(markCompleted = true)
                    advanceToNextVideo()
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                if (isPlaying) startProgressTicker() else flushCurrentProgress()
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                val newCmId = mediaItem?.mediaId?.toIntOrNull() ?: return
                if (newCmId == _currentCmId.value) return
                flushCurrentProgress()
                _currentCmId.value = newCmId
                resolvedStreamCache[newCmId]?.let { _currentStream.value = Loadable.Loaded(it) }
            }

            override fun onPlayerError(error: PlaybackException) {
                oneShotChannel.trySend(VideoPlayerOneShotEvent.PlaybackError(error))
            }
        }
        playerListener = listener
        controller.addListener(listener)
    }

    /**
     * Resolves the video's stream and starts playing it, resuming from the saved position when
     * the video is not yet completed. Resolution waits for an active account because the stream
     * lookup needs the session. On success the playlist queue expansion kicks off in the
     * background.
     */
    private fun loadAndPlay(cmId: Int) {
        val controller = _player.value ?: return
        queueExpansionJob?.cancel()
        val generation = ++queueGeneration
        viewModelScope.launch {
            _syncStatus.value = SyncStatus.Refreshing
            _currentStream.value = Loadable.NotYetLoaded
            activeAccountId.filterNotNull().first()
            runCatching { resolveVideoStream(cmId) }
                .onSuccess { stream ->
                    if (generation != queueGeneration) return@onSuccess
                    resolvedStreamCache[cmId] = stream
                    _currentStream.value = Loadable.Loaded(stream)
                    _syncStatus.value = SyncStatus.Idle
                    val startMs = progressByCmId.value[cmId]
                        ?.takeIf { !it.completed }
                        ?.positionMs ?: 0L
                    val item = buildMediaItem(cmId, stream)
                    controller.setMediaItem(item, startMs)
                    controller.prepare()
                    controller.playWhenReady = true
                    queueExpansionJob = launchQueueExpansion(generation, cmId)
                }
                .onFailure {
                    if (generation != queueGeneration) return@onFailure
                    _syncStatus.value = SyncStatus.Failed(it)
                    oneShotChannel.trySend(VideoPlayerOneShotEvent.ResolveFailed(it))
                }
        }
    }

    /**
     * Resolves every other playlist item in the background and inserts them around the current
     * item via addMediaItems, so the system notification gets a full queue with working
     * prev/next without blocking the initial play call. [generation] is re-checked after every
     * suspend point so the expansion silently aborts when a newer playback supersedes it.
     */
    private fun launchQueueExpansion(generation: Long, currentCmId: Int): Job = viewModelScope.launch {
        val controller = _player.value ?: return@launch
        val items = playlist.first { it.isNotEmpty() }
        if (generation != queueGeneration) return@launch
        val currentIndex = items.indexOfFirst { it.cmId == currentCmId }.takeIf { it >= 0 } ?: return@launch

        val before = items.subList(0, currentIndex)
        val beforeMediaItems = mutableListOf<MediaItem>()
        for (listItem in before) {
            if (generation != queueGeneration) return@launch
            val stream = resolveAndCacheStream(listItem.cmId) ?: continue
            beforeMediaItems += buildMediaItem(listItem.cmId, stream)
        }
        if (generation != queueGeneration) return@launch
        if (beforeMediaItems.isNotEmpty()) controller.addMediaItems(0, beforeMediaItems)

        val after = items.subList(currentIndex + 1, items.size)
        val afterMediaItems = mutableListOf<MediaItem>()
        for (listItem in after) {
            if (generation != queueGeneration) return@launch
            val stream = resolveAndCacheStream(listItem.cmId) ?: continue
            afterMediaItems += buildMediaItem(listItem.cmId, stream)
        }
        if (generation != queueGeneration) return@launch
        if (afterMediaItems.isNotEmpty()) controller.addMediaItems(afterMediaItems)
    }

    private suspend fun resolveAndCacheStream(cmId: Int): VideoStream? {
        resolvedStreamCache[cmId]?.let { return it }
        return runCatching { resolveVideoStream(cmId) }.getOrNull()
            ?.also { resolvedStreamCache[cmId] = it }
    }

    private fun startProgressTicker() {
        if (progressTickerJob?.isActive == true) return
        progressTickerJob = viewModelScope.launch {
            while (true) {
                delay(PROGRESS_TICK_MS)
                val controller = _player.value ?: continue
                if (!controller.isPlaying) continue
                persistProgress(
                    cmId = _currentCmId.value,
                    positionMs = controller.currentPosition,
                    durationMs = controller.duration.takeIf { it != C.TIME_UNSET } ?: 0L,
                )
            }
        }
    }

    private fun flushCurrentProgress(markCompleted: Boolean = false) {
        val controller = _player.value ?: return
        val position = if (markCompleted && controller.duration != C.TIME_UNSET) {
            controller.duration
        } else {
            controller.currentPosition.coerceAtLeast(0L)
        }
        val duration = controller.duration.takeIf { it != C.TIME_UNSET } ?: 0L
        persistProgress(cmId = _currentCmId.value, positionMs = position, durationMs = duration)
    }

    private fun persistProgress(cmId: Int, positionMs: Long, durationMs: Long) {
        viewModelScope.launch {
            val accountId = activeAccountId.filterNotNull().first()
            runCatching {
                saveVideoProgress(accountId, courseId, cmId, positionMs, durationMs)
            }
        }
    }

    private fun advanceToNextVideo() {
        val items = playlist.value
        val currentIdx = items.indexOfFirst { it.isCurrent }
        val next = items.getOrNull(currentIdx + 1) ?: return
        selectVideo(next.cmId)
    }

    private fun buildMediaItem(cmId: Int, stream: VideoStream): MediaItem {
        val playlistItem = playlist.value.firstOrNull { it.cmId == cmId }
        val metadata = MediaMetadata.Builder()
            .setTitle(playlistItem?.title ?: "Video del corso")
            .setArtist(playlistItem?.sectionName)
            .setArtworkUri(Uri.parse(stream.thumbnailUrl))
            .setMediaType(MediaMetadata.MEDIA_TYPE_VIDEO)
            .build()
        return MediaItem.Builder()
            .setMediaId(cmId.toString())
            .setUri(stream.hlsUrl)
            .setMimeType(MimeTypes.APPLICATION_M3U8)
            .setMediaMetadata(metadata)
            .build()
    }

    /**
     * Flattens the course's visible sections into the ordered video playlist. The last-seen
     * marker goes to the most recently updated video that is started but not completed.
     */
    private fun buildPlaylist(
        details: CourseDetails,
        progress: Map<Int, VideoProgress>,
        currentCmId: Int,
    ): List<VideoPlayerPlaylistItem> {
        val lastSeenCmId = progress.values
            .filter { !it.completed && it.progressFraction > 0.01f }
            .maxByOrNull { it.lastUpdatedAt }
            ?.cmId
        val items = mutableListOf<VideoPlayerPlaylistItem>()
        for (section in details.sections) {
            if (!section.visible) continue
            for (module in section.modules) {
                if (!module.visible) continue
                val videoCmId = module.kalvidresCmIdOrNull() ?: continue
                val p = progress[videoCmId]
                items += VideoPlayerPlaylistItem(
                    cmId = videoCmId,
                    title = module.name,
                    sectionName = section.name.takeIf { it.isNotBlank() }
                        ?: "Sezione ${section.sectionNumber}",
                    progressFraction = p?.progressFraction ?: 0f,
                    completed = p?.completed == true,
                    isCurrent = videoCmId == currentCmId,
                    isLastSeen = videoCmId == lastSeenCmId,
                )
            }
        }
        return items
    }

    /**
     * Flushes the final progress and tears playback down. Releasing the controller alone would
     * leave the ExoPlayer in [VideoPlaybackService] running; stop + clear pushes the session
     * idle so the service can tear down.
     */
    override fun onCleared() {
        flushCurrentProgress()
        progressTickerJob?.cancel()
        queueExpansionJob?.cancel()
        val controller = _player.value
        if (controller != null) {
            playerListener?.let { controller.removeListener(it) }
            runCatching {
                controller.stop()
                controller.clearMediaItems()
            }
        }
        mediaControllerFuture?.let { MediaController.releaseFuture(it) }
        mediaControllerFuture = null
        _player.value = null
        super.onCleared()
    }

    private companion object {
        const val KEY_COURSE_ID = "courseId"
        const val KEY_CM_ID = "cmId"
        const val STATE_KEEP_ALIVE_MS = 5_000L
        const val PROGRESS_TICK_MS = 5_000L
    }
}

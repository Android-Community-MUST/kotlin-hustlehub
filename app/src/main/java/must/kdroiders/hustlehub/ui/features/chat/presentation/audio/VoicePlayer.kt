package must.kdroiders.hustlehub.ui.features.chat.presentation.audio

import android.content.Context
import android.net.Uri
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.net.URL

data class PlayerState(
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val currentPositionMs: Int = 0,
    val durationMs: Int = 0,
    val playingUrl: String? = null,
    val playbackSpeed: Float = 1.0f,
)

/**
 * Manages a single ExoPlayer instance for voice-note playback.
 *
 * Features:
 * - Downloads remote URLs to local cache before playback (idempotent — skips download if cached).
 * - Automatic audio focus and AudioBecomingNoisy handling via [AudioAttributes] + handleAudioFocus.
 * - Playback-speed cycling: 1.0x → 1.5x → 2.0x → 1.0x.
 * - Singleton-safe: only one clip plays at a time; calling [play] with a different URL
 *   stops the current clip first.
 */
class VoicePlayer(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.Main)
    private var updateJob: Job? = null

    private val _playerState = MutableStateFlow(PlayerState())
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    private val exoPlayer: ExoPlayer by lazy {
        ExoPlayer.Builder(context)
            .build()
            .apply {
                // Audio focus + BECOMING_NOISY — handled automatically by ExoPlayer
                val audioAttributes = AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_SPEECH)
                    .build()
                setAudioAttributes(audioAttributes, /* handleAudioFocus = */ true)

                addListener(object : Player.Listener {
                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        _playerState.update { it.copy(isPlaying = isPlaying) }
                        if (isPlaying) startProgressUpdates() else stopProgressUpdates()
                    }

                    override fun onPlaybackStateChanged(state: Int) {
                        val buffering = state == Player.STATE_BUFFERING
                        val ready = state == Player.STATE_READY
                        _playerState.update { it.copy(isBuffering = buffering) }
                        if (ready) {
                            // Capture total duration once the player is prepared
                            val dur = duration.takeIf { it > 0L }?.toInt() ?: 0
                            _playerState.update { it.copy(durationMs = dur) }
                        }
                        if (state == Player.STATE_ENDED) {
                            stop()
                        }
                    }
                })
            }
    }

    private val speedCycle = listOf(1.0f, 1.5f, 2.0f)

    /** Toggle the current playback speed through the cycle [1.0x, 1.5x, 2.0x]. */
    fun toggleSpeed() {
        val current = _playerState.value.playbackSpeed
        val idx = speedCycle.indexOf(current)
        val next = speedCycle[(idx + 1) % speedCycle.size]
        exoPlayer.playbackParameters = PlaybackParameters(next)
        _playerState.update { it.copy(playbackSpeed = next) }
    }

    /**
     * Play or pause a voice note at [url].
     *
     * - If the same URL is already playing → pauses.
     * - If the same URL is paused → resumes.
     * - Otherwise → downloads (if needed), caches, and plays from scratch.
     */
    fun play(url: String) {
        val current = _playerState.value
        if (current.playingUrl == url) {
            if (current.isPlaying) pause() else resume()
            return
        }

        // Stop any clip currently playing
        stopPlayback()

        _playerState.update { it.copy(playingUrl = url, isBuffering = true) }

        scope.launch {
            try {
                val localUri = getOrDownload(url)
                exoPlayer.apply {
                    // Preserve current speed when switching tracks
                    val speed = _playerState.value.playbackSpeed
                    playbackParameters = PlaybackParameters(speed)
                    setMediaItem(MediaItem.fromUri(localUri))
                    prepare()
                    play()
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to play voice note: $url")
                _playerState.update { it.copy(isBuffering = false, playingUrl = null) }
            }
        }
    }

    fun pause() {
        exoPlayer.pause()
        _playerState.update { it.copy(isPlaying = false) }
        stopProgressUpdates()
    }

    private fun resume() {
        exoPlayer.play()
        _playerState.update { it.copy(isPlaying = true) }
    }

    fun stop() {
        stopPlayback()
        _playerState.update {
            it.copy(
                isPlaying = false,
                isBuffering = false,
                currentPositionMs = 0,
                durationMs = 0,
                playingUrl = null,
            )
        }
    }

    fun release() {
        stopProgressUpdates()
        exoPlayer.release()
    }

    // Private helpers

    private fun stopPlayback() {
        stopProgressUpdates()
        try {
            exoPlayer.stop()
            exoPlayer.clearMediaItems()
        } catch (e: Exception) {
            Timber.w(e, "Error stopping ExoPlayer")
        }
    }

    /**
     * Returns a local [Uri] for [url]. Downloads and caches the file on first call;
     * returns the cached file URI on subsequent calls (even across app restarts).
     */
    private suspend fun getOrDownload(url: String): Uri = withContext(Dispatchers.IO) {
        val cacheDir = File(context.cacheDir, "voice_cache").also { it.mkdirs() }
        val fileName = "voice_${url.hashCode().toUInt()}.m4a"
        val cacheFile = File(cacheDir, fileName)

        if (!cacheFile.exists()) {
            URL(url).openStream().use { input ->
                cacheFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        }
        Uri.fromFile(cacheFile)
    }

    private fun startProgressUpdates() {
        stopProgressUpdates()
        updateJob = scope.launch {
            while (true) {
                val pos = exoPlayer.currentPosition.coerceAtLeast(0L).toInt()
                val dur = exoPlayer.duration.takeIf { it > 0L }?.toInt() ?: _playerState.value.durationMs
                _playerState.update { it.copy(currentPositionMs = pos, durationMs = dur) }
                delay(PROGRESS_UPDATE_INTERVAL_MS)
            }
        }
    }

    private fun stopProgressUpdates() {
        updateJob?.cancel()
        updateJob = null
    }

    private companion object {
        /** How often (ms) to poll ExoPlayer's current position for the progress bar. */
        const val PROGRESS_UPDATE_INTERVAL_MS = 150L
    }
}

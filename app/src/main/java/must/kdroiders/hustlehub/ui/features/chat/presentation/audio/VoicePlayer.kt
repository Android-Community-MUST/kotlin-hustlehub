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

class VoicePlayer(private val context: Context) {
    private val scope = CoroutineScope(Dispatchers.Main)
    private var updateJob: Job? = null

    private val proximityAudioRouter = ProximityAudioRouter(
        context = context,
        onEarpieceModeChanged = { isEarpiece ->
            scope.launch {
                try {
                    val player = exoPlayer ?: return@launch
                    if (isEarpiece) {
                        val currentPos = player.currentPosition
                        val targetPos = if (currentPos < SMART_REWIND_THRESHOLD_MS) {
                            0L
                        } else {
                            (currentPos - SMART_REWIND_DURATION_MS).coerceAtLeast(0L)
                        }
                        player.volume = 0f
                        player.seekTo(targetPos)
                        delay(TRANSITION_SETTLE_DELAY_MS)
                        player.volume = 1f
                    } else {
                        pause()
                    }
                } catch (e: Throwable) {
                    Timber.w(e, "VoicePlayer: Failed to handle proximity audio transition")
                }
            }
        },
    )

    private val _playerState = MutableStateFlow(PlayerState())
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    private var exoPlayer: ExoPlayer? = null

    private fun getOrCreatePlayer(): ExoPlayer {
        return exoPlayer ?: ExoPlayer
            .Builder(context)
            .build()
            .apply {
                val audioAttributes = AudioAttributes
                    .Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_SPEECH)
                    .build()
                setAudioAttributes(audioAttributes, true)

                addListener(object : Player.Listener {
                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        _playerState.update { it.copy(isPlaying = isPlaying) }
                        if (isPlaying) {
                            startProgressUpdates()
                        } else {
                            stopProgressUpdates()
                        }
                    }

                    override fun onPlaybackStateChanged(state: Int) {
                        val buffering = state == Player.STATE_BUFFERING
                        val ready = state == Player.STATE_READY
                        _playerState.update { it.copy(isBuffering = buffering) }
                        if (ready) {
                            val dur = duration.takeIf { it > 0L }?.toInt() ?: 0
                            _playerState.update { it.copy(durationMs = dur) }
                        }
                        if (state == Player.STATE_ENDED) {
                            seekTo(0)
                            pause()
                            _playerState.update {
                                it.copy(
                                    currentPositionMs = 0,
                                )
                            }
                        }
                    }
                })
            }.also { exoPlayer = it }
    }

    private val speedCycle = listOf(1.0f, 1.5f, 2.0f)

    fun toggleSpeed() {
        val current = _playerState.value.playbackSpeed
        val idx = speedCycle.indexOf(current)
        val next = speedCycle[(idx + 1) % speedCycle.size]
        exoPlayer?.playbackParameters = PlaybackParameters(next)
        _playerState.update { it.copy(playbackSpeed = next) }
    }

    fun play(url: String) {
        val current = _playerState.value
        if (current.playingUrl == url) {
            if (current.isPlaying) pause() else resume()
            return
        }

        stopPlayback()

        _playerState.update { it.copy(playingUrl = url, isBuffering = true) }

        scope.launch {
            try {
                val localUri = getOrDownload(url)
                getOrCreatePlayer().apply {
                    val speed = _playerState.value.playbackSpeed
                    playbackParameters = PlaybackParameters(speed)
                    setMediaItem(MediaItem.fromUri(localUri))
                    prepare()
                    play()
                }
                proximityAudioRouter.start()
            } catch (e: Exception) {
                Timber.e(e, "Failed to play voice note: $url")
                proximityAudioRouter.stop()
                _playerState.update { it.copy(isBuffering = false, playingUrl = null) }
            }
        }
    }

    fun pause() {
        proximityAudioRouter.stop()
        exoPlayer?.volume = 1f
        exoPlayer?.pause()
        _playerState.update { it.copy(isPlaying = false) }
        stopProgressUpdates()
    }

    private fun resume() {
        proximityAudioRouter.start()
        exoPlayer?.play()
        _playerState.update { it.copy(isPlaying = true) }
    }

    fun stop() {
        proximityAudioRouter.stop()
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
        proximityAudioRouter.release()
        try {
            exoPlayer?.volume = 1f
            exoPlayer?.release()
            exoPlayer = null
        } catch (e: Throwable) {
            Timber.w(e, "VoicePlayer: error releasing ExoPlayer")
        }
    }

    private fun stopPlayback() {
        stopProgressUpdates()
        proximityAudioRouter.stop()
        try {
            exoPlayer?.volume = 1f
            exoPlayer?.stop()
            exoPlayer?.clearMediaItems()
        } catch (e: Exception) {
            Timber.w(e, "Error stopping ExoPlayer")
        }
    }

    private suspend fun getOrDownload(url: String): Uri =
        withContext(Dispatchers.IO) {
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
                val player = exoPlayer ?: break
                val pos = player.currentPosition.coerceAtLeast(0L).toInt()
                val dur = player.duration.takeIf { it > 0L }?.toInt() ?: _playerState.value.durationMs
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
        const val PROGRESS_UPDATE_INTERVAL_MS = 150L
        const val SMART_REWIND_THRESHOLD_MS = 3000L
        const val SMART_REWIND_DURATION_MS = 1800L
        const val TRANSITION_SETTLE_DELAY_MS = 120L
    }
}

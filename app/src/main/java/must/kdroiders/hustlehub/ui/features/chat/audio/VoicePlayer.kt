package must.kdroiders.hustlehub.ui.features.chat.audio

import android.media.MediaPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PlayerState(
    val isPlaying: Boolean = false,
    val currentPositionMs: Int = 0,
    val durationMs: Int = 0,
    val playingUrl: String? = null,
)

class VoicePlayer {
    private var mediaPlayer: MediaPlayer? = null
    private val _playerState = MutableStateFlow(PlayerState())
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()
    
    private val scope = CoroutineScope(Dispatchers.Main)
    private var updateJob: Job? = null

    fun play(url: String) {
        if (_playerState.value.playingUrl == url) {
            if (_playerState.value.isPlaying) {
                pause()
            } else {
                resume()
            }
            return
        }

        stop()

        mediaPlayer = MediaPlayer().apply {
            setDataSource(url)
            setOnPreparedListener { mp ->
                mp.start()
                _playerState.update {
                    it.copy(
                        isPlaying = true,
                        durationMs = mp.duration,
                        playingUrl = url,
                    )
                }
                startUpdatingProgress()
            }
            setOnCompletionListener {
                stop()
            }
            prepareAsync()
        }
    }

    private fun resume() {
        mediaPlayer?.start()
        _playerState.update { it.copy(isPlaying = true) }
        startUpdatingProgress()
    }

    fun pause() {
        mediaPlayer?.pause()
        _playerState.update { it.copy(isPlaying = false) }
        stopUpdatingProgress()
    }

    fun stop() {
        stopUpdatingProgress()
        mediaPlayer?.apply {
            try {
                if (isPlaying) {
                    stop()
                }
            } catch (e: Exception) {
                // Ignore state errors on stop
            }
            release()
        }
        mediaPlayer = null
        _playerState.update {
            it.copy(
                isPlaying = false,
                currentPositionMs = 0,
                durationMs = 0,
                playingUrl = null,
            )
        }
    }

    private fun startUpdatingProgress() {
        stopUpdatingProgress()
        updateJob = scope.launch {
            while (true) {
                val current = mediaPlayer?.currentPosition ?: 0
                _playerState.update { it.copy(currentPositionMs = current) }
                delay(200)
            }
        }
    }

    private fun stopUpdatingProgress() {
        updateJob?.cancel()
        updateJob = null
    }
}

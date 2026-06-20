package must.kdroiders.hustlehub.ui.features.chat.presentation.audio

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import java.io.File

class VoiceRecorder(private val context: Context) {
    private var mediaRecorder: MediaRecorder? = null
    private var currentOutputFile: File? = null

    fun startRecording(conversationId: String): File? {
        val cacheDir = context.cacheDir
        val file = File(cacheDir, "voice_${conversationId}_${System.currentTimeMillis()}.m4a")
        currentOutputFile = file

        mediaRecorder = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(file.absolutePath)
                prepare()
                start()
            }
        } catch (e: Exception) {
            mediaRecorder?.release()
            mediaRecorder = null
            null
        }
        return currentOutputFile
    }

    fun stopRecording(): File? {
        try {
            mediaRecorder?.stop()
        } catch (e: Exception) {
            // Can happen if recording is stopped immediately after start
        } finally {
            mediaRecorder?.release()
            mediaRecorder = null
        }
        return currentOutputFile
    }

    fun cancelRecording() {
        try {
            mediaRecorder?.stop()
        } catch (e: Exception) {
        } finally {
            mediaRecorder?.release()
            mediaRecorder = null
        }
        currentOutputFile?.delete()
        currentOutputFile = null
    }
}

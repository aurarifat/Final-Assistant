package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.concurrent.atomic.AtomicBoolean

class MayaAudioManager(private val context: Context) {
    private val tag = "MayaAudioManager"
    private var mediaPlayer: MediaPlayer? = null
    private var currentTempFile: File? = null
    private val isAudioPlaying = AtomicBoolean(false)

    val isPlaying: Boolean
        get() = isAudioPlaying.get()

    /**
     * Streams the ElevenLabs audio ResponseBody to a managed local temp cache and begins playback.
     */
    suspend fun playAudioStream(
        responseBody: ResponseBody,
        onCompletion: (() -> Unit)? = null
    ): Boolean = withContext(Dispatchers.IO) {
        stop()

        var tempFile: File? = null
        try {
            val cacheDir = File(context.cacheDir, "maya_audio").apply { if (!exists()) mkdirs() }
            tempFile = File.createTempFile("eleven_tts_", ".mp3", cacheDir)

            val inputStream: InputStream = responseBody.byteStream()
            val outputStream = FileOutputStream(tempFile)

            val buffer = ByteArray(8192)
            var bytesRead: Int
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
            }
            outputStream.flush()
            outputStream.close()
            inputStream.close()

            if (!tempFile.exists() || tempFile.length() == 0L) {
                Log.e(tag, "Audio stream empty or file write failed")
                tempFile.delete()
                withContext(Dispatchers.Main) { onCompletion?.invoke() }
                return@withContext false
            }

            currentTempFile = tempFile

            withContext(Dispatchers.Main) {
                val mp = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                            .setUsage(AudioAttributes.USAGE_ASSISTANCE_NAVIGATION_GUIDANCE)
                            .build()
                    )
                    setDataSource(tempFile.absolutePath)
                    setOnPreparedListener { player ->
                        isAudioPlaying.set(true)
                        player.start()
                    }
                    setOnCompletionListener { player ->
                        isAudioPlaying.set(false)
                        cleanupPlayer(player)
                        onCompletion?.invoke()
                    }
                    setOnErrorListener { player, what, extra ->
                        Log.e(tag, "MediaPlayer error: what=$what extra=$extra")
                        isAudioPlaying.set(false)
                        cleanupPlayer(player)
                        onCompletion?.invoke()
                        true
                    }
                    prepareAsync()
                }
                mediaPlayer = mp
            }
            true
        } catch (e: Exception) {
            Log.e(tag, "Failed to stream and play audio", e)
            tempFile?.delete()
            isAudioPlaying.set(false)
            withContext(Dispatchers.Main) { onCompletion?.invoke() }
            false
        }
    }

    fun stop() {
        try {
            mediaPlayer?.let { player ->
                if (player.isPlaying) {
                    player.stop()
                }
                cleanupPlayer(player)
            }
        } catch (e: Exception) {
            Log.w(tag, "Error stopping media player", e)
        } finally {
            mediaPlayer = null
            isAudioPlaying.set(false)
        }
    }

    private fun cleanupPlayer(player: MediaPlayer) {
        try {
            player.reset()
            player.release()
        } catch (e: Exception) {
            Log.w(tag, "Error releasing media player", e)
        }
        currentTempFile?.let { file ->
            try {
                if (file.exists()) file.delete()
            } catch (_: Exception) {}
        }
        currentTempFile = null
    }
}

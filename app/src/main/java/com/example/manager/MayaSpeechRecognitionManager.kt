package com.example.manager

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class MayaSpeechRecognitionManager(
    private val context: Context,
    private val onSpeechResult: (text: String, isSingleWord: Boolean) -> Unit
) {
    private val tag = "MayaSpeechManager"
    private val mainHandler = Handler(Looper.getMainLooper())
    private var speechRecognizer: SpeechRecognizer? = null

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _detectedWord = MutableStateFlow<String?>(null)
    val detectedWord: StateFlow<String?> = _detectedWord.asStateFlow()

    private val _recognizedPhrase = MutableStateFlow<String?>(null)
    val recognizedPhrase: StateFlow<String?> = _recognizedPhrase.asStateFlow()

    private val _rmsDb = MutableStateFlow(0f)
    val rmsDb: StateFlow<Float> = _rmsDb.asStateFlow()

    private var shouldKeepListening = false
    private var isRecognitionActive = false

    fun isAvailable(): Boolean {
        return SpeechRecognizer.isRecognitionAvailable(context)
    }

    fun startListening(keepListening: Boolean = false) {
        mainHandler.post {
            shouldKeepListening = keepListening
            if (isRecognitionActive) {
                return@post
            }

            try {
                if (speechRecognizer == null) {
                    speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                        setRecognitionListener(createListener())
                    }
                }

                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toLanguageTag())
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                    putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 500L)
                    putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 1000L)
                    putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 800L)
                    putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
                }

                speechRecognizer?.startListening(intent)
                isRecognitionActive = true
                _isListening.value = true
                Log.d(tag, "Speech recognition started (keepListening=$keepListening)")
            } catch (e: Exception) {
                Log.e(tag, "Error starting speech recognizer", e)
                isRecognitionActive = false
                _isListening.value = false
            }
        }
    }

    fun stopListening() {
        mainHandler.post {
            shouldKeepListening = false
            try {
                speechRecognizer?.stopListening()
            } catch (e: Exception) {
                Log.w(tag, "Error stopping speech recognizer", e)
            } finally {
                isRecognitionActive = false
                _isListening.value = false
                _rmsDb.value = 0f
            }
        }
    }

    fun cancel() {
        mainHandler.post {
            shouldKeepListening = false
            try {
                speechRecognizer?.cancel()
            } catch (e: Exception) {
                Log.w(tag, "Error canceling speech recognizer", e)
            } finally {
                isRecognitionActive = false
                _isListening.value = false
                _rmsDb.value = 0f
            }
        }
    }

    fun destroy() {
        mainHandler.post {
            shouldKeepListening = false
            try {
                speechRecognizer?.destroy()
            } catch (e: Exception) {
                Log.w(tag, "Error destroying speech recognizer", e)
            } finally {
                speechRecognizer = null
                isRecognitionActive = false
                _isListening.value = false
            }
        }
    }

    private fun restartIfActive(delayMillis: Long = 400L) {
        if (!shouldKeepListening) return
        mainHandler.postDelayed({
            if (shouldKeepListening && !isRecognitionActive) {
                startListening(keepListening = true)
            }
        }, delayMillis)
    }

    private fun createListener(): RecognitionListener {
        return object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                _isListening.value = true
            }

            override fun onBeginningOfSpeech() {
                _isListening.value = true
            }

            override fun onRmsChanged(rmsdB: Float) {
                _rmsDb.value = rmsdB.coerceIn(0f, 12f)
            }

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {
                _isListening.value = false
                _rmsDb.value = 0f
            }

            override fun onError(error: Int) {
                isRecognitionActive = false
                _isListening.value = false
                _rmsDb.value = 0f
                Log.d(tag, "SpeechRecognizer error: $error")

                // Handle benign errors (timeout, no speech match) when in keepListening mode
                when (error) {
                    SpeechRecognizer.ERROR_NO_MATCH,
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> {
                        restartIfActive(500L)
                    }
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY,
                    SpeechRecognizer.ERROR_CLIENT -> {
                        try {
                            speechRecognizer?.cancel()
                        } catch (_: Exception) {}
                        restartIfActive(800L)
                    }
                    else -> {
                        restartIfActive(1000L)
                    }
                }
            }

            override fun onResults(results: Bundle?) {
                isRecognitionActive = false
                _isListening.value = false
                _rmsDb.value = 0f

                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val primaryMatch = matches?.firstOrNull()?.trim()

                if (!primaryMatch.isNullOrEmpty()) {
                    val words = primaryMatch.split("\\s+".toRegex()).filter { it.isNotBlank() }
                    val isSingle = words.size == 1
                    val singleWord = if (isSingle) words.first() else words.lastOrNull()

                    _detectedWord.value = singleWord
                    _recognizedPhrase.value = primaryMatch
                    Log.d(tag, "Recognized text: '$primaryMatch' (isSingleWord=$isSingle, detectedWord=$singleWord)")

                    onSpeechResult(primaryMatch, isSingle)
                } else {
                    restartIfActive(400L)
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val partial = matches?.firstOrNull()?.trim()
                if (!partial.isNullOrEmpty()) {
                    val words = partial.split("\\s+".toRegex()).filter { it.isNotBlank() }
                    val latestWord = words.lastOrNull()
                    if (latestWord != null) {
                        _detectedWord.value = latestWord
                    }
                    _recognizedPhrase.value = partial
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        }
    }
}

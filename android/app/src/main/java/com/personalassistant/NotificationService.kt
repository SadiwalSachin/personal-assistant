package com.personalassistant

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.os.Bundle
import android.util.Log
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.facebook.react.bridge.Arguments
import java.util.Locale

class NotificationService : NotificationListenerService(), TextToSpeech.OnInitListener {

    private var speechRecognizer: android.speech.SpeechRecognizer? = null
    private var recogListener: android.speech.RecognitionListener? = null
    private var isListening = false
    private var tts: TextToSpeech? = null
    private var isTtsReady = false
    private var pendingReplyAction: android.app.Notification.Action? = null
    private var pendingIntent: android.app.PendingIntent? = null
    private var replyInputs: Array<android.app.RemoteInput>? = null
    
    // Conversation State
    private enum class VoiceState { IDLE, CONFIRMATION, DICTATION }
    private var currentState = VoiceState.IDLE
    private var currentLanguage = "en-IN" 

    override fun onCreate() {
        super.onCreate()
        Log.d("NotificationService", "Service created, initializing TTS and SpeechRecognizer")
        tts = TextToSpeech(this, this)
        
        android.os.Handler(android.os.Looper.getMainLooper()).post {
             initializeSpeechRecognizer()
        }
    }

    private fun initializeSpeechRecognizer() {
        if (android.speech.SpeechRecognizer.isRecognitionAvailable(this)) {
            speechRecognizer = android.speech.SpeechRecognizer.createSpeechRecognizer(this)
            recogListener = object : android.speech.RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) { Log.d("NotificationService", "Ready for speech") }
                override fun onBeginningOfSpeech() { Log.d("NotificationService", "User started speaking") }
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() { Log.d("NotificationService", "User finished speaking") }
                override fun onError(error: Int) {
                    Log.e("NotificationService", "Speech error: $error")
        isListening = false
                    stopForeground(true)
                    // If error, maybe reset state or say "Sorry, I didn't catch that"
                    if (currentState != VoiceState.IDLE) {
                       speak("Sorry, I didn't catch that. Please try again later.")
                       currentState = VoiceState.IDLE
                    }
                }
                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(android.speech.SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        val spokenText = matches[0]
                        Log.d("NotificationService", "Recognized: $spokenText")
                        processVoiceCommand(spokenText)
                    } else {
            isListening = false
                        stopForeground(true)
                    }
                }
                override fun onPartialResults(partialResults: Bundle?) {}
                override fun onEvent(eventType: Int, params: Bundle?) {}
            }
            speechRecognizer?.setRecognitionListener(recogListener)
        } else {
            Log.e("NotificationService", "Speech Recognition not available")
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.let { textToSpeech ->
                val result = textToSpeech.setLanguage(Locale("en", "IN"))
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    textToSpeech.setLanguage(Locale.US)
                    currentLanguage = "en-US"
                } else {
                    currentLanguage = "en-IN"
                }
                textToSpeech.setSpeechRate(0.9f)
                textToSpeech.setPitch(1.0f)
                
                // Add an UtteranceProgressListener to start listening *after* TTS finishes
                textToSpeech.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {}
                    override fun onDone(utteranceId: String?) {
                        if (utteranceId == "ask_confirmation" || utteranceId == "ask_message") {
                             android.os.Handler(android.os.Looper.getMainLooper()).post {
                                startListening()
                             }
                        }
                    }
                    override fun onError(utteranceId: String?) {}
                })

                isTtsReady = true
                Log.d("NotificationService", "TTS initialized successfully")
            }
        } else {
            Log.e("NotificationService", "TTS initialization failed")
        }
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.d("NotificationService", "Notification Listener Connected!")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        // We typically only want to auto-reply to messaging apps.
        // For simplicity, let's enable it for any app that has a "Reply" action with RemoteInput.
        val extras: Bundle = sbn.notification.extras
        val title = extras.getString("android.title")
        val text = extras.getCharSequence("android.text")?.toString()
        
        Log.d("NotificationService", "Notification received: $title - $text from ${sbn.packageName}")

        // 1. Check for Reply Action
        val replyAction = findReplyAction(sbn.notification)
        
        if (replyAction != null && isTtsReady && title != null) {
            // Store for later
            pendingReplyAction = replyAction
            pendingIntent = replyAction.actionIntent
            replyInputs = replyAction.remoteInputs
            
            // 2. Announce and Ask
            currentState = VoiceState.CONFIRMATION
            val announcement = "Boss, message from ${title}. ${text ?: ""}. Do you want to reply?"
            speak(announcement, "ask_confirmation")
            
        } else if (isTtsReady && title != null) {
            // Just announce without asking
             currentState = VoiceState.IDLE
             val announcement = "Boss, notification from ${title}. ${text ?: ""}"
             speak(announcement, "just_announce")
        }


        // Also emit to React Native
        val map = Arguments.createMap().apply {
            putString("packageName", sbn.packageName)
            putString("title", title)
            putString("text", text)
            putDouble("timestamp", sbn.postTime.toDouble())
        }
        NotificationEventEmitter.emit("onNotification", map)
    }

    private fun findReplyAction(notification: android.app.Notification): android.app.Notification.Action? {
        val actions = notification.actions ?: return null
        for (action in actions) {
            if (action.remoteInputs != null) {
                // Usually the reply action has a RemoteInput with resultKey "text" or acts as a reply
                // We'll return the first one that accepts text input.
                return action
            }
        }
        return null
    }

    private fun speak(text: String, utteranceId: String? = null) {
        tts?.speak(text, TextToSpeech.QUEUE_ADD, null, utteranceId ?: "tts_${System.currentTimeMillis()}")
    }

    private fun startListening() {
        if (!isListening) {
            // Promote to Foreground Service to access microphone from background
            try {
                val notification = android.app.Notification.Builder(this, "default_channel") 
                    .setContentTitle("Voice Assistant")
                    .setContentText("Listening for your reply...")
                    .setSmallIcon(android.R.drawable.ic_menu_call)
                    .build()
                
                // IMPORTANT: ID must be non-zero
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                    startForeground(1337, notification, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE)
                } else {
                    startForeground(1337, notification)
                }
                
                val intent = android.content.Intent(android.speech.RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                intent.putExtra(android.speech.RecognizerIntent.EXTRA_LANGUAGE_MODEL, android.speech.RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                intent.putExtra(android.speech.RecognizerIntent.EXTRA_LANGUAGE, currentLanguage)
                
                speechRecognizer?.startListening(intent)
                isListening = true
                Log.d("NotificationService", "Started listening")
                
            } catch (e: Exception) {
                Log.e("NotificationService", "Failed to start listening: ${e.message}")
                stopForeground(true)
                currentState = VoiceState.IDLE
            }
        }
    }

    private fun processVoiceCommand(spokenText: String) {
        isListening = false
        stopForeground(true) // Done listening for now
        
        when (currentState) {
            VoiceState.CONFIRMATION -> {
                val lower = spokenText.lowercase()
                if (lower.contains("yes") || lower.contains("sure") || lower.contains("okay") || lower.contains("yep")) {
                    currentState = VoiceState.DICTATION
                    speak("What is the message?", "ask_message")
                } else {
                    // "No", "Cancel", "No need", or anything else
                    speak("Okay, ignored.")
                    currentState = VoiceState.IDLE
                    pendingReplyAction = null
                }
            }
            VoiceState.DICTATION -> {
                 // Spoken text IS the message
                 sendReply(spokenText)
                 speak("Reply sent.")
                 currentState = VoiceState.IDLE
            }
            else -> {
                currentState = VoiceState.IDLE
            }
        }
    }

    private fun sendReply(message: String) {
        try {
            if (pendingIntent != null && replyInputs != null) {
                val intent = android.content.Intent()
                val bundle = Bundle()
                
                for (input in replyInputs!!) {
                    bundle.putCharSequence(input.resultKey, message)
                }
                
                android.app.RemoteInput.addResultsToIntent(replyInputs, intent, bundle)
                pendingIntent?.send(this, 0, intent)
                Log.d("NotificationService", "Reply sent: $message")
            }
        } catch (e: Exception) {
            Log.e("NotificationService", "Error sending reply: ${e.message}")
            speak("Sorry, I couldn't send the reply.")
        }
    }
    
    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        Log.d("NotificationService", "Notification Listener Disconnected!")
    }

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        speechRecognizer?.destroy()
        super.onDestroy()
        Log.d("NotificationService", "Service destroyed, TTS and SpeechRecognizer shut down")
    }
}

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

    private var tts: TextToSpeech? = null
    private var isTtsReady = false

    override fun onCreate() {
        super.onCreate()
        Log.d("NotificationService", "Service created, initializing TTS")
        tts = TextToSpeech(this, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.let { textToSpeech ->
                val result = textToSpeech.setLanguage(Locale("en", "IN"))
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("NotificationService", "TTS language not supported, using default")
                    textToSpeech.setLanguage(Locale.US)
                }
                textToSpeech.setSpeechRate(0.9f)
                textToSpeech.setPitch(1.0f)
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
        val extras: Bundle = sbn.notification.extras
        val title = extras.getString("android.title")
        val text = extras.getCharSequence("android.text")?.toString()
        
        Log.d("NotificationService", "Notification received: $title - $text from ${sbn.packageName}")

        // Speak the notification using native TTS
        if (isTtsReady && title != null) {
            val announcement = "Boss, this is the notification from ${title}. ${text ?: ""}"
            tts?.speak(announcement, TextToSpeech.QUEUE_ADD, null, "notification_${System.currentTimeMillis()}")
            Log.d("NotificationService", "TTS speaking: $announcement")
        }

        // Also emit to React Native (for UI updates when app is open)
        val map = Arguments.createMap().apply {
            putString("packageName", sbn.packageName)
            putString("title", title)
            putString("text", text)
            putDouble("timestamp", sbn.postTime.toDouble())
        }

        NotificationEventEmitter.emit("onNotification", map)
        Log.d("NotificationService", "Event emitted to React Native")
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        Log.d("NotificationService", "Notification Listener Disconnected!")
    }

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
        Log.d("NotificationService", "Service destroyed, TTS shut down")
    }
}

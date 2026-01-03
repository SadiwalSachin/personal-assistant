package com.personalassistant

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.os.Bundle
import android.util.Log
import com.facebook.react.bridge.Arguments

class NotificationService : NotificationListenerService() {

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.d("NotificationService", "Notification Listener Connected!")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val extras: Bundle = sbn.notification.extras
        val title = extras.getString("android.title")
        val text = extras.getCharSequence("android.text")?.toString()
        
        Log.d("NotificationService", "Notification received: $title - $text from ${sbn.packageName}")

        val map = Arguments.createMap().apply {
            putString("packageName", sbn.packageName)  // Fixed: was "package", now "packageName"
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
}

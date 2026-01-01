package com.personalassistant

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.os.Bundle
import com.facebook.react.bridge.Arguments

class NotificationService : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification) {

        val extras: Bundle = sbn.notification.extras

        val map = Arguments.createMap().apply {
            putString("package", sbn.packageName)
            putString("title", extras.getString("android.title"))
            putString("text", extras.getCharSequence("android.text")?.toString())
            putDouble("timestamp", sbn.postTime.toDouble())
        }

        NotificationEventEmitter.emit("onNotification", map)
    }
}

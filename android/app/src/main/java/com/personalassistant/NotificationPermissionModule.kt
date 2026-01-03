package com.personalassistant

import android.content.Intent
import android.provider.Settings
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod

import com.facebook.react.bridge.Promise

class NotificationPermissionModule(
    reactContext: ReactApplicationContext
) : ReactContextBaseJavaModule(reactContext) {

    override fun getName(): String {
        return "NotificationPermission"
    }

    override fun initialize() {
        super.initialize()
        NotificationEventEmitter.setReactContext(reactApplicationContext)
    }

    @ReactMethod
    fun openNotificationAccessSettings() {
        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        reactApplicationContext.startActivity(intent)
    }

    @ReactMethod
    fun checkNotificationAccess(promise: Promise) {
        val packageName = reactApplicationContext.packageName
        val flat = Settings.Secure.getString(reactApplicationContext.contentResolver, "enabled_notification_listeners")
        val enabled = flat != null && flat.contains(packageName)
        promise.resolve(enabled)
    }
}

package com.personalassistant

import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.modules.core.DeviceEventManagerModule

object NotificationEventEmitter {

    private var reactContext: ReactApplicationContext? = null

    fun setReactContext(context: ReactApplicationContext) {
        reactContext = context
    }

    fun emit(eventName: String, data: Any?) {
        reactContext?.getJSModule(
            DeviceEventManagerModule.RCTDeviceEventEmitter::class.java
        )?.emit(eventName, data)
    }
}

# Background Notification Fix - Implementation Details

## Problem
The app was only capturing and announcing notifications when it was open. When the app was closed or in the background, notifications were not being captured or spoken.

## Root Cause
1. **TTS was implemented in React Native layer** - React Native doesn't run when the app is in the background
2. **NotificationService had no native TTS** - The service could capture notifications but couldn't speak them without the React Native bridge

## Solution Implemented

### 1. Native TTS in NotificationService
- Added Android's native `TextToSpeech` API directly to the `NotificationService.kt`
- The service now initializes TTS when it starts and speaks notifications independently
- TTS is configured with Indian English voice (en-IN) with fallback to US English
- Speech rate: 0.9x, Pitch: 1.0x for natural sounding announcements

### 2. Enhanced Service Configuration
**AndroidManifest.xml changes:**
- Added `FOREGROUND_SERVICE` permission for reliable background operation
- Added `WAKE_LOCK` permission to ensure service stays active
- Added service label for better identification in Android settings
- Set `android:enabled="true"` to ensure service starts properly

### 3. Proper Lifecycle Management
- TTS initializes when service is created (`onCreate`)
- TTS is properly shut down when service is destroyed (`onDestroy`)
- Prevents memory leaks and ensures clean operation

### 4. Dual Announcement Prevention
- Removed TTS call from React Native `NotificationsScreen.tsx`
- Native service handles all TTS announcements
- React Native only updates the UI when app is open

## How It Works Now

### When App is CLOSED/Background:
1. Android system delivers notification to your `NotificationService`
2. Service extracts notification title and text
3. **Native TTS speaks**: "Boss, this is the notification from [title]. [text]"
4. Notification is logged for debugging

### When App is OPEN:
1. Same as above, PLUS:
2. Service emits event to React Native via `NotificationEventEmitter`
3. React Native updates the UI to show notification in the list
4. TTS still handled by native service (no duplicate announcements)

## Testing Instructions

### Step 1: Clean Build
```bash
cd android
./gradlew clean
cd ..
```

### Step 2: Rebuild and Install
```bash
npx react-native run-android
```

### Step 3: Grant Notification Access Permission
1. Open the app
2. When prompted, tap "Open Settings"
3. Find "Personal Assistant" in the list
4. Toggle ON the notification access
5. Go back to the app

### Step 4: Test Background Operation
1. **Close the app completely** (swipe away from recent apps)
2. Send yourself a test notification (WhatsApp, SMS, email, etc.)
3. **You should hear**: "Boss, this is the notification from [app name]. [message]"
4. The app does NOT need to be open!

### Step 5: Test Foreground Operation
1. Open the app and go to Notifications screen
2. Send yourself a test notification
3. You should:
   - **Hear** the announcement (same as background)
   - **See** the notification appear in the list

## Debugging

### Check if Service is Running
```bash
adb shell dumpsys notification_listener
```
Look for `com.personalassistant/com.personalassistant.NotificationService`

### View Logs
```bash
adb logcat | grep NotificationService
```

You should see:
- "Service created, initializing TTS"
- "TTS initialized successfully"
- "Notification received: [title] - [text]"
- "TTS speaking: Boss, this is the notification..."

### Common Issues

**Issue: No sound when app is closed**
- Solution: Make sure notification access permission is granted
- Check: Settings → Apps → Special Access → Notification Access

**Issue: TTS not working at all**
- Solution: Check if TTS engine is installed on device
- Go to: Settings → System → Languages & Input → Text-to-Speech
- Install Google Text-to-Speech if needed

**Issue: Service disconnects**
- Solution: Disable battery optimization for the app
- Go to: Settings → Apps → Personal Assistant → Battery → Unrestricted

## Files Modified

1. **NotificationService.kt** - Added native TTS implementation
2. **AndroidManifest.xml** - Added permissions and service configuration
3. **NotificationsScreen.tsx** - Removed duplicate TTS call
4. **No changes needed to**: NotificationEventEmitter, NotificationPermissionModule

## Technical Details

### TTS Configuration
- Language: English (India) - `Locale("en", "IN")`
- Fallback: English (US) - `Locale.US`
- Speech Rate: 0.9 (slightly slower for clarity)
- Pitch: 1.0 (normal pitch)
- Queue Mode: `QUEUE_ADD` (queues multiple notifications)

### Service Lifecycle
```
onCreate() → Initialize TTS
  ↓
onInit() → Configure TTS (language, rate, pitch)
  ↓
onListenerConnected() → Service ready
  ↓
onNotificationPosted() → Speak notification + emit to React Native
  ↓
onDestroy() → Shutdown TTS
```

## Performance Considerations
- TTS initialization is async (doesn't block notification processing)
- Service runs in background with minimal battery impact
- Notifications are queued if TTS is still speaking
- Proper cleanup prevents memory leaks

---

**Status**: ✅ Background notifications now work independently of app state
**Last Updated**: January 4, 2026

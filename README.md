# 🤖 Personal Assistant App

A premium, intelligent personal assistant application built with React Native. Takes care of your notifications by announcing them audibly, addressing you as "Boss", and providing a sleek, dark-themed control center.

## ✨ Features

- **🗣️ Smart Voice Announcements**
  - Reads out incoming notifications automatically.
  - Personal touch: Addresses you as "Boss" before every announcement.
  - Example: *"Boss, notification is there. WhatsApp. Message from John..."*
  - Uses localized Indian English accent (`en-IN`) for a natural feel.

- **🔔 Advanced Notification Center**
  - Real-time listening and capturing of system notifications.
  - Displays notifications in a beautiful card-based list.
  - Shows app icons (WhatsApp, Instagram, Gmail, etc.) for quick recognition.
  - Timestamps and "Clear All" functionality.

- **🎨 Premium UI/UX**
  - **Dark Mode First**: Sleek, AMOLED-friendly dark interface (`#0a0a0f`).
  - **Smooth Navigation**: Stack navigation with custom animations.
  - **Interactive Elements**: Touch feedback, micro-interactions, and status indicators.

## 🛠️ Tech Stack

- **Core**: React Native (0.83.1)
- **Language**: TypeScript
- **Navigation**: React Navigation (Native Stack)
- **Text-to-Speech**: `react-native-tts`
- **Notifications**: Custom Native Module integration (`NotificationPermission`)

## 🚀 Getting Started

### Prerequisites

- Node.js & npm/yarn
- Android Studio / Android SDK (for Android build)
- Java Development Kit (JDK) 17

### Installation

1. **Clone the repository**
   ```bash
   git clone <your-repo-url>
   cd personalAssistant
   ```

2. **Install Dependencies**
   ```bash
   npm install
   ```

3. **Run the Application**
   ```bash
   # For Android
   npm run android
   
   # For iOS
   npm run ios
   ```

## 📱 Application Flow

1. **Home Screen**: 
   - Welcomes you with "Hello, Boss 👋".
   - Quick access cards to **Notifications** and **Notification Access Settings**.
   
2. **Permission Setup**:
   - On first launch, tap "Notification Access" to grant permission in Android settings.
   
3. **Notification Screen**:
   - View history of all spoken notifications.
   - Clear history when needed.

## 📝 Configuration

The TTS settings can be customized in `src/services/ttsService.ts`:

```typescript
Tts.setDefaultLanguage('en-IN'); // Set Language
Tts.setDefaultRate(0.5);         // Set Speed
Tts.setDefaultPitch(1.0);        // Set Pitch
```

## 🤝 Contributing

Feel free to fork this project and submit pull requests. You are the Boss!

---
*Built with ❤️ for the Boss.*

import Tts from 'react-native-tts';

// Initialize TTS with Indian English voice
export const initializeTts = () => {
    Tts.setDefaultLanguage('en-IN');
    Tts.setDefaultRate(0.5);
    Tts.setDefaultPitch(1.0);
};

// Announce notification with "Boss" prefix
export const announceNotification = (title: string, text: string) => {
    const announcement = `Boss, this is the notification from ${title ?? 'unknown source'}. ${text ?? ''}`;
    Tts.stop();
    Tts.speak(announcement);
};

// Stop any ongoing speech
export const stopSpeech = () => {
    Tts.stop();
};

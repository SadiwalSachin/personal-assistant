import {
  View,
  Text,
  FlatList,
  NativeEventEmitter,
  NativeModules,
  TouchableOpacity,
} from 'react-native';
import { useEffect, useState } from 'react';
import Tts from 'react-native-tts';

const { NotificationPermission } = NativeModules;

const eventEmitter = new NativeEventEmitter(
  NativeModules.NotificationPermission
);

export default function App() {

  // 🔊 TTS Configuration
  Tts.setDefaultLanguage('en-IN');
  Tts.setDefaultRate(0.5);
  Tts.setDefaultPitch(1.0);

  const openSettings = () => {
    NotificationPermission.openNotificationAccessSettings();
  };

  const [notifications, setNotifications] = useState<any[]>([]);

  useEffect(() => {
    const sub = eventEmitter.addListener(
      'onNotification',
      (data) => {

        // 🔊 TEXT TO SPEECH (ADDED)
        const message = `${data.title ?? ''}. ${data.text ?? ''}`;
        Tts.stop();
        Tts.speak(message);

        // Existing logic (unchanged)
        setNotifications(prev => [data, ...prev]);
      }
    );

    return () => sub.remove();
  }, []);

  return (
    <>
      <View
        style={{
          flex: 1,
          justifyContent: 'center',
          alignItems: 'center',
          backgroundColor: 'white',
        }}
      >
        <Text style={{ color: 'black', fontSize: 18 }}>App</Text>

        <TouchableOpacity
          style={{
            marginTop: 20,
            padding: 10,
            backgroundColor: 'lightblue',
            borderRadius: 10,
          }}
          onPress={openSettings}
        >
          <Text style={{ color: 'black' }}>
            Open Notification Access Settings
          </Text>
        </TouchableOpacity>
      </View>

      <FlatList
        data={notifications}
        keyExtractor={(_, i) => i.toString()}
        renderItem={({ item }) => (
          <View style={{ padding: 12, backgroundColor: '#222' }}>
            <Text style={{ color: 'white', fontWeight: 'bold' }}>
              {item.title}
            </Text>
            <Text style={{ color: 'white' }}>
              {item.text}
            </Text>
          </View>
        )}
      />
    </>
  );
}

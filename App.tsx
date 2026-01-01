import React, { useEffect } from 'react';
import { NavigationContainer } from '@react-navigation/native';
import AppNavigator from './src/navigation/AppNavigator';
import { initializeTts } from './src/services/ttsService';

export default function App() {
  useEffect(() => {
    // Initialize TTS on app start
    initializeTts();
  }, []);

  return (
    <NavigationContainer>
      <AppNavigator />
    </NavigationContainer>
  );
}

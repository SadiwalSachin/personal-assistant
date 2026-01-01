import React from 'react';
import {
    View,
    Text,
    TouchableOpacity,
    StyleSheet,
    StatusBar,
    NativeModules,
} from 'react-native';
import { NativeStackNavigationProp } from '@react-navigation/native-stack';
import { RootStackParamList } from '../navigation/NavigationTypes';

const { NotificationPermission } = NativeModules;

type HomeScreenProps = {
    navigation: NativeStackNavigationProp<RootStackParamList, 'Home'>;
};

export default function HomeScreen({ navigation }: HomeScreenProps) {
    const openSettings = () => {
        NotificationPermission.openNotificationAccessSettings();
    };

    return (
        <View style={styles.container}>
            <StatusBar barStyle="light-content" backgroundColor="#0a0a0f" />

            {/* Header */}
            <View style={styles.header}>
                <Text style={styles.greeting}>Hello,</Text>
                <Text style={styles.title}>Boss 👋</Text>
                <Text style={styles.subtitle}>Your personal assistant is ready</Text>
            </View>

            {/* Cards Container */}
            <View style={styles.cardsContainer}>
                {/* Notifications Card */}
                <TouchableOpacity
                    style={styles.card}
                    onPress={() => navigation.navigate('Notifications')}
                    activeOpacity={0.8}
                >
                    <View style={styles.cardIconContainer}>
                        <Text style={styles.cardIcon}>🔔</Text>
                    </View>
                    <Text style={styles.cardTitle}>Notifications</Text>
                    <Text style={styles.cardDescription}>
                        View all incoming notifications with TTS announcements
                    </Text>
                    <View style={styles.cardArrow}>
                        <Text style={styles.arrowText}>→</Text>
                    </View>
                </TouchableOpacity>

                {/* Settings Card */}
                <TouchableOpacity
                    style={[styles.card, styles.settingsCard]}
                    onPress={openSettings}
                    activeOpacity={0.8}
                >
                    <View style={[styles.cardIconContainer, styles.settingsIconContainer]}>
                        <Text style={styles.cardIcon}>⚙️</Text>
                    </View>
                    <Text style={styles.cardTitle}>Notification Access</Text>
                    <Text style={styles.cardDescription}>
                        Open settings to grant notification access permission
                    </Text>
                    <View style={styles.cardArrow}>
                        <Text style={styles.arrowText}>→</Text>
                    </View>
                </TouchableOpacity>
            </View>

            {/* Footer */}
            <View style={styles.footer}>
                <Text style={styles.footerText}>Personal Assistant v1.0</Text>
            </View>
        </View>
    );
}

const styles = StyleSheet.create({
    container: {
        flex: 1,
        backgroundColor: '#0a0a0f',
        paddingHorizontal: 20,
    },
    header: {
        marginTop: 60,
        marginBottom: 40,
    },
    greeting: {
        fontSize: 18,
        color: '#888',
        fontWeight: '400',
    },
    title: {
        fontSize: 36,
        color: '#fff',
        fontWeight: '700',
        marginTop: 4,
    },
    subtitle: {
        fontSize: 16,
        color: '#666',
        marginTop: 8,
    },
    cardsContainer: {
        flex: 1,
        gap: 16,
    },
    card: {
        backgroundColor: '#1a1a24',
        borderRadius: 20,
        padding: 24,
        borderWidth: 1,
        borderColor: '#2a2a3a',
    },
    settingsCard: {
        backgroundColor: '#141420',
    },
    cardIconContainer: {
        width: 56,
        height: 56,
        borderRadius: 16,
        backgroundColor: '#6366f1',
        justifyContent: 'center',
        alignItems: 'center',
        marginBottom: 16,
    },
    settingsIconContainer: {
        backgroundColor: '#3b82f6',
    },
    cardIcon: {
        fontSize: 28,
    },
    cardTitle: {
        fontSize: 22,
        color: '#fff',
        fontWeight: '600',
        marginBottom: 8,
    },
    cardDescription: {
        fontSize: 14,
        color: '#888',
        lineHeight: 20,
    },
    cardArrow: {
        position: 'absolute',
        right: 24,
        top: 24,
        width: 40,
        height: 40,
        borderRadius: 12,
        backgroundColor: '#2a2a3a',
        justifyContent: 'center',
        alignItems: 'center',
    },
    arrowText: {
        fontSize: 20,
        color: '#fff',
    },
    footer: {
        paddingVertical: 24,
        alignItems: 'center',
    },
    footerText: {
        color: '#444',
        fontSize: 12,
    },
});

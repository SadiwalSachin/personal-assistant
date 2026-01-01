import React, { useEffect, useState } from 'react';
import {
    View,
    Text,
    FlatList,
    StyleSheet,
    TouchableOpacity,
    StatusBar,
    NativeEventEmitter,
    NativeModules,
} from 'react-native';
import { NativeStackNavigationProp } from '@react-navigation/native-stack';
import { RootStackParamList } from '../navigation/NavigationTypes';
import { announceNotification } from '../services/ttsService';

const eventEmitter = new NativeEventEmitter(NativeModules.NotificationPermission);

type NotificationItem = {
    id: string;
    title: string;
    text: string;
    packageName: string;
    timestamp: number;
};

type NotificationsScreenProps = {
    navigation: NativeStackNavigationProp<RootStackParamList, 'Notifications'>;
};

export default function NotificationsScreen({ navigation }: NotificationsScreenProps) {
    const [notifications, setNotifications] = useState<NotificationItem[]>([]);

    useEffect(() => {
        const subscription = eventEmitter.addListener('onNotification', (data) => {
            const newNotification: NotificationItem = {
                id: Date.now().toString(),
                title: data.title ?? 'No Title',
                text: data.text ?? '',
                packageName: data.packageName ?? '',
                timestamp: Date.now(),
            };

            // Announce with TTS
            announceNotification(newNotification.title, newNotification.text);

            // Add to list
            setNotifications(prev => [newNotification, ...prev]);
        });

        return () => subscription.remove();
    }, []);

    const clearAll = () => {
        setNotifications([]);
    };

    const formatTime = (timestamp: number) => {
        const date = new Date(timestamp);
        return date.toLocaleTimeString('en-US', {
            hour: '2-digit',
            minute: '2-digit',
            hour12: true,
        });
    };

    const getAppIcon = (packageName: string) => {
        // Return emoji based on common apps
        if (packageName.includes('whatsapp')) return '💬';
        if (packageName.includes('instagram')) return '📷';
        if (packageName.includes('youtube')) return '▶️';
        if (packageName.includes('gmail') || packageName.includes('mail')) return '📧';
        if (packageName.includes('chrome')) return '🌐';
        if (packageName.includes('phone') || packageName.includes('dialer')) return '📞';
        if (packageName.includes('message') || packageName.includes('sms')) return '💬';
        return '🔔';
    };

    const renderNotification = ({ item }: { item: NotificationItem }) => (
        <View style={styles.notificationCard}>
            <View style={styles.notificationHeader}>
                <View style={styles.iconContainer}>
                    <Text style={styles.appIcon}>{getAppIcon(item.packageName)}</Text>
                </View>
                <View style={styles.notificationContent}>
                    <Text style={styles.notificationTitle} numberOfLines={1}>
                        {item.title}
                    </Text>
                    <Text style={styles.notificationTime}>{formatTime(item.timestamp)}</Text>
                </View>
            </View>
            <Text style={styles.notificationText} numberOfLines={3}>
                {item.text}
            </Text>
            <View style={styles.notificationFooter}>
                <Text style={styles.packageName} numberOfLines={1}>
                    {item.packageName.split('.').pop()}
                </Text>
            </View>
        </View>
    );

    const renderEmptyState = () => (
        <View style={styles.emptyContainer}>
            <Text style={styles.emptyIcon}>🔔</Text>
            <Text style={styles.emptyTitle}>No Notifications Yet</Text>
            <Text style={styles.emptyText}>
                Incoming notifications will appear here and be announced with TTS
            </Text>
        </View>
    );

    return (
        <View style={styles.container}>
            <StatusBar barStyle="light-content" backgroundColor="#0a0a0f" />

            {/* Header */}
            <View style={styles.header}>
                <TouchableOpacity
                    style={styles.backButton}
                    onPress={() => navigation.goBack()}
                >
                    <Text style={styles.backArrow}>←</Text>
                </TouchableOpacity>
                <Text style={styles.headerTitle}>Notifications</Text>
                {notifications.length > 0 && (
                    <TouchableOpacity style={styles.clearButton} onPress={clearAll}>
                        <Text style={styles.clearText}>Clear All</Text>
                    </TouchableOpacity>
                )}
            </View>

            {/* Notification Count Badge */}
            {notifications.length > 0 && (
                <View style={styles.countBadge}>
                    <Text style={styles.countText}>
                        {notifications.length} notification{notifications.length !== 1 ? 's' : ''}
                    </Text>
                </View>
            )}

            {/* Notifications List */}
            <FlatList
                data={notifications}
                keyExtractor={(item) => item.id}
                renderItem={renderNotification}
                contentContainerStyle={[
                    styles.listContainer,
                    notifications.length === 0 && styles.emptyListContainer,
                ]}
                ListEmptyComponent={renderEmptyState}
                showsVerticalScrollIndicator={false}
            />
        </View>
    );
}

const styles = StyleSheet.create({
    container: {
        flex: 1,
        backgroundColor: '#0a0a0f',
    },
    header: {
        flexDirection: 'row',
        alignItems: 'center',
        paddingHorizontal: 20,
        paddingTop: 50,
        paddingBottom: 20,
        backgroundColor: '#0a0a0f',
    },
    backButton: {
        width: 44,
        height: 44,
        borderRadius: 14,
        backgroundColor: '#1a1a24',
        justifyContent: 'center',
        alignItems: 'center',
        marginRight: 16,
    },
    backArrow: {
        fontSize: 22,
        color: '#fff',
    },
    headerTitle: {
        flex: 1,
        fontSize: 24,
        fontWeight: '700',
        color: '#fff',
    },
    clearButton: {
        paddingHorizontal: 16,
        paddingVertical: 10,
        borderRadius: 12,
        backgroundColor: '#ef4444',
    },
    clearText: {
        color: '#fff',
        fontSize: 14,
        fontWeight: '600',
    },
    countBadge: {
        marginHorizontal: 20,
        marginBottom: 16,
        paddingHorizontal: 16,
        paddingVertical: 10,
        backgroundColor: '#1a1a24',
        borderRadius: 12,
        alignSelf: 'flex-start',
        borderWidth: 1,
        borderColor: '#6366f1',
    },
    countText: {
        color: '#6366f1',
        fontSize: 14,
        fontWeight: '600',
    },
    listContainer: {
        paddingHorizontal: 20,
        paddingBottom: 20,
    },
    emptyListContainer: {
        flex: 1,
        justifyContent: 'center',
    },
    notificationCard: {
        backgroundColor: '#1a1a24',
        borderRadius: 20,
        padding: 20,
        marginBottom: 12,
        borderWidth: 1,
        borderColor: '#2a2a3a',
    },
    notificationHeader: {
        flexDirection: 'row',
        alignItems: 'center',
        marginBottom: 12,
    },
    iconContainer: {
        width: 48,
        height: 48,
        borderRadius: 14,
        backgroundColor: '#6366f1',
        justifyContent: 'center',
        alignItems: 'center',
        marginRight: 14,
    },
    appIcon: {
        fontSize: 24,
    },
    notificationContent: {
        flex: 1,
        flexDirection: 'row',
        justifyContent: 'space-between',
        alignItems: 'center',
    },
    notificationTitle: {
        flex: 1,
        fontSize: 18,
        fontWeight: '600',
        color: '#fff',
        marginRight: 10,
    },
    notificationTime: {
        fontSize: 12,
        color: '#666',
    },
    notificationText: {
        fontSize: 15,
        color: '#aaa',
        lineHeight: 22,
        marginBottom: 12,
    },
    notificationFooter: {
        borderTopWidth: 1,
        borderTopColor: '#2a2a3a',
        paddingTop: 12,
    },
    packageName: {
        fontSize: 12,
        color: '#555',
    },
    emptyContainer: {
        alignItems: 'center',
        paddingHorizontal: 40,
    },
    emptyIcon: {
        fontSize: 64,
        marginBottom: 20,
        opacity: 0.5,
    },
    emptyTitle: {
        fontSize: 22,
        fontWeight: '600',
        color: '#fff',
        marginBottom: 12,
    },
    emptyText: {
        fontSize: 16,
        color: '#666',
        textAlign: 'center',
        lineHeight: 24,
    },
});

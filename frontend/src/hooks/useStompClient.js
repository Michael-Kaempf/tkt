import { useState, useEffect, useCallback } from 'react';
import { Client } from '@stomp/stompjs';

export function useStompClient({ onWordCount }) {
    const [connectionStatus, setConnectionStatus] = useState('connecting');

    useEffect(() => {
        const client = new Client({
            brokerURL: 'ws://localhost:8080/ws/websocket',
            debug: function (str) {
                console.debug(str);
            },
            reconnectDelay: 5000,
            heartbeatIncoming: 4000,
            heartbeatOutgoing: 4000,
        });

        client.onConnect = () => {
            setConnectionStatus('connected');

            // Subscribe to the word count topic
            client.subscribe('/topic/wordcount', message => {
                try {
                    const wordCount = JSON.parse(message.body);
                    onWordCount(wordCount.wordFrequencies);
                } catch (error) {
                    console.error('Error parsing message:', error);
                }
            });
        };

        client.onDisconnect = () => {
            setConnectionStatus('disconnected');
        };

        client.onStompError = (frame) => {
            console.error('STOMP error:', frame);
            setConnectionStatus('error');
        };

        // Handle WebSocket close event
        client.webSocketFactory = () => {
            const socket = new WebSocket('ws://localhost:8080/ws/websocket');

            socket.onclose = () => {
                console.warn('WebSocket closed unexpectedly');
                setConnectionStatus('disconnected');
            };

            socket.onerror = (error) => {
                console.error('WebSocket error:', error);
                setConnectionStatus('error');
            };

            return socket;
        };

        // Start the connection
        client.activate();

        // Cleanup on unmount
        return () => {
            if (client.connected) {
                client.deactivate();
            }
        };
    }, [onWordCount]);

    return { connectionStatus };
}

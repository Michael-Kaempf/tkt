import React, { useState, useEffect, useCallback } from 'react';
import WordCloud from './components/WordCloud';
import { useStompClient } from './hooks/useStompClient';

function App() {
    const [wordCount, setWordCount] = useState({});

    const onWordCount = useCallback((newWordCount) => {
        setWordCount(newWordCount);
    }, []);

    const { connectionStatus } = useStompClient({
        onWordCount
    });

    return (
        <div className="min-h-screen bg-gray-100 p-4">
            <div className="max-w-7xl mx-auto">
                <header className="mb-4">
                    <h1 className="text-3xl font-bold text-gray-900">Blog Word Counter</h1>
                    <div className={`text-sm mt-2 ${
                        connectionStatus === 'connected' ? 'text-green-600' :
                            connectionStatus === 'error' ? 'text-red-600' :
                                'text-yellow-600'
                    }`}>
                        Status: {connectionStatus}
                    </div>
                </header>
                <main>
                    <WordCloud wordCount={wordCount} />
                </main>
            </div>
        </div>
    );
}

export default App;
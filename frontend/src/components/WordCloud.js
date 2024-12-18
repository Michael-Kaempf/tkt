import React from 'react';

const WordCloud = ({ wordCount }) => {
    const sortedWords = Object.entries(wordCount)
        .sort(([, a], [, b]) => b - a);

    return (
        <div className="bg-white rounded-lg shadow p-4">
            <h2 className="text-xl font-semibold mb-4">Word Frequency</h2>
            <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4 w-full">
                {sortedWords.map(([word, count]) => (
                    <div
                        key={word}
                        className="p-2 bg-blue-100 rounded flex justify-between items-center"
                    >
                        <span className="font-medium truncate mr-2">{word}</span>
                        <span className="text-blue-600 flex-shrink-0">{count}</span>
                    </div>
                ))}
            </div>
        </div>
    );
};

export default WordCloud;

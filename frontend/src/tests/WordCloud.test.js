import { render, screen } from '@testing-library/react';
import WordCloud from '../components/WordCloud';

describe('WordCloud', () => {
    test('renders word frequencies correctly', () => {
        const wordCount = {
            'test': 5,
            'example': 3
        };

        render(<WordCloud wordCount={wordCount} />);

        expect(screen.getByText('test')).toBeInTheDocument();
        expect(screen.getByText('5')).toBeInTheDocument();
        expect(screen.getByText('example')).toBeInTheDocument();
        expect(screen.getByText('3')).toBeInTheDocument();
    });

    test('handles empty word count', () => {
        render(<WordCloud wordCount={{}} />);
        expect(screen.getByText('Word Frequency')).toBeInTheDocument();
    });
});

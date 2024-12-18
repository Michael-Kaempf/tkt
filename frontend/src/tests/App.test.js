import { render, screen } from '@testing-library/react';
import App from '../App';

jest.mock('../hooks/useStompClient', () => ({
    useStompClient: () => ({
        connectionStatus: 'connected'
    })
}));

describe('App', () => {
    test('renders header and status', () => {
        render(<App />);
        expect(screen.getByText('Blog Word Counter')).toBeInTheDocument();
        expect(screen.getByText('Status: connected')).toBeInTheDocument();
    });
});


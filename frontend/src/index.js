import React from 'react';
import ReactDOM from 'react-dom';
import App from './App'; // Importiere deine Haupt-Komponente
import './index.css';     // Optional: Dein CSS-Stylesheet

// Render die App in das HTML-Element mit der ID "root"
ReactDOM.render(
    <React.StrictMode>
        <App />
    </React.StrictMode>,
    document.getElementById('root')
);

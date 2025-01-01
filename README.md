# Blog Word Counter

A real-time blog word analysis system that fetches blog posts from WordPress sites and visualizes word frequencies.

## System Architecture

### Backend (Spring Boot)

- RESTful API for fetching blog posts (using application.yaml configuration)
- WebSocket server for real-time word frequency updates
- Word frequency analysis engine with customizable logic (WordAnalyzerService)
- Scheduled tasks for periodic blog fetching (configuration in application.yaml)

### Frontend (React)

- Real-time word cloud visualization component
- WebSocket client for receiving live word frequency updates
- Responsive grid layout for optimal display on various devices
- Tailwind CSS for styling

## Prerequisites

Before you begin, ensure you have the following software installed:

- Java 21 or higher:
    - Linux (apt-based, e.g., Ubuntu, Debian): sudo apt update && sudo apt install openjdk-21-jdk
    - Linux (yum-based, e.g., CentOS, Fedora): sudo yum install java-21-openjdk-devel
    - macOS (using Homebrew): brew tap homebrew/cask-versions && brew install --cask temurin21
    - Windows: Download the JDK from https://adoptium.net/temurin/releases and follow the installation instructions. Set
      the JAVA_HOME environment variable.
- Node.js 20 or higher:
    - It is highly recommended to use a Node version manager like nvm (Node Version Manager) or n to manage Node
      versions.
    - Using nvm (Linux, macOS, Windows Subsystem for Linux):
    - Install nvm: Follow the instructions at https://github.com/nvm-sh/nvm.
    - Install Node.js 20: nvm install 20
    - Use Node.js 20: nvm use 20
    - Using n (Linux, macOS): npm install -g n && n 20
    - Direct Installation (Less recommended): Download the installer from https://nodejs.org/ and follow the
      instructions.
- npm 9 or higher: npm is typically installed with Node.js. You can update it with: npm install -g npm@latest
- Gradle 8.5 or higher:
    - Using SDKMAN! (Linux, macOS, Windows Subsystem for Linux, Cygwin, Windows Git Bash): sdk install gradle
    - Manual installation: Download the binary distribution from https://gradle.org/releases/ and follow the
      installation instructions. Set the GRADLE_HOME and add %GRADLE_HOME%\bin to your PATH environment variable (
      Windows) or \$GRADLE_HOME/bin to your \$PATH (Linux/macOS).
- Docker (optional)

## Installation & Setup

### Backend Setup

1. Clone the repository

```bash
git clone https://github.com/Michael-Kaempf/tkt.git
cd tkt
```

2. Build and run the backend

```bash
cd backend
gradle clean build
gradle bootRun
```

The backend service will be available at http://localhost:8080.

3. Configure

done in file `src/main/resources/application.yaml`:

```yaml
server:
  port: 8080

spring:
  application:
    name: blog-word-counter

wordpress:
  api:
    base-url: https://thekey.academy/wp-json/wp/v2
    initial-delay: 5000 # 5 seconds
    fetch-interval: 10000  # 10 seconds
    posts-per-page: 10

logging:
  level:
    root: INFO
    com.example.blogcounter: DEBUG
  file:
    name: logs/application.log
```

### Frontend Setup

1. Navigate to frontend

```bash
cd ../frontend
```

2. Install dependencies

```bash
npm install
```

3. Start development server

```bash
npm start
```

The frontend will be available at `http://localhost:3000`

Note: Development mode supports:

- Hot reloading
- Live CSS updates
- Source maps
- Development error overlay

## Docker Deployment

### Using Docker Compose

1. Build and start services:

```bash
docker-compose up --build
```

2. Access services:

- Frontend: http://localhost:3000
- Backend: http://localhost:8080

### Manual Docker Build

```bash
# Backend
cd backend
gradle build
docker build -t backend:local .

# Frontend
cd ../frontend
npm run build
docker build -t frontend:local .
```

## Testing

### Backend Tests

```bash
cd backend
gradle test jacocoTestReport
```

### Frontend Tests

```bash
cd ../frontend
npm test
```

## API Documentation

### WebSocket

- Connect to `ws://localhost:8080/ws/websocket`
- Receives real-time updates of word frequencies in JSON format

## Project Structure

```
blog-word-counter/
├── backend/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── com/example/blogcounter/
│   │   │   └── resources/
│   │   │       └── application.yaml
│   │   └── test/
│   ├── build.gradle
│   └── Dockerfile
├── frontend/
│   ├── src/
│   │   ├── components/
│   │   ├── hooks/
│   │   ├── tests/
│   │   ├── App.js
│   │   ├── index.css
│   │   └── index.js
│   ├── package.json
│   ├── tailwind.config.js
│   ├── Dockerfile
│   └── nginx.conf
├── docker-compose.yaml
└── README.md
```

## Development

### Change New Blog Source

Change WordPress site URLs in `application.yaml`:

use

```yaml
wordpress:
  api:
    base-url:
      - https://thekey.academy/wp-json/wp/v2
```

or

```yaml
wordpress:
  api:
    base-url:
      - https://internate.org/wp-json/wp/v2
```

### Customizing Word Analysis

Modify `WordAnalyzerService` to adjust:

- Word filtering
- Minimum word length
- Stop words
- Language-specific processing

### Backend Setup

Change to project root.

Build production JAR:

```bash
cd backend
gradle bootJar
```

Run with production profile:

```bash
java -jar backend/build/libs/backend-0.0.1-SNAPSHOT.jar
```

### Frontend Setup

1. Navigate and install

```bash
cd frontend
```

2. Install dependencies

```bash
npm install
```

3. Start development server

```bash
# Development mode with hot reloading
npm run dev

# or to start standard development server
npm start
```

Development server will be available at `http://localhost:3000`

## Production Deployment

### Backend

Build production JAR:

```bash
gradle bootJar -Pprod
```

Run with production profile:

```bash
java -jar backend/build/libs/backend-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

### Frontend

Build production bundle:

```bash
cd frontend
npm run build
```

### Docker Deployment

### Docker Local Deployment

Change to project root.

```bash
docker-compose -f docker-compose.local.yml up -d
```

### Docker Production Deployment

Change to project root.

```bash
docker-compose -f docker-compose.yml up -d
```

## Monitoring & Maintenance

- Logs in `logs/application.log`
- Spring Boot Actuator endpoints at `/actuator/*`
- Frontend error tracking via browser console

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Support

For support and questions, please open an issue in the GitHub repository.
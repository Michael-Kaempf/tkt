# Blog Word Counter

A real-time blog word analysis system that fetches blog posts from WordPress sites and visualizes word frequencies.

## System Architecture

### Backend (Spring Boot)
- RESTful API for blog post fetching
- WebSocket server for real-time updates
- Word frequency analysis engine
- Scheduled tasks for periodic blog fetching

### Frontend (React)
- Real-time word cloud visualization
- WebSocket client for live updates
- Responsive grid layout
- Tailwind CSS for styling

## Prerequisites

- Java 17 or higher
- Node.js 18 or higher
- npm 9 or higher
- Maven 3.8 or higher

## Installation & Setup

### Backend Setup

1. Clone the repository
```bash
git clone https://github.com/yourusername/blog-word-counter.git
cd blog-word-counter/backend
```

2. Configure the application
   Create or modify `src/main/resources/application.yaml`:
```yaml
server:
  port: 8080

spring:
  application:
    name: blog-word-counter
  
wordpress:
  api:
    base-url: https://internate.org/wp-json/wp/v2
    fetch-interval: 10000  # 10 seconds
    posts-per-page: 10

logging:
  level:
    root: INFO
    com.yourcompany.blogwordcounter: DEBUG
  file:
    name: logs/application.log
```

3. Build and run the application
```bash
./mvnw clean install
./mvnw spring-boot:run
```

The backend will be available at `http://localhost:8080`

### Frontend Setup

1. Navigate to the frontend directory
```bash
cd ../frontend
```

2. Install dependencies
```bash
npm install
```

3. Start the development server
```bash
npm start
```

The frontend will be available at `http://localhost:3000`

## Testing

### Backend Tests
```bash
cd backend
./mvnw test
```

### Frontend Tests
```bash
cd frontend
npm test
```

## API Documentation

### REST Endpoints

- `GET /api/words/count` - Get current word frequency count
- `GET /api/health` - Health check endpoint

### WebSocket

- Connect to `ws://localhost:8080/ws/words`
- Receives real-time updates of word frequencies in JSON format

## Project Structure

```
blog-word-counter/
├── backend/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── com/yourcompany/blogwordcounter/
│   │   │   │       ├── config/
│   │   │   │       ├── controller/
│   │   │   │       ├── service/
│   │   │   │       └── model/
│   │   │   └── resources/
│   │   │       └── application.yaml
│   │   └── test/
│   └── pom.xml
├── frontend/
│   ├── src/
│   │   ├── components/
│   │   ├── hooks/
│   │   ├── tests/
│   │   ├── App.js
│   │   └── index.js
│   ├── package.json
│   └── tailwind.config.js
└── README.md
```

## Development

### Adding New Blog Sources

1. Add the new WordPress site URL in `application.yaml`:
```yaml
wordpress:
  api:
    sources:
      - https://internate.org/wp-json/wp/v2
      - https://thekey.academy/wp-json/wp/v2
```

2. The system will automatically fetch from all configured sources.

### Customizing Word Analysis

Modify the `WordAnalyzerService` to adjust:
- Word filtering
- Minimum word length
- Stop words
- Language-specific processing

## Production Deployment

### Backend
1. Build the production JAR:
```bash
./mvnw clean package -Pprod
```

2. Run with production profile:
```bash
java -jar target/blog-word-counter-1.0.0.jar --spring.profiles.active=prod
```

### Frontend
1. Build the production bundle:
```bash
npm run build
```

2. Serve the `build` directory using your preferred web server

## Monitoring & Maintenance

- Application logs are written to `logs/application.log`
- Spring Boot Actuator endpoints available at `/actuator/*`
- Frontend error tracking via browser console
- Health check endpoint at `/api/health`

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
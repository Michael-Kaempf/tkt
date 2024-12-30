# Build stage
FROM node:20.10.0-alpine AS builder
WORKDIR /app

# Install dependencies
COPY src/package*.json ./
RUN npm ci

# Copy source code
COPY src/ ./

# Build application
RUN npm run build

# Runtime stage
FROM nginx:alpine
WORKDIR /usr/share/nginx/html

# Copy nginx configuration
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Copy built files from builder stage
COPY --from=builder /app/build .

# Expose port
EXPOSE 80

# Health check
HEALTHCHECK --interval=30s --timeout=3s \
  CMD wget -q --spider http://localhost:80 || exit 1

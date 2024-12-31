# Runtime stage
FROM nginx:alpine
WORKDIR /usr/share/nginx/html

# Nginx Konfiguration für SPA
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Kopiere die gebauten Frontend-Dateien
COPY build/ /usr/share/nginx/html

EXPOSE 3000

# Gesundheitscheck
HEALTHCHECK --interval=30s --timeout=3s \
  CMD wget -q --spider http://localhost:3000 || exit 1
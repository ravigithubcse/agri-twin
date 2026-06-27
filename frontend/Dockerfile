# ── Stage 1: Build Angular app ──────────────────────────────────────────────
FROM node:22-alpine AS build
WORKDIR /app

COPY package.json package-lock.json ./
RUN npm ci

COPY . .
RUN npm run build -- --configuration production

# ── Stage 2: Serve with Nginx ────────────────────────────────────────────────
FROM nginx:alpine
WORKDIR /usr/share/nginx/html

# Copy Angular build output
COPY --from=build /app/dist/frontend/browser /usr/share/nginx/html

# Copy Nginx config and startup script
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY start.sh /start.sh
RUN chmod +x /start.sh

EXPOSE 80

HEALTHCHECK --interval=15s --timeout=5s --start-period=10s --retries=3 \
  CMD wget -q --spider http://localhost:80/health || exit 1

CMD ["/start.sh"]

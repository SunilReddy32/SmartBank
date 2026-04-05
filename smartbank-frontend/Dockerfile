# ─────────────────────────────────────────────
# Stage 1: BUILD
# Use Node.js to install dependencies and build React app
# ─────────────────────────────────────────────
FROM node:20-alpine AS builder

WORKDIR /app

# Copy package files first for layer caching
# Dependencies only re-install when package.json changes
COPY package.json package-lock.json ./
RUN npm ci --silent

# Copy source and build for production
COPY . .
RUN npm run build

# ─────────────────────────────────────────────
# Stage 2: RUN
# Use Nginx to serve the built static files
# Nginx is tiny (~20MB) and extremely fast for serving React apps
# ─────────────────────────────────────────────
FROM nginx:alpine

# Remove the default Nginx welcome page
RUN rm -rf /usr/share/nginx/html/*

# Copy the built React files from builder stage
COPY --from=builder /app/dist /usr/share/nginx/html

# Copy custom Nginx config
# This handles React Router (returns index.html for all routes)
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

# Health check
HEALTHCHECK --interval=30s --timeout=5s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:80 || exit 1

CMD ["nginx", "-g", "daemon off;"]
# 前端构建 + Nginx（在项目根目录 build，context 为仓库根）
FROM node:18-alpine AS frontend
WORKDIR /build
ENV npm_config_audit=false \
    npm_config_fund=false
RUN npm config set engine-strict false
COPY package.json package-lock.json ./
RUN npm ci --legacy-peer-deps --no-audit --no-fund
COPY client ./client
RUN npm run build:portal
RUN npm run build:admin

FROM nginx:1.25-alpine
COPY docker/nginx/default.conf /etc/nginx/conf.d/default.conf
COPY --from=frontend /build/dist/portal/ /usr/share/nginx/html/
COPY --from=frontend /build/dist/admin/ /usr/share/nginx/html/admin/

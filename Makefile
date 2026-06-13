# TT 本地启动（对齐 IMP：docker compose up）
.PHONY: help env up down logs ps rebuild api infra

help:
	@echo "用法（在仓库根目录）："
	@echo "  make env      复制 .env.example → .env（首次必做）"
	@echo "  make up       Docker 全栈：mysql + rabbitmq + api + nginx"
	@echo "  make infra    仅 mysql + rabbitmq（本地 mvn / vite 时用）"
	@echo "  make api      infra + mvn spring-boot:run"
	@echo "  make down     停止容器"
	@echo "  make logs     跟踪日志"
	@echo "  make rebuild  强制重建镜像后启动"

env:
	@bash scripts/bootstrap-env.sh

up: env
	docker compose up -d --build

rebuild: env
	docker compose up -d --build --force-recreate

infra: env
	docker compose up -d mysql rabbitmq

down:
	docker compose down

logs:
	docker compose logs -f

ps:
	docker compose ps

api: env infra
	@echo "==> API 使用 Docker MySQL 127.0.0.1:3307（与 .env 默认一致）"
	DB_HOST=127.0.0.1 DB_PORT=3307 DB_USERNAME=tt_user DB_PASSWORD=ttsecret \
		mvn spring-boot:run -pl tt-admin/tt-admin-api -am

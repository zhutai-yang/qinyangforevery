#!/usr/bin/env bash
# 本地 MySQL 初始化（对齐 IMP db 治理：schema.sql + data.sql + migration）
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
DB_DIR="$ROOT/tt-admin/tt-admin-api/src/main/resources/db"

DB_HOST="${DB_HOST:-127.0.0.1}"
DB_PORT="${DB_PORT:-3306}"
DB_NAME="${DB_NAME:-tt_event}"
DB_USERNAME="${DB_USERNAME:-root}"
DB_PASSWORD="${DB_PASSWORD:-match123456}"

MYSQL=(mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USERNAME" -p"$DB_PASSWORD")

echo "==> Applying schema.sql"
"${MYSQL[@]}" < "$DB_DIR/schema.sql"

echo "==> Applying data.sql"
"${MYSQL[@]}" "$DB_NAME" < "$DB_DIR/data.sql"

if compgen -G "$DB_DIR/migration/*.sql" > /dev/null; then
  echo "==> Applying Flyway migration scripts (manual, for dev without app startup)"
  for f in "$DB_DIR"/migration/V*.sql; do
    echo "    $f"
    "${MYSQL[@]}" "$DB_NAME" < "$f"
  done
fi

echo "Done. Start API: mvn spring-boot:run -pl tt-admin/tt-admin-api -am"

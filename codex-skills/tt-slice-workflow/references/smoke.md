# Smoke

Use for `/smoke`.

Purpose: dynamically validate core API paths before PR, then produce `smoke-report.md`.

Preconditions:

| Check | Method |
|-------|--------|
| JWT config | `TT_JWT_SECRET` or test profile exists |
| Security whitelist | New `/api/admin/**` paths have expected security config |
| DB | `application-test.yml` or docker MySQL is available |
| Server | API service can run on expected port, normally `8096` |

Path selection:

1. Read `acceptance-baseline.md`.
2. Choose 3-8 P0 CRUD/public paths.
3. Include login/auth when admin endpoints require JWT.

Example curl templates:

```bash
curl -s http://localhost:8096/api/health

curl -s -X POST http://localhost:8096/api/admin/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"..."}'

curl -s http://localhost:8096/api/public/events
```

Startup options:

```bash
mvn spring-boot:run -pl tt-admin/tt-admin-api -am
docker compose up -d tt-admin-api
```

Use the lightest startup path that matches the environment. Do not leave long-running sessions unmanaged; report any active server session.

Report fields:

- Environment and startup method.
- Curl cases: request, expected status/body cue, actual status/body cue.
- Auth token source if used.
- PASS/FAIL summary.

PASS when all selected P0 requests behave as expected. FAIL when service starts but cases fail. BLOCKED when required env/DB/service startup is unavailable.

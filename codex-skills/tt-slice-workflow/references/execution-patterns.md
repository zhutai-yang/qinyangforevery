# TT Execution Patterns

Backend pattern:

1. Put application logic under `tt-shared/tt-application/**/service/**`.
2. Keep `tt-admin/tt-admin-api/**/controller/**` as HTTP adapters.
3. For schema changes, update `schema.sql`, add a Flyway `V*.sql`, and sync Docker init SQL when applicable.
4. Verify with `mvn clean verify -pl tt-admin/tt-admin-api -am`.

Frontend pattern:

1. Admin pages live under `client/admin`; portal pages live under `client/portal`.
2. Register routes in the matching `main.js`.
3. Reuse `client/shared/request.js` or local `api.js`.
4. Verify with `npm run lint` and `npm run build:all` when frontend files changed.

Planning pattern:

1. Derive implementation and acceptance baselines from slice docs and anchors.
2. Split execution by business closure.
3. Attach every task to AC IDs and concrete paths.

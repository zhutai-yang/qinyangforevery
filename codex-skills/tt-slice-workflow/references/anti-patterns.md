# TT Anti-Patterns

Use this when reviewing baselines, execution plans, or implementation diffs.

| ID | Anti-pattern | Preferred handling |
|----|--------------|--------------------|
| AP-001 | Baseline uses legacy names like `backend/` or `schema-mysql.sql` | Use `tt-admin-api`, `schema.sql`, and Flyway |
| AP-002 | Controller contains business logic | Keep Controller thin; put logic in `tt-application` services |
| AP-003 | AC says only "works normally" | Write testable Given/When/Then or curl-verifiable AC |
| AP-004 | Plan is split by technical layer | Split by business closure, not Controller/Service/Mapper layers |
| AP-005 | Frontend rewrites backend business state | Frontend calls API and refreshes data |
| AP-006 | Table change only adds migration | Sync `schema.sql`, Flyway migration, and `docker/mysql/init/01-schema.sql` |
| AP-007 | Admin and portal UI stacks mixed | Admin uses Element UI; portal uses Vant |
| AP-008 | Portal 401 redirects to `/admin/login` | Portal should use its own unauthenticated state/flow |
| AP-009 | Shared files modified without scope note | Mark shared changes in scope-check |
| AP-010 | Handoff before verification is green | Run required backend/frontend gates first |

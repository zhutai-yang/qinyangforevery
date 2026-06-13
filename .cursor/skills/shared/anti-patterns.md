# TT 反模式库

> 各 Skill 执行时对照本节，避免重复踩坑。

## slice-bootstrap

| ID | 反模式 | 正确做法 |
|----|--------|----------|
| AP-001 | baseline 写 `backend/`、`schema-mysql.sql` | 写 `tt-admin-api`、`schema.sql` + Flyway |
| AP-002 | 前端官网写 Element UI | 官网 Vant，后台 Element UI |
| AP-003 | AC 不可测试（「功能正常」） | 每条 AC 有 Given/When/Then 或可 curl 验证 |

## slice-plan

| ID | 反模式 | 正确做法 |
|----|--------|----------|
| AP-004 | 按 Controller/Service 分层拆任务 | 按业务闭环拆（如「赛事 CRUD 闭环」） |
| AP-005 | 子任务无对应 AC 编号 | 每个子任务关联 acceptance-baseline 中的 AC |

## verify / scope-check

| ID | 反模式 | 正确做法 |
|----|--------|----------|
| AP-006 | 改表只写 migration 不改 schema.sql | 同步 `schema.sql` + `docker/mysql/init/01-schema.sql` |
| AP-007 | 越界改其他切片 owned 文件 | scope-check 按 baseline 的 owned 列表审计 |
| AP-008 | portal api.js 401 跳 admin 登录 | 仅 admin 端跳转 |

## handoff

| ID | 反模式 | 正确做法 |
|----|--------|----------|
| AP-009 | verify FAIL 仍 handoff | 必须 verify PASS |
| AP-010 | 未跑 `mvn verify` | handoff 前本地/CI 绿 |

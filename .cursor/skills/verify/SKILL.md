---
name: verify
description: |
  对照 acceptance-baseline 审查实现，产出 verify-report.md。
  触发词: "/verify"
---

# verify（TT 版）

> **协议**: `shared/skill-protocol.md`

## 输入

- `acceptance-baseline.md`
- `implementation-baseline.md`
- 分支 diff + 代码

## 产出

- `verify-report.md`（PASS/FAIL + AC 矩阵）

## Phase 1 结构审查

| 检查 | 方法 |
|------|------|
| API 契约 | 路径/参数/Security 与 baseline 一致 |
| 分层 | Controller 薄层，逻辑在 `tt-application` |
| 改表 | PO/Mapper 与 `schema.sql` 一致 |
| 测试 | 相关 `*Test.java` 存在且绿 |

## Phase 2 AC 对账

每条 AC：✅ PASS / ❌ FAIL / ⏭️ N/A + 证据（类名或 `文件:行号`）

## 构建门禁

```bash
mvn clean verify -pl tt-admin/tt-admin-api -am
```

## FAIL

修复后重跑；禁止带 FAIL 进入 scope-check/handoff。

## 下游

→ `/scope-check` → `/hygiene` → `/resolve` → `/smoke` → `/handoff`

---
name: baseline-review
description: |
  基线三轮审查：顶层一致性 → 覆盖充分性 → AC 完备性。
  插入 slice-bootstrap 与 slice-plan 之间。
  触发词: "/baseline-review", "基线审查"
---

# baseline-review（TT 版）

> **协议**: `shared/skill-protocol.md`  
> **输入**: `implementation-baseline.md` + `acceptance-baseline.md` + 锚定项/概述  
> **产出**: `baseline-review-report.md`（可选 `design-sync-proposals/DS-*.md`）

## 流程

```
slice-bootstrap → [baseline-review] → slice-plan
                      ↑ FAIL 返回修正
```

## R1 顶层一致性（必做）

| ID | 检查项 | 对照 |
|----|--------|------|
| R1-01 | API 路径/方法 | baseline §一 vs 概述/已有 Controller |
| R1-02 | 表/字段 | baseline §二 vs `schema.sql` |
| R1-03 | 技术栈 | baseline header vs `a-锚定项/02-技术栈.md` |
| R1-04 | 包路径 | baseline §三 vs `com.tt.*` 模块结构 |
| R1-05 | 前端栈 | 若含前端：admin=Element UI，portal=Vant |

**顶层缺口** → 调用 `/design-sync` 产出 DS 提案，BLOCKED 直至决策。

## R2 覆盖充分性

| ID | 检查项 | 门槛 |
|----|--------|------|
| R2-01 | 正常+异常路径 | 每个 API ≥1 正常 + ≥1 异常 AC |
| R2-02 | 权限/认证 | 管理端 API 有 JWT 相关 AC |
| R2-03 | 改表同步 | 若改表，AC 含 schema+Flyway 检查 |
| R2-04 | owned 范围 | baseline §五 列出 glob，供 scope-check |

## R3 AC 完备性（快速复核）

| ID | 检查项 | Simple | Standard |
|----|--------|--------|----------|
| R3-01 | AC 总数 | ≥15 | ≥30 |
| R3-02 | 每条可测 | 无「功能正常」类模糊 AC | 同左 |
| R3-03 | AC→接口映射 | 抽样 3 条，均有对应 API/页面 | 同左 |

## 结论

| result | 条件 |
|--------|------|
| PASS | 三轮无阻断项 |
| FAIL | 基线错误 → 回 `/slice-bootstrap` |
| BLOCKED | 顶层缺口待 DS 决策 |

在 baseline header 标记 `review_status: passed/failed`。

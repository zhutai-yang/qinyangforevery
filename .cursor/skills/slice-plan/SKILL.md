---
name: slice-plan
description: |
  从 baseline 产出 execution-plan.md，含方案审查与子任务拆分。
  触发词: "/slice-plan", "执行计划"
---

# slice-plan（TT 版）

> **前置**: `/baseline-review` PASS  
> **产出**: 同目录 `execution-plan.md`

## 步骤

1. 读 `implementation-baseline.md` + `acceptance-baseline.md` + `shared/anti-patterns.md`
2. **按业务闭环**拆子任务（禁止按 Controller/Service 分层 — AP-004）
3. 每任务关联 AC 编号、涉及文件路径、依赖关系
4. 方案审查 6 项 + 风险预判 3 项
5. 自验证后写入 `execution-plan.md`

## 子任务规则

| 复杂度 | 建议任务数 | 单任务 AC 上限 |
|--------|-----------|---------------|
| Simple | 3~5 | ≤10 |
| Standard | 5~8 | ≤12 |

单任务 >8 文件必须拆分。禁止写「Day 1/2」时间估计，用「单元 1/2/3」。

## 方案审查

| ID | 检查 |
|----|------|
| DR-1 | API 字段在 DDL/PO 有对应 |
| DR-2 | 每个 API 有正常+异常 AC |
| DR-3 | 业务规则有 AC 覆盖 |
| DR-4 | 无孤立子任务/孤立 AC |
| DR-5 | 复用 `AdminAuthPort`、`request.js` 等已标注 |
| DR-6 | owned 范围与 baseline §五 一致 |

## 输出章节

§目标 · §范围 · §子任务表 · §编排图 · §风险 · §审查报告

## 结论

| result | next_action |
|--------|-------------|
| PASS | CONTINUE → 编码 |
| FAIL | RETRY → 修正 plan/baseline |

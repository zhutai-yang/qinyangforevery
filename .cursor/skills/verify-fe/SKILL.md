---
name: verify-fe
description: |
  前端切片审查：接口对齐 + 规范 + AC 对账。
  触发词: "/verify-fe", "前端审查"
---

# verify-fe（TT 版）

> **前置**: 前端页面编码完成  
> **栈**: Vue 2.7 + Vite；admin=Element UI；portal=Vant

## Phase 0 架构（必过）

| ID | 检查 |
|----|------|
| VF0-01 | 无前端改写业务 status |
| VF0-02 | 操作后 refresh 列表/详情 |
| VF0-03 | portal 401 不 redirect 到 `/admin/login` |
| VF0-04 | 无多 API 前端事务组合 |

## Phase 1 接口对齐

| ID | 检查 |
|----|------|
| VF1-01 | `api.js` 路径与后端 Controller 一致 |
| VF1-02 | GET/POST 方法与后端一致 |
| VF1-03 | 请求字段名与后端 DTO 一致 |
| VF1-04 | AC 要求的功能均已调用 API |

## Phase 2 规范

| ID | 检查 |
|----|------|
| VF2-01 | 路由在 `main.js` 注册 |
| VF2-02 | 危险操作有 confirm |
| VF2-03 | 列表有空状态 / loading |
| VF2-04 | `npm run lint` 通过 |

## Phase 3 AC 对账

- 每条 PASS 锚定 `文件:行号`
- 覆盖率 Simple≥90%，Standard≥95%
- 产出 `verify-fe-report.md`

## 命令

```bash
cd client && npm run lint && npm run build:all
```

## 链

```
verify-fe → scope-check → hygiene → resolve → smoke → handoff
```

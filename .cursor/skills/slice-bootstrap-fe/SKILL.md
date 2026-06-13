---
name: slice-bootstrap-fe
description: |
  从前端切片概述派生 implementation/acceptance baseline。
  触发词: "/slice-bootstrap-fe", "前端派生基线"
---

# slice-bootstrap-fe（TT 版）

> **对称**: 后端 `/slice-bootstrap`  
> **产出目录**: `d-前端视图设计（前端）/切片UI-N-*/`

## 输入

- `00-切片概述.md` 或 UI 切片 README
- `a-锚定项/02-技术栈.md`（Vue 2.7、Vite、admin=Element UI、portal=Vant）
- 后端 `implementation-baseline.md`（API 契约）
- 现有 `client/admin`、`client/portal` 代码

## 五阶段（TT 精简）

1. **定位提取**：页面、路由、依赖 API；对照后端 Controller
2. **基础设施扫描**：`client/shared/request.js`、各端 `api.js`、`main.js` 路由模式
3. **风格对齐**：参照已有 admin/portal 页面命名与目录
4. **涌现**：空状态、loading、401 处理（portal 不跳 admin 登录 — AP-008）
5. **多轮对齐**：Simple 2 轮 / Standard 3 轮 AC

## API 路径规则

- 读 `vite.config.js` proxy 与 axios baseURL
- 后端 `@RequestMapping("/api/xxx")` → 前端路径不含重复 `/api` 前缀

## UI/API GAP

设计文档有字段但 API 无返回 → 列出 GAP 表，等用户选：补 API / 简化展示 / 推迟

## 模板

- [impl-template.md](reference/impl-template.md)
- [acc-template.md](reference/acc-template.md)

## 自检

- [ ] admin 与 portal 栈未混用
- [ ] 每条 AC 可手工或 lint 验证
- [ ] owned 范围写入 baseline §五

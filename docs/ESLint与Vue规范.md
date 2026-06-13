# ESLint 与 Vue 规范

> 技术栈版本见 [`乒乓球赛事管理系统（TT）整体规划/a-锚定项/02-技术栈.md`](../乒乓球赛事管理系统（TT）整体规划/a-锚定项/02-技术栈.md)。

## 使用方式

```bash
npm install --legacy-peer-deps
npm run lint
npm run lint:fix
```

配置：根目录 `.eslintrc.cjs`（`eslint-plugin-vue`）。

## 范围

- `client/admin/**`、`client/portal/**`、`client/shared/**`
- 不含 `client/vite.*.config.js`（Node 环境）

## 说明

- 后端：**Java 17 + Spring Boot 3.2.5**（`tt-admin-api`），无 Node 版 API。
- 开发：Vite 2.9 将 `/api/` 代理到 `http://127.0.0.1:8096`（可通过 `VITE_API_TARGET` 覆盖）。

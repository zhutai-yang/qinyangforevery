# 前端 `client/` 说明

## 目录

| 目录 | 说明 |
|------|------|
| `shared/` | 公共 axios 封装（`request.js`） |
| `admin/` | 管理后台（Vue2 + Element UI），路由 `base: /admin/` |
| `portal/` | 官网（Vue2 + Vant 移动端） |
| `vite.admin.config.js` / `vite.portal.config.js` | Vite 配置（在仓库根目录执行 `npm run dev:*`） |

## 本地开发

在仓库根目录执行，环境变量在 `.env` / `.env.local` 中配置：

| 变量 | 说明 | 默认 |
|------|------|------|
| `VITE_API_TARGET` | 开发代理 `/api/` 转发目标 | `http://127.0.0.1:8096` |
| `VITE_ADMIN_PORT` | 管理端 dev 端口 | `5174` |

```bash
npm run dev:admin    # http://localhost:5174/admin/
npm run dev:portal   # http://localhost:5173/
```

## API 约定

- 管理端：`client/admin/api.js` → `baseURL: '/api/admin'`
- 官网：`client/portal/api.js` → `baseURL: '/api/public'`（401 不跳转登录页）

## 构建

```bash
npm run build:all   # → dist/admin/、dist/portal/
```

# TT 执行模式

## 后端新增 API

1. `tt-application/service/{domain}/` 写应用服务
2. `tt-admin-api/.../controller/` 写 Controller（薄层）
3. 改表：`schema.sql` + `migration/V*.sql`
4. `mvn verify`

## 前端新增页面

1. `client/{admin|portal}/views/` 加页面
2. `main.js` 注册路由
3. 复用 `client/shared/request.js` 或 `api.js`
4. `npm run lint` + 手工点路径

## 切片分支

```bash
git checkout -b slice-3-public-api
```

合并前跑完整 Skills 链后半段：verify → scope-check → smoke → handoff。

---
name: hygiene
description: |
  本切片改动文件的代码卫生审计（死代码、TODO、命名残留等）。
  触发词: "/hygiene", "代码卫生"
---

# hygiene（TT 版）

> **位置**: scope-check 之后、resolve 之前  
> **范围**: `git diff develop...HEAD`（或 main）内文件

## 5 维审计

| 维 | 检查 |
|----|------|
| 死代码 | 未引用方法、大段注释代码 |
| 命名残留 | `backend/`、`TableTennis`、`schema-mysql` 等旧名 |
| TODO/FIXME | 本切片新增的高优先级 FIXME |
| 导入/依赖 | 未使用 import；前端 console.log 调试残留 |
| 配置泄漏 | 硬编码密码、JWT secret |

## 分级

- 🔴 阻断：FIXME、密钥、BOM 导致编译失败
- 🟡 警告：TODO、console.log
- 🟢 建议：可后续清理

## 产出

`hygiene-report.md`；🔴=0 可 CONTINUE，否则 RETRY 修复。

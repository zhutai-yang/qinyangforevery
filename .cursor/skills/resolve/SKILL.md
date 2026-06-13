---
name: resolve
description: |
  PR 前预合并冲突检测与可自动冲突处理。
  触发词: "/resolve", "预合并"
---

# resolve（TT 版）

> **前置**: hygiene PASS

## 步骤

```bash
git fetch origin main
git merge origin/main --no-commit --no-ff
git diff --name-only --diff-filter=U
```

## 冲突分类

| 类型 | 处理 |
|------|------|
| import/格式 | 可自动合并 |
| 同文件逻辑 | 人工确认，标注报告 |
| schema.sql | 禁止静默覆盖，合并双方 migration |

冲突解决后：

```bash
mvn verify -pl tt-admin/tt-admin-api -am
```

## 产出

`resolve-report.md`：冲突文件、处理方式、verify 结果。

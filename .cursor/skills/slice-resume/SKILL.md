---
name: slice-resume
description: |
  会话中断后从 Git 恢复切片开发上下文。
  触发词: "/slice-resume", "恢复会话"
---

# slice-resume（TT 版）

> **协议**: `shared/skill-protocol.md`

## 步骤

1. 读 Git 中 `implementation-baseline.md` + `acceptance-baseline.md` + `execution-plan.md`
2. 解析分支 commit（建议格式 `[切片N] T{n}: 描述`）
3. 运行 `mvn compile -pl tt-admin/tt-admin-api -am`；前端改动则 `npm run lint`
4. `git status` 检查未提交改动
5. 产出续接报告：已完成/进行中/待开始子任务 + 下一步

## 编译命令

```bash
mvn compile -pl tt-admin/tt-admin-api -am
cd client && npm run lint
```

## 自动继续

- 编译 PASS + 无脏改动 → 继续 execution-plan 下一任务
- 编译 FAIL → 先修编译
- 有未提交改动 → 先 commit/stash

## 输出

`slice-resume-report.md` 或会话内结构化报告。

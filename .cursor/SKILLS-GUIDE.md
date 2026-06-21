# TT 项目 Skills 指南

> **配套**: [CLAUDE.md](../CLAUDE.md)（速查）、[skill-protocol.md](skills/shared/skill-protocol.md)（协议）

## 1. 与 IMP 的关系

TT 沿用 IMP 切片 + Skills 链，按赛事系统规模**精简**（无 slice-coord/claim/release 等云协作技能）。核心链路与 IMP 一致。

## 2. 完整流程

### 后端切片

1. 在 `e-切片规划与里程碑.md` 登记
2. `/slice-bootstrap` → baseline 两文件
3. `/baseline-review` → `baseline-review-report.md`
4. `/slice-plan` → `execution-plan.md`
5. 分支 `slice-{N}-{slug}` 编码
6. `/verify` → `/scope-check` → `/hygiene` → `/resolve` → `/smoke` → `/handoff`
7. PR

### 前端切片

1. `/slice-bootstrap-fe`（目录在 `d-前端视图设计（前端）/`）
2. 同上 baseline-review / slice-plan
3. `/verify-fe` 替代 verify
4. 其余链相同

### 设计缺口

任意阶段发现与 `02-技术栈.md` 或 API 契约冲突 → `/design-sync`

## 3. Skills 文件一览

```
.cursor/skills/
├── shared/           skill-protocol, anti-patterns, execution-patterns
├── slice-bootstrap/  + reference/
├── slice-bootstrap-fe/ + reference/
├── baseline-review/
├── slice-plan/
├── slice-resume/
├── verify/
├── verify-fe/
├── scope-check/
├── hygiene/
├── resolve/
├── smoke/
├── handoff/
├── design-sync/
├── tt-slice-workflow-testing/
└── athlete-data-import/
```

## 4. 模块/闭环测试技能

`tt-slice-workflow-testing` 用于“测一下赛事管理 / 验一下成绩录入 / 检查官网赛程字段 / 跑后台到官网闭环”等场景。

它会先发现模块，再让使用者选择测试模式（冒烟、字段/API、后台到官网、完整闭环、性能、静态计划、可视化证据），随后生成用例清单并输出 Markdown 摘要 + HTML 证据报告。测试证据默认放在仓库外部的 sibling artifact 目录，不进入 git。

`athlete-data-import` 用于采集单个运动员的公开资料、排名、荣誉、近期成绩、未来赛程与来源证据，输出后台“明星选手 -> 快捷导入/更新”可直接粘贴的 JSON。

## 5. 未迁移的 IMP 技能（TT 不需要）

| IMP 技能 | 原因 |
|----------|------|
| slice-coord, claim, release | 云 Agent 并行领取 |
| slice-brief | 概述已由 `00-切片概述.md` 承担 |
| doc-consistency-check | 可 periodic 人工；需要时再补 |
| split-return-csv | IMP 发码域专用 |

## 6. 文档与代码映射

| 切片 | 代码 |
|------|------|
| 登录鉴权 | `AuthService`, `SecurityConfig`, `fnd_user` |
| 业务录入 | `*AdminService`, `/api/admin/**` |
| 官网 | `PublicService`, `/api/public/**` |

## 7. 本地自检

```bash
mvn clean verify -pl tt-admin/tt-admin-api -am
cd client && npm run lint && npm run build:all
docker compose up -d --build   # 可选全栈
```

## 8. 自动继续规则

见 `skill-protocol.md`：PASS → 下一 skill；FAIL → 修复重试；BLOCKED → 升级人工。

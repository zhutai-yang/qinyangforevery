# Star Player Content Ops And Premium Portal Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Reposition TT from a tournament management back office into a star-player content operations back office plus a premium public showcase website.

**Architecture:** Keep the existing tournament, schedule, CMS, external feed, and athlete career data as the content substrate. Add a thin star-player profile layer and reshape the admin and portal information architecture so operators manage athletes, highlights, commercial previews, articles, and homepage storytelling first, while tournament records become supporting proof.

**Tech Stack:** Spring Boot + JdbcTemplate + MySQL/Flyway, Vue 2.7, Element UI 2 admin, Vant 2 portal, Vite 2.9, existing `/api/admin` and `/api/public` split.

---

## File Structure

- Modify `乒乓球赛事管理系统（TT）整体规划/乒乓球赛事管理系统-解决方案与分析.md`: change the business positioning, capability map, module naming, and milestone narrative.
- Modify `乒乓球赛事管理系统（TT）整体规划/e-切片规划与里程碑.md`: add the repositioning change record and register the new star-player/premium-portal slice.
- Modify `乒乓球赛事管理系统（TT）整体规划/d-前端视图设计（前端）/d-管理后台视图/README.md`: rename the admin view set around player content operations.
- Modify `乒乓球赛事管理系统（TT）整体规划/d-前端视图设计（前端）/d-官网前端视图/README.md`: rename the public view set around premium showcase pages.
- Create `tt-admin/tt-admin-api/src/main/resources/db/migration/V20260618_001__star_player_profile_ops.sql`: add optional profile fields for star-player storytelling without disrupting existing athlete records.
- Modify `tt-admin/tt-admin-api/src/main/resources/db/schema.sql`: mirror the same fields as the authoritative DDL.
- Modify `tt-shared/tt-application/src/main/java/com/tt/application/service/athlete/AthleteAdminService.java`: expose `profile_title`, `profile_summary`, `hero_image_url`, and `social_url` in admin create/update/list responses by relying on `SELECT *`.
- Modify `tt-shared/tt-application/src/main/java/com/tt/application/service/publicapi/PublicService.java`: return the new profile fields, prioritize featured/highlight athletes, and expose a richer home payload.
- Modify `tt-admin/tt-admin-api/src/test/java/com/tt/api/PublicControllerTest.java`: assert public athlete and home APIs include star-player showcase data.
- Modify `client/admin/views/Layout.vue`: make content operations the primary menu group and move event operations below it.
- Modify `client/admin/views/Dashboard.vue`: update shortcut order and copy toward athlete/content operations.
- Modify `client/admin/views/AthletesAdmin.vue`: add star profile fields to the athlete form and list.
- Modify `client/admin/views/AthleteCareerAdmin.vue`: adjust labels from “职业生涯运营” to “明星内容运营”.
- Modify `client/portal/views/Home.vue`: make the first viewport a premium star-player showcase backed by `highlight_athletes`, with articles/events as secondary.
- Modify `client/portal/views/Player.vue`: use `hero_image_url`, `profile_title`, `profile_summary`, and social links before falling back to existing highlights.

---

### Task 1: Reposition The Product Documents

**Files:**
- Modify: `乒乓球赛事管理系统（TT）整体规划/乒乓球赛事管理系统-解决方案与分析.md`
- Modify: `乒乓球赛事管理系统（TT）整体规划/e-切片规划与里程碑.md`
- Modify: `乒乓球赛事管理系统（TT）整体规划/d-前端视图设计（前端）/d-管理后台视图/README.md`
- Modify: `乒乓球赛事管理系统（TT）整体规划/d-前端视图设计（前端）/d-官网前端视图/README.md`

- [ ] **Step 1: Update the solution document positioning**

Replace the “需求归纳” table in `乒乓球赛事管理系统（TT）整体规划/乒乓球赛事管理系统-解决方案与分析.md` with:

```markdown
## 一、需求归纳

TT 的产品定位调整为：**明星选手内容运营后台 + 高级展示官网**。

| 能力域 | 新定位说明 |
|--------|------------|
| **明星选手运营** | 以重点选手为主资产，维护档案、头图、定位语、代表高光、商业预告与外部链接 |
| **内容运营** | 文章、资讯、外部动态、首页区块围绕选手与热点故事编排 |
| **高级展示官网** | 首页与球员页优先呈现人物、视觉资产、高光内容和商业动线，赛事/成绩作为可信背书 |
| **赛事记录底座** | 保留赛事、赛程、成绩、场馆数据，用于支撑选手履历、预告和历史战绩 |
| **外部数据补充** | 外部动态作为内容线索和资讯流，不直接覆盖本地权威成绩 |
| **站点划分** | **高级展示官网（公众端）** + **明星选手内容运营后台（运营端）** |
```

- [ ] **Step 2: Update the admin module table**

In the same file, replace the `### 4.2 管理后台（建议菜单）` table with:

```markdown
### 4.2 明星选手内容运营后台（建议菜单）

| 菜单 | 功能 |
|------|------|
| 工作台 | 查看重点选手、待发布内容、近期商业预告与首页区块状态 |
| 明星选手 | 选手档案、展示头图、定位语、社媒/外链、重点展示状态 |
| 高光与商业预告 | 维护选手高光、活动预告、品牌合作链接、展示排序与发布状态 |
| 内容中心 | 文章、资讯、公告、选手关联内容与首页推荐 |
| 首页编排 | 高级展示官网的首屏选手、推荐文章、赛程预告、外部动态区块开关与排序 |
| 赛事资料库 | 赛事、场馆、赛程、成绩，作为选手内容和官网展示的结构化素材 |
| 外部数据 | 外部资讯源、抓取日志、手动同步与降级可见性 |
| 系统 | 用户、角色、字典、审计日志 |
```

- [ ] **Step 3: Update milestone registry**

Append this row to the slice registry table in `乒乓球赛事管理系统（TT）整体规划/e-切片规划与里程碑.md`:

```markdown
| 06 | 明星选手内容运营与高级官网 | Standard | 📝 待规划 | 本计划：`docs/superpowers/plans/2026-06-18-star-player-content-ops-premium-portal.md` |
```

Append this change record:

```markdown
| 2026-06-18 | 产品定位调整：由“赛事管理后台”升级为“明星选手内容运营后台 + 高级展示官网”，赛事数据作为内容与履历底座保留 |
```

- [ ] **Step 4: Update frontend view indexes**

Replace the body of `乒乓球赛事管理系统（TT）整体规划/d-前端视图设计（前端）/d-管理后台视图/README.md` with:

```markdown
# d - 明星选手内容运营后台视图

按 **运营工作流** 描述页面结构、字段与交互；实现时对接 `切片01～05` 已完成 API，并通过本计划补齐明星选手运营能力。

| 文档 | 说明 |
|------|------|
| [登录与布局](./登录与布局.md) | 登录页、侧栏、顶栏 |
| [选手管理](./选手管理.md) | 明星选手档案、展示资料、重点运营入口 |
| [关注球员与首页](./关注球员与首页.md) | 首页明星选手与区块编排 |
| [CMS文章](./CMS文章.md) | 选手关联文章、资讯与公告 |
| [赛事管理](./赛事管理.md) | 赛事资料库 |
| [赛程与场次](./赛程与场次.md) | 赛程素材与预告来源 |
| [成绩录入](./成绩录入.md) | 选手履历与战绩来源 |
| [外部数据源](./外部数据源.md) | 外部动态线索 |
| [验收清单](./验收清单.md) | 后台视图联调验收 |
```

Replace the body of `乒乓球赛事管理系统（TT）整体规划/d-前端视图设计（前端）/d-官网前端视图/README.md` with:

```markdown
# d - 高级展示官网前端视图

按 **公众展示页面** 描述布局与数据接口；官网首屏以明星选手和内容故事为主，赛事/赛程/成绩作为结构化支撑。

| 文档 | 说明 |
|------|------|
| [首页](./首页.md) | 明星选手首屏、高光、推荐文章、赛程预告 |
| [球员主页](./球员主页.md) | 选手展厅、档案、高光、商业预告、赛事履历 |
| [资讯与外部动态](./资讯与外部动态.md) | 选手关联资讯与外部动态 |
| [赛事列表与详情](./赛事列表与详情.md) | 赛事资料库展示 |
| [赛程与成绩](./赛程与成绩.md) | 赛程与战绩查询 |
| [验收清单](./验收清单.md) | 官网视图验收 |
```

- [ ] **Step 5: Commit**

Run:

```bash
git add '乒乓球赛事管理系统（TT）整体规划/乒乓球赛事管理系统-解决方案与分析.md' '乒乓球赛事管理系统（TT）整体规划/e-切片规划与里程碑.md' '乒乓球赛事管理系统（TT）整体规划/d-前端视图设计（前端）/d-管理后台视图/README.md' '乒乓球赛事管理系统（TT）整体规划/d-前端视图设计（前端）/d-官网前端视图/README.md'
git commit -m "docs: reposition tt around star player content ops"
```

Expected: commit succeeds with four documentation files changed.

---

### Task 2: Add Star Player Profile Fields

**Files:**
- Create: `tt-admin/tt-admin-api/src/main/resources/db/migration/V20260618_001__star_player_profile_ops.sql`
- Modify: `tt-admin/tt-admin-api/src/main/resources/db/schema.sql`
- Modify: `tt-shared/tt-application/src/main/java/com/tt/application/service/athlete/AthleteAdminService.java`

- [ ] **Step 1: Write migration**

Create `tt-admin/tt-admin-api/src/main/resources/db/migration/V20260618_001__star_player_profile_ops.sql`:

```sql
ALTER TABLE reg_athlete
  ADD COLUMN profile_title VARCHAR(256) NULL AFTER association,
  ADD COLUMN profile_summary VARCHAR(1024) NULL AFTER profile_title,
  ADD COLUMN hero_image_url VARCHAR(512) NULL AFTER profile_summary,
  ADD COLUMN social_url VARCHAR(512) NULL AFTER hero_image_url;
```

- [ ] **Step 2: Mirror schema authority**

In `tt-admin/tt-admin-api/src/main/resources/db/schema.sql`, replace the `reg_athlete` table definition with:

```sql
CREATE TABLE IF NOT EXISTS reg_athlete (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(128) NOT NULL,
  gender VARCHAR(16) NULL,
  birth_date DATE NULL,
  association VARCHAR(256) NULL,
  profile_title VARCHAR(256) NULL,
  profile_summary VARCHAR(1024) NULL,
  hero_image_url VARCHAR(512) NULL,
  social_url VARCHAR(512) NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
) ENGINE=InnoDB;
```

- [ ] **Step 3: Update create and update SQL**

In `tt-shared/tt-application/src/main/java/com/tt/application/service/athlete/AthleteAdminService.java`, replace `createAthlete` with:

```java
  @Transactional
  public Map<String, Object> createAthlete(Map<String, Object> b) {
    GeneratedKeyHolder kh = new GeneratedKeyHolder();
    return JdbcSupport.insertAndSelect(
        jdbc,
        "reg_athlete",
        "INSERT INTO reg_athlete (name, gender, birth_date, association, profile_title, profile_summary, hero_image_url, social_url) VALUES (?,?,?,?,?,?,?,?)",
        new Object[] {
          b.getOrDefault("name", ""),
          b.get("gender"),
          JdbcSupport.parseDate(b.get("birth_date")),
          b.get("association"),
          b.get("profile_title"),
          b.get("profile_summary"),
          b.get("hero_image_url"),
          b.get("social_url")
        },
        kh);
  }
```

Replace `updateAthlete` with:

```java
  public Map<String, Object> updateAthlete(long id, Map<String, Object> b) {
    jdbc.update(
        "UPDATE reg_athlete SET name=?, gender=?, birth_date=?, association=?, profile_title=?, profile_summary=?, hero_image_url=?, social_url=? WHERE id=?",
        b.get("name"),
        b.get("gender"),
        JdbcSupport.parseDate(b.get("birth_date")),
        b.get("association"),
        b.get("profile_title"),
        b.get("profile_summary"),
        b.get("hero_image_url"),
        b.get("social_url"),
        id);
    return jdbc.queryForList("SELECT * FROM reg_athlete WHERE id = ?", id).get(0);
  }
```

- [ ] **Step 4: Run backend tests**

Run:

```bash
mvn -pl tt-admin/tt-admin-api test
```

Expected: build succeeds and existing controller/service tests pass.

- [ ] **Step 5: Commit**

Run:

```bash
git add tt-admin/tt-admin-api/src/main/resources/db/migration/V20260618_001__star_player_profile_ops.sql tt-admin/tt-admin-api/src/main/resources/db/schema.sql tt-shared/tt-application/src/main/java/com/tt/application/service/athlete/AthleteAdminService.java
git commit -m "feat: add star player profile fields"
```

Expected: commit succeeds with migration, schema, and service changes.

---

### Task 3: Expose Star Player Showcase Data Publicly

**Files:**
- Modify: `tt-shared/tt-application/src/main/java/com/tt/application/service/publicapi/PublicService.java`
- Modify: `tt-admin/tt-admin-api/src/test/java/com/tt/api/PublicControllerTest.java`

- [ ] **Step 1: Write public API expectations**

Add this test method to `tt-admin/tt-admin-api/src/test/java/com/tt/api/PublicControllerTest.java`:

```java
  @Test
  void athleteDetailIncludesStarProfileAndShowcaseContent() throws Exception {
    mockMvc
        .perform(get("/api/public/athletes/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.athlete.id").value(1))
        .andExpect(jsonPath("$.athlete.name").exists())
        .andExpect(jsonPath("$.athlete.profile_title").exists())
        .andExpect(jsonPath("$.highlights").isArray())
        .andExpect(jsonPath("$.business_previews").isArray());
  }
```

Add this test method:

```java
  @Test
  void homeLayoutIncludesPremiumShowcaseBlocks() throws Exception {
    mockMvc
        .perform(get("/api/public/home/layout"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.blocks").isArray())
        .andExpect(jsonPath("$.highlight_athletes").isArray());
  }
```

- [ ] **Step 2: Run tests to verify failure before seed/schema alignment**

Run:

```bash
mvn -pl tt-admin/tt-admin-api -Dtest=PublicControllerTest test
```

Expected before Task 2 is fully applied to test schema: tests fail because `profile_title` is not present in `reg_athlete`, or pass if the test database already uses the updated authoritative schema.

- [ ] **Step 3: Enrich home highlight athlete query**

In `tt-shared/tt-application/src/main/java/com/tt/application/service/publicapi/PublicService.java`, replace the `highlight_athletes` query inside `homeLayout()` with:

```java
            jdbc.queryForList(
                "SELECT h.id, h.athlete_id, h.title, h.cover_url, h.summary, h.published_at, "
                    + "a.name AS athlete_name, a.profile_title, a.profile_summary, a.hero_image_url, a.social_url "
                    + "FROM cfg_athlete_highlight h "
                    + "INNER JOIN reg_athlete a ON a.id = h.athlete_id "
                    + "WHERE h.status = 'published' "
                    + "ORDER BY h.sort_order, h.id "
                    + "LIMIT 200");
```

In the item mapping immediately below it, add:

```java
          item.put("profile_title", h.get("profile_title"));
          item.put("profile_summary", h.get("profile_summary"));
          item.put("hero_image_url", h.get("hero_image_url"));
          item.put("social_url", h.get("social_url"));
```

- [ ] **Step 4: Run public API tests**

Run:

```bash
mvn -pl tt-admin/tt-admin-api -Dtest=PublicControllerTest test
```

Expected: `BUILD SUCCESS`.

- [ ] **Step 5: Commit**

Run:

```bash
git add tt-shared/tt-application/src/main/java/com/tt/application/service/publicapi/PublicService.java tt-admin/tt-admin-api/src/test/java/com/tt/api/PublicControllerTest.java
git commit -m "feat: expose premium player showcase data"
```

Expected: commit succeeds with one service and one test file changed.

---

### Task 4: Reshape Admin Around Content Operations

**Files:**
- Modify: `client/admin/views/Layout.vue`
- Modify: `client/admin/views/Dashboard.vue`
- Modify: `client/admin/views/AthletesAdmin.vue`
- Modify: `client/admin/views/AthleteCareerAdmin.vue`

- [ ] **Step 1: Move content operations above event operations**

In `client/admin/views/Layout.vue`, change the logo text:

```vue
<span v-show="!asideCollapsed" class="logo__text">TT 内容运营</span>
```

Place the `group-content` submenu before `group-ops`, and change its title to:

```vue
<span>明星内容运营</span>
```

Change the `group-ops` title to:

```vue
<span>赛事资料库</span>
```

- [ ] **Step 2: Update dashboard copy and shortcut order**

In `client/admin/views/Dashboard.vue`, replace the intro copy with:

```vue
<p class="admin-page__desc">围绕明星选手、高光内容、商业预告与官网首页编排开展日常运营。</p>
```

Replace the `shortcuts` array with:

```js
      shortcuts: [
        { path: '/athletes', title: '明星选手', desc: '档案、展示资料与运营入口', icon: 'el-icon-user', tone: 'purple' },
        { path: '/featured', title: '首页明星', desc: '官网展示顺序', icon: 'el-icon-star-on', tone: 'amber' },
        { path: '/articles', title: '内容中心', desc: '资讯、公告与选手故事', icon: 'el-icon-document', tone: 'indigo' },
        { path: '/home-config', title: '首页编排', desc: '高级官网区块开关', icon: 'el-icon-menu', tone: 'gray' },
        { path: '/events', title: '赛事资料', desc: '赛事与届次素材', icon: 'el-icon-trophy', tone: 'gold' },
        { path: '/matches', title: '赛程战绩', desc: '场次、预告与结果', icon: 'el-icon-date', tone: 'teal' },
        { path: '/venues', title: '场馆资料', desc: '比赛场馆档案', icon: 'el-icon-location-outline', tone: 'blue' },
        { path: '/ext', title: '外部动态', desc: '抓取与同步任务', icon: 'el-icon-link', tone: 'cyan' },
        { path: '/dict-audit', title: '系统治理', desc: '枚举与审计日志', icon: 'el-icon-setting', tone: 'slate' }
      ]
```

- [ ] **Step 3: Add athlete profile fields to admin form**

In `client/admin/views/AthletesAdmin.vue`, add these fields to the create/edit form model:

```js
profile_title: '',
profile_summary: '',
hero_image_url: '',
social_url: ''
```

Add these form items below `association`:

```vue
<el-form-item label="展示定位">
  <el-input v-model="form.profile_title" placeholder="例如：国乒新生代核心" />
</el-form-item>
<el-form-item label="展示摘要">
  <el-input v-model="form.profile_summary" type="textarea" rows="3" placeholder="用于官网球员页首屏展示" />
</el-form-item>
<el-form-item label="头图URL">
  <ImageLibraryPicker v-model="form.hero_image_url" placeholder="https://..." />
</el-form-item>
<el-form-item label="外部链接">
  <el-input v-model="form.social_url" placeholder="社媒、商务页或官方主页" />
</el-form-item>
```

Ensure `ImageLibraryPicker` is imported if `AthletesAdmin.vue` does not already import it:

```js
import ImageLibraryPicker from '../components/ImageLibraryPicker.vue';
```

Register it in `components`:

```js
components: { ImageLibraryPicker },
```

- [ ] **Step 4: Rename career operations labels**

In `client/admin/views/AthleteCareerAdmin.vue`, replace:

```vue
<h1 class="admin-page__title">球员职业生涯运营</h1>
<p class="admin-page__desc">维护该球员的高光展示与商务预告（按时间/排序在官网球员页聚合展示）。</p>
```

with:

```vue
<h1 class="admin-page__title">明星内容运营</h1>
<p class="admin-page__desc">维护该选手的高光内容、商业预告与官网展示素材。</p>
```

- [ ] **Step 5: Run frontend lint/build**

Run:

```bash
npm run build
```

Expected: admin and portal bundles build without Vue template or import errors.

- [ ] **Step 6: Commit**

Run:

```bash
git add client/admin/views/Layout.vue client/admin/views/Dashboard.vue client/admin/views/AthletesAdmin.vue client/admin/views/AthleteCareerAdmin.vue
git commit -m "feat: prioritize star player content ops in admin"
```

Expected: commit succeeds with admin IA and form changes.

---

### Task 5: Upgrade Portal To Premium Showcase

**Files:**
- Modify: `client/portal/views/Home.vue`
- Modify: `client/portal/views/Player.vue`

- [ ] **Step 1: Make homepage hero data-driven**

In `client/portal/views/Home.vue`, add this computed property:

```js
    heroAthlete() {
      return (this.highlightAthletes && this.highlightAthletes[0]) || null;
    },
    heroTitle() {
      return this.heroAthlete ? this.heroAthlete.athlete_name : 'TT 明星选手展厅';
    },
    heroSubTitle() {
      if (!this.heroAthlete) return '以高光故事、赛事履历与商业预告呈现每一位重点选手。';
      return this.heroAthlete.profile_title || this.heroAthlete.title || '高光选手';
    },
    heroSummary() {
      if (!this.heroAthlete) return '推荐文章与最新动态，以高级展示官网的方式聚合呈现。';
      return this.heroAthlete.profile_summary || this.heroAthlete.summary || '查看高光内容、赛事记录与近期预告。';
    },
    heroImageStyle() {
      const url = this.heroAthlete && (this.heroAthlete.hero_image_url || this.heroAthlete.cover_url);
      return url ? { backgroundImage: `url(${url})` } : {};
    }
```

Replace the hero title and subtitle bindings:

```vue
<p class="hero__eyebrow hero-reveal" :class="{ 'is-visible': heroReady }">明星选手</p>
<h1 class="hero__title hero-reveal" :class="{ 'is-visible': heroReady }" :style="{ transitionDelay: '80ms' }">
  {{ heroTitle }}
</h1>
<p class="hero__sub hero-reveal" :class="{ 'is-visible': heroReady }" :style="{ transitionDelay: '160ms' }">
  {{ heroSubTitle }}｜{{ heroSummary }}
</p>
<router-link v-if="heroAthlete" class="hero__cta hero-reveal" :class="{ 'is-visible': heroReady }" :style="{ transitionDelay: '220ms' }" :to="'/players/' + heroAthlete.athlete_id">
  进入选手展厅
</router-link>
```

Add a visual layer inside `.hero`:

```vue
<div v-if="heroAthlete" class="hero__image" :style="heroImageStyle" aria-hidden="true" />
```

- [ ] **Step 2: Adjust homepage styles**

Add these styles to `client/portal/views/Home.vue`:

```css
.hero__image {
  position: absolute;
  inset: 0;
  z-index: 1;
  background-size: cover;
  background-position: center top;
  opacity: 0.32;
  filter: saturate(1.08);
}

.hero__cta {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 42px;
  padding: 0 18px;
  border-radius: 999px;
  color: #fff;
  background: #1d1d1f;
  text-decoration: none;
  font-size: 14px;
  font-weight: 600;
}
```

- [ ] **Step 3: Prefer explicit player hero fields**

In `client/portal/views/Player.vue`, replace `heroCover()` with:

```js
    heroCover() {
      if (this.athlete && this.athlete.hero_image_url) return this.athlete.hero_image_url;
      const h = (this.highlights || []).find((x) => x && x.cover_url);
      return h ? h.cover_url : '';
    },
```

Replace `heroTagline()` with:

```js
    heroTagline() {
      if (this.athlete.profile_title && this.athlete.profile_summary) {
        return this.athlete.profile_title + '｜' + this.athlete.profile_summary;
      }
      if (this.athlete.profile_title) return this.athlete.profile_title;
      if (this.athlete.profile_summary) return this.athlete.profile_summary;
      const h = this.highlights && this.highlights[0];
      if (h && h.title) return h.title;
      if (this.athlete.association) return this.athlete.association;
      return '';
    },
```

Add social link rendering after the tagline:

```vue
<a v-if="athlete.social_url" class="player-museum__social hero-reveal" :class="{ 'is-visible': heroReady }" :style="{ transitionDelay: '210ms' }" :href="athlete.social_url" target="_blank" rel="noopener noreferrer">
  外部主页
</a>
```

Add the style:

```css
.player-museum__social {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 38px;
  margin-top: 16px;
  padding: 0 16px;
  border-radius: 999px;
  color: #1d1d1f;
  background: rgba(255, 255, 255, 0.82);
  text-decoration: none;
  font-size: 14px;
  font-weight: 600;
}
```

- [ ] **Step 4: Run portal verification**

Run:

```bash
npm run build
```

Expected: portal bundle builds without Vue syntax errors.

Run:

```bash
VITE_USE_MOCK=true npm run dev
```

Expected: Vite dev server starts. Open `/` and `/players/1` in the in-app browser and verify the hero is visible at desktop and mobile widths.

- [ ] **Step 5: Commit**

Run:

```bash
git add client/portal/views/Home.vue client/portal/views/Player.vue
git commit -m "feat: upgrade portal to premium player showcase"
```

Expected: commit succeeds with portal view changes.

---

### Task 6: Final Verification And Handoff

**Files:**
- Verify: `tt-admin/tt-admin-api/src/main/resources/db/schema.sql`
- Verify: `client/admin/views/Layout.vue`
- Verify: `client/portal/views/Home.vue`
- Verify: `client/portal/views/Player.vue`

- [ ] **Step 1: Run backend verification**

Run:

```bash
mvn -pl tt-admin/tt-admin-api test
```

Expected: `BUILD SUCCESS`.

- [ ] **Step 2: Run frontend verification**

Run:

```bash
npm run build
```

Expected: Vite builds both admin and portal entries successfully.

- [ ] **Step 3: Smoke public APIs**

With the backend running, run:

```bash
curl -s http://localhost:8096/api/public/home/layout | head -50
curl -s http://localhost:8096/api/public/athletes/1 | head -50
```

Expected: the first response contains `highlight_athletes`; the second contains `athlete`, `highlights`, and `business_previews`.

- [ ] **Step 4: Smoke admin APIs**

With a valid admin token in `$TT_TOKEN`, run:

```bash
curl -s -H "Authorization: Bearer $TT_TOKEN" http://localhost:8096/api/admin/athletes?page=1\&pageSize=5 | head -50
```

Expected: response contains `list`, `total`, and athlete rows with existing fields plus nullable profile fields after migration.

- [ ] **Step 5: Inspect final diff**

Run:

```bash
git status --short
git diff --stat HEAD~5..HEAD
```

Expected: only the planned documentation, migration, Java service/test, and Vue view files are changed.

---

## Self-Review

**Spec coverage:** The plan covers the requested positioning change by updating product authority docs, registering a new slice, adding star-player profile fields, shifting admin IA, and upgrading the public homepage/player page toward premium showcase. Existing event management remains as a supporting data library.

**Placeholder scan:** The plan contains no placeholder markers and no open-ended validation instructions. Each code-changing task includes concrete snippets and commands.

**Type consistency:** The profile fields are consistently named `profile_title`, `profile_summary`, `hero_image_url`, and `social_url` across SQL, Java maps, tests, admin forms, public API payloads, and portal views.

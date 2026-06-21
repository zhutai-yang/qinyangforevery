<template>
  <div class="home-showcase">
    <section class="hero" aria-label="TT 青春赛场首页">
      <div class="hero__soft-bg" aria-hidden="true" />
      <header class="home-nav hero-reveal" :class="{ 'is-visible': heroReady }">
        <router-link class="home-brand" to="/" aria-label="TT 青春赛场首页">
          <span>TT</span>
          <strong>青春赛场</strong>
        </router-link>
        <nav class="home-nav__links" aria-label="首页导航">
          <router-link to="/">首页</router-link>
          <router-link to="/events">赛事赛果</router-link>
          <a href="#players">明星选手</a>
          <router-link to="/news">新闻资讯</router-link>
          <a href="#moments">精彩视频</a>
          <a href="#tickets">票务信息</a>
          <a href="#about">关于我们</a>
        </nav>
        <router-link class="home-nav__search" to="/news" aria-label="搜索">⌕</router-link>
      </header>

      <div class="hero__main">
        <div class="hero__copy">
          <p v-if="heroSeason" class="hero__season hero-reveal" :class="{ 'is-visible': heroReady }">
            <span class="season-ball" aria-hidden="true" />
            {{ heroSeason }}
          </p>
          <h1 v-if="heroTitle" class="hero__title hero-reveal" :class="{ 'is-visible': heroReady }" :style="{ transitionDelay: '80ms' }">
            <span class="hero__title-line">{{ heroTitle }}</span>
          </h1>
          <p v-if="heroSlogan" class="hero__slogan hero-reveal" :class="{ 'is-visible': heroReady }" :style="{ transitionDelay: '140ms' }">
            {{ heroSlogan }}
          </p>
          <div v-if="heroMeta.length" class="hero__meta hero-reveal" :class="{ 'is-visible': heroReady }" :style="{ transitionDelay: '180ms' }">
            <span v-for="item in heroMeta" :key="item">{{ item }}</span>
          </div>
          <div v-if="heroCtaText" class="hero__actions hero-reveal" :class="{ 'is-visible': heroReady }" :style="{ transitionDelay: '220ms' }">
            <router-link class="hero__cta" :to="heroCtaTo">{{ heroCtaText }} <span>›</span></router-link>
          </div>
        </div>

        <div v-if="displayAthletes.length" class="hero__featured-players hero-reveal" :class="{ 'is-visible': heroReady }" :style="{ transitionDelay: '120ms' }">
          <header class="hero__featured-head">
            <span>明星选手</span>
            <router-link to="/events">进入选手展厅 ›</router-link>
          </header>
          <div class="hero__featured-list">
            <router-link
              v-for="(p, i) in displayAthletes.slice(0, 2)"
              :key="p.athlete_id || p.athlete_name"
              class="hero-player-card"
              :to="playerTo(p)"
            >
              <span class="hero-player-card__photo" :class="'hero-player-card__photo--' + ((i % 2) + 1)">
                <img v-if="p.hero_image_url || p.cover_url" :src="p.hero_image_url || p.cover_url" alt="" />
                <span v-else>{{ initials(p.athlete_name) }}</span>
              </span>
              <span class="hero-player-card__body">
                <strong>{{ p.athlete_name }}</strong>
                <em v-if="playerMeta(p)">{{ playerMeta(p) }}</em>
                <span v-if="playerScore(p)">{{ playerScore(p) }}</span>
              </span>
              <b v-if="p.world_rank != null">NO.{{ p.world_rank }}</b>
            </router-link>
          </div>
        </div>
      </div>

    </section>

    <section class="content" aria-label="首页内容">
      <div class="content__inner">
        <p v-if="loading" class="content__loading section-reveal" :class="{ 'is-visible': sectionVisible }">加载中…</p>

        <template v-else>
          <div class="content-grid">
            <section class="panel panel--articles section-reveal" :class="{ 'is-visible': sectionVisible }">
              <header class="panel__head">
                <h2><span>▣</span> 推荐文章</h2>
                <router-link to="/news">查看更多 ›</router-link>
              </header>
              <ul class="news-list">
                <li v-for="(a, i) in displayArticles" :key="a.id || a.title">
                  <router-link class="news-item" :to="articleTo(a)">
                    <span class="news-thumb" :class="'news-thumb--' + ((i % 4) + 1)">
                      <img v-if="a.cover_url || a.cover" :src="a.cover_url || a.cover" alt="" />
                    </span>
                    <span class="news-body">
                      <span v-if="articleTag(a)" class="news-tag" :class="'news-tag--' + ((i % 4) + 1)">{{ articleTag(a) }}</span>
                      <strong>{{ a.title }}</strong>
                      <em v-if="a.summary">{{ a.summary }}</em>
                      <span class="news-meta">
                        <span v-if="a.published_at">{{ a.published_at }}</span>
                        <span v-if="a.view_count != null">👁 {{ a.view_count }}</span>
                      </span>
                    </span>
                  </router-link>
                </li>
              </ul>
              <div v-if="!displayArticles.length" class="content-empty">暂无推荐文章</div>
            </section>
          </div>

          <section v-if="momentsConfig.title || momentsConfig.description" id="moments" class="moments-section section-reveal" :class="{ 'is-visible': sectionVisible }">
            <div class="moments-copy">
              <h2 v-if="momentsConfig.title">{{ momentsConfig.title }}</h2>
              <p v-if="momentsConfig.description">{{ momentsConfig.description }}</p>
              <router-link v-if="momentsConfig.link_text" :to="momentsConfig.link_to || '/news'">{{ momentsConfig.link_text }} ›</router-link>
            </div>
            <div class="moments-board" aria-hidden="true">
              <span class="moments-board__play">▶</span>
              <span class="moments-board__trail moments-board__trail--one" />
              <span class="moments-board__trail moments-board__trail--two" />
            </div>
          </section>

          <router-link v-if="ticketsConfig.title || ticketsConfig.description" id="tickets" class="join-banner section-reveal" :class="{ 'is-visible': sectionVisible }" :to="ticketsConfig.link_to || '/events'">
            <span class="join-banner__gear" aria-hidden="true" />
            <strong v-if="ticketsConfig.title">{{ ticketsConfig.title }}</strong>
            <em v-if="ticketsConfig.description">{{ ticketsConfig.description }}</em>
            <b v-if="ticketsConfig.link_text">{{ ticketsConfig.link_text }} ›</b>
          </router-link>

          <section v-if="aboutConfig.title || aboutConfig.description" id="about" class="about-section section-reveal" :class="{ 'is-visible': sectionVisible }">
            <div>
              <h2 v-if="aboutConfig.title">{{ aboutConfig.title }}</h2>
              <p v-if="aboutConfig.description">{{ aboutConfig.description }}</p>
            </div>
            <router-link v-if="aboutConfig.link_text" :to="aboutConfig.link_to || '/events'">{{ aboutConfig.link_text }} ›</router-link>
          </section>
        </template>
      </div>
    </section>
  </div>
</template>

<script>
import api from '../api.js';

export default {
  name: 'HomePage',
  data() {
    return {
      loading: true,
      blocks: [],
      featuredArticles: [],
      featuredPlayers: [],
      highlightAthletes: [],
      heroReady: false,
      sectionVisible: false,
      cardsVisible: false
    };
  },
  computed: {
    displayArticles() {
      return (this.featuredArticles || []).slice(0, 4);
    },
    displayAthletes() {
      const source = this.featuredPlayers && this.featuredPlayers.length ? this.featuredPlayers : this.highlightAthletes;
      return (source || []).map((item) => this.normalizeAthlete(item)).filter((item) => item.athlete_id || item.athlete_name).slice(0, 3);
    },
    blockConfigMap() {
      return (this.blocks || []).reduce((map, block) => {
        map[block.block_key] = this.parseBlockConfig(block);
        return map;
      }, {});
    },
    bannerConfig() {
      return this.blockConfigMap.banner || {};
    },
    heroSeason() {
      return this.bannerConfig.season || '';
    },
    heroTitle() {
      return this.bannerConfig.title || '';
    },
    heroSlogan() {
      return this.bannerConfig.slogan || this.bannerConfig.subtitle || '';
    },
    heroMeta() {
      if (Array.isArray(this.bannerConfig.meta)) return this.bannerConfig.meta.filter(Boolean);
      const dateRange = this.bannerConfig.date_range || this.formatDateRange(this.bannerConfig.start_date, this.bannerConfig.end_date);
      return [dateRange, this.bannerConfig.location].filter(Boolean);
    },
    heroCtaText() {
      return this.bannerConfig.cta_text || '';
    },
    heroCtaTo() {
      return this.bannerConfig.cta_to || '/events';
    },
    momentsConfig() {
      return this.blockConfigMap.moments || this.blockConfigMap.video || {};
    },
    athletesConfig() {
      return this.blockConfigMap.highlight_athletes || this.blockConfigMap.featured_players || {};
    },
    ticketsConfig() {
      return this.blockConfigMap.tickets || {};
    },
    aboutConfig() {
      return this.blockConfigMap.about || {};
    }
  },
  async mounted() {
    requestAnimationFrame(() => {
      this.heroReady = true;
    });

    try {
      const { data } = await api.get('/home/layout');
      this.blocks = (data && data.blocks) || [];
      this.featuredArticles = (data && data.featured_articles) || [];
      this.featuredPlayers = (data && data.featured_players) || [];
      this.highlightAthletes = (data && data.highlight_athletes) || [];
    } catch (e) {
      this.$message.error('加载失败');
    } finally {
      this.loading = false;
      this.$nextTick(() => this.observeSection());
    }
  },
  methods: {
    parseBlockConfig(block) {
      const raw = block && (block.config || block.config_json);
      if (!raw) return {};
      if (typeof raw === 'object') return raw;
      try {
        return JSON.parse(raw);
      } catch (e) {
        return {};
      }
    },
    formatDateRange(start, end) {
      if (start && end && start !== end) return start + ' - ' + end;
      return start || end || '';
    },
    normalizeAthlete(item) {
      if (!item) return {};
      if (item.athlete) {
        const athlete = item.athlete || {};
        return {
          athlete_id: athlete.id,
          athlete_name: athlete.name,
          country: athlete.association || athlete.nationality,
          group: this.genderGroup(athlete.gender),
          hero_image_url: athlete.hero_image_url,
          cover_url: athlete.cover_url,
          world_rank: athlete.current_world_rank,
          score: athlete.score
        };
      }
      return item;
    },
    genderGroup(gender) {
      if (gender === 'male' || gender === 'M' || gender === '男') return '男单';
      if (gender === 'female' || gender === 'F' || gender === '女') return '女单';
      return '';
    },
    playerMeta(player) {
      return [player.country, player.group || player.category].filter(Boolean).join(' ｜ ');
    },
    playerScore(player) {
      const parts = [];
      if (player.world_rank != null) parts.push('世界排名 ' + player.world_rank);
      if (player.score != null) parts.push('积分 ' + player.score);
      return parts.join(' · ');
    },
    articleTag(article) {
      return article.category_name || article.category || article.tag || '';
    },
    articleTo(article) {
      if (!article || !article.id) return '/news';
      return '/news/' + (article.slug || article.id);
    },
    playerTo(player) {
      if (!player || !player.athlete_id) return '/events';
      return '/players/' + player.athlete_id;
    },
    initials(name) {
      return String(name || 'TT').slice(0, 2).toUpperCase();
    },
    observeSection() {
      if (typeof IntersectionObserver === 'undefined') {
        this.sectionVisible = true;
        this.cardsVisible = true;
        return;
      }
      const el = this.$el.querySelector('.content');
      if (!el) {
        this.sectionVisible = true;
        this.cardsVisible = true;
        return;
      }
      const io = new IntersectionObserver(
        (entries) => {
          entries.forEach((entry) => {
            if (entry.isIntersecting) {
              this.sectionVisible = true;
              requestAnimationFrame(() => {
                this.cardsVisible = true;
              });
              io.disconnect();
            }
          });
        },
        { rootMargin: '0px 0px -12% 0px', threshold: 0.05 }
      );
      io.observe(el);
    }
  }
};
</script>

<style scoped>
.home-showcase {
  --ink: #151923;
  --muted: #7b8190;
  --coral: #ff4b59;
  --coral-dark: #f03a45;
  --green: #55d2a2;
  --blue: #5d91f5;
  --yellow: #ffd057;
  --paper: #fff;
  --soft: #f8fbff;
  --ease-out: cubic-bezier(0.16, 1, 0.3, 1);
  overflow-x: hidden;
  color: var(--ink);
  background:
    radial-gradient(circle at 0% 50%, rgba(255, 75, 89, 0.08), transparent 22%),
    linear-gradient(180deg, #fff 0%, #f9fbff 100%);
}

.hero {
  position: relative;
  min-height: 700px;
  padding: 26px clamp(22px, 4vw, 42px) 0;
  overflow: hidden;
  isolation: isolate;
}

.hero__soft-bg {
  position: absolute;
  inset: 0;
  z-index: 0;
  background:
    radial-gradient(circle at 6% 78%, rgba(255, 75, 89, 0.1), transparent 20%),
    radial-gradient(circle at 88% 26%, rgba(93, 145, 245, 0.18), transparent 32%),
    linear-gradient(135deg, #fff 0%, #fbfdff 58%, #fff5ca 100%);
}

.home-nav {
  position: relative;
  z-index: 5;
  display: flex;
  align-items: center;
  gap: 28px;
  max-width: 1180px;
  margin: 0 auto 48px;
}

.home-brand {
  display: inline-flex;
  align-items: center;
  gap: 11px;
  color: var(--ink);
  text-decoration: none;
  white-space: nowrap;
}

.home-brand span {
  color: var(--coral);
  font-size: 36px;
  font-style: italic;
  font-weight: 950;
  line-height: 1;
}

.home-brand strong {
  font-size: 24px;
  font-weight: 950;
}

.home-nav__links {
  display: flex;
  align-items: center;
  justify-content: center;
  flex: 1;
  gap: clamp(18px, 3vw, 42px);
  min-width: 0;
}

.home-nav__links a {
  position: relative;
  color: rgba(21, 25, 35, 0.92);
  text-decoration: none;
  font-size: 15px;
  font-weight: 800;
  white-space: nowrap;
}

.home-nav__links a.router-link-exact-active::after,
.home-nav__links a:hover::after {
  content: '';
  position: absolute;
  left: 50%;
  bottom: -14px;
  width: 24px;
  height: 3px;
  border-radius: 99px;
  background: var(--coral);
  transform: translateX(-50%);
}

.home-nav__search {
  display: grid;
  place-items: center;
  width: 36px;
  height: 36px;
  margin-left: auto;
  color: var(--ink);
  text-decoration: none;
  font-size: 31px;
  font-weight: 300;
}

.hero__main {
  position: relative;
  z-index: 2;
  display: grid;
  grid-template-columns: minmax(300px, 0.86fr) minmax(300px, 1.14fr);
  align-items: center;
  max-width: 1180px;
  min-height: 500px;
  margin: 0 auto;
}

.hero__copy {
  position: relative;
  z-index: 4;
  padding-bottom: 72px;
}

.hero__season {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  min-height: 44px;
  margin: 0 0 28px;
  padding: 0 18px 0 9px;
  color: #fff;
  border-radius: 12px 22px 22px 12px;
  background: linear-gradient(135deg, var(--coral), #ff7e84);
  box-shadow: 0 16px 34px rgba(255, 75, 89, 0.24);
  font-size: 18px;
  font-weight: 950;
}

.season-ball {
  display: grid;
  place-items: center;
  width: 34px;
  height: 34px;
  border-radius: 50%;
  background: #fff;
}

.season-ball::after {
  content: '';
  width: 14px;
  height: 14px;
  border-radius: 50%;
  border: 5px solid var(--coral);
}

.hero__title {
  display: grid;
  gap: 8px;
  margin: 0;
  color: var(--ink);
  font-size: clamp(48px, 6.4vw, 82px);
  font-weight: 950;
  letter-spacing: 0;
  line-height: 0.95;
  text-shadow: none;
}

.hero__title-line {
  position: relative;
  display: inline-block;
  width: max-content;
  padding-right: 0.08em;
  color: var(--ink);
  letter-spacing: 0.01em;
  transform: skew(-3deg);
}

.hero__title-line--accent {
  color: var(--coral);
  letter-spacing: 0;
  text-shadow: 0 3px 0 rgba(255, 75, 89, 0.1);
}

.hero__title-line--accent::after {
  content: '';
  position: absolute;
  left: 0.04em;
  right: 0.02em;
  bottom: -0.08em;
  height: 0.16em;
  border-radius: 999px;
  background: linear-gradient(90deg, rgba(255, 75, 89, 0.22), rgba(255, 208, 87, 0.42));
  transform: skew(-14deg);
  z-index: -1;
}

.hero__slogan {
  margin: 24px 0 0;
  color: rgba(21, 25, 35, 0.55);
  font-size: 22px;
  font-weight: 500;
  letter-spacing: 0.16em;
}

.hero__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 16px 34px;
  margin-top: 34px;
  color: rgba(21, 25, 35, 0.72);
  font-size: 15px;
  font-weight: 700;
}

.hero__meta span {
  position: relative;
  padding-left: 24px;
}

.hero__meta span::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  width: 14px;
  height: 14px;
  border-radius: 4px;
  background: var(--coral);
  transform: translateY(-50%);
  box-shadow: inset 0 0 0 3px rgba(255, 255, 255, 0.75);
}

.hero__actions {
  display: flex;
  flex-wrap: wrap;
  gap: 22px;
  margin-top: 32px;
}

.hero__cta,
.hero__ghost {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 14px;
  min-width: 142px;
  min-height: 60px;
  padding: 0 28px;
  border-radius: 999px;
  text-decoration: none;
  font-size: 17px;
  font-weight: 900;
}

.hero__cta {
  color: #fff;
  background: linear-gradient(135deg, var(--coral), #ff394e);
  box-shadow: 0 18px 36px rgba(255, 75, 89, 0.3);
}

.hero__cta span {
  display: grid;
  place-items: center;
  width: 27px;
  height: 27px;
  border-radius: 50%;
  color: var(--coral);
  background: #fff;
  font-size: 24px;
  line-height: 1;
}

.hero__ghost {
  color: rgba(21, 25, 35, 0.74);
  background: rgba(255, 255, 255, 0.78);
  border: 1px solid rgba(21, 25, 35, 0.1);
  box-shadow: 0 16px 36px rgba(21, 25, 35, 0.06);
}

.hero__ghost span {
  color: var(--coral);
}

.hero__featured-players {
  align-self: center;
  min-width: 0;
  padding: 24px;
  border: 1px solid rgba(21, 25, 35, 0.08);
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.9);
  box-shadow: 0 24px 56px rgba(21, 25, 35, 0.1);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
}

.hero__featured-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  margin-bottom: 18px;
}

.hero__featured-head span {
  color: var(--ink);
  font-size: 28px;
  font-weight: 950;
}

.hero__featured-head a {
  color: rgba(21, 25, 35, 0.5);
  font-size: 14px;
  font-weight: 900;
  text-decoration: none;
}

.hero__featured-list {
  display: grid;
  gap: 16px;
}

.hero-player-card {
  position: relative;
  display: grid;
  grid-template-columns: 118px minmax(0, 1fr);
  gap: 16px;
  align-items: center;
  min-height: 136px;
  padding: 14px 18px 14px 14px;
  color: var(--ink);
  text-decoration: none;
  border-radius: 16px;
  background: linear-gradient(135deg, #f4fbff, #fff);
  box-shadow: inset 0 0 0 1px rgba(21, 25, 35, 0.04);
}

.hero-player-card__photo {
  display: grid;
  place-items: center;
  width: 118px;
  aspect-ratio: 1;
  overflow: hidden;
  border-radius: 14px;
  color: #fff;
  background: linear-gradient(145deg, #ff6472, #ff4052);
  font-size: 30px;
  font-weight: 950;
}

.hero-player-card__photo--2 {
  background: linear-gradient(145deg, #5d91f5, #2864d8);
}

.hero-player-card__photo img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.hero-player-card__body {
  display: grid;
  gap: 7px;
  min-width: 0;
  padding-right: 68px;
}

.hero-player-card__body strong {
  font-size: 28px;
  line-height: 1.12;
  font-weight: 950;
}

.hero-player-card__body em,
.hero-player-card__body span {
  color: rgba(21, 25, 35, 0.55);
  font-size: 15px;
  font-style: normal;
  font-weight: 800;
}

.hero-player-card b {
  position: absolute;
  top: 18px;
  right: 18px;
  padding: 7px 13px;
  border-radius: 999px;
  color: #fff;
  background: var(--coral);
  font-size: 13px;
  line-height: 1;
}

.content {
  position: relative;
  padding: 36px clamp(22px, 4vw, 42px) 110px;
  background:
    radial-gradient(circle at 0 0, rgba(255, 75, 89, 0.06), transparent 22%),
    linear-gradient(180deg, #fff7f5 0%, #fbfdff 32%, #fff 100%);
}

.content__inner {
  max-width: 1180px;
  margin: 0 auto;
}

.content__loading {
  margin: 0;
  color: var(--muted);
  font-weight: 700;
}

.content-empty {
  min-height: 112px;
  display: grid;
  place-items: center;
  border: 1px dashed rgba(21, 25, 35, 0.14);
  border-radius: 12px;
  color: rgba(21, 25, 35, 0.46);
  font-size: 14px;
  font-weight: 800;
}

.content-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 24px;
}

.panel {
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.9);
  box-shadow: 0 20px 56px rgba(21, 25, 35, 0.07);
  padding: 22px;
}

.panel__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 18px;
}

.panel__head h2 {
  display: flex;
  align-items: center;
  gap: 10px;
  margin: 0;
  color: var(--ink);
  font-size: 24px;
  font-weight: 950;
}

.panel__head h2 span {
  color: var(--coral);
  font-size: 22px;
}

.panel__head a {
  color: rgba(21, 25, 35, 0.48);
  text-decoration: none;
  font-size: 14px;
  font-weight: 800;
}

.news-list {
  list-style: none;
  display: grid;
  gap: 17px;
  margin: 0;
  padding: 0;
}

.news-item {
  display: grid;
  grid-template-columns: 205px minmax(0, 1fr);
  gap: 22px;
  color: var(--ink);
  text-decoration: none;
  padding-bottom: 17px;
  border-bottom: 1px solid rgba(21, 25, 35, 0.08);
}

.news-list li:last-child .news-item {
  border-bottom: 0;
  padding-bottom: 0;
}

.news-thumb {
  position: relative;
  min-height: 108px;
  overflow: hidden;
  border-radius: 9px;
  background:
    radial-gradient(circle at 72% 24%, rgba(255, 255, 255, 0.8) 0 8%, transparent 9%),
    linear-gradient(145deg, #1b2440, #ff4b59);
}

.news-thumb::after {
  content: '';
  position: absolute;
  width: 80px;
  height: 80px;
  right: 18px;
  bottom: -20px;
  border-radius: 50%;
  background: radial-gradient(circle, #ef233c 0 58%, #1f2a44 59% 64%, transparent 65%);
}

.news-thumb--2 {
  background: linear-gradient(145deg, #112f66, #5d91f5);
}

.news-thumb--3 {
  background: linear-gradient(145deg, #ffe2b0, #55d2a2);
}

.news-thumb--4 {
  background: linear-gradient(145deg, #0e172e, #ff4b59);
}

.news-thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.news-body {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.news-tag {
  align-self: flex-start;
  padding: 5px 12px;
  border-radius: 999px;
  color: var(--coral);
  background: rgba(255, 75, 89, 0.1);
  font-size: 12px;
  font-style: normal;
  font-weight: 900;
}

.news-tag--2 {
  color: #20a576;
  background: rgba(85, 210, 162, 0.16);
}

.news-tag--3 {
  color: #477ee9;
  background: rgba(93, 145, 245, 0.13);
}

.news-tag--4 {
  color: #de9b00;
  background: rgba(255, 208, 87, 0.18);
}

.news-body strong {
  font-size: 18px;
  font-weight: 900;
  line-height: 1.35;
}

.news-body em {
  color: rgba(21, 25, 35, 0.48);
  font-size: 14px;
  font-style: normal;
  font-weight: 700;
  line-height: 1.45;
}

.news-meta {
  display: flex;
  justify-content: space-between;
  gap: 14px;
  color: rgba(21, 25, 35, 0.4);
  font-size: 13px;
  font-weight: 700;
}

.moments-section {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(220px, 0.72fr);
  gap: 22px;
  align-items: center;
  margin-top: 26px;
  padding: 28px;
  border-radius: 16px;
  color: #fff;
  background:
    radial-gradient(circle at 80% 20%, rgba(255, 208, 87, 0.32), transparent 28%),
    linear-gradient(135deg, #172033, #2857b8 62%, #ff4b59);
  box-shadow: 0 22px 58px rgba(23, 32, 51, 0.18);
  overflow: hidden;
}

.moments-copy h2,
.about-section h2 {
  margin: 0;
  font-size: 30px;
  line-height: 1.16;
  font-weight: 950;
}

.moments-copy p,
.about-section p {
  margin: 10px 0 0;
  font-size: 15px;
  line-height: 1.7;
  font-weight: 700;
}

.moments-copy p {
  color: rgba(255, 255, 255, 0.78);
}

.moments-copy a,
.about-section a {
  display: inline-flex;
  margin-top: 18px;
  color: inherit;
  font-size: 14px;
  font-weight: 900;
  text-decoration: none;
}

.moments-board {
  position: relative;
  min-height: 180px;
  border-radius: 18px;
  background:
    linear-gradient(140deg, rgba(255, 255, 255, 0.26), rgba(255, 255, 255, 0.06)),
    repeating-linear-gradient(135deg, rgba(255, 255, 255, 0.16) 0 2px, transparent 2px 18px);
  border: 1px solid rgba(255, 255, 255, 0.22);
}

.moments-board__play {
  position: absolute;
  left: 50%;
  top: 50%;
  display: grid;
  width: 66px;
  height: 66px;
  place-items: center;
  border-radius: 50%;
  color: #ff4b59;
  background: #fff;
  font-size: 24px;
  box-shadow: 0 16px 36px rgba(21, 25, 35, 0.2);
  transform: translate(-50%, -50%);
}

.moments-board__trail {
  position: absolute;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.32);
}

.moments-board__trail--one {
  left: 24px;
  top: 34px;
  width: 92px;
  height: 12px;
}

.moments-board__trail--two {
  right: 30px;
  bottom: 38px;
  width: 130px;
  height: 12px;
}

.join-banner {
  position: relative;
  display: grid;
  grid-template-columns: auto minmax(0, 1fr) auto;
  align-items: center;
  gap: 18px;
  min-height: 108px;
  margin-top: 26px;
  padding: 18px 38px;
  color: #fff;
  text-decoration: none;
  border-radius: 16px;
  background:
    radial-gradient(circle at 8% 38%, rgba(255, 208, 87, 0.95) 0 10px, transparent 11px),
    radial-gradient(circle at 14% 54%, #fff 0 9px, transparent 10px),
    linear-gradient(135deg, var(--coral), #ff4b59 57%, var(--yellow));
  box-shadow: 0 18px 44px rgba(255, 75, 89, 0.17);
  overflow: hidden;
}

.join-banner__gear {
  width: 130px;
  height: 74px;
  border-radius: 50%;
  background: radial-gradient(circle, #ef233c 0 58%, #1f2a44 59% 65%, transparent 66%);
  transform: rotate(-16deg);
}

.join-banner strong {
  font-size: 34px;
  font-weight: 950;
}

.join-banner em {
  display: block;
  margin-top: 4px;
  font-size: 17px;
  font-style: normal;
  font-weight: 800;
}

.join-banner b {
  justify-self: end;
  padding: 14px 38px;
  border-radius: 999px;
  color: var(--coral);
  background: #fff;
  box-shadow: 0 12px 28px rgba(21, 25, 35, 0.12);
}

.about-section {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  margin-top: 26px;
  padding: 30px 34px;
  border: 1px solid rgba(47, 125, 255, 0.12);
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.86);
  box-shadow: 0 18px 48px rgba(21, 25, 35, 0.07);
}

.about-section h2 {
  color: var(--ink);
}

.about-section p {
  max-width: 720px;
  color: var(--muted);
}

.about-section a {
  flex: 0 0 auto;
  padding: 13px 24px;
  border-radius: 999px;
  color: #fff;
  background: var(--blue);
  box-shadow: 0 14px 28px rgba(93, 145, 245, 0.18);
}

.hero-reveal,
.section-reveal {
  opacity: 0;
  transform: translateY(24px);
  transition: opacity 0.82s var(--ease-out), transform 0.82s var(--ease-out);
}

.hero-reveal.is-visible,
.section-reveal.is-visible {
  opacity: 1;
  transform: translateY(0);
}

@media (max-width: 980px) and (min-width: 621px) {
  .hero {
    min-height: 690px;
    padding-inline: 20px;
  }

  .home-nav {
    align-items: flex-start;
    flex-wrap: wrap;
    margin-bottom: 34px;
  }

  .home-nav__links {
    order: 3;
    justify-content: flex-start;
    flex: 1 0 100%;
    overflow-x: auto;
    padding: 0 0 12px;
    -webkit-overflow-scrolling: touch;
  }

  .hero__main {
    display: block;
    min-height: 0;
  }

  .hero__copy {
    padding-bottom: 0;
  }

  .hero__season {
    min-height: 34px;
    margin-bottom: 18px;
    font-size: 14px;
  }

  .season-ball {
    width: 26px;
    height: 26px;
  }

  .season-ball::after {
    width: 10px;
    height: 10px;
    border-width: 4px;
  }

  .hero__title {
    font-size: clamp(44px, 8.2vw, 66px);
  }

  .hero__slogan {
    margin-top: 18px;
    font-size: 17px;
    letter-spacing: 0.1em;
  }

  .hero__meta {
    gap: 12px 18px;
    margin-top: 24px;
    font-size: 13px;
  }

  .hero__actions {
    gap: 14px;
    margin-top: 24px;
  }

  .hero__cta,
  .hero__ghost {
    min-width: 126px;
    min-height: 52px;
    padding-inline: 22px;
    font-size: 15px;
  }

  .hero__featured-players {
    width: min(100%, 620px);
    margin: 22px auto 0;
  }

  .content-grid {
    grid-template-columns: 1fr;
  }

  .moments-section {
    grid-template-columns: 1fr;
  }

  .about-section {
    display: grid;
  }
}

@media (max-width: 620px) {
  .hero {
    padding: 22px 18px 0;
  }

  .hero__main {
    display: block;
    min-height: 0;
  }

  .hero__copy {
    padding-bottom: 0;
  }

  .home-nav {
    flex-wrap: wrap;
    gap: 14px;
    margin-bottom: 34px;
  }

  .home-nav__links {
    order: 3;
    flex: 1 0 100%;
    justify-content: flex-start;
    gap: 22px;
    overflow-x: auto;
    padding-bottom: 12px;
    -webkit-overflow-scrolling: touch;
  }

  .home-nav__search {
    margin-left: auto;
  }

  .home-brand span {
    font-size: 31px;
  }

  .home-brand strong {
    font-size: 21px;
  }

  .hero__title {
    font-size: clamp(44px, 15vw, 66px);
  }

  .hero__slogan {
    font-size: 17px;
    letter-spacing: 0.1em;
  }

  .hero__actions {
    gap: 12px;
  }

  .hero__cta,
  .hero__ghost {
    min-height: 50px;
    padding: 0 20px;
  }

  .hero__featured-players {
    margin-top: 22px;
    padding: 18px;
  }

  .hero__featured-head span {
    font-size: 24px;
  }

  .hero-player-card {
    grid-template-columns: 92px minmax(0, 1fr);
    min-height: 112px;
  }

  .hero-player-card__photo {
    width: 92px;
    font-size: 24px;
  }

  .hero-player-card__body {
    padding-right: 58px;
  }

  .hero-player-card__body strong {
    font-size: 22px;
  }

  .hero-player-card__body em,
  .hero-player-card__body span {
    font-size: 13px;
  }

  .content-grid {
    grid-template-columns: 1fr;
  }

  .moments-section {
    grid-template-columns: 1fr;
    padding: 22px;
  }

  .moments-copy h2,
  .about-section h2 {
    font-size: 25px;
  }

  .moments-board {
    min-height: 150px;
  }

  .about-section {
    display: grid;
    padding: 24px;
  }

  .about-section a {
    justify-self: start;
  }

  .news-item {
    grid-template-columns: 118px minmax(0, 1fr);
    gap: 14px;
  }

  .news-thumb {
    min-height: 90px;
  }

  .join-banner {
    grid-template-columns: 1fr;
    padding: 24px;
  }

  .join-banner__gear {
    display: none;
  }

  .join-banner b {
    justify-self: start;
  }
}

@media (prefers-reduced-motion: reduce) {
  .hero__motion--a {
    animation: none;
  }

  .hero-reveal,
  .section-reveal {
    opacity: 1 !important;
    transform: none !important;
    transition: none !important;
  }
}
</style>

<template>
  <div class="player-museum">
    <van-loading v-if="loading" class="player-museum__loading" vertical>加载中…</van-loading>

    <van-empty v-else-if="!hasAthlete" class="player-museum__empty" description="未找到该球员或加载失败" />

    <template v-else>
      <section class="player-museum__hero" :class="{ 'player-museum__hero--has-cover': heroCover }">
        <div class="player-museum__hero-bg" aria-hidden="true">
          <div v-if="heroCover" class="player-museum__hero-cover" :style="heroCoverStyle" />
          <div class="player-museum__hero-mesh" />
          <div class="player-museum__hero-glow player-museum__hero-glow--a" />
          <div class="player-museum__hero-glow player-museum__hero-glow--b" />
        </div>
        <div class="player-museum__hero-inner">
          <p class="player-museum__eyebrow hero-reveal" :class="{ 'is-visible': heroReady }">球员展厅</p>
          <h1 class="player-museum__title hero-reveal" :class="{ 'is-visible': heroReady }" :style="{ transitionDelay: '70ms' }">
            {{ athlete.name || '球员' }}
          </h1>
          <p v-if="heroTagline" class="player-museum__tagline hero-reveal" :class="{ 'is-visible': heroReady }" :style="{ transitionDelay: '140ms' }">
            {{ heroTagline }}
          </p>
        </div>
      </section>

      <div class="player-museum__body">
        <section
          v-if="profileItems.length"
          data-museum-section="profile"
          class="player-museum__section section-reveal"
          :class="{ 'is-visible': sectionVisible.profile }"
        >
          <h2 class="player-museum__section-title">档案</h2>
          <div class="player-museum__chips">
            <span v-for="(it, i) in profileItems" :key="i" class="player-museum__chip">{{ it.label }} · {{ it.value }}</span>
          </div>
        </section>

        <section data-museum-section="upcoming" class="player-museum__section section-reveal" :class="{ 'is-visible': sectionVisible.upcoming }">
          <h2 class="player-museum__section-title">赛事预告</h2>
          <ul v-if="upcoming.length" class="player-museum__cards">
            <li v-for="m in upcoming" :key="m.id" class="player-museum__card-wrap card-reveal" :class="{ 'is-visible': cardsReady }">
              <button type="button" class="player-museum__card" @click="goEvent(m)">
                <span class="player-museum__card-kicker">{{ m.scheduled_at || '时间待定' }}</span>
                <span class="player-museum__card-title">{{ m.event_name || '赛事' }}</span>
                <span class="player-museum__card-meta">{{ previewMatchLabel(m) }}</span>
                <span v-if="m.event_id" class="player-museum__card-arrow" aria-hidden="true">→</span>
              </button>
            </li>
          </ul>
          <p v-else class="player-museum__muted">暂无赛事预告</p>
        </section>

        <section data-museum-section="history" class="player-museum__section section-reveal" :class="{ 'is-visible': sectionVisible.history }">
          <h2 class="player-museum__section-title">赛事记录</h2>
          <ul v-if="history.length" class="player-museum__timeline">
            <li v-for="m in history" :key="m.id" class="player-museum__timeline-item">
              <button type="button" class="player-museum__timeline-card" @click="goEvent(m)">
                <time class="player-museum__timeline-date">{{ m.scheduled_at || '—' }}</time>
                <div class="player-museum__timeline-body">
                  <span class="player-museum__timeline-event">{{ m.event_name || '赛事' }}</span>
                  <span class="player-museum__timeline-detail">{{ historyMatchLabel(m) }}</span>
                </div>
              </button>
            </li>
          </ul>
          <p v-else class="player-museum__muted">暂无赛事记录</p>
        </section>

        <section
          v-if="highlights.length"
          data-museum-section="highlights"
          class="player-museum__section section-reveal"
          :class="{ 'is-visible': sectionVisible.highlights }"
        >
          <h2 class="player-museum__section-title">高光展柜</h2>
          <div class="player-museum__swipe-wrap">
            <van-swipe class="player-museum__swipe" :autoplay="highlights.length > 1 ? 5000 : 0" :show-indicators="highlights.length > 1" indicator-color="rgba(255,255,255,0.85)">
              <van-swipe-item v-for="h in highlights" :key="h.id" class="player-museum__swipe-item">
                <div class="player-museum__highlight">
                  <div v-if="h.cover_url" class="player-museum__highlight-visual">
                    <img :src="h.cover_url" class="player-museum__highlight-img" alt="" loading="lazy" />
                    <div class="player-museum__highlight-scrim" />
                  </div>
                  <div class="player-museum__highlight-text">
                    <h3 class="player-museum__highlight-title">{{ h.title }}</h3>
                    <p v-if="h.summary" class="player-museum__highlight-summary">{{ h.summary }}</p>
                  </div>
                </div>
              </van-swipe-item>
            </van-swipe>
          </div>
        </section>

        <section data-museum-section="business" class="player-museum__section section-reveal" :class="{ 'is-visible': sectionVisible.business }">
          <h2 class="player-museum__section-title">商务预告</h2>
          <ul v-if="business_previews.length" class="player-museum__biz-list">
            <li v-for="b in business_previews" :key="b.id">
              <button type="button" class="player-museum__biz-card" @click="openBusiness(b)">
                <img v-if="b.cover_url" :src="b.cover_url" class="player-museum__biz-cover" alt="" loading="lazy" />
                <div class="player-museum__biz-text">
                  <span class="player-museum__biz-title">{{ b.title }}</span>
                  <span class="player-museum__biz-meta">{{ businessPreviewLabel(b) }}</span>
                </div>
                <span v-if="b.link_url" class="player-museum__card-arrow" aria-hidden="true">↗</span>
              </button>
            </li>
          </ul>
          <p v-else class="player-museum__muted">暂无商务预告</p>
        </section>
      </div>
    </template>
  </div>
</template>

<script>
import api from '../api.js';

export default {
  name: 'PlayerPage',
  props: { id: { type: [String, Number], required: true } },
  data() {
    return {
      loading: true,
      athlete: {},
      upcoming: [],
      history: [],
      highlights: [],
      business_previews: [],
      heroReady: false,
      sectionVisible: {
        profile: false,
        upcoming: false,
        history: false,
        highlights: false,
        business: false
      },
      cardsReady: false,
      io: null
    };
  },
  computed: {
    hasAthlete() {
      return !!(this.athlete && this.athlete.id != null);
    },
    heroCover() {
      const h = (this.highlights || []).find((x) => x && x.cover_url);
      return h ? h.cover_url : '';
    },
    heroCoverStyle() {
      if (!this.heroCover) return {};
      return { backgroundImage: `url(${this.heroCover})` };
    },
    heroTagline() {
      const h = this.highlights && this.highlights[0];
      if (h && h.title) return h.title;
      if (this.athlete.association) return this.athlete.association;
      return '';
    },
    profileItems() {
      const a = this.athlete || {};
      const rows = [];
      if (a.gender) rows.push({ label: '性别', value: a.gender });
      if (a.association) rows.push({ label: '协会', value: a.association });
      if (a.birth_date) rows.push({ label: '出生日期', value: a.birth_date });
      return rows;
    }
  },
  watch: {
    id: 'fetch'
  },
  mounted() {
    this.fetch();
  },
  beforeDestroy() {
    this.disconnectObserver();
  },
  methods: {
    playersLabel(m) {
      const parts = Array.isArray(m.participants) ? m.participants : [];
      const names = parts
        .slice()
        .sort((a, b) => (a.side_order || 0) - (b.side_order || 0))
        .map((p) => p.athlete_name)
        .filter(Boolean);
      return names.length ? names.join(' vs ') : '';
    },
    previewMatchLabel(m) {
      const pieces = [];
      if (m.round_label) pieces.push(m.round_label);
      const players = this.playersLabel(m);
      if (players) pieces.push(players);
      return pieces.join(' · ') || '—';
    },
    historyMatchLabel(m) {
      const pieces = [];
      if (m.round_label) pieces.push(m.round_label);
      const players = this.playersLabel(m);
      if (players) pieces.push(players);
      const r = m.result || {};
      if (r.score_home != null && r.score_away != null) {
        pieces.push(`比分 ${r.score_home}:${r.score_away}`);
      }
      if (r.winner_athlete_id != null) {
        const winner = (m.participants || []).find((p) => String(p.athlete_id) === String(r.winner_athlete_id));
        if (winner && winner.athlete_name) pieces.push('胜者 ' + winner.athlete_name);
      }
      return pieces.join(' · ') || '—';
    },
    businessPreviewLabel(b) {
      const pieces = [];
      if (b.scheduled_at) pieces.push(b.scheduled_at);
      if (b.summary) pieces.push(b.summary);
      return pieces.join(' · ') || '—';
    },
    openBusiness(b) {
      if (!b || !b.link_url) return;
      try {
        window.open(b.link_url, '_blank');
      } catch (e) {
        // ignore
      }
    },
    goEvent(m) {
      if (!m || m.event_id == null) return;
      this.$router.push('/events/' + m.event_id);
    },
    disconnectObserver() {
      if (this.io) {
        this.io.disconnect();
        this.io = null;
      }
    },
    setupReveal() {
      this.disconnectObserver();
      this.$nextTick(() => {
        requestAnimationFrame(() => {
          this.heroReady = true;
        });
        if (typeof IntersectionObserver === 'undefined') {
          this.sectionVisible = {
            profile: true,
            upcoming: true,
            history: true,
            highlights: true,
            business: true
          };
          this.cardsReady = true;
          return;
        }
        this.io = new IntersectionObserver(
          (entries) => {
            entries.forEach((entry) => {
              if (!entry.isIntersecting) return;
              const key = entry.target.getAttribute('data-museum-section');
              if (key && Object.prototype.hasOwnProperty.call(this.sectionVisible, key)) {
                this.sectionVisible[key] = true;
                if (key === 'upcoming') {
                  requestAnimationFrame(() => {
                    this.cardsReady = true;
                  });
                }
              }
            });
          },
          { rootMargin: '0px 0px -8% 0px', threshold: 0.08 }
        );
        this.$el.querySelectorAll('[data-museum-section]').forEach((el) => this.io.observe(el));
      });
    },
    async fetch() {
      this.loading = true;
      this.heroReady = false;
      this.cardsReady = false;
      this.sectionVisible = {
        profile: false,
        upcoming: false,
        history: false,
        highlights: false,
        business: false
      };
      try {
        const { data } = await api.get('/athletes/' + this.id);
        this.athlete = (data && data.athlete) || {};
        this.upcoming = (data && data.upcoming) || [];
        this.history = (data && data.history) || [];
        this.highlights = (data && data.highlights) || [];
        this.business_previews = (data && data.business_previews) || [];
      } catch (e) {
        this.$message.error('加载失败');
        this.athlete = {};
        this.upcoming = [];
        this.history = [];
        this.highlights = [];
        this.business_previews = [];
      } finally {
        this.loading = false;
        if (this.hasAthlete) {
          this.$nextTick(() => this.setupReveal());
        }
      }
    }
  }
};
</script>

<style scoped>
.player-museum {
  --neo-ease: cubic-bezier(0.25, 0.1, 0.25, 1);
  --neo-ease-out: cubic-bezier(0.16, 1, 0.3, 1);
  --museum-text: #1d1d1f;
  --museum-muted: rgba(0, 0, 0, 0.45);
  margin: -12px -12px 0;
  width: calc(100% + 24px);
  box-sizing: border-box;
  overflow-x: hidden;
}

@media (min-width: 768px) {
  .player-museum {
    margin: -20px -24px 0;
    width: calc(100% + 48px);
  }
}

.player-museum__loading {
  padding: 48px 0;
  text-align: center;
}

.player-museum__empty {
  padding: 48px 16px;
}

/* —— Hero —— */
.player-museum__hero {
  position: relative;
  min-height: min(52vh, 420px);
  display: flex;
  align-items: flex-end;
  padding: 32px 20px 40px;
  isolation: isolate;
}

.player-museum__hero-bg {
  position: absolute;
  inset: 0;
  z-index: 0;
  overflow: hidden;
}

.player-museum__hero-cover {
  position: absolute;
  inset: 0;
  background-size: cover;
  background-position: center top;
  transform: scale(1.02);
  filter: saturate(1.05);
}

.player-museum__hero--has-cover .player-museum__hero-mesh {
  opacity: 0.88;
}

.player-museum__hero-mesh {
  position: absolute;
  inset: 0;
  background:
    radial-gradient(ellipse 80% 55% at 50% -15%, rgba(120, 150, 255, 0.38), transparent 55%),
    radial-gradient(ellipse 55% 40% at 100% 40%, rgba(255, 120, 180, 0.14), transparent 50%),
    linear-gradient(180deg, rgba(245, 245, 247, 0.94) 0%, rgba(232, 232, 237, 0.98) 100%);
  animation: museum-mesh 20s var(--neo-ease) infinite alternate;
}

.player-museum__hero--has-cover .player-museum__hero-mesh {
  background:
    linear-gradient(180deg, rgba(15, 15, 20, 0.45) 0%, rgba(10, 10, 14, 0.82) 100%),
    radial-gradient(ellipse 90% 60% at 50% 0%, rgba(99, 102, 241, 0.25), transparent 55%);
  animation: none;
}

.player-museum__hero-glow {
  position: absolute;
  border-radius: 50%;
  filter: blur(72px);
  opacity: 0.5;
  pointer-events: none;
  animation: museum-float 16s var(--neo-ease) infinite alternate;
}

.player-museum__hero-glow--a {
  width: min(58vw, 400px);
  height: min(58vw, 400px);
  top: 8%;
  right: -12%;
  background: radial-gradient(circle, rgba(99, 102, 241, 0.42), transparent 72%);
}

.player-museum__hero-glow--b {
  width: min(48vw, 340px);
  height: min(48vw, 340px);
  bottom: 18%;
  left: -8%;
  background: radial-gradient(circle, rgba(236, 72, 153, 0.22), transparent 72%);
  animation-delay: -4s;
}

@keyframes museum-mesh {
  0% {
    filter: hue-rotate(0deg) saturate(1);
    transform: scale(1);
  }
  100% {
    filter: hue-rotate(14deg) saturate(1.06);
    transform: scale(1.02);
  }
}

@keyframes museum-float {
  0% {
    transform: translate(0, 0) scale(1);
  }
  100% {
    transform: translate(-14px, 10px) scale(1.06);
  }
}

.player-museum__hero-inner {
  position: relative;
  z-index: 2;
  max-width: 720px;
  width: 100%;
  margin: 0 auto;
}

.player-museum__hero--has-cover .player-museum__eyebrow,
.player-museum__hero--has-cover .player-museum__title,
.player-museum__hero--has-cover .player-museum__tagline {
  color: #fff;
  text-shadow: 0 2px 24px rgba(0, 0, 0, 0.35);
}

.player-museum__hero--has-cover .player-museum__eyebrow {
  color: rgba(255, 255, 255, 0.75);
}

.player-museum__eyebrow {
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: rgba(0, 0, 0, 0.42);
  margin: 0 0 10px;
}

.player-museum__title {
  font-size: clamp(28px, 8vw, 44px);
  font-weight: 600;
  letter-spacing: -0.03em;
  line-height: 1.12;
  margin: 0 0 12px;
  color: var(--museum-text);
}

.player-museum__tagline {
  font-size: clamp(15px, 3.5vw, 17px);
  line-height: 1.45;
  color: rgba(0, 0, 0, 0.52);
  margin: 0;
  max-width: 40em;
}

.hero-reveal {
  opacity: 0;
  transform: translateY(22px) scale(0.99);
  filter: blur(6px);
  transition:
    opacity 0.95s var(--neo-ease-out),
    transform 0.95s var(--neo-ease-out),
    filter 0.95s var(--neo-ease-out);
}

.hero-reveal.is-visible {
  opacity: 1;
  transform: translateY(0) scale(1);
  filter: blur(0);
}

/* —— Body —— */
.player-museum__body {
  padding: 8px 12px 32px;
  background: linear-gradient(180deg, #fafafa 0%, #f5f5f7 100%);
}

@media (min-width: 768px) {
  .player-museum__body {
    padding: 12px 24px 40px;
  }
}

.player-museum__section {
  margin-top: 28px;
}

.player-museum__section:first-child {
  margin-top: 20px;
}

.player-museum__section-title {
  font-size: 20px;
  font-weight: 600;
  letter-spacing: -0.02em;
  margin: 0 0 14px;
  color: var(--museum-text);
}

.section-reveal {
  opacity: 0;
  transform: translateY(16px);
  transition: opacity 0.75s var(--neo-ease-out), transform 0.75s var(--neo-ease-out);
}

.section-reveal.is-visible {
  opacity: 1;
  transform: translateY(0);
}

.player-museum__muted {
  margin: 0;
  color: var(--museum-muted);
  font-size: 14px;
}

/* Chips */
.player-museum__chips {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.player-museum__chip {
  display: inline-block;
  padding: 8px 12px;
  border-radius: 999px;
  font-size: 13px;
  color: var(--museum-text);
  background: rgba(255, 255, 255, 0.78);
  border: 1px solid rgba(0, 0, 0, 0.06);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
}

/* Cards (upcoming) */
.player-museum__cards {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.player-museum__card-wrap {
  opacity: 0;
  transform: translateY(12px);
  transition: opacity 0.55s var(--neo-ease-out), transform 0.55s var(--neo-ease-out);
}

.player-museum__card-wrap.is-visible {
  opacity: 1;
  transform: translateY(0);
}

.player-museum__card {
  width: 100%;
  text-align: left;
  display: grid;
  grid-template-columns: 1fr auto;
  grid-template-rows: auto auto;
  gap: 2px 8px;
  padding: 16px 18px;
  border: none;
  border-radius: 16px;
  cursor: pointer;
  font: inherit;
  color: var(--museum-text);
  background: rgba(255, 255, 255, 0.82);
  border: 1px solid rgba(0, 0, 0, 0.06);
  box-shadow: 0 2px 16px rgba(0, 0, 0, 0.05);
  backdrop-filter: saturate(180%) blur(16px);
  -webkit-backdrop-filter: saturate(180%) blur(16px);
  transition: transform 0.35s var(--neo-ease-out), box-shadow 0.35s var(--neo-ease-out);
}

.player-museum__card:active {
  transform: scale(0.995);
}

.player-museum__card-kicker {
  grid-column: 1;
  font-size: 12px;
  font-weight: 600;
  color: rgba(0, 0, 0, 0.45);
  letter-spacing: 0.02em;
}

.player-museum__card-title {
  grid-column: 1;
  font-size: 16px;
  font-weight: 600;
}

.player-museum__card-meta {
  grid-column: 1 / -1;
  font-size: 13px;
  color: rgba(0, 0, 0, 0.48);
  line-height: 1.4;
}

.player-museum__card-arrow {
  grid-column: 2;
  grid-row: 1 / 3;
  align-self: center;
  opacity: 0.35;
  font-size: 18px;
}

.player-museum__card:disabled .player-museum__card-arrow {
  visibility: hidden;
}

/* Timeline */
.player-museum__timeline {
  list-style: none;
  margin: 0;
  padding: 0 0 0 4px;
  border-left: 2px solid rgba(0, 0, 0, 0.08);
}

.player-museum__timeline-item {
  position: relative;
  padding-left: 18px;
  margin-bottom: 14px;
}

.player-museum__timeline-item::before {
  content: '';
  position: absolute;
  left: -7px;
  top: 6px;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: linear-gradient(135deg, #6366f1, #a855f7);
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.2);
}

.player-museum__timeline-card {
  width: 100%;
  text-align: left;
  padding: 12px 14px;
  border-radius: 14px;
  border: 1px solid rgba(0, 0, 0, 0.06);
  background: rgba(255, 255, 255, 0.9);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  cursor: pointer;
  font: inherit;
  transition: transform 0.3s var(--neo-ease-out), box-shadow 0.3s var(--neo-ease-out);
}

.player-museum__timeline-card:active {
  transform: scale(0.995);
}

.player-museum__timeline-date {
  display: block;
  font-size: 12px;
  font-weight: 600;
  color: rgba(0, 0, 0, 0.42);
  margin-bottom: 6px;
}

.player-museum__timeline-event {
  display: block;
  font-size: 15px;
  font-weight: 600;
  color: var(--museum-text);
  margin-bottom: 4px;
}

.player-museum__timeline-detail {
  display: block;
  font-size: 13px;
  color: rgba(0, 0, 0, 0.5);
  line-height: 1.45;
}

/* Highlights swipe */
.player-museum__swipe-wrap {
  border-radius: 18px;
  overflow: hidden;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.player-museum__swipe {
  border-radius: 18px;
  background: #1a1a1f;
}

.player-museum__swipe-item {
  height: 100%;
}

.player-museum__highlight {
  min-height: 240px;
  display: flex;
  flex-direction: column;
  position: relative;
  color: #fff;
}

.player-museum__highlight-visual {
  position: relative;
  height: 160px;
  flex-shrink: 0;
}

.player-museum__highlight-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.player-museum__highlight-scrim {
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, transparent 20%, rgba(10, 10, 14, 0.92) 100%);
}

.player-museum__highlight-text {
  padding: 16px 18px 22px;
  flex: 1;
  background: linear-gradient(180deg, #222228 0%, #16161a 100%);
}

.player-museum__highlight-title {
  margin: 0 0 8px;
  font-size: 17px;
  font-weight: 600;
  line-height: 1.35;
}

.player-museum__highlight-summary {
  margin: 0;
  font-size: 14px;
  line-height: 1.5;
  color: rgba(255, 255, 255, 0.72);
  display: -webkit-box;
  -webkit-line-clamp: 4;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* Business */
.player-museum__biz-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.player-museum__biz-card {
  width: 100%;
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 14px 16px;
  border-radius: 16px;
  border: 1px solid rgba(0, 0, 0, 0.06);
  background: rgba(255, 255, 255, 0.88);
  box-shadow: 0 2px 14px rgba(0, 0, 0, 0.05);
  cursor: pointer;
  font: inherit;
  text-align: left;
  transition: transform 0.3s var(--neo-ease-out), box-shadow 0.3s var(--neo-ease-out);
}

.player-museum__biz-card:active {
  transform: scale(0.995);
}

.player-museum__biz-cover {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  object-fit: cover;
  flex-shrink: 0;
  background: rgba(0, 0, 0, 0.06);
}

.player-museum__biz-text {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.player-museum__biz-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--museum-text);
}

.player-museum__biz-meta {
  font-size: 13px;
  color: rgba(0, 0, 0, 0.48);
  line-height: 1.4;
}

@media (prefers-reduced-motion: reduce) {
  .player-museum__hero-mesh {
    animation: none;
  }

  .player-museum__hero-glow {
    animation: none;
  }

  .hero-reveal,
  .section-reveal,
  .player-museum__card-wrap {
    opacity: 1 !important;
    transform: none !important;
    filter: none !important;
    transition: none !important;
  }
}
</style>

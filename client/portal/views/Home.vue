<template>
  <div class="home-neo">
    <section class="hero" aria-label="首页">
      <div class="hero__mesh" aria-hidden="true" />
      <div class="hero__glow hero__glow--a" aria-hidden="true" />
      <div class="hero__glow hero__glow--b" aria-hidden="true" />
      <div class="hero__inner">
        <p class="hero__eyebrow hero-reveal" :class="{ 'is-visible': heroReady }">ttf</p>
        <h1 class="hero__title hero-reveal" :class="{ 'is-visible': heroReady }" :style="{ transitionDelay: '80ms' }">
          探索 · 记录 · 每一拍精彩
        </h1>
        <p class="hero__sub hero-reveal" :class="{ 'is-visible': heroReady }" :style="{ transitionDelay: '160ms' }">
          推荐文章与最新动态，以流畅动效呈现。
        </p>
      </div>
    </section>

    <section class="content" aria-label="首页内容">
      <div class="content__inner">
        <p v-if="loading" class="content__loading section-reveal" :class="{ 'is-visible': sectionVisible }">加载中…</p>

        <template v-else>
          <div v-for="b in enabledBlocks" :key="b.block_key">
            <h2
              v-if="b.block_key === 'featured_articles'"
              class="content__title section-reveal"
              :class="{ 'is-visible': sectionVisible }"
            >
              推荐文章
            </h2>
            <h2
              v-else-if="b.block_key === 'highlight_athletes'"
              class="content__title section-reveal"
              :class="{ 'is-visible': sectionVisible }"
            >
              高光运动员
            </h2>

            <ul v-if="b.block_key === 'featured_articles' && featuredArticles.length" class="article-list">
              <li
                v-for="(a, i) in featuredArticles"
                :key="a.id"
                class="article-card card-reveal"
                :class="{ 'is-visible': cardsVisible }"
                :style="{ transitionDelay: `${120 + i * 70}ms` }"
              >
                <router-link class="article-card__link" :to="'/news/' + (a.slug || a.id)">
                  <span class="article-card__shine" aria-hidden="true" />
                  <span class="article-card__title">{{ a.title }}</span>
                  <span class="article-card__arrow" aria-hidden="true">→</span>
                </router-link>
              </li>
            </ul>

            <p v-else-if="b.block_key === 'featured_articles'" class="content__empty section-reveal" :class="{ 'is-visible': sectionVisible }">
              暂无推荐文章
            </p>

            <ul v-if="b.block_key === 'highlight_athletes' && highlightAthletes.length" class="athlete-list">
              <li
                v-for="(p, i) in highlightAthletes"
                :key="p.athlete_id"
                class="athlete-card card-reveal"
                :class="{ 'is-visible': cardsVisible }"
                :style="{ transitionDelay: `${120 + i * 70}ms` }"
              >
                <router-link class="athlete-card__link" :to="'/players/' + p.athlete_id">
                  <img v-if="p.cover_url" :src="p.cover_url" class="athlete-card__cover" alt="" />
                  <div class="athlete-card__text">
                    <div class="athlete-card__name">{{ p.athlete_name }}</div>
                    <div class="athlete-card__title">{{ p.title }}</div>
                    <p v-if="p.summary" class="athlete-card__summary">{{ p.summary }}</p>
                  </div>
                  <span class="athlete-card__arrow" aria-hidden="true">→</span>
                </router-link>
              </li>
            </ul>

            <p v-else-if="b.block_key === 'highlight_athletes'" class="content__empty section-reveal" :class="{ 'is-visible': sectionVisible }">
              暂无高光运动员
            </p>
          </div>
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
      highlightAthletes: [],
      heroReady: false,
      sectionVisible: false,
      cardsVisible: false
    };
  },
  computed: {
    enabledBlocks() {
      return (this.blocks || []).filter((b) => b && (b.enabled === true || b.enabled === 1 || String(b.enabled) === '1'));
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
      this.highlightAthletes = (data && data.highlight_athletes) || [];
    } catch (e) {
      this.$message.error('加载失败');
    } finally {
      this.loading = false;
      this.$nextTick(() => this.observeSection());
    }
  },
  methods: {
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
.home-neo {
  --neo-ease: cubic-bezier(0.25, 0.1, 0.25, 1);
  --neo-ease-out: cubic-bezier(0.16, 1, 0.3, 1);
  overflow-x: hidden;
}

/* Hero：Mac 风格渐变网格 + 呼吸光晕 */
.hero {
  position: relative;
  min-height: min(72vh, 640px);
  display: flex;
  align-items: flex-end;
  padding: clamp(48px, 12vw, 120px) clamp(24px, 6vw, 48px) clamp(56px, 10vw, 96px);
  isolation: isolate;
}

.hero__mesh {
  position: absolute;
  inset: 0;
  z-index: 0;
  background:
    radial-gradient(ellipse 80% 50% at 50% -20%, rgba(120, 150, 255, 0.35), transparent 55%),
    radial-gradient(ellipse 60% 40% at 100% 50%, rgba(255, 120, 180, 0.12), transparent 50%),
    radial-gradient(ellipse 50% 35% at 0% 80%, rgba(100, 210, 255, 0.18), transparent 45%),
    linear-gradient(180deg, #f5f5f7 0%, #e8e8ed 100%);
  background-size: 100% 100%, 100% 100%, 100% 100%, 100% 100%;
  animation: mesh-shift 18s var(--neo-ease) infinite alternate;
}

@keyframes mesh-shift {
  0% {
    filter: hue-rotate(0deg) saturate(1);
    transform: scale(1);
  }
  100% {
    filter: hue-rotate(12deg) saturate(1.08);
    transform: scale(1.02);
  }
}

.hero__glow {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.55;
  z-index: 1;
  pointer-events: none;
  animation: float-glow 14s var(--neo-ease) infinite alternate;
}

.hero__glow--a {
  width: min(60vw, 420px);
  height: min(60vw, 420px);
  top: 10%;
  right: -10%;
  background: radial-gradient(circle, rgba(99, 102, 241, 0.45), transparent 70%);
}

.hero__glow--b {
  width: min(50vw, 360px);
  height: min(50vw, 360px);
  bottom: 20%;
  left: -5%;
  background: radial-gradient(circle, rgba(236, 72, 153, 0.25), transparent 70%);
  animation-delay: -3s;
}

@keyframes float-glow {
  0% {
    transform: translate(0, 0) scale(1);
  }
  100% {
    transform: translate(-18px, 12px) scale(1.08);
  }
}

.hero__inner {
  position: relative;
  z-index: 2;
  max-width: 960px;
  margin: 0 auto;
  width: 100%;
}

.hero__eyebrow {
  font-size: 13px;
  font-weight: 600;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: rgba(0, 0, 0, 0.45);
  margin: 0 0 12px;
}

.hero__title {
  font-size: clamp(32px, 7vw, 56px);
  font-weight: 600;
  letter-spacing: -0.03em;
  line-height: 1.08;
  margin: 0 0 16px;
  color: #1d1d1f;
}

.hero__sub {
  font-size: clamp(16px, 2.4vw, 21px);
  line-height: 1.45;
  color: rgba(0, 0, 0, 0.55);
  max-width: 36em;
  margin: 0;
}

/* 错入 + 淡入（类似产品页 reveal） */
.hero-reveal {
  opacity: 0;
  transform: translateY(28px) scale(0.98);
  filter: blur(8px);
  transition:
    opacity 1s var(--neo-ease-out),
    transform 1s var(--neo-ease-out),
    filter 1s var(--neo-ease-out);
}

.hero-reveal.is-visible {
  opacity: 1;
  transform: translateY(0) scale(1);
  filter: blur(0);
}

.content {
  padding: 48px 24px 80px;
  background: linear-gradient(180deg, #fff 0%, #fafafa 100%);
}

.content__inner {
  max-width: 960px;
  margin: 0 auto;
}

.content__title {
  font-size: 28px;
  font-weight: 600;
  letter-spacing: -0.02em;
  margin: 0 0 24px;
  color: #1d1d1f;
}

.content__loading,
.content__empty {
  color: rgba(0, 0, 0, 0.45);
  margin: 0;
}

.section-reveal {
  opacity: 0;
  transform: translateY(20px);
  transition: opacity 0.85s var(--neo-ease-out), transform 0.85s var(--neo-ease-out);
}

.section-reveal.is-visible {
  opacity: 1;
  transform: translateY(0);
}

.article-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.article-card {
  opacity: 0;
  transform: translateY(18px) scale(0.98);
  transition:
    opacity 0.7s var(--neo-ease-out),
    transform 0.7s var(--neo-ease-out),
    box-shadow 0.35s var(--neo-ease);
}

.article-card.is-visible {
  opacity: 1;
  transform: translateY(0) scale(1);
}

.article-card__link {
  position: relative;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 18px 20px;
  border-radius: 16px;
  text-decoration: none;
  color: #1d1d1f;
  background: rgba(255, 255, 255, 0.72);
  border: 1px solid rgba(0, 0, 0, 0.06);
  box-shadow: 0 2px 16px rgba(0, 0, 0, 0.04);
  overflow: hidden;
  backdrop-filter: saturate(180%) blur(20px);
  -webkit-backdrop-filter: saturate(180%) blur(20px);
  transition: transform 0.45s var(--neo-ease-out), box-shadow 0.45s var(--neo-ease-out), border-color 0.35s ease;
}

.article-card__link:hover {
  transform: translateY(-3px) scale(1.01);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.1);
  border-color: rgba(0, 0, 0, 0.08);
}

.article-card__link:active {
  transform: translateY(-1px) scale(1.005);
}

.article-card__shine {
  position: absolute;
  inset: 0;
  background: linear-gradient(105deg, transparent 40%, rgba(255, 255, 255, 0.5) 50%, transparent 60%);
  transform: translateX(-100%);
  transition: transform 0.6s var(--neo-ease);
}

.article-card__link:hover .article-card__shine {
  transform: translateX(100%);
}

.article-card__title {
  flex: 1;
  font-size: 17px;
  font-weight: 500;
  letter-spacing: -0.01em;
}

.article-card__arrow {
  flex-shrink: 0;
  opacity: 0.35;
  transition: transform 0.35s var(--neo-ease-out), opacity 0.35s ease;
}

.article-card__link:hover .article-card__arrow {
  transform: translateX(4px);
  opacity: 0.9;
}

.athlete-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.athlete-card {
  opacity: 0;
  transform: translateY(18px) scale(0.98);
  transition:
    opacity 0.7s var(--neo-ease-out),
    transform 0.7s var(--neo-ease-out),
    box-shadow 0.35s var(--neo-ease);
}

.athlete-card.is-visible {
  opacity: 1;
  transform: translateY(0) scale(1);
}

.athlete-card__link {
  position: relative;
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 18px 20px;
  border-radius: 16px;
  text-decoration: none;
  color: #1d1d1f;
  background: rgba(255, 255, 255, 0.72);
  border: 1px solid rgba(0, 0, 0, 0.06);
  box-shadow: 0 2px 16px rgba(0, 0, 0, 0.04);
  overflow: hidden;
  backdrop-filter: saturate(180%) blur(20px);
  -webkit-backdrop-filter: saturate(180%) blur(20px);
  transition: transform 0.45s var(--neo-ease-out), box-shadow 0.45s var(--neo-ease-out), border-color 0.35s ease;
}

.athlete-card__link:hover {
  transform: translateY(-3px) scale(1.01);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.1);
  border-color: rgba(0, 0, 0, 0.08);
}

.athlete-card__link:active {
  transform: translateY(-1px) scale(1.005);
}

.athlete-card__cover {
  width: 56px;
  height: 56px;
  border-radius: 14px;
  object-fit: cover;
  flex-shrink: 0;
  background: rgba(0, 0, 0, 0.05);
}

.athlete-card__text {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.athlete-card__name {
  font-size: 16px;
  font-weight: 600;
  letter-spacing: -0.01em;
}

.athlete-card__title {
  font-size: 14px;
  font-weight: 500;
  color: rgba(0, 0, 0, 0.6);
}

.athlete-card__summary {
  margin: 0;
  font-size: 13px;
  color: rgba(0, 0, 0, 0.45);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.athlete-card__arrow {
  flex-shrink: 0;
  opacity: 0.35;
  transition: transform 0.35s var(--neo-ease-out), opacity 0.35s ease;
  padding-top: 6px;
}

.athlete-card__link:hover .athlete-card__arrow {
  transform: translateX(4px);
  opacity: 0.9;
}

@media (prefers-reduced-motion: reduce) {
  .hero__mesh {
    animation: none;
  }

  .hero__glow {
    animation: none;
  }

  .hero-reveal,
  .section-reveal,
  .article-card,
  .athlete-card {
    opacity: 1 !important;
    transform: none !important;
    filter: none !important;
    transition: none !important;
  }

  .article-card__shine {
    display: none;
  }

  .article-card__link {
    transition: none;
  }

  .article-card__link:hover {
    transform: none;
  }
}
</style>

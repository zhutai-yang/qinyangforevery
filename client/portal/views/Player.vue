<template>
  <div class="player-museum">
    <van-loading
      v-if="loading"
      class="player-museum__loading"
      vertical
    >
      加载中…
    </van-loading>

    <van-empty
      v-else-if="!hasAthlete"
      class="player-museum__empty"
      description="未找到该球员或加载失败"
    />

    <template v-else>
      <section
        id="top"
        class="player-museum__hero"
        :class="{ 'player-museum__hero--has-cover': heroCover }"
      >
        <div
          class="player-museum__hero-bg"
          aria-hidden="true"
        >
          <div
            v-if="heroCover"
            class="player-museum__hero-cover"
            :style="heroCoverStyle"
          />
          <div class="player-museum__hero-video-noise" />
          <div class="player-museum__hero-scanline" />
          <div class="player-museum__hero-mesh" />
          <div class="player-museum__hero-glow player-museum__hero-glow--a" />
          <div class="player-museum__hero-glow player-museum__hero-glow--b" />
        </div>
        <header
          class="player-museum__cover-mast hero-reveal"
          :class="{ 'is-visible': heroReady }"
        >
          <span>TT 球员志</span>
          <strong>球员专题</strong>
          <em>{{ currentIssue }}</em>
        </header>
        <div
          class="player-museum__hero-inner"
          :class="{ 'player-museum__hero-inner--no-cover': !heroCover }"
        >
          <div class="player-museum__hero-copy">
            <p
              class="player-museum__eyebrow hero-reveal"
              :class="{ 'is-visible': heroReady }"
            >
              球员主页
            </p>
            <h1
              class="player-museum__title hero-reveal"
              :class="{ 'is-visible': heroReady }"
              :style="{ transitionDelay: '70ms' }"
            >
              {{ athlete.name || '球员' }}
            </h1>
            <p
              v-if="heroIdentityMeta"
              class="player-museum__identity hero-reveal"
              :class="{ 'is-visible': heroReady }"
              :style="{ transitionDelay: '110ms' }"
            >
              {{ heroIdentityMeta }}
            </p>
            <p
              v-if="heroTagline"
              class="player-museum__tagline hero-reveal"
              :class="{ 'is-visible': heroReady }"
              :style="{ transitionDelay: '140ms' }"
            >
              {{ heroTagline }}
            </p>
            <div
              class="player-museum__hero-actions hero-reveal"
              :class="{ 'is-visible': heroReady }"
              :style="{ transitionDelay: '210ms' }"
            >
              <button
                v-if="latestHighlight"
                type="button"
                class="player-museum__primary-action"
                @click="scrollToSection('highlights')"
              >
                <span>焦点时刻</span>
                <strong>{{ latestHighlight.title }}</strong>
              </button>
              <a
                v-if="athlete.social_url"
                class="player-museum__ghost-action"
                :href="athlete.social_url"
                target="_blank"
                rel="noopener noreferrer"
              >
                外部主页
              </a>
            </div>
          </div>
          <div
            v-if="heroCover"
            class="player-museum__cover-figure hero-reveal"
            :class="{ 'is-visible': heroReady }"
            :style="{ transitionDelay: '180ms' }"
            aria-hidden="true"
          >
            <div class="player-museum__cover-card">
              <img
                v-if="heroCover"
                :src="heroCover"
                alt=""
              >
              <div
                v-else
                class="player-museum__cover-initials"
              >
                {{ athleteInitials }}
              </div>
              <span class="player-museum__cover-rank">{{ coverRank }}</span>
              <i />
            </div>
          </div>
          <div
            class="player-museum__hero-panel hero-reveal"
            :class="{ 'is-visible': heroReady }"
            :style="{ transitionDelay: '280ms' }"
          >
            <span class="player-museum__panel-label">赛场动态</span>
            <strong>{{ heroPanelTitle }}</strong>
            <p>{{ heroPanelMeta }}</p>
          </div>
        </div>
      </section>

      <nav
        v-if="navSections.length"
        class="player-museum__quicknav"
        aria-label="球员主页导航"
      >
        <button
          v-for="item in navSections"
          :key="item.key"
          type="button"
          @click="scrollToSection(item.key)"
        >
          {{ item.label }}
        </button>
      </nav>

      <div class="player-museum__body">
        <section
          v-if="latestHighlight"
          id="spotlight"
          data-museum-section="spotlight"
          class="player-museum__section player-museum__spotlight section-reveal"
          :class="{ 'is-visible': sectionVisible.spotlight }"
        >
          <div class="player-museum__spotlight-media">
            <img
              v-if="latestHighlight.cover_url"
              :src="latestHighlight.cover_url"
              alt=""
              loading="lazy"
            >
            <div
              v-else
              class="player-museum__spotlight-fallback"
            >
              {{ athlete.name || 'TT' }}
            </div>
            <div
              class="player-museum__play-mark"
              aria-hidden="true"
            >
              ▶
            </div>
          </div>
          <div class="player-museum__spotlight-copy">
            <span class="player-museum__section-kicker">焦点时刻</span>
            <h2>{{ latestHighlight.title }}</h2>
            <p>{{ latestHighlight.summary || '代表性片段已经收录，适合快速进入这位选手近期状态。' }}</p>
            <button
              type="button"
              class="player-museum__text-action"
              @click="scrollToSection('highlights')"
            >
              查看精彩片段
            </button>
          </div>
        </section>

        <section
          v-if="profileItems.length"
          id="profile"
          data-museum-section="profile"
          class="player-museum__section section-reveal"
          :class="{ 'is-visible': sectionVisible.profile }"
        >
          <div class="player-museum__section-head">
            <span class="player-museum__section-kicker">球员信息</span>
            <h2 class="player-museum__section-title">
              球员概览
            </h2>
          </div>
          <div class="player-museum__profile-overview">
            <div class="player-museum__profile-main">
              <p class="player-museum__profile-name">
                {{ athlete.name || '球员' }}
              </p>
              <p class="player-museum__profile-note">
                {{ scoutingNote }}
              </p>
            </div>
            <div
              v-if="profileMeta"
              class="player-museum__profile-meta"
            >
              {{ profileMeta }}
            </div>
          </div>
        </section>

        <section
          v-if="careerHighlights.length"
          id="career"
          data-museum-section="achievements"
          class="player-museum__section player-museum__career section-reveal"
          :class="{ 'is-visible': sectionVisible.achievements }"
        >
          <div class="player-museum__career-swiper">
            <div class="player-museum__career-track">
              <article
                v-for="item in careerHighlights"
                :key="item.key"
                class="player-museum__career-card"
              >
                <span class="player-museum__career-kind">{{ item.badge }}</span>
                <span class="player-museum__career-year">{{ item.date }}</span>
                <h2>{{ item.title }}</h2>
                <p>{{ item.detail }}</p>
              </article>
            </div>
            <div class="player-museum__career-indicator">
              <span>01</span>
              <i />
              <span>{{ String(careerHighlights.length).padStart(2, '0') }}</span>
            </div>
          </div>
        </section>

        <section
          v-if="highlights.length"
          id="highlights"
          data-museum-section="highlights"
          class="player-museum__section section-reveal"
          :class="{ 'is-visible': sectionVisible.highlights }"
        >
          <div class="player-museum__section-head">
            <span class="player-museum__section-kicker">精彩片段</span>
            <h2 class="player-museum__section-title">
              精彩片段
            </h2>
          </div>
          <div class="player-museum__highlight-rail">
            <article
              v-for="(h, index) in highlights"
              :key="h.id"
              class="player-museum__film-card"
              :style="{ transitionDelay: index * 70 + 'ms' }"
            >
              <div class="player-museum__film-visual">
                <img
                  v-if="h.cover_url"
                  :src="h.cover_url"
                  alt=""
                  loading="lazy"
                >
                <div
                  v-else
                  class="player-museum__film-fallback"
                >
                  {{ index + 1 }}
                </div>
              </div>
              <div class="player-museum__film-copy">
                <span>{{ h.published_at || '片段' }}</span>
                <h3>{{ h.title }}</h3>
                <p v-if="h.summary">
                  {{ h.summary }}
                </p>
              </div>
            </article>
          </div>
        </section>

        <section
          id="matches"
          data-museum-section="upcoming"
          class="player-museum__section section-reveal"
          :class="{ 'is-visible': sectionVisible.upcoming }"
        >
          <div class="player-museum__section-head">
            <span class="player-museum__section-kicker">近期赛程</span>
            <h2 class="player-museum__section-title">
              赛事雷达
            </h2>
          </div>
          <ul
            v-if="upcoming.length"
            class="player-museum__cards"
          >
            <li
              v-for="m in upcoming"
              :key="'upcoming-' + (m.source_type || 'managed') + '-' + m.id"
              class="player-museum__card-wrap card-reveal"
              :class="{ 'is-visible': cardsReady }"
            >
              <button
                type="button"
                class="player-museum__card"
                @click="goEvent(m)"
              >
                <span class="player-museum__card-kicker">{{ m.scheduled_at || '时间待定' }}</span>
                <span class="player-museum__card-title">{{ m.event_name || '赛事' }}</span>
                <span class="player-museum__card-meta">{{ previewMatchLabel(m) }}</span>
                <span
                  v-if="m.event_id"
                  class="player-museum__card-arrow"
                  aria-hidden="true"
                >→</span>
              </button>
            </li>
          </ul>
          <p
            v-else
            class="player-museum__muted"
          >
            暂无赛事预告
          </p>
        </section>

        <section
          id="business"
          data-museum-section="business"
          class="player-museum__section player-museum__business section-reveal"
          :class="{ 'is-visible': sectionVisible.business }"
        >
          <div class="player-museum__section-head">
            <span class="player-museum__section-kicker">活动预告</span>
            <h2 class="player-museum__section-title">
              商务预告
            </h2>
          </div>
          <ul
            v-if="business_previews.length"
            class="player-museum__biz-list"
          >
            <li
              v-for="b in business_previews"
              :key="b.id"
            >
              <button
                type="button"
                class="player-museum__biz-card"
                @click="openBusiness(b)"
              >
                <img
                  v-if="b.cover_url"
                  :src="b.cover_url"
                  class="player-museum__biz-cover"
                  alt=""
                  loading="lazy"
                >
                <div class="player-museum__biz-text">
                  <span class="player-museum__biz-title">{{ b.title }}</span>
                  <span class="player-museum__biz-meta">{{ businessPreviewLabel(b) }}</span>
                </div>
                <span
                  v-if="b.link_url"
                  class="player-museum__card-arrow"
                  aria-hidden="true"
                >↗</span>
              </button>
            </li>
          </ul>
          <p
            v-else
            class="player-museum__muted"
          >
            暂无商务预告
          </p>
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
      achievements: [],
      highlights: [],
      business_previews: [],
      heroReady: false,
      sectionVisible: {
        spotlight: false,
        profile: false,
        achievements: false,
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
      if (this.athlete && this.athlete.hero_image_url) return this.athlete.hero_image_url;
      const h = (this.highlights || []).find((x) => x && x.cover_url);
      return h ? h.cover_url : '';
    },
    heroCoverStyle() {
      if (!this.heroCover) return {};
      return { backgroundImage: `url(${this.heroCover})` };
    },
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
    heroIdentityMeta() {
      const a = this.athlete || {};
      const pieces = [];
      if (a.birth_date) pieces.push('生日 ' + a.birth_date);
      const registry = this.cleanPortalText(a.birth_place || a.nationality || a.association);
      if (registry) pieces.push('籍贯 ' + registry);
      return pieces.join(' · ');
    },
    latestHighlight() {
      return (this.highlights && this.highlights[0]) || null;
    },
    athleteInitials() {
      const name = String((this.athlete && (this.athlete.name_en || this.athlete.name)) || 'TT').trim();
      if (!name) return 'TT';
      const parts = name.split(/\s+/).filter(Boolean);
      if (parts.length > 1) return parts.map((p) => p[0]).join('').slice(0, 2).toUpperCase();
      return name.slice(0, 2).toUpperCase();
    },
    coverRank() {
      if (this.athlete.current_world_rank) return 'WORLD #' + this.athlete.current_world_rank;
      if (this.athlete.highest_world_rank) return 'BEST #' + this.athlete.highest_world_rank;
      return 'FEATURE';
    },
    currentIssue() {
      const now = new Date();
      const y = now.getFullYear();
      const m = String(now.getMonth() + 1).padStart(2, '0');
      return y + '.' + m;
    },
    heroPanelTitle() {
      if (this.upcoming && this.upcoming.length) return '下一站已锁定';
      if (this.latestHighlight) return '焦点时刻已收录';
      if (this.achievements && this.achievements.length) return '荣誉履历在线';
      return '球员档案已开启';
    },
    heroPanelMeta() {
      const next = this.upcoming && this.upcoming[0];
      if (next) return [next.scheduled_at, next.event_name, this.previewMatchLabel(next)].filter(Boolean).join(' · ');
      if (this.latestHighlight) return this.latestHighlight.summary || this.latestHighlight.title;
      const achievement = this.achievements && this.achievements[0];
      if (achievement) return [achievement.year, achievement.event_name, this.achievementLabel(achievement)].filter(Boolean).join(' · ');
      return this.athlete.association || '资料、精彩片段、赛事履历集中展示';
    },
    navSections() {
      const items = [];
      if (this.latestHighlight) items.push({ key: 'spotlight', label: '焦点' });
      if (this.highlights && this.highlights.length) items.push({ key: 'highlights', label: '片段' });
      if (this.careerHighlights.length) items.push({ key: 'career', label: '生涯' });
      if (this.upcoming && this.upcoming.length) items.push({ key: 'matches', label: '赛程' });
      if (this.profileItems.length) items.push({ key: 'profile', label: '档案' });
      if (this.business_previews && this.business_previews.length) items.push({ key: 'business', label: '商务' });
      return items;
    },
    careerHighlights() {
      const achievements = (this.achievements || []).slice(0, 3).map((a) => ({
        key: 'achievement-' + a.id,
        badge: '荣誉',
        date: a.year || '—',
        title: a.event_name || '赛事荣誉',
        detail: this.achievementLabel(a)
      }));
      if (achievements.length >= 4) return achievements.slice(0, 4);
      const matches = (this.history || []).slice(0, 4 - achievements.length).map((m) => ({
        key: 'match-' + (m.source_type || 'managed') + '-' + m.id,
        badge: '代表战',
        date: m.scheduled_at || '—',
        title: m.event_name || '赛事记录',
        detail: this.historyMatchLabel(m)
      }));
      return achievements.concat(matches).slice(0, 4);
    },
    scoutingNote() {
      const a = this.athlete || {};
      if (a.profile_summary) return a.profile_summary;
      const pieces = [];
      if (a.playing_style) pieces.push(a.playing_style);
      if (a.major_identity) pieces.push(a.major_identity);
      if (a.profile_title) pieces.push(a.profile_title);
      if (pieces.length) return pieces.join(' · ');
      return '围绕焦点时刻、近期赛程和生涯节点，快速了解这位选手的当前状态与代表性履历。';
    },
    profileItems() {
      const a = this.athlete || {};
      const rows = [];
      if (a.association) rows.push({ label: '协会', value: a.association });
      if (a.nationality) rows.push({ label: '国籍', value: a.nationality });
      if (a.height_cm) rows.push({ label: '身高', value: a.height_cm + 'cm' });
      if (a.birth_date) rows.push({ label: '生日', value: a.birth_date });
      if (a.playing_style) rows.push({ label: '打法', value: a.playing_style });
      return rows.slice(0, 5);
    },
    profileMeta() {
      const a = this.athlete || {};
      const pieces = [];
      const association = this.cleanPortalText(a.association);
      const nationality = this.cleanPortalText(a.nationality);
      if (association) pieces.push(association);
      if (nationality && nationality !== association) pieces.push(nationality);
      if (a.height_cm) pieces.push(a.height_cm + 'cm');
      if (a.playing_style) pieces.push(this.cleanPortalText(a.playing_style));
      return pieces.join(' · ');
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
      if (m.level_label) pieces.push(m.level_label);
      if (m.round_label) pieces.push(m.round_label);
      const players = this.playersLabel(m);
      if (players) pieces.push(players);
      if (m.location) pieces.push(m.location);
      if (m.note) pieces.push(m.note);
      return pieces.join(' · ') || '—';
    },
    historyMatchLabel(m) {
      const pieces = [];
      if (m.round_label) pieces.push(m.round_label);
      if (m.category) pieces.push(m.category);
      const players = this.playersLabel(m);
      if (players) pieces.push(players);
      if (m.result_label) pieces.push(m.result_label);
      if (m.opponent) pieces.push('对手 ' + m.opponent);
      if (m.score) pieces.push('比分 ' + m.score);
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
    achievementLabel(a) {
      const pieces = [];
      if (a.category) pieces.push(a.category);
      if (a.result_label) pieces.push(a.result_label);
      if (a.partner_or_team) pieces.push(a.partner_or_team);
      if (a.opponent) pieces.push('对手 ' + a.opponent);
      if (a.score) pieces.push(a.score);
      return pieces.join(' · ') || '—';
    },
    cleanPortalText(value) {
      return String(value || '')
        .replace(/\s*\/\s*[A-Za-z][A-Za-z\s.-]*/g, '')
        .replace(/\s*\([A-Za-z][^)]+\)/g, '')
        .trim();
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
    scrollToSection(id) {
      this.$nextTick(() => {
        const el = this.$el.querySelector('#' + id);
        if (!el) return;
        el.scrollIntoView({ behavior: 'smooth', block: 'start' });
      });
    },
    useDemoProfile() {
      this.athlete = {
        id: this.id,
        name: '林高远',
        name_en: 'Lin Gaoyuan',
        gender: '男',
        association: '中国乒乓球队',
        nationality: '中国',
        birth_date: '1995-03-19',
        height_cm: 175,
        playing_style: '左手横板，弧圈结合快攻',
        current_world_rank: 8,
        highest_world_rank: 2,
        major_identity: '世界冠军 / 国际赛场核心选手',
        profile_title: '速度、落点与连续进攻的高压样本',
        profile_summary: '点击进入后先看到焦点时刻，再沿年份和赛事线索回看代表比赛、荣誉节点和内容故事。'
      };
      this.highlights = [
        {
          id: 'demo-h-1',
          title: '决胜局连续压迫，反手快撕打开胜负手',
          summary: '前三板抢先上手，中远台衔接保持高速，关键分把线路变化拉满。',
          published_at: '2026-06-18'
        },
        {
          id: 'demo-h-2',
          title: '冠军点名场面：发抢节奏完全接管',
          summary: '从发球旋转到第二板落点，形成一套极具识别度的连续得分模板。',
          published_at: '2026-05-29'
        },
        {
          id: 'demo-h-3',
          title: '逆风局追分，连续相持撕开防线',
          summary: '多拍对抗中保持低失误，反手位转正手位的连接极快。',
          published_at: '2026-04-12'
        }
      ];
      this.upcoming = [
        {
          id: 'demo-u-1',
          event_id: 1,
          scheduled_at: '2026-06-22 19:30',
          event_name: 'TT 明星挑战赛',
          round_label: '半决赛',
          level_label: '焦点战',
          location: '上海',
          participants: [{ athlete_name: '林高远', side_order: 1 }, { athlete_name: '王楚钦', side_order: 2 }]
        }
      ];
      this.achievements = [
        { id: 'demo-a-1', year: '2025', event_name: 'WTT 常规挑战赛', category: '男单', result_label: '冠军' },
        { id: 'demo-a-2', year: '2024', event_name: '国际团体赛', category: '男团', result_label: '冠军' },
        { id: 'demo-a-3', year: '2023', event_name: '亚洲锦标赛', category: '男双', result_label: '冠军' }
      ];
      this.history = [
        {
          id: 'demo-m-1',
          event_id: 1,
          scheduled_at: '2026-05-29',
          event_name: 'TT 巡回赛澳门站',
          round_label: '决赛',
          result_label: '胜',
          score: '4:2',
          participants: [{ athlete_name: '林高远', side_order: 1 }, { athlete_name: '张本智和', side_order: 2 }]
        },
        {
          id: 'demo-m-2',
          event_id: 2,
          scheduled_at: '2026-04-12',
          event_name: 'TT 超级联赛',
          round_label: '四分之一决赛',
          result_label: '胜',
          score: '3:1',
          participants: [{ athlete_name: '林高远', side_order: 1 }, { athlete_name: '林昀儒', side_order: 2 }]
        }
      ];
      this.business_previews = [
        {
          id: 'demo-b-1',
          title: '城市见面会与球迷互动开放预约',
          summary: '现场训练展示、签名合影和装备展示同步开启。',
          scheduled_at: '2026-06-30 14:00',
          link_url: ''
        }
      ];
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
            achievements: true,
            upcoming: true,
            history: true,
            highlights: true,
            business: true,
            spotlight: true
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
        spotlight: false,
        profile: false,
        achievements: false,
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
        this.achievements = (data && data.achievements) || [];
        this.highlights = (data && data.highlights) || [];
        this.business_previews = (data && data.business_previews) || [];
      } catch (e) {
        if (import.meta.env.DEV) {
          this.useDemoProfile();
        } else {
          this.$message.error('加载失败');
          this.athlete = {};
          this.upcoming = [];
          this.history = [];
          this.achievements = [];
          this.highlights = [];
          this.business_previews = [];
        }
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

/* Premium athlete site treatment */
.player-museum {
  --neo-ease: cubic-bezier(0.25, 0.1, 0.25, 1);
  --neo-ease-out: cubic-bezier(0.16, 1, 0.3, 1);
  --museum-text: #f8fafc;
  --museum-muted: rgba(226, 232, 240, 0.66);
  --museum-panel: rgba(15, 23, 42, 0.72);
  --museum-line: rgba(255, 255, 255, 0.14);
  min-height: 100vh;
  background:
    radial-gradient(circle at 20% 0%, rgba(239, 68, 68, 0.2), transparent 24rem),
    radial-gradient(circle at 90% 12%, rgba(34, 211, 238, 0.16), transparent 22rem),
    linear-gradient(180deg, #020617 0%, #09090f 46%, #111827 100%);
  color: var(--museum-text);
}

.player-museum__hero {
  min-height: calc(100vh - 46px);
  min-height: calc(100dvh - 46px);
  align-items: center;
  padding: 56px 18px 112px;
  overflow: hidden;
}

.player-museum__hero::after {
  content: '';
  position: absolute;
  left: 50%;
  bottom: 32px;
  width: min(76vw, 640px);
  height: 1px;
  transform: translateX(-50%);
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.72), transparent);
  box-shadow: 0 0 30px rgba(34, 211, 238, 0.5);
  z-index: 2;
}

.player-museum__hero-cover {
  background-position: center;
  transform: scale(1.08);
  filter: saturate(1.12) contrast(1.08);
  animation: player-cover-drift 18s var(--neo-ease) infinite alternate;
}

.player-museum__hero-video-noise,
.player-museum__hero-scanline {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.player-museum__hero-video-noise {
  opacity: 0.14;
  mix-blend-mode: screen;
  background-image:
    linear-gradient(rgba(255, 255, 255, 0.08) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255, 255, 255, 0.06) 1px, transparent 1px);
  background-size: 18px 18px;
  mask-image: linear-gradient(180deg, transparent, #000 18%, #000 78%, transparent);
}

.player-museum__hero-scanline {
  opacity: 0.18;
  background: linear-gradient(180deg, transparent 0%, rgba(255, 255, 255, 0.18) 50%, transparent 100%);
  height: 34%;
  animation: player-scan 7s linear infinite;
}

.player-museum__hero-mesh,
.player-museum__hero--has-cover .player-museum__hero-mesh {
  opacity: 1;
  background:
    radial-gradient(ellipse 78% 62% at 54% 42%, transparent 0%, rgba(2, 6, 23, 0.36) 38%, rgba(2, 6, 23, 0.9) 78%),
    linear-gradient(90deg, rgba(2, 6, 23, 0.94) 0%, rgba(2, 6, 23, 0.58) 46%, rgba(2, 6, 23, 0.82) 100%),
    linear-gradient(180deg, rgba(2, 6, 23, 0.08) 0%, rgba(2, 6, 23, 0.96) 100%);
}

.player-museum__hero-glow {
  opacity: 0.76;
  mix-blend-mode: screen;
}

.player-museum__hero-glow--a {
  background: radial-gradient(circle, rgba(34, 211, 238, 0.44), transparent 70%);
}

.player-museum__hero-glow--b {
  background: radial-gradient(circle, rgba(248, 113, 113, 0.36), transparent 72%);
}

.player-museum__hero-inner {
  display: grid;
  grid-template-columns: minmax(0, 1fr);
  gap: 24px;
  max-width: 1120px;
}

.player-museum__hero-copy {
  min-width: 0;
}

.player-museum__eyebrow,
.player-museum__hero--has-cover .player-museum__eyebrow {
  margin: 0 0 14px;
  color: rgba(125, 211, 252, 0.88);
  font-size: 11px;
  font-weight: 800;
  letter-spacing: 0;
  text-transform: uppercase;
}

.player-museum__title,
.player-museum__hero--has-cover .player-museum__title {
  margin: 0;
  color: #fff;
  font-size: clamp(54px, 18vw, 168px);
  font-weight: 900;
  letter-spacing: 0;
  line-height: 0.84;
  text-transform: uppercase;
  text-shadow: 0 28px 70px rgba(0, 0, 0, 0.5);
}

.player-museum__tagline,
.player-museum__hero--has-cover .player-museum__tagline {
  max-width: 660px;
  margin: 24px 0 0;
  color: rgba(241, 245, 249, 0.78);
  font-size: clamp(16px, 3.8vw, 22px);
  line-height: 1.55;
  text-shadow: 0 12px 28px rgba(0, 0, 0, 0.45);
}

.player-museum__hero-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 28px;
}

.player-museum__primary-action,
.player-museum__ghost-action,
.player-museum__text-action {
  border: 0;
  cursor: pointer;
  font: inherit;
  text-decoration: none;
}

.player-museum__primary-action {
  display: inline-flex;
  flex-direction: column;
  gap: 3px;
  max-width: min(100%, 360px);
  min-height: 62px;
  padding: 12px 18px;
  border-radius: 999px;
  color: #020617;
  background: linear-gradient(135deg, #f8fafc 0%, #67e8f9 52%, #fca5a5 100%);
  box-shadow: 0 18px 46px rgba(34, 211, 238, 0.22);
  text-align: left;
}

.player-museum__primary-action span {
  font-size: 11px;
  font-weight: 900;
  text-transform: uppercase;
}

.player-museum__primary-action strong {
  overflow: hidden;
  max-width: 100%;
  font-size: 14px;
  font-weight: 900;
  line-height: 1.2;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.player-museum__ghost-action {
  display: inline-flex;
  align-items: center;
  min-height: 48px;
  padding: 0 18px;
  border: 1px solid rgba(255, 255, 255, 0.18);
  border-radius: 999px;
  color: #f8fafc;
  background: rgba(15, 23, 42, 0.46);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
}

.player-museum__hero-panel {
  align-self: end;
  padding: 18px;
  border: 1px solid var(--museum-line);
  border-radius: 8px;
  background:
    linear-gradient(135deg, rgba(15, 23, 42, 0.74), rgba(15, 23, 42, 0.34)),
    linear-gradient(90deg, rgba(34, 211, 238, 0.2), transparent);
  box-shadow: 0 24px 70px rgba(0, 0, 0, 0.36);
  backdrop-filter: blur(18px);
  -webkit-backdrop-filter: blur(18px);
}

.player-museum__panel-label,
.player-museum__section-kicker {
  display: block;
  color: rgba(125, 211, 252, 0.84);
  font-size: 11px;
  font-weight: 900;
  letter-spacing: 0;
  text-transform: uppercase;
}

.player-museum__hero-panel strong {
  display: block;
  margin-top: 8px;
  color: #fff;
  font-size: 23px;
  line-height: 1.16;
}

.player-museum__hero-panel p {
  margin: 10px 0 0;
  color: rgba(226, 232, 240, 0.76);
  font-size: 14px;
  line-height: 1.5;
}

.player-museum__kpi-strip {
  position: absolute;
  right: 14px;
  bottom: 18px;
  left: 14px;
  z-index: 3;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 8px;
  max-width: 760px;
  margin: 0 auto;
}

.player-museum__kpi {
  padding: 12px;
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: 8px;
  background: rgba(2, 6, 23, 0.52);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
}

.player-museum__kpi strong {
  display: block;
  color: #fff;
  font-size: 22px;
  line-height: 1;
}

.player-museum__kpi span {
  display: block;
  margin-top: 5px;
  color: rgba(226, 232, 240, 0.62);
  font-size: 12px;
}

.player-museum__quicknav {
  position: sticky;
  top: 46px;
  z-index: 8;
  display: flex;
  gap: 8px;
  overflow-x: auto;
  padding: 10px 12px;
  background: rgba(2, 6, 23, 0.78);
  border-top: 1px solid rgba(255, 255, 255, 0.08);
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(18px);
  -webkit-backdrop-filter: blur(18px);
}

.player-museum__quicknav button {
  flex: 0 0 auto;
  min-width: 58px;
  height: 34px;
  padding: 0 14px;
  border: 1px solid rgba(255, 255, 255, 0.14);
  border-radius: 999px;
  color: rgba(248, 250, 252, 0.9);
  background: rgba(255, 255, 255, 0.06);
  font: inherit;
  font-size: 13px;
}

.player-museum__body {
  padding: 18px 14px 48px;
  background: transparent;
}

.player-museum__section {
  max-width: 1120px;
  margin: 34px auto 0;
  scroll-margin-top: 98px;
}

.player-museum__section:first-child {
  margin-top: 20px;
}

.player-museum__section-head {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 16px;
}

.player-museum__section-title {
  margin: 0;
  color: #fff;
  font-size: clamp(24px, 6vw, 44px);
  font-weight: 900;
  letter-spacing: 0;
  line-height: 1.05;
}

.player-museum__muted {
  color: var(--museum-muted);
}

.player-museum__spotlight {
  display: grid;
  grid-template-columns: minmax(0, 1fr);
  gap: 0;
  overflow: hidden;
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: 8px;
  background: rgba(15, 23, 42, 0.62);
  box-shadow: 0 30px 90px rgba(0, 0, 0, 0.32);
}

.player-museum__spotlight-media {
  position: relative;
  min-height: 260px;
  overflow: hidden;
  background: linear-gradient(135deg, #0f172a, #1f2937);
}

.player-museum__spotlight-media img {
  width: 100%;
  height: 100%;
  min-height: 260px;
  object-fit: cover;
  display: block;
  filter: saturate(1.1) contrast(1.06);
}

.player-museum__spotlight-media::after {
  content: '';
  position: absolute;
  inset: 0;
  background:
    linear-gradient(180deg, transparent 24%, rgba(2, 6, 23, 0.82) 100%),
    linear-gradient(90deg, rgba(2, 6, 23, 0.08), transparent);
}

.player-museum__spotlight-fallback,
.player-museum__film-fallback {
  display: grid;
  width: 100%;
  height: 100%;
  min-height: 260px;
  place-items: center;
  color: rgba(255, 255, 255, 0.16);
  font-size: 54px;
  font-weight: 900;
}

.player-museum__play-mark {
  position: absolute;
  left: 18px;
  bottom: 18px;
  z-index: 2;
  display: grid;
  width: 58px;
  height: 58px;
  place-items: center;
  border-radius: 999px;
  color: #020617;
  background: #f8fafc;
  box-shadow: 0 16px 50px rgba(248, 250, 252, 0.24);
}

.player-museum__spotlight-copy {
  padding: 22px;
}

.player-museum__spotlight-copy h2 {
  margin: 9px 0 12px;
  color: #fff;
  font-size: clamp(26px, 8vw, 56px);
  line-height: 1;
}

.player-museum__spotlight-copy p {
  margin: 0;
  color: rgba(226, 232, 240, 0.72);
  font-size: 15px;
  line-height: 1.62;
}

.player-museum__text-action {
  margin-top: 18px;
  padding: 0;
  color: #67e8f9;
  background: transparent;
  font-weight: 800;
}

.player-museum__chips {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.player-museum__chip {
  min-height: 54px;
  padding: 13px 14px;
  border-radius: 8px;
  color: rgba(248, 250, 252, 0.9);
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.1), rgba(255, 255, 255, 0.035));
  border: 1px solid rgba(255, 255, 255, 0.12);
  box-shadow: none;
}

.player-museum__highlight-rail {
  display: grid;
  grid-auto-columns: minmax(255px, 72vw);
  grid-auto-flow: column;
  gap: 14px;
  margin: 0 -14px;
  padding: 2px 14px 18px;
  overflow-x: auto;
  scroll-snap-type: x mandatory;
}

.player-museum__film-card {
  position: relative;
  overflow: hidden;
  min-height: 390px;
  border: 1px solid rgba(255, 255, 255, 0.14);
  border-radius: 8px;
  background: #0f172a;
  scroll-snap-align: start;
  transition:
    transform 0.45s var(--neo-ease-out),
    border-color 0.45s var(--neo-ease-out),
    box-shadow 0.45s var(--neo-ease-out);
}

.player-museum__film-card:active {
  transform: scale(0.985);
}

.player-museum__film-visual {
  position: absolute;
  inset: 0;
}

.player-museum__film-visual img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
  filter: saturate(1.08) contrast(1.05);
  transform: scale(1.02);
}

.player-museum__film-visual::after {
  content: '';
  position: absolute;
  inset: 0;
  background:
    linear-gradient(180deg, rgba(2, 6, 23, 0.06), rgba(2, 6, 23, 0.9)),
    linear-gradient(90deg, rgba(2, 6, 23, 0.5), transparent 58%);
}

.player-museum__film-copy {
  position: absolute;
  right: 0;
  bottom: 0;
  left: 0;
  z-index: 2;
  padding: 18px;
}

.player-museum__film-copy span {
  color: rgba(125, 211, 252, 0.88);
  font-size: 11px;
  font-weight: 900;
}

.player-museum__film-copy h3 {
  margin: 7px 0 8px;
  color: #fff;
  font-size: 24px;
  line-height: 1.08;
}

.player-museum__film-copy p {
  display: -webkit-box;
  overflow: hidden;
  margin: 0;
  color: rgba(226, 232, 240, 0.74);
  font-size: 14px;
  line-height: 1.5;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 3;
}

.player-museum__cards,
.player-museum__biz-list {
  gap: 12px;
}

.player-museum__card,
.player-museum__biz-card,
.player-museum__timeline-card {
  border-radius: 8px;
  color: #f8fafc;
  background: linear-gradient(135deg, rgba(15, 23, 42, 0.84), rgba(15, 23, 42, 0.46));
  border: 1px solid rgba(255, 255, 255, 0.12);
  box-shadow: 0 18px 60px rgba(0, 0, 0, 0.2);
  backdrop-filter: blur(18px);
  -webkit-backdrop-filter: blur(18px);
}

.player-museum__card-title,
.player-museum__biz-title,
.player-museum__timeline-event {
  color: #fff;
}

.player-museum__card-kicker,
.player-museum__timeline-date {
  color: rgba(125, 211, 252, 0.78);
}

.player-museum__card-meta,
.player-museum__biz-meta,
.player-museum__timeline-detail {
  color: rgba(226, 232, 240, 0.65);
}

.player-museum__timeline {
  border-left: 1px solid rgba(125, 211, 252, 0.34);
}

.player-museum__timeline-item {
  margin-bottom: 16px;
}

.player-museum__timeline-item::before {
  left: -7px;
  background: #67e8f9;
  box-shadow: 0 0 0 4px rgba(34, 211, 238, 0.14), 0 0 30px rgba(34, 211, 238, 0.7);
}

.player-museum__timeline-kind {
  display: inline-flex;
  width: fit-content;
  margin-bottom: 8px;
  padding: 3px 8px;
  border-radius: 999px;
  color: #020617;
  background: #67e8f9;
  font-size: 11px;
  font-weight: 900;
}

.player-museum__business {
  padding: 24px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 8px;
  background:
    radial-gradient(circle at 16% 0%, rgba(248, 113, 113, 0.2), transparent 22rem),
    rgba(15, 23, 42, 0.4);
}

.player-museum__biz-cover {
  border-radius: 8px;
}

@keyframes player-cover-drift {
  0% {
    transform: scale(1.06) translate3d(0, 0, 0);
  }
  100% {
    transform: scale(1.12) translate3d(-12px, 10px, 0);
  }
}

@keyframes player-scan {
  0% {
    transform: translateY(-120%);
  }
  100% {
    transform: translateY(320%);
  }
}

@media (hover: hover) {
  .player-museum__film-card:hover,
  .player-museum__card:hover,
  .player-museum__biz-card:hover,
  .player-museum__timeline-card:hover {
    transform: translateY(-4px);
    border-color: rgba(125, 211, 252, 0.42);
    box-shadow: 0 28px 90px rgba(34, 211, 238, 0.12);
  }
}

@media (min-width: 768px) {
  .player-museum__hero {
    padding: 76px 36px 124px;
  }

  .player-museum__hero-inner {
    grid-template-columns: minmax(0, 1fr) 320px;
    align-items: end;
  }

  .player-museum__kpi-strip {
    grid-template-columns: repeat(4, minmax(0, 1fr));
    right: 36px;
    left: 36px;
  }

  .player-museum__body {
    padding: 28px 36px 64px;
  }

  .player-museum__spotlight {
    grid-template-columns: minmax(0, 1.15fr) minmax(320px, 0.85fr);
  }

  .player-museum__spotlight-media,
  .player-museum__spotlight-media img {
    min-height: 430px;
  }

  .player-museum__spotlight-copy {
    align-self: end;
    padding: 34px;
  }

  .player-museum__chips {
    grid-template-columns: repeat(4, minmax(0, 1fr));
  }

  .player-museum__highlight-rail {
    grid-auto-columns: minmax(320px, 380px);
    margin-right: 0;
    margin-left: 0;
    padding-right: 0;
    padding-left: 0;
  }
}

@media (max-width: 360px) {
  .player-museum__title,
  .player-museum__hero--has-cover .player-museum__title {
    font-size: 46px;
  }

  .player-museum__chips {
    grid-template-columns: 1fr;
  }
}

@media (prefers-reduced-motion: reduce) {
  .player-museum__hero-cover,
  .player-museum__hero-scanline {
    animation: none !important;
  }
}

/* TT unified style pass: matches the public portal while keeping the premium athlete-page motion. */
.player-museum {
  --tt-ink: #151923;
  --tt-muted: #7b8190;
  --tt-coral: #ff4b59;
  --tt-coral-dark: #f03a45;
  --tt-blue: #5d91f5;
  --tt-blue-deep: #175cd3;
  --tt-green: #55d2a2;
  --tt-yellow: #ffd057;
  --tt-paper: #fff;
  --tt-soft: #f8fbff;
  --museum-text: var(--tt-ink);
  --museum-muted: var(--tt-muted);
  background:
    radial-gradient(circle at 4% 20%, rgba(255, 75, 89, 0.08), transparent 22%),
    radial-gradient(circle at 88% 18%, rgba(93, 145, 245, 0.16), transparent 30%),
    linear-gradient(180deg, #fff 0%, #f9fbff 58%, #fff8ef 100%);
  color: var(--tt-ink);
}

.player-museum__hero {
  min-height: min(760px, calc(100vh - 46px));
  min-height: min(760px, calc(100dvh - 46px));
  padding: 44px clamp(20px, 4vw, 42px) 118px;
}

.player-museum__hero::after {
  bottom: 48px;
  background: linear-gradient(90deg, transparent, rgba(255, 75, 89, 0.65), rgba(93, 145, 245, 0.48), transparent);
  box-shadow: 0 0 26px rgba(255, 75, 89, 0.2);
}

.player-museum__hero-mesh,
.player-museum__hero--has-cover .player-museum__hero-mesh {
  background:
    radial-gradient(circle at 12% 72%, rgba(255, 75, 89, 0.12), transparent 22%),
    radial-gradient(circle at 84% 28%, rgba(93, 145, 245, 0.2), transparent 31%),
    linear-gradient(135deg, rgba(255, 255, 255, 0.9) 0%, rgba(240, 247, 255, 0.84) 58%, rgba(255, 247, 237, 0.9) 100%);
}

.player-museum__hero--has-cover .player-museum__hero-mesh {
  background:
    linear-gradient(90deg, rgba(255, 255, 255, 0.96) 0%, rgba(255, 255, 255, 0.74) 47%, rgba(255, 255, 255, 0.5) 100%),
    linear-gradient(180deg, rgba(255, 255, 255, 0.16), rgba(249, 251, 255, 0.96));
}

.player-museum__hero-video-noise {
  opacity: 0.16;
  mix-blend-mode: multiply;
  background-image:
    linear-gradient(rgba(23, 92, 211, 0.08) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255, 75, 89, 0.06) 1px, transparent 1px);
}

.player-museum__hero-scanline {
  opacity: 0.18;
  background: linear-gradient(180deg, transparent 0%, rgba(255, 75, 89, 0.22) 50%, transparent 100%);
}

.player-museum__hero-glow {
  mix-blend-mode: normal;
  filter: blur(78px);
}

.player-museum__hero-glow--a {
  background: radial-gradient(circle, rgba(93, 145, 245, 0.28), transparent 70%);
}

.player-museum__hero-glow--b {
  background: radial-gradient(circle, rgba(255, 208, 87, 0.28), transparent 72%);
}

.player-museum__eyebrow,
.player-museum__hero--has-cover .player-museum__eyebrow,
.player-museum__panel-label,
.player-museum__section-kicker,
.player-museum__film-copy span,
.player-museum__card-kicker,
.player-museum__timeline-date {
  color: var(--tt-coral);
}

.player-museum__title,
.player-museum__hero--has-cover .player-museum__title {
  color: var(--tt-ink);
  text-shadow: 0 8px 0 rgba(255, 75, 89, 0.07);
  transform: skew(-3deg);
}

.player-museum__tagline,
.player-museum__hero--has-cover .player-museum__tagline {
  color: rgba(21, 25, 35, 0.72);
  text-shadow: none;
}

.player-museum__primary-action {
  color: #fff;
  background: linear-gradient(135deg, var(--tt-coral), var(--tt-coral-dark));
  box-shadow: 0 18px 36px rgba(255, 75, 89, 0.3);
}

.player-museum__primary-action strong {
  color: #fff;
}

.player-museum__ghost-action {
  color: rgba(21, 25, 35, 0.78);
  background: rgba(255, 255, 255, 0.78);
  border-color: rgba(21, 25, 35, 0.1);
}

.player-museum__hero-panel,
.player-museum__kpi,
.player-museum__chip,
.player-museum__card,
.player-museum__biz-card,
.player-museum__timeline-card {
  color: var(--tt-ink);
  background: rgba(255, 255, 255, 0.88);
  border: 1px solid rgba(21, 25, 35, 0.08);
  box-shadow: 0 18px 42px rgba(21, 25, 35, 0.08);
}

.player-museum__hero-panel {
  background:
    radial-gradient(circle at 12% 0%, rgba(255, 208, 87, 0.26), transparent 36%),
    rgba(255, 255, 255, 0.88);
}

.player-museum__hero-panel strong,
.player-museum__kpi strong,
.player-museum__section-title,
.player-museum__card-title,
.player-museum__biz-title,
.player-museum__timeline-event {
  color: var(--tt-ink);
}

.player-museum__hero-panel p,
.player-museum__kpi span,
.player-museum__card-meta,
.player-museum__biz-meta,
.player-museum__timeline-detail,
.player-museum__film-copy p,
.player-museum__spotlight-copy p {
  color: rgba(21, 25, 35, 0.62);
}

.player-museum__quicknav {
  background: rgba(255, 255, 255, 0.92);
  border-top: 1px solid rgba(47, 125, 255, 0.1);
  border-bottom: 1px solid rgba(47, 125, 255, 0.1);
  box-shadow: 0 12px 30px rgba(23, 32, 51, 0.06);
}

.player-museum__quicknav button {
  color: rgba(21, 25, 35, 0.78);
  background: rgba(248, 251, 255, 0.88);
  border-color: rgba(21, 25, 35, 0.08);
  font-weight: 800;
}

.player-museum__body {
  background: transparent;
}

.player-museum__spotlight {
  background:
    radial-gradient(circle at 100% 0%, rgba(93, 145, 245, 0.14), transparent 32%),
    var(--tt-paper);
  border: 1px solid rgba(21, 25, 35, 0.08);
  box-shadow: 0 24px 58px rgba(21, 25, 35, 0.12);
}

.player-museum__spotlight-media {
  background:
    radial-gradient(circle at 35% 35%, rgba(255, 208, 87, 0.36), transparent 22%),
    linear-gradient(135deg, rgba(93, 145, 245, 0.18), rgba(255, 75, 89, 0.12));
}

.player-museum__spotlight-media::after {
  background:
    linear-gradient(180deg, transparent 18%, rgba(21, 25, 35, 0.22) 100%),
    linear-gradient(90deg, rgba(255, 255, 255, 0.08), transparent);
}

.player-museum__spotlight-copy h2,
.player-museum__film-copy h3 {
  color: var(--tt-ink);
}

.player-museum__play-mark {
  color: #fff;
  background: var(--tt-coral);
  box-shadow: 0 16px 34px rgba(255, 75, 89, 0.28);
}

.player-museum__text-action {
  color: var(--tt-blue-deep);
}

.player-museum__film-card {
  background: #fff;
  border-color: rgba(21, 25, 35, 0.08);
  box-shadow: 0 18px 42px rgba(21, 25, 35, 0.1);
}

.player-museum__film-visual::after {
  background:
    linear-gradient(180deg, transparent 20%, rgba(255, 255, 255, 0.94) 100%),
    linear-gradient(90deg, rgba(255, 255, 255, 0.34), transparent 58%);
}

.player-museum__film-fallback,
.player-museum__spotlight-fallback {
  color: rgba(255, 75, 89, 0.16);
  background:
    radial-gradient(circle at 35% 35%, rgba(255, 208, 87, 0.34), transparent 24%),
    linear-gradient(135deg, rgba(93, 145, 245, 0.16), rgba(255, 75, 89, 0.11));
}

.player-museum__timeline {
  border-left-color: rgba(93, 145, 245, 0.26);
}

.player-museum__timeline-item::before {
  background: var(--tt-blue);
  box-shadow: 0 0 0 4px rgba(93, 145, 245, 0.12), 0 0 28px rgba(93, 145, 245, 0.28);
}

.player-museum__timeline-kind {
  color: #fff;
  background: var(--tt-blue);
}

.player-museum__business {
  background:
    radial-gradient(circle at 16% 0%, rgba(255, 208, 87, 0.22), transparent 24rem),
    linear-gradient(135deg, #ffffff 0%, #f0f7ff 58%, #fff7ed 100%);
  border-color: rgba(21, 25, 35, 0.08);
  box-shadow: 0 18px 42px rgba(21, 25, 35, 0.08);
}

@media (hover: hover) {
  .player-museum__film-card:hover,
  .player-museum__card:hover,
  .player-museum__biz-card:hover,
  .player-museum__timeline-card:hover {
    border-color: rgba(255, 75, 89, 0.22);
    box-shadow: 0 24px 58px rgba(21, 25, 35, 0.12);
  }
}

/* Editorial magazine upgrade */
.player-museum {
  background:
    linear-gradient(90deg, rgba(21, 25, 35, 0.045) 1px, transparent 1px),
    radial-gradient(circle at 8% 18%, rgba(255, 75, 89, 0.1), transparent 24rem),
    radial-gradient(circle at 88% 16%, rgba(93, 145, 245, 0.18), transparent 28rem),
    linear-gradient(180deg, #fffdf8 0%, #f8fbff 54%, #ffffff 100%);
  background-size: 72px 72px, auto, auto, auto;
}

.player-museum__hero {
  display: block;
  min-height: min(820px, calc(100vh - 46px));
  min-height: min(820px, calc(100dvh - 46px));
  padding: 34px clamp(18px, 4vw, 54px) 112px;
}

.player-museum__hero::before {
  content: 'TABLE TENNIS / FEATURE STORY / PLAYER ARCHIVE';
  position: absolute;
  right: clamp(18px, 4vw, 54px);
  bottom: 98px;
  z-index: 3;
  writing-mode: vertical-rl;
  color: rgba(21, 25, 35, 0.34);
  font-size: 10px;
  font-weight: 950;
  letter-spacing: 0;
}

.player-museum__cover-mast {
  position: relative;
  z-index: 4;
  display: grid;
  grid-template-columns: 1fr auto 1fr;
  align-items: end;
  gap: 18px;
  max-width: 1180px;
  margin: 0 auto 44px;
  padding-bottom: 14px;
  border-bottom: 4px solid var(--tt-ink);
  color: var(--tt-ink);
}

.player-museum__cover-mast span,
.player-museum__cover-mast em {
  font-size: 12px;
  font-style: normal;
  font-weight: 950;
  letter-spacing: 0;
  text-transform: uppercase;
}

.player-museum__cover-mast em {
  justify-self: end;
}

.player-museum__cover-mast strong {
  font-size: clamp(20px, 4vw, 54px);
  font-weight: 950;
  line-height: 0.86;
  letter-spacing: 0;
  text-align: center;
}

.player-museum__hero-inner {
  position: relative;
  grid-template-columns: minmax(0, 0.9fr) minmax(300px, 0.58fr);
  grid-template-areas:
    'copy figure'
    'panel figure';
  align-items: end;
  max-width: 1180px;
  min-height: 520px;
  margin: 0 auto;
}

.player-museum__hero-copy {
  grid-area: copy;
  align-self: center;
  padding: 26px 0 34px;
}

.player-museum__cover-figure {
  grid-area: figure;
  position: relative;
  z-index: 3;
  align-self: stretch;
  min-height: 520px;
}

.player-museum__cover-card {
  position: relative;
  height: 100%;
  min-height: 520px;
  overflow: hidden;
  border-radius: 0;
  background:
    radial-gradient(circle at 52% 22%, rgba(255, 208, 87, 0.46), transparent 20%),
    linear-gradient(145deg, rgba(255, 75, 89, 0.18), rgba(93, 145, 245, 0.2) 62%, rgba(255, 255, 255, 0.92));
  border: 4px solid var(--tt-ink);
  box-shadow: 18px 18px 0 rgba(21, 25, 35, 0.08), 0 34px 70px rgba(21, 25, 35, 0.16);
  transform: rotate(1.5deg);
}

.player-museum__cover-card img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
  filter: saturate(1.08) contrast(1.03);
}

.player-museum__cover-card::before {
  content: '';
  position: absolute;
  inset: 20px;
  border: 1px solid rgba(21, 25, 35, 0.2);
  pointer-events: none;
}

.player-museum__cover-card i {
  position: absolute;
  right: -24px;
  bottom: -44px;
  width: 170px;
  height: 170px;
  border-radius: 50%;
  background: var(--tt-coral);
  opacity: 0.9;
}

.player-museum__cover-initials {
  position: absolute;
  inset: 0;
  display: grid;
  place-items: center;
  color: rgba(21, 25, 35, 0.86);
  font-size: clamp(116px, 18vw, 230px);
  font-weight: 950;
  line-height: 0.8;
  letter-spacing: 0;
  transform: skew(-8deg);
  text-shadow: 8px 8px 0 rgba(255, 75, 89, 0.14);
}

.player-museum__cover-rank {
  position: absolute;
  top: 22px;
  left: 22px;
  z-index: 2;
  padding: 8px 12px;
  color: #fff;
  background: var(--tt-ink);
  font-size: 11px;
  font-weight: 950;
  letter-spacing: 0;
}

.player-museum__eyebrow,
.player-museum__hero--has-cover .player-museum__eyebrow {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 18px;
  color: var(--tt-coral);
  font-size: 12px;
  font-weight: 950;
}

.player-museum__eyebrow::before {
  content: '';
  width: 46px;
  height: 4px;
  background: var(--tt-coral);
}

.player-museum__title,
.player-museum__hero--has-cover .player-museum__title {
  max-width: 820px;
  font-size: clamp(76px, 12.5vw, 190px);
  letter-spacing: 0;
  line-height: 0.76;
  text-shadow: 8px 8px 0 rgba(255, 75, 89, 0.09);
}

.player-museum__tagline,
.player-museum__hero--has-cover .player-museum__tagline {
  max-width: 600px;
  margin-top: 26px;
  padding-left: 18px;
  border-left: 5px solid var(--tt-coral);
  font-size: clamp(17px, 2vw, 25px);
  font-weight: 800;
  color: rgba(21, 25, 35, 0.78);
}

.player-museum__hero-panel {
  grid-area: panel;
  max-width: 520px;
  border-radius: 0;
  border: 2px solid var(--tt-ink);
  box-shadow: 10px 10px 0 rgba(21, 25, 35, 0.08);
}

.player-museum__primary-action {
  border-radius: 0;
  box-shadow: 10px 10px 0 rgba(255, 75, 89, 0.18);
}

.player-museum__ghost-action,
.player-museum__quicknav button,
.player-museum__kpi,
.player-museum__chip,
.player-museum__card,
.player-museum__biz-card,
.player-museum__timeline-card,
.player-museum__spotlight,
.player-museum__film-card,
.player-museum__business {
  border-radius: 0;
}

.player-museum__kpi-strip {
  right: clamp(18px, 4vw, 54px);
  bottom: 22px;
  left: clamp(18px, 4vw, 54px);
  max-width: 1180px;
}

.player-museum__kpi {
  border: 2px solid rgba(21, 25, 35, 0.12);
}

.player-museum__quicknav {
  top: 46px;
  justify-content: center;
}

.player-museum__quicknav button {
  min-width: 72px;
  border: 2px solid rgba(21, 25, 35, 0.1);
}

.player-museum__body {
  padding-top: 42px;
}

.player-museum__section-head {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  align-items: end;
  gap: 20px;
  border-top: 4px solid var(--tt-ink);
  padding-top: 18px;
}

.player-museum__section-kicker {
  grid-column: 2;
  grid-row: 1;
  justify-self: end;
  padding: 5px 9px;
  color: #fff;
  background: var(--tt-coral);
}

.player-museum__section-title {
  grid-column: 1;
  grid-row: 1;
  font-size: clamp(34px, 6vw, 72px);
  line-height: 0.88;
}

.player-museum__spotlight {
  grid-template-columns: minmax(0, 0.95fr) minmax(300px, 1.05fr);
  border: 4px solid var(--tt-ink);
  box-shadow: 18px 18px 0 rgba(21, 25, 35, 0.07);
}

.player-museum__spotlight-copy {
  padding: clamp(26px, 5vw, 58px);
}

.player-museum__spotlight-copy h2 {
  font-size: clamp(38px, 5.8vw, 78px);
  line-height: 0.92;
  letter-spacing: 0;
}

.player-museum__highlight-rail {
  grid-auto-columns: minmax(340px, 410px);
  gap: 20px;
}

.player-museum__film-card {
  min-height: 520px;
  border: 3px solid var(--tt-ink);
  box-shadow: 12px 12px 0 rgba(21, 25, 35, 0.06);
}

.player-museum__film-copy {
  padding: 24px;
}

.player-museum__film-copy h3 {
  font-size: 30px;
  line-height: 0.98;
}

.player-museum__cards {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.player-museum__card,
.player-museum__biz-card,
.player-museum__timeline-card {
  border: 2px solid rgba(21, 25, 35, 0.1);
}

@media (max-width: 767px) {
  .player-museum__hero {
    min-height: auto;
    padding-top: 24px;
    padding-bottom: 122px;
  }

  .player-museum__hero::before {
    display: none;
  }

  .player-museum__cover-mast {
    grid-template-columns: 1fr;
    gap: 8px;
    margin-bottom: 24px;
  }

  .player-museum__cover-mast strong {
    text-align: left;
  }

  .player-museum__cover-mast em {
    justify-self: start;
  }

  .player-museum__hero-inner {
    display: grid;
    grid-template-columns: 1fr;
    grid-template-areas:
      'copy'
      'figure'
      'panel';
    min-height: 0;
    gap: 22px;
  }

  .player-museum__hero-copy {
    padding-bottom: 0;
  }

  .player-museum__cover-figure,
  .player-museum__cover-card {
    min-height: 290px;
  }

  .player-museum__cover-card {
    transform: rotate(0);
  }

  .player-museum__title,
  .player-museum__hero--has-cover .player-museum__title {
    font-size: clamp(62px, 22vw, 92px);
  }

  .player-museum__tagline,
  .player-museum__hero--has-cover .player-museum__tagline {
    font-size: 17px;
  }

  .player-museum__kpi-strip {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .player-museum__section-head {
    grid-template-columns: 1fr;
  }

  .player-museum__section-kicker,
  .player-museum__section-title {
    grid-column: 1;
    grid-row: auto;
    justify-self: start;
  }

  .player-museum__spotlight {
    grid-template-columns: 1fr;
  }

  .player-museum__highlight-rail {
    grid-auto-columns: minmax(270px, 82vw);
  }

  .player-museum__film-card {
    min-height: 430px;
  }

  .player-museum__cards {
    grid-template-columns: 1fr;
  }
}

/* Visual polish after screenshot review: make the no-photo state feel intentional, not like a placeholder. */
.player-museum {
  background:
    radial-gradient(circle at 9% 15%, rgba(255, 75, 89, 0.08), transparent 21rem),
    radial-gradient(circle at 84% 12%, rgba(93, 145, 245, 0.16), transparent 30rem),
    linear-gradient(180deg, #fffefb 0%, #f7fbff 50%, #ffffff 100%);
}

.player-museum__cover-mast {
  border-bottom-width: 2px;
}

.player-museum__cover-mast strong {
  font-size: clamp(24px, 4.8vw, 62px);
  letter-spacing: 0;
}

.player-museum__hero-inner {
  grid-template-columns: minmax(0, 0.92fr) minmax(300px, 0.55fr);
  gap: clamp(28px, 5vw, 72px);
}

.player-museum__title,
.player-museum__hero--has-cover .player-museum__title {
  text-shadow: 6px 6px 0 rgba(255, 75, 89, 0.08);
}

.player-museum__cover-card {
  min-height: 500px;
  border: 0;
  border-radius: 28px;
  background:
    radial-gradient(circle at 50% 24%, rgba(255, 208, 87, 0.52), transparent 17%),
    radial-gradient(circle at 68% 72%, rgba(255, 75, 89, 0.74), transparent 22%),
    radial-gradient(circle at 18% 66%, rgba(93, 145, 245, 0.32), transparent 24%),
    linear-gradient(145deg, rgba(255, 255, 255, 0.96), rgba(231, 241, 255, 0.9));
  box-shadow: 0 34px 86px rgba(21, 25, 35, 0.14);
  transform: rotate(0.8deg);
}

.player-museum__cover-card::before {
  inset: 24px;
  border: 1px solid rgba(21, 25, 35, 0.08);
  border-radius: 20px;
}

.player-museum__cover-card::after {
  content: '';
  position: absolute;
  left: 16%;
  right: 13%;
  bottom: 18%;
  height: 4px;
  border-radius: 999px;
  background: rgba(21, 25, 35, 0.82);
  transform: rotate(-11deg);
  box-shadow:
    26px -52px 0 -1px rgba(21, 25, 35, 0.72),
    -18px -92px 0 -1px rgba(21, 25, 35, 0.58);
}

.player-museum__cover-card i {
  right: 30px;
  bottom: 30px;
  width: 92px;
  height: 92px;
  background: var(--tt-coral);
  box-shadow:
    -210px -238px 0 -34px rgba(255, 208, 87, 0.86),
    -120px -92px 0 -42px rgba(93, 145, 245, 0.68);
}

.player-museum__cover-initials {
  top: auto;
  right: auto;
  bottom: 36px;
  left: 36px;
  display: block;
  width: auto;
  height: auto;
  color: rgba(21, 25, 35, 0.08);
  font-size: clamp(96px, 12vw, 160px);
  line-height: 0.75;
  transform: none;
  text-shadow: none;
}

.player-museum__cover-rank {
  top: 28px;
  left: 28px;
  border-radius: 999px;
  background: rgba(21, 25, 35, 0.88);
}

.player-museum__hero-panel,
.player-museum__primary-action,
.player-museum__spotlight,
.player-museum__film-card {
  box-shadow: 0 24px 58px rgba(21, 25, 35, 0.12);
}

.player-museum__hero-panel {
  border: 0;
  border-radius: 20px;
}

.player-museum__primary-action {
  border-radius: 18px;
}

.player-museum__ghost-action,
.player-museum__quicknav button,
.player-museum__kpi,
.player-museum__chip,
.player-museum__card,
.player-museum__biz-card,
.player-museum__timeline-card,
.player-museum__spotlight,
.player-museum__film-card,
.player-museum__business {
  border-radius: 20px;
}

.player-museum__section-head {
  border-top: 2px solid rgba(21, 25, 35, 0.88);
}

.player-museum__section-kicker {
  border-radius: 999px;
}

.player-museum__spotlight,
.player-museum__film-card {
  border: 0;
}

.player-museum__spotlight {
  overflow: hidden;
}

.player-museum__film-card {
  min-height: 480px;
}

.player-museum__film-visual::after {
  background:
    linear-gradient(180deg, transparent 12%, rgba(255, 255, 255, 0.92) 82%),
    radial-gradient(circle at 50% 35%, rgba(255, 208, 87, 0.22), transparent 28%);
}

@media (max-width: 767px) {
  .player-museum__hero {
    padding-bottom: 100px;
  }

  .player-museum__cover-mast {
    border-bottom-width: 2px;
  }

  .player-museum__cover-mast strong {
    font-size: 31px;
    line-height: 0.95;
  }

  .player-museum__hero-inner {
    grid-template-columns: minmax(0, 1fr) !important;
    gap: 18px;
  }

  .player-museum__title,
  .player-museum__hero--has-cover .player-museum__title {
    font-size: clamp(58px, 21vw, 86px);
    white-space: nowrap;
  }

  .player-museum__tagline,
  .player-museum__hero--has-cover .player-museum__tagline {
    margin-top: 18px;
    font-size: 16px;
  }

  .player-museum__cover-figure,
  .player-museum__cover-card {
    min-height: 245px;
  }

  .player-museum__cover-card {
    border-radius: 22px;
  }

  .player-museum__cover-card::after {
    left: 18%;
    right: 16%;
    bottom: 34%;
  }

  .player-museum__cover-card i {
    right: 22px;
    bottom: 22px;
    width: 72px;
    height: 72px;
    box-shadow:
      -138px -102px 0 -24px rgba(255, 208, 87, 0.86),
      -76px -48px 0 -30px rgba(93, 145, 245, 0.68);
  }

  .player-museum__cover-initials {
    bottom: 24px;
    left: 24px;
    font-size: 92px;
  }

  .player-museum__kpi-strip {
    bottom: 16px;
  }

  .player-museum__film-card {
    min-height: 390px;
  }
}

/* Third pass: more athlete, less placeholder. */
.player-museum__hero-inner {
  grid-template-columns: minmax(0, 0.86fr) minmax(330px, 0.62fr);
}

.player-museum__title,
.player-museum__hero--has-cover .player-museum__title {
  max-width: 760px;
  font-size: clamp(72px, 10vw, 150px);
  line-height: 0.82;
}

.player-museum__tagline,
.player-museum__hero--has-cover .player-museum__tagline {
  max-width: 560px;
  font-size: clamp(16px, 1.75vw, 22px);
  line-height: 1.6;
}

.player-museum__cover-card {
  min-height: 510px;
  background:
    radial-gradient(circle at 72% 18%, rgba(255, 208, 87, 0.72), transparent 9%),
    radial-gradient(circle at 78% 74%, rgba(255, 75, 89, 0.68), transparent 20%),
    radial-gradient(circle at 24% 66%, rgba(93, 145, 245, 0.34), transparent 24%),
    linear-gradient(150deg, #ffffff 0%, #eff6ff 56%, #fff8ef 100%);
}

.player-museum__cover-card::after {
  left: 13%;
  right: 10%;
  bottom: 24%;
  height: 3px;
  background: rgba(21, 25, 35, 0.7);
  transform: rotate(-10deg);
  box-shadow:
    40px -78px 0 -1px rgba(21, 25, 35, 0.42),
    -20px -132px 0 -1px rgba(21, 25, 35, 0.28),
    70px 58px 0 -1px rgba(21, 25, 35, 0.22);
}

.player-museum__cover-initials {
  bottom: 18px;
  left: 24px;
  color: rgba(21, 25, 35, 0.045);
  font-size: clamp(120px, 14vw, 190px);
}

.player-museum__cover-athlete {
  position: absolute;
  inset: 0;
  z-index: 2;
  transform: translate(4%, 3%) rotate(-5deg);
}

.player-museum__cover-athlete span {
  position: absolute;
  display: block;
}

.player-museum__athlete-head {
  top: 22%;
  left: 48%;
  width: 46px;
  height: 46px;
  border-radius: 50%;
  background: var(--tt-ink);
  box-shadow: 12px 12px 0 rgba(255, 75, 89, 0.12);
}

.player-museum__athlete-body {
  top: 33%;
  left: 42%;
  width: 92px;
  height: 138px;
  border-radius: 54px 54px 26px 26px;
  background: linear-gradient(180deg, var(--tt-ink), #343947);
  transform: rotate(13deg);
}

.player-museum__athlete-arm {
  height: 24px;
  border-radius: 999px;
  background: var(--tt-ink);
  transform-origin: left center;
}

.player-museum__athlete-arm--hit {
  top: 40%;
  left: 53%;
  width: 150px;
  transform: rotate(-26deg);
}

.player-museum__athlete-arm--free {
  top: 46%;
  left: 34%;
  width: 112px;
  transform: rotate(33deg);
  opacity: 0.9;
}

.player-museum__athlete-leg {
  width: 32px;
  height: 142px;
  border-radius: 999px;
  background: var(--tt-ink);
  transform-origin: top center;
}

.player-museum__athlete-leg--front {
  top: 59%;
  left: 51%;
  transform: rotate(-44deg);
}

.player-museum__athlete-leg--back {
  top: 58%;
  left: 42%;
  transform: rotate(28deg);
  opacity: 0.9;
}

.player-museum__athlete-paddle {
  top: 30%;
  left: 83%;
  width: 58px;
  height: 58px;
  border-radius: 50%;
  background: var(--tt-coral);
  box-shadow:
    -26px 40px 0 -22px var(--tt-yellow),
    0 0 0 9px rgba(255, 75, 89, 0.15);
}

.player-museum__cover-card i {
  opacity: 0.78;
}

.player-museum__hero-panel {
  max-width: 560px;
}

.player-museum__film-card {
  background:
    radial-gradient(circle at 60% 22%, rgba(255, 208, 87, 0.26), transparent 20%),
    linear-gradient(180deg, #fff 0%, #f8fbff 100%);
}

@media (max-width: 767px) {
  .player-museum__title,
  .player-museum__hero--has-cover .player-museum__title {
    font-size: clamp(54px, 20vw, 82px);
    line-height: 0.88;
  }

  .player-museum__cover-athlete {
    transform: translate(-3%, 5%) scale(0.66) rotate(-5deg);
    transform-origin: center center;
  }

  .player-museum__athlete-paddle {
    left: 80%;
  }

  .player-museum__cover-initials {
    font-size: 78px;
  }
}

/* Cleaner editorial pass: no fake athlete art without a real hero image; profile reads like a magazine facts table. */
.player-museum__hero-inner--no-cover {
  grid-template-columns: minmax(0, 1fr);
  grid-template-areas:
    'copy'
    'panel';
  max-width: 930px;
  min-height: 0;
  gap: 28px;
}

.player-museum__hero-inner--no-cover .player-museum__hero-copy {
  max-width: 850px;
}

.player-museum__hero-inner--no-cover .player-museum__hero-panel {
  max-width: 720px;
}

.player-museum__hero:has(.player-museum__hero-inner--no-cover) {
  min-height: min(680px, calc(100vh - 46px));
  min-height: min(680px, calc(100dvh - 46px));
}

.player-museum__section-head {
  border-top: 1px solid rgba(21, 25, 35, 0.18);
}

.player-museum__section-kicker {
  padding: 0;
  color: var(--tt-coral);
  background: transparent;
  border-radius: 0;
  font-size: 12px;
  letter-spacing: 0.02em;
}

.player-museum__chips {
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0;
  overflow: hidden;
  border-top: 1px solid rgba(21, 25, 35, 0.12);
  border-left: 1px solid rgba(21, 25, 35, 0.12);
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.66);
  box-shadow: 0 20px 48px rgba(21, 25, 35, 0.06);
}

.player-museum__chip {
  display: grid;
  grid-template-columns: minmax(82px, 0.38fr) minmax(0, 1fr);
  align-items: baseline;
  gap: 16px;
  min-height: 0;
  padding: 18px 20px;
  border: 0;
  border-right: 1px solid rgba(21, 25, 35, 0.12);
  border-bottom: 1px solid rgba(21, 25, 35, 0.12);
  border-radius: 0;
  background: transparent;
  box-shadow: none;
}

.player-museum__chip em {
  color: rgba(21, 25, 35, 0.48);
  font-size: 13px;
  font-style: normal;
  font-weight: 900;
}

.player-museum__chip strong {
  min-width: 0;
  color: var(--tt-ink);
  font-size: 16px;
  line-height: 1.45;
}

@media (max-width: 767px) {
  .player-museum__hero-inner--no-cover {
    gap: 20px;
  }

  .player-museum__hero:has(.player-museum__hero-inner--no-cover) {
    min-height: auto;
  }

  .player-museum__chips {
    grid-template-columns: 1fr;
    border-radius: 18px;
  }

  .player-museum__chip {
    grid-template-columns: minmax(74px, 0.34fr) minmax(0, 1fr);
    padding: 15px 16px;
  }

  .player-museum__chip strong {
    font-size: 15px;
  }
}

/* Market-reference pass: athlete profile facts should feel like editorial notes, not a data table. */
#profile.player-museum__section {
  position: relative;
  padding: clamp(20px, 4vw, 34px);
  overflow: hidden;
  border-radius: 30px;
  background:
    radial-gradient(circle at 90% 12%, rgba(255, 208, 87, 0.24), transparent 18rem),
    radial-gradient(circle at 0% 100%, rgba(93, 145, 245, 0.12), transparent 18rem),
    rgba(255, 255, 255, 0.78);
  box-shadow: 0 26px 70px rgba(21, 25, 35, 0.08);
}

#profile .player-museum__section-head {
  grid-template-columns: minmax(0, 1fr);
  gap: 8px;
  margin-bottom: 24px;
  padding-top: 0;
  border-top: 0;
}

#profile .player-museum__section-kicker,
#profile .player-museum__section-title {
  grid-column: auto;
  grid-row: auto;
  justify-self: start;
}

#profile .player-museum__section-kicker {
  color: var(--tt-coral);
  background: transparent;
}

#profile .player-museum__section-title {
  max-width: 7em;
}

.player-museum__chips {
  display: grid;
  grid-template-columns: repeat(12, minmax(0, 1fr));
  gap: 12px;
  overflow: visible;
  border: 0;
  border-radius: 0;
  background: transparent;
  box-shadow: none;
}

.player-museum__chip {
  grid-column: span 4;
  display: flex;
  min-height: 116px;
  flex-direction: column;
  justify-content: space-between;
  gap: 18px;
  padding: 18px;
  border: 0;
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.78);
  box-shadow: inset 0 0 0 1px rgba(21, 25, 35, 0.06);
}

.player-museum__chip:nth-child(1),
.player-museum__chip:nth-child(7),
.player-museum__chip:nth-child(8) {
  grid-column: span 3;
  min-height: 138px;
  color: #fff;
  background:
    radial-gradient(circle at 90% 0%, rgba(255, 208, 87, 0.24), transparent 44%),
    linear-gradient(135deg, #151923 0%, #2d3340 100%);
  box-shadow: 0 18px 42px rgba(21, 25, 35, 0.14);
}

.player-museum__chip:nth-child(3),
.player-museum__chip:nth-child(4),
.player-museum__chip:nth-child(5),
.player-museum__chip:nth-child(6) {
  grid-column: span 6;
  min-height: 92px;
  border-radius: 999px;
  flex-direction: row;
  align-items: center;
  justify-content: flex-start;
}

.player-museum__chip:nth-child(9),
.player-museum__chip:nth-child(10),
.player-museum__chip:nth-child(11) {
  grid-column: span 4;
  background: rgba(248, 251, 255, 0.86);
}

.player-museum__chip em {
  color: rgba(21, 25, 35, 0.45);
  font-size: 12px;
  font-style: normal;
  font-weight: 950;
  letter-spacing: 0.02em;
  text-transform: uppercase;
}

.player-museum__chip strong {
  min-width: 0;
  color: var(--tt-ink);
  font-size: clamp(17px, 2vw, 24px);
  font-weight: 950;
  line-height: 1.18;
  overflow-wrap: anywhere;
}

.player-museum__chip:nth-child(1) em,
.player-museum__chip:nth-child(7) em,
.player-museum__chip:nth-child(8) em {
  color: rgba(255, 255, 255, 0.62);
}

.player-museum__chip:nth-child(1) strong,
.player-museum__chip:nth-child(7) strong,
.player-museum__chip:nth-child(8) strong {
  color: #fff;
  font-size: clamp(28px, 4vw, 46px);
  line-height: 0.94;
}

@media (max-width: 767px) {
  #profile.player-museum__section {
    padding: 20px 16px;
    border-radius: 24px;
  }

  #profile .player-museum__section-title {
    max-width: none;
  }

  .player-museum__chips {
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 10px;
  }

  .player-museum__chip,
  .player-museum__chip:nth-child(1),
  .player-museum__chip:nth-child(3),
  .player-museum__chip:nth-child(4),
  .player-museum__chip:nth-child(5),
  .player-museum__chip:nth-child(6),
  .player-museum__chip:nth-child(7),
  .player-museum__chip:nth-child(8),
  .player-museum__chip:nth-child(9),
  .player-museum__chip:nth-child(10),
  .player-museum__chip:nth-child(11) {
    grid-column: span 1;
    min-height: 104px;
    flex-direction: column;
    align-items: flex-start;
    justify-content: space-between;
    border-radius: 18px;
    padding: 15px;
  }

  .player-museum__chip:nth-child(3),
  .player-museum__chip:nth-child(4),
  .player-museum__chip:nth-child(5),
  .player-museum__chip:nth-child(6),
  .player-museum__chip:nth-child(9),
  .player-museum__chip:nth-child(10),
  .player-museum__chip:nth-child(11) {
    background: rgba(255, 255, 255, 0.82);
  }

  .player-museum__chip:nth-child(7),
  .player-museum__chip:nth-child(8),
  .player-museum__chip:nth-child(10),
  .player-museum__chip:nth-child(11) {
    grid-column: span 2;
  }

  .player-museum__chip strong,
  .player-museum__chip:nth-child(1) strong,
  .player-museum__chip:nth-child(7) strong,
  .player-museum__chip:nth-child(8) strong {
    font-size: 20px;
    line-height: 1.15;
  }

  .player-museum__chip em {
    font-size: 11px;
  }
}

/* NBA-style final profile layout: summary first, fewer facts, no field wall. */
#profile.player-museum__section {
  padding: clamp(18px, 4vw, 34px);
}

.player-museum__profile-overview {
  display: grid;
  grid-template-columns: minmax(0, 0.9fr) minmax(360px, 1.1fr);
  gap: 18px;
  align-items: stretch;
}

.player-museum__profile-main {
  display: flex;
  min-height: 260px;
  flex-direction: column;
  justify-content: flex-end;
  padding: 26px;
  border-radius: 28px;
  color: #fff;
  background:
    radial-gradient(circle at 14% 18%, rgba(255, 208, 87, 0.24), transparent 30%),
    linear-gradient(135deg, #151923 0%, #2d3340 100%);
  box-shadow: 0 22px 52px rgba(21, 25, 35, 0.16);
}

.player-museum__profile-name {
  margin: 0 0 14px;
  font-size: clamp(34px, 4.8vw, 68px);
  font-weight: 950;
  line-height: 0.9;
  letter-spacing: 0;
}

.player-museum__profile-note {
  max-width: 32em;
  margin: 0;
  color: rgba(255, 255, 255, 0.74);
  font-size: 15px;
  font-weight: 700;
  line-height: 1.55;
}

.player-museum__metric-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.player-museum__metric {
  display: flex;
  min-height: 124px;
  flex-direction: column;
  justify-content: space-between;
  padding: 20px;
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.86);
  box-shadow: inset 0 0 0 1px rgba(21, 25, 35, 0.06);
}

.player-museum__metric strong {
  color: var(--tt-ink);
  font-size: clamp(34px, 4vw, 58px);
  font-weight: 950;
  line-height: 0.92;
}

.player-museum__metric span {
  color: rgba(21, 25, 35, 0.5);
  font-size: 13px;
  font-weight: 950;
}

.player-museum__fact-row {
  grid-column: 1 / -1;
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  padding-top: 2px;
}

.player-museum__fact {
  display: inline-flex;
  align-items: center;
  gap: 9px;
  min-height: 42px;
  padding: 0 14px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.82);
  box-shadow: inset 0 0 0 1px rgba(21, 25, 35, 0.08);
}

.player-museum__fact em {
  color: rgba(21, 25, 35, 0.42);
  font-size: 12px;
  font-style: normal;
  font-weight: 950;
}

.player-museum__fact strong {
  color: var(--tt-ink);
  font-size: 14px;
  font-weight: 950;
}

@media (max-width: 767px) {
  .player-museum__profile-overview {
    grid-template-columns: 1fr;
    gap: 12px;
  }

  .player-museum__profile-main {
    min-height: 190px;
    padding: 20px;
    border-radius: 24px;
  }

  .player-museum__profile-name {
    font-size: 38px;
  }

  .player-museum__metric-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 10px;
  }

  .player-museum__metric {
    min-height: 96px;
    padding: 15px;
    border-radius: 20px;
  }

  .player-museum__metric strong {
    font-size: 34px;
  }

  .player-museum__fact-row {
    gap: 8px;
  }

  .player-museum__fact {
    max-width: 100%;
    min-height: 38px;
  }
}

/* Compact stat strip: keep the numbers, remove the empty-card feeling. */
.player-museum__metric-grid {
  align-self: end;
  display: grid;
  grid-template-columns: 1fr;
  gap: 0;
  overflow: hidden;
  border-radius: 26px;
  background: rgba(255, 255, 255, 0.88);
  box-shadow: inset 0 0 0 1px rgba(21, 25, 35, 0.08);
}

.player-museum__metric {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  min-height: 0;
  align-items: baseline;
  gap: 14px;
  padding: 16px 20px;
  border-bottom: 1px solid rgba(21, 25, 35, 0.08);
  border-radius: 0;
  background: transparent;
  box-shadow: none;
}

.player-museum__metric:last-child {
  border-bottom: 0;
}

.player-museum__metric strong {
  color: var(--tt-ink);
  font-size: clamp(28px, 3.4vw, 46px);
  font-weight: 950;
  line-height: 0.9;
}

.player-museum__metric span {
  color: rgba(21, 25, 35, 0.52);
  font-size: 13px;
  font-weight: 950;
}

@media (min-width: 768px) {
  .player-museum__metric-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .player-museum__metric:nth-child(odd) {
    border-right: 1px solid rgba(21, 25, 35, 0.08);
  }

  .player-museum__metric:nth-last-child(-n + 2) {
    border-bottom: 0;
  }
}

@media (max-width: 767px) {
  .player-museum__metric-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
    border-radius: 22px;
  }

  .player-museum__metric {
    grid-template-columns: 1fr;
    gap: 8px;
    padding: 13px 14px;
  }

  .player-museum__metric:nth-child(odd) {
    border-right: 1px solid rgba(21, 25, 35, 0.08);
  }

  .player-museum__metric:nth-last-child(-n + 2) {
    border-bottom: 0;
  }

  .player-museum__metric strong {
    font-size: 31px;
  }
}

/* Final emphasis pass: surface player highlights immediately and keep colors on TT theme. */
.player-museum__hero-highlights {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  max-width: 760px;
  margin-top: 18px;
}

.player-museum__hero-highlights span {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  max-width: 100%;
  min-height: 38px;
  padding: 0 12px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.84);
  box-shadow: inset 0 0 0 1px rgba(21, 25, 35, 0.08), 0 10px 24px rgba(21, 25, 35, 0.06);
}

.player-museum__hero-highlights em {
  color: var(--tt-coral);
  font-size: 11px;
  font-style: normal;
  font-weight: 950;
  white-space: nowrap;
}

.player-museum__hero-highlights strong {
  overflow: hidden;
  max-width: 220px;
  color: var(--tt-ink);
  font-size: 13px;
  font-weight: 950;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.player-museum__profile-main {
  color: var(--tt-ink);
  background:
    radial-gradient(circle at 15% 12%, rgba(255, 208, 87, 0.36), transparent 28%),
    radial-gradient(circle at 92% 86%, rgba(255, 75, 89, 0.22), transparent 30%),
    linear-gradient(135deg, rgba(255, 255, 255, 0.96), rgba(238, 246, 255, 0.94));
  box-shadow: inset 0 0 0 1px rgba(21, 25, 35, 0.06), 0 22px 52px rgba(21, 25, 35, 0.1);
}

.player-museum__profile-name {
  color: var(--tt-ink);
}

.player-museum__profile-note {
  color: rgba(21, 25, 35, 0.68);
}

.player-museum__metric-grid {
  background: rgba(255, 255, 255, 0.92);
}

.player-museum__metric strong {
  color: var(--tt-coral);
}

.player-museum__metric:nth-child(2) strong {
  color: var(--tt-blue-deep);
}

.player-museum__metric:nth-child(3) strong,
.player-museum__metric:nth-child(4) strong {
  color: var(--tt-ink);
}

.player-museum__fact {
  background: rgba(255, 255, 255, 0.9);
}

@media (max-width: 767px) {
  .player-museum__hero-highlights {
    gap: 7px;
    margin-top: 15px;
  }

  .player-museum__hero-highlights span {
    min-height: 34px;
    padding: 0 10px;
  }

  .player-museum__hero-highlights strong {
    max-width: 190px;
    font-size: 12px;
  }
}

/* Star-player overview reset: remove busy chips; present a clean profile intro. */
.player-museum__hero-highlights {
  display: none !important;
}

.player-museum__profile-overview {
  gap: 12px;
}

.player-museum__profile-main {
  min-height: 0;
  padding: clamp(22px, 4vw, 34px);
  border-radius: 28px;
}

.player-museum__profile-name {
  margin-bottom: 14px;
}

.player-museum__profile-note {
  max-width: 44em;
}

.player-museum__profile-meta {
  width: fit-content;
  max-width: 100%;
  padding: 10px 14px;
  border-radius: 999px;
  color: rgba(21, 25, 35, 0.68);
  background: rgba(255, 255, 255, 0.88);
  box-shadow: inset 0 0 0 1px rgba(21, 25, 35, 0.08);
  font-size: 14px;
  font-weight: 900;
  line-height: 1.45;
}

.player-museum__fact-row,
.player-museum__fact {
  display: none !important;
}

@media (max-width: 767px) {
  .player-museum__profile-main {
    padding: 20px;
  }

  .player-museum__profile-meta {
    width: auto;
    border-radius: 18px;
  }
}

/* Section nav should feel like a lightweight table of contents, not form pills. */
.player-museum__quicknav {
  top: 46px;
  justify-content: flex-start;
  gap: 0;
  padding: 0 14px;
  background: rgba(255, 255, 255, 0.86);
  border-top: 1px solid rgba(21, 25, 35, 0.06);
  border-bottom: 1px solid rgba(21, 25, 35, 0.06);
  box-shadow: 0 10px 26px rgba(21, 25, 35, 0.04);
}

.player-museum__quicknav button {
  position: relative;
  min-width: auto;
  height: 48px;
  padding: 0 15px;
  border: 0;
  border-radius: 0;
  color: rgba(21, 25, 35, 0.62);
  background: transparent;
  box-shadow: none;
  font-size: 14px;
  font-weight: 950;
}

.player-museum__quicknav button::after {
  content: '';
  position: absolute;
  right: 14px;
  bottom: 0;
  left: 14px;
  height: 3px;
  border-radius: 999px 999px 0 0;
  background: transparent;
}

.player-museum__quicknav button:first-child {
  color: var(--tt-coral);
}

.player-museum__quicknav button:first-child::after {
  background: var(--tt-coral);
}

@media (max-width: 767px) {
  .player-museum__quicknav {
    display: none;
  }

  .player-museum__quicknav button {
    height: 44px;
    padding: 0 13px;
    font-size: 13px;
  }
}

/* Career snapshot: keep career signal without bringing back the heavy timeline. */
.player-museum__career {
  padding: clamp(18px, 4vw, 30px);
  border-radius: 30px;
  background:
    radial-gradient(circle at 10% 0%, rgba(93, 145, 245, 0.12), transparent 18rem),
    linear-gradient(135deg, rgba(255, 255, 255, 0.9), rgba(248, 251, 255, 0.82));
  box-shadow: 0 22px 58px rgba(21, 25, 35, 0.07);
}

.player-museum__career-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}

.player-museum__career-card {
  display: flex;
  min-height: 180px;
  flex-direction: column;
  justify-content: space-between;
  padding: 18px;
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.88);
  box-shadow: inset 0 0 0 1px rgba(21, 25, 35, 0.06);
}

.player-museum__career-card:nth-child(1) {
  background:
    radial-gradient(circle at 90% 0%, rgba(255, 208, 87, 0.26), transparent 40%),
    linear-gradient(135deg, #151923, #303746);
  color: #fff;
}

.player-museum__career-year {
  color: var(--tt-coral);
  font-size: 18px;
  font-weight: 950;
}

.player-museum__career-card:nth-child(1) .player-museum__career-year {
  color: #fff;
}

.player-museum__career-kind {
  width: fit-content;
  margin-top: 8px;
  padding: 4px 9px;
  border-radius: 999px;
  color: #fff;
  background: var(--tt-blue);
  font-size: 12px;
  font-weight: 950;
}

.player-museum__career-card h3 {
  margin: auto 0 8px;
  color: var(--tt-ink);
  font-size: 19px;
  line-height: 1.18;
}

.player-museum__career-card:nth-child(1) h3 {
  color: #fff;
}

.player-museum__career-card p {
  margin: 0;
  color: rgba(21, 25, 35, 0.58);
  font-size: 13px;
  line-height: 1.45;
}

.player-museum__career-card:nth-child(1) p {
  color: rgba(255, 255, 255, 0.68);
}

@media (max-width: 900px) {
  .player-museum__career-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 767px) {
  .player-museum__career {
    padding: 18px 16px;
    border-radius: 24px;
  }

  .player-museum__career-grid {
    display: flex;
    gap: 10px;
    margin: 0 -16px;
    padding: 0 16px 6px;
    overflow-x: auto;
    scroll-snap-type: x mandatory;
  }

  .player-museum__career-card {
    flex: 0 0 76%;
    min-height: 168px;
    scroll-snap-align: start;
  }
}

/* Profile after removing secondary stat grid. */
.player-museum__profile-overview {
  grid-template-columns: 1fr;
}

.player-museum__profile-main {
  min-height: 220px;
}

.player-museum__fact-row {
  padding-top: 0;
}

@media (min-width: 768px) {
  .player-museum__profile-main {
    min-height: 240px;
  }
}

@media (max-width: 767px) {
  .player-museum__profile-main {
    min-height: 170px;
  }
}

/* Clean archive timeline: compact sports history instead of flowchart boxes. */
#archive.player-museum__section {
  padding-top: 8px;
}

.player-museum__timeline {
  position: relative;
  display: grid;
  gap: 12px;
  margin: 0;
  padding: 0 0 0 26px;
  border-left: 0;
}

.player-museum__timeline::before {
  content: '';
  position: absolute;
  top: 4px;
  bottom: 4px;
  left: 6px;
  width: 2px;
  border-radius: 999px;
  background: linear-gradient(180deg, rgba(93, 145, 245, 0.88), rgba(255, 75, 89, 0.18));
}

.player-museum__timeline-item {
  position: relative;
  margin: 0;
  padding: 0;
}

.player-museum__timeline-item::before {
  left: -26px;
  top: 22px;
  width: 12px;
  height: 12px;
  border: 3px solid #fff;
  background: var(--tt-blue);
  box-shadow: 0 0 0 5px rgba(93, 145, 245, 0.14);
}

.player-museum__timeline-card {
  display: grid;
  grid-template-columns: 108px minmax(0, 1fr);
  gap: 16px;
  width: 100%;
  min-height: 86px;
  align-items: start;
  padding: 16px 18px;
  border: 0;
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.88);
  box-shadow: 0 14px 36px rgba(21, 25, 35, 0.07), inset 0 0 0 1px rgba(21, 25, 35, 0.06);
}

.player-museum__timeline-date {
  margin: 0;
  color: var(--tt-coral);
  font-size: 18px;
  font-weight: 950;
  line-height: 1.1;
}

.player-museum__timeline-body {
  min-width: 0;
}

.player-museum__timeline-kind {
  margin: 0 0 8px;
  padding: 4px 9px;
  color: #fff;
  background: var(--tt-blue);
  font-size: 12px;
  line-height: 1;
}

.player-museum__timeline-event {
  margin: 0 0 6px;
  color: var(--tt-ink);
  font-size: 18px;
  font-weight: 950;
  line-height: 1.25;
}

.player-museum__timeline-detail {
  color: rgba(21, 25, 35, 0.56);
  font-size: 14px;
  line-height: 1.45;
}

@media (max-width: 767px) {
  .player-museum__timeline {
    gap: 10px;
    padding-left: 18px;
  }

  .player-museum__timeline::before {
    left: 4px;
  }

  .player-museum__timeline-item::before {
    left: -19px;
    top: 18px;
    width: 10px;
    height: 10px;
    box-shadow: 0 0 0 4px rgba(93, 145, 245, 0.14);
  }

  .player-museum__timeline-card {
    grid-template-columns: 1fr;
    gap: 8px;
    min-height: 0;
    padding: 14px 15px;
    border-radius: 18px;
  }

  .player-museum__timeline-date {
    font-size: 16px;
  }

  .player-museum__timeline-kind {
    margin-bottom: 7px;
  }

  .player-museum__timeline-event {
    font-size: 17px;
  }

  .player-museum__timeline-detail {
    font-size: 13px;
  }
}

/* Final Chinese / warm TT red theme pass. */
.player-museum {
  --tt-ink: #171923;
  --tt-muted: #747783;
  --tt-coral: #d83f4b;
  --tt-coral-dark: #b92936;
  --tt-blue: #d83f4b;
  --tt-blue-deep: #b92936;
  --tt-yellow: #f6c96d;
  background:
    radial-gradient(circle at 8% 16%, rgba(216, 63, 75, 0.1), transparent 21rem),
    radial-gradient(circle at 88% 10%, rgba(216, 63, 75, 0.08), transparent 28rem),
    linear-gradient(180deg, #fffafa 0%, #fff6f3 46%, #ffffff 100%);
}

.player-museum__hero-mesh,
.player-museum__hero--has-cover .player-museum__hero-mesh {
  background:
    radial-gradient(circle at 15% 72%, rgba(216, 63, 75, 0.1), transparent 24%),
    radial-gradient(circle at 82% 24%, rgba(246, 201, 109, 0.16), transparent 28%),
    linear-gradient(135deg, rgba(255, 255, 255, 0.92) 0%, rgba(255, 247, 245, 0.88) 62%, rgba(255, 255, 255, 0.96) 100%);
}

.player-museum__hero-video-noise,
.player-museum__hero-scanline {
  display: none;
}

.player-museum__hero-glow--a {
  background: radial-gradient(circle, rgba(216, 63, 75, 0.16), transparent 70%);
}

.player-museum__hero-glow--b {
  background: radial-gradient(circle, rgba(246, 201, 109, 0.18), transparent 72%);
}

.player-museum__eyebrow,
.player-museum__hero--has-cover .player-museum__eyebrow,
.player-museum__panel-label,
.player-museum__section-kicker,
.player-museum__film-copy span,
.player-museum__career-year {
  color: var(--tt-coral);
}

.player-museum__eyebrow::before {
  background: var(--tt-coral);
}

.player-museum__title,
.player-museum__hero--has-cover .player-museum__title {
  text-shadow: 5px 5px 0 rgba(216, 63, 75, 0.08);
}

.player-museum__tagline,
.player-museum__hero--has-cover .player-museum__tagline {
  border-left-color: var(--tt-coral);
}

.player-museum__primary-action,
.player-museum__play-mark {
  background: linear-gradient(135deg, var(--tt-coral), var(--tt-coral-dark));
  box-shadow: 0 16px 34px rgba(216, 63, 75, 0.24);
}

.player-museum__hero-panel,
.player-museum__spotlight,
#profile.player-museum__section,
.player-museum__career,
.player-museum__business {
  background:
    radial-gradient(circle at 88% 10%, rgba(246, 201, 109, 0.16), transparent 16rem),
    rgba(255, 255, 255, 0.86);
}

.player-museum__profile-main,
.player-museum__spotlight-media,
.player-museum__film-fallback,
.player-museum__spotlight-fallback {
  background:
    radial-gradient(circle at 16% 16%, rgba(246, 201, 109, 0.22), transparent 30%),
    radial-gradient(circle at 92% 88%, rgba(216, 63, 75, 0.12), transparent 32%),
    linear-gradient(135deg, rgba(255, 255, 255, 0.96), rgba(255, 247, 245, 0.94));
}

.player-museum__career-card:nth-child(1) {
  background:
    radial-gradient(circle at 90% 0%, rgba(246, 201, 109, 0.22), transparent 40%),
    linear-gradient(135deg, #171923, #34313a);
}

.player-museum__career-kind,
.player-museum__timeline-kind {
  background: var(--tt-coral);
}

.player-museum__quicknav button:first-child,
.player-museum__text-action {
  color: var(--tt-coral);
}

.player-museum__quicknav button:first-child::after {
  background: var(--tt-coral);
}

/* Final palette correction: remove cheap red, use ink + warm gold accents. */
.player-museum {
  --tt-ink: #151923;
  --tt-muted: #747783;
  --tt-coral: #9b7a2f;
  --tt-coral-dark: #6f5520;
  --tt-blue: #24324a;
  --tt-blue-deep: #151923;
  --tt-yellow: #d9b45f;
  background:
    radial-gradient(circle at 8% 16%, rgba(217, 180, 95, 0.16), transparent 21rem),
    radial-gradient(circle at 88% 10%, rgba(36, 50, 74, 0.08), transparent 28rem),
    linear-gradient(180deg, #fffdf8 0%, #f7f4ec 46%, #ffffff 100%);
}

.player-museum__hero-mesh,
.player-museum__hero--has-cover .player-museum__hero-mesh {
  background:
    radial-gradient(circle at 16% 72%, rgba(217, 180, 95, 0.14), transparent 24%),
    radial-gradient(circle at 82% 24%, rgba(36, 50, 74, 0.08), transparent 28%),
    linear-gradient(135deg, rgba(255, 255, 255, 0.94) 0%, rgba(248, 245, 238, 0.9) 62%, rgba(255, 255, 255, 0.96) 100%);
}

.player-museum__hero::after {
  background: linear-gradient(90deg, transparent, rgba(217, 180, 95, 0.58), rgba(36, 50, 74, 0.24), transparent);
  box-shadow: 0 0 24px rgba(217, 180, 95, 0.18);
}

.player-museum__hero-glow--a {
  background: radial-gradient(circle, rgba(217, 180, 95, 0.14), transparent 70%);
}

.player-museum__hero-glow--b {
  background: radial-gradient(circle, rgba(36, 50, 74, 0.1), transparent 72%);
}

.player-museum__eyebrow,
.player-museum__hero--has-cover .player-museum__eyebrow,
.player-museum__panel-label,
.player-museum__section-kicker,
.player-museum__film-copy span,
.player-museum__career-year,
.player-museum__text-action,
.player-museum__quicknav button:first-child {
  color: var(--tt-coral);
}

.player-museum__eyebrow::before,
.player-museum__quicknav button:first-child::after {
  background: var(--tt-coral);
}

.player-museum__title,
.player-museum__hero--has-cover .player-museum__title {
  text-shadow: 5px 5px 0 rgba(217, 180, 95, 0.12);
}

.player-museum__tagline,
.player-museum__hero--has-cover .player-museum__tagline {
  border-left-color: var(--tt-coral);
}

.player-museum__primary-action,
.player-museum__play-mark {
  background: linear-gradient(135deg, #24324a, #151923);
  box-shadow: 0 16px 34px rgba(21, 25, 35, 0.22);
}

.player-museum__primary-action span {
  color: rgba(217, 180, 95, 0.92);
}

.player-museum__hero-panel,
.player-museum__spotlight,
#profile.player-museum__section,
.player-museum__career,
.player-museum__business {
  background:
    radial-gradient(circle at 88% 10%, rgba(217, 180, 95, 0.16), transparent 16rem),
    rgba(255, 255, 255, 0.88);
}

.player-museum__profile-main,
.player-museum__spotlight-media,
.player-museum__film-fallback,
.player-museum__spotlight-fallback {
  background:
    radial-gradient(circle at 16% 16%, rgba(217, 180, 95, 0.2), transparent 30%),
    radial-gradient(circle at 92% 88%, rgba(36, 50, 74, 0.07), transparent 32%),
    linear-gradient(135deg, rgba(255, 255, 255, 0.96), rgba(248, 245, 238, 0.94));
}

.player-museum__career-card:nth-child(1) {
  background:
    radial-gradient(circle at 90% 0%, rgba(217, 180, 95, 0.18), transparent 40%),
    linear-gradient(135deg, #151923, #293247);
}

.player-museum__career-kind,
.player-museum__timeline-kind {
  background: #24324a;
}

.player-museum__cover-rank {
  background: #151923;
}

.player-museum__cover-card i {
  background: var(--tt-yellow);
}

/* Official-theme reset: remove magazine clutter, red labels, heavy lines and frames. */
.player-museum {
  --tt-ink: #172033;
  --tt-muted: #6b7280;
  --tt-coral: #175cd3;
  --tt-coral-dark: #0f4bb5;
  --tt-blue: #175cd3;
  --tt-blue-deep: #0f4bb5;
  --tt-yellow: #f5c84c;
  background:
    radial-gradient(circle at 86% 10%, rgba(47, 125, 255, 0.12), transparent 28rem),
    radial-gradient(circle at 8% 20%, rgba(85, 210, 162, 0.08), transparent 22rem),
    linear-gradient(180deg, #f7fbff 0%, #ffffff 44%, #f7fbff 100%);
}

.player-museum__cover-mast {
  display: none;
}

.player-museum__hero {
  min-height: auto;
  padding-top: clamp(42px, 7vw, 82px);
  padding-bottom: clamp(42px, 7vw, 76px);
}

.player-museum__hero::before,
.player-museum__hero::after {
  display: none;
}

.player-museum__hero-mesh,
.player-museum__hero--has-cover .player-museum__hero-mesh {
  background:
    radial-gradient(circle at 82% 16%, rgba(47, 125, 255, 0.16), transparent 30%),
    radial-gradient(circle at 10% 86%, rgba(85, 210, 162, 0.1), transparent 24%),
    linear-gradient(135deg, #ffffff 0%, #edf6ff 58%, #ffffff 100%);
}

.player-museum__hero-glow {
  opacity: 0.34;
}

.player-museum__hero-inner,
.player-museum__hero-inner--no-cover {
  max-width: 1080px;
}

.player-museum__eyebrow,
.player-museum__hero--has-cover .player-museum__eyebrow {
  margin-bottom: 14px;
  color: var(--tt-blue);
  font-size: 13px;
  letter-spacing: 0;
}

.player-museum__eyebrow::before {
  width: 28px;
  height: 3px;
  background: var(--tt-blue);
}

.player-museum__title,
.player-museum__hero--has-cover .player-museum__title {
  color: var(--tt-ink);
  text-shadow: none;
  transform: none;
}

.player-museum__tagline,
.player-museum__hero--has-cover .player-museum__tagline {
  border-left: 0;
  color: rgba(23, 32, 51, 0.72);
  font-weight: 700;
}

.player-museum__primary-action,
.player-museum__play-mark {
  border-radius: 999px;
  background: linear-gradient(135deg, #175cd3, #2f7dff);
  box-shadow: 0 16px 34px rgba(23, 92, 211, 0.22);
}

.player-museum__primary-action span {
  color: rgba(255, 255, 255, 0.78);
}

.player-museum__hero-panel,
.player-museum__spotlight,
#profile.player-museum__section,
.player-museum__career,
.player-museum__business,
.player-museum__film-card,
.player-museum__card,
.player-museum__biz-card {
  border: 0;
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.9);
  box-shadow: 0 18px 44px rgba(23, 32, 51, 0.08);
}

.player-museum__hero-panel {
  max-width: 680px;
}

.player-museum__panel-label,
.player-museum__section-kicker {
  display: none;
}

.player-museum__section-head {
  display: block;
  margin-bottom: 18px;
  padding-top: 0;
  border-top: 0;
}

.player-museum__section-title {
  color: var(--tt-ink);
  font-size: clamp(28px, 5vw, 54px);
  line-height: 1.05;
}

.player-museum__profile-main,
.player-museum__spotlight-media,
.player-museum__film-fallback,
.player-museum__spotlight-fallback {
  background:
    radial-gradient(circle at 80% 20%, rgba(47, 125, 255, 0.12), transparent 30%),
    linear-gradient(135deg, #ffffff 0%, #f1f7ff 100%);
}

.player-museum__profile-meta {
  border-radius: 14px;
  background: #f7fbff;
}

.player-museum__career-card,
.player-museum__career-card:nth-child(1) {
  color: var(--tt-ink);
  background: rgba(255, 255, 255, 0.92);
  box-shadow: inset 0 0 0 1px rgba(23, 32, 51, 0.06);
}

.player-museum__career-card:nth-child(1) h3,
.player-museum__career-card:nth-child(1) p,
.player-museum__career-card:nth-child(1) .player-museum__career-year {
  color: inherit;
}

.player-museum__career-kind {
  background: rgba(23, 92, 211, 0.1);
  color: var(--tt-blue);
}

.player-museum__film-copy span,
.player-museum__career-year,
.player-museum__text-action {
  color: var(--tt-blue);
}

.player-museum__highlight-rail {
  gap: 14px;
}

.player-museum__business {
  display: none;
}

@media (max-width: 767px) {
  .player-museum__hero {
    padding-top: 36px;
    padding-bottom: 40px;
  }

  .player-museum__section-title {
    font-size: 30px;
  }

  .player-museum__business {
    display: none;
  }
}

/* Portal palette alignment: follow the existing home/news/events surfaces. */
.player-museum {
  --tt-ink: #151923;
  --tt-muted: #7b8190;
  --tt-coral: #ff4b59;
  --tt-coral-dark: #f03a45;
  --tt-blue: #175cd3;
  --tt-blue-deep: #0f4bb5;
  --tt-soft-blue: #5d91f5;
  --tt-green: #55d2a2;
  --tt-yellow: #ffd057;
  color: var(--tt-ink);
  background:
    radial-gradient(circle at 4% 10%, rgba(255, 75, 89, 0.04), transparent 22rem),
    radial-gradient(circle at 94% 8%, rgba(255, 208, 87, 0.18), transparent 24rem),
    linear-gradient(180deg, #ffffff 0%, #f7fbff 52%, #ffffff 100%);
}

.player-museum__hero-mesh,
.player-museum__hero--has-cover .player-museum__hero-mesh {
  background:
    radial-gradient(circle at 12% 22%, rgba(255, 75, 89, 0.06), transparent 28%),
    radial-gradient(circle at 76% 12%, rgba(255, 208, 87, 0.34), transparent 30%),
    radial-gradient(circle at 18% 86%, rgba(85, 210, 162, 0.12), transparent 28%),
    linear-gradient(135deg, #ffffff 0%, #fbfdff 58%, #fff8cf 100%);
}

.player-museum__hero-glow {
  opacity: 0.22;
  background:
    radial-gradient(circle at 68% 30%, rgba(93, 145, 245, 0.24), transparent 34%),
    radial-gradient(circle at 28% 76%, rgba(255, 208, 87, 0.16), transparent 28%);
}

.player-museum__film-copy span,
.player-museum__career-year,
.player-museum__text-action,
.player-museum__card-kicker,
.player-museum__timeline-date,
.player-museum__timeline-year,
.player-museum__card-arrow {
  color: var(--tt-coral);
}

.player-museum__eyebrow,
.player-museum__hero--has-cover .player-museum__eyebrow {
  display: none;
}

.player-museum__eyebrow::before {
  display: none;
}

.player-museum__primary-action,
.player-museum__play-mark {
  background: linear-gradient(135deg, var(--tt-coral), #ff7e84);
  box-shadow: 0 16px 34px rgba(255, 75, 89, 0.2);
}

.player-museum__identity,
.player-museum__hero--has-cover .player-museum__identity {
  margin: 10px 0 0;
  color: rgba(21, 25, 35, 0.6);
  font-size: clamp(15px, 2.6vw, 18px);
  font-weight: 900;
  line-height: 1.5;
}

.player-museum__hero-panel,
.player-museum__spotlight,
#profile.player-museum__section,
.player-museum__career,
.player-museum__film-card,
.player-museum__card,
.player-museum__biz-card {
  border: 1px solid rgba(47, 125, 255, 0.08);
  background: rgba(255, 255, 255, 0.88);
  box-shadow: 0 18px 46px rgba(21, 25, 35, 0.07);
}

.player-museum__hero-panel {
  background:
    radial-gradient(circle at 92% 0%, rgba(255, 208, 87, 0.2), transparent 38%),
    rgba(255, 255, 255, 0.88);
}

.player-museum__profile-main,
.player-museum__spotlight-media,
.player-museum__film-fallback,
.player-museum__spotlight-fallback {
  background:
    radial-gradient(circle at 34% 26%, rgba(255, 208, 87, 0.24), transparent 24%),
    radial-gradient(circle at 76% 70%, rgba(85, 210, 162, 0.13), transparent 30%),
    linear-gradient(135deg, rgba(47, 125, 255, 0.08), rgba(255, 255, 255, 0.94));
}

.player-museum__profile-meta {
  background: rgba(247, 251, 255, 0.86);
  box-shadow: inset 0 0 0 1px rgba(47, 125, 255, 0.07);
}

.player-museum__profile-label,
.player-museum__card-label,
.player-museum__next-label {
  color: rgba(21, 25, 35, 0.52);
}

.player-museum__career-card,
.player-museum__career-card:nth-child(1) {
  color: var(--tt-ink);
  border: 1px solid rgba(47, 125, 255, 0.08);
  background:
    radial-gradient(circle at 100% 0%, rgba(93, 145, 245, 0.1), transparent 34%),
    rgba(255, 255, 255, 0.92);
  box-shadow: 0 12px 30px rgba(21, 25, 35, 0.055);
}

.player-museum__career-kind {
  color: var(--tt-blue);
  background: rgba(47, 125, 255, 0.1);
}

.player-museum__career-card:nth-child(2) .player-museum__career-kind {
  color: #15825d;
  background: rgba(85, 210, 162, 0.16);
}

.player-museum__career-card:nth-child(3) .player-museum__career-kind {
  color: #9a6a00;
  background: rgba(255, 208, 87, 0.22);
}

.player-museum__section-title {
  color: var(--tt-ink);
  letter-spacing: 0;
}

.player-museum__section-subtitle,
.player-museum__profile-text,
.player-museum__career-card p,
.player-museum__film-copy p,
.player-museum__tagline,
.player-museum__hero--has-cover .player-museum__tagline {
  color: rgba(21, 25, 35, 0.68);
}

.player-museum__film-card::after,
.player-museum__spotlight::after,
.player-museum__career::after {
  background: rgba(47, 125, 255, 0.1);
}

@media (max-width: 767px) {
  .player-museum {
    background:
      radial-gradient(circle at 0% 8%, rgba(255, 75, 89, 0.035), transparent 16rem),
      radial-gradient(circle at 100% 4%, rgba(255, 208, 87, 0.2), transparent 18rem),
      linear-gradient(180deg, #ffffff 0%, #f7fbff 55%, #ffffff 100%);
  }

  .player-museum__hero-mesh,
  .player-museum__hero--has-cover .player-museum__hero-mesh {
    background:
      radial-gradient(circle at 8% 18%, rgba(255, 75, 89, 0.045), transparent 30%),
      radial-gradient(circle at 88% 12%, rgba(255, 208, 87, 0.26), transparent 34%),
      linear-gradient(135deg, #ffffff 0%, #fbfdff 62%, #fff8cf 100%);
  }

  .player-museum__hero-panel,
  .player-museum__spotlight,
  #profile.player-museum__section,
  .player-museum__career,
  .player-museum__film-card,
  .player-museum__card {
    border-radius: 20px;
    box-shadow: 0 12px 32px rgba(21, 25, 35, 0.065);
  }
}

/* Final tone pass: text uses ink opacity only, no colored type accents. */
.player-museum {
  --tt-coral: #151923;
  --tt-coral-dark: #151923;
  --tt-blue: #151923;
  --tt-blue-deep: #151923;
}

.player-museum__film-copy span,
.player-museum__career-year,
.player-museum__text-action,
.player-museum__card-kicker,
.player-museum__timeline-date,
.player-museum__timeline-year,
.player-museum__card-arrow,
.player-museum__career-kind,
.player-museum__career-card:nth-child(2) .player-museum__career-kind,
.player-museum__career-card:nth-child(3) .player-museum__career-kind,
.player-museum__profile-label,
.player-museum__card-label,
.player-museum__next-label {
  color: rgba(21, 25, 35, 0.52);
}

.player-museum__text-action {
  color: rgba(21, 25, 35, 0.78);
}

.player-museum__career-kind,
.player-museum__career-card:nth-child(2) .player-museum__career-kind,
.player-museum__career-card:nth-child(3) .player-museum__career-kind {
  background: rgba(21, 25, 35, 0.06);
}

.player-museum__primary-action,
.player-museum__play-mark {
  color: #ffffff;
  background: linear-gradient(135deg, #151923, #2b3344);
  box-shadow: 0 16px 34px rgba(21, 25, 35, 0.18);
}

.player-museum__primary-action span {
  color: rgba(255, 255, 255, 0.68);
}

.player-museum__cards {
  gap: 14px;
}

.player-museum__card,
.player-museum__card:nth-child(n),
.player-museum__card-wrap:nth-child(n) .player-museum__card {
  position: relative;
  overflow: hidden;
  border: 1px solid rgba(21, 25, 35, 0.055);
  background:
    radial-gradient(circle at 92% 8%, rgba(21, 25, 35, 0.035), transparent 34%),
    linear-gradient(145deg, rgba(255, 255, 255, 0.98) 0%, rgba(249, 250, 252, 0.94) 56%, rgba(255, 255, 255, 0.98) 100%);
  box-shadow:
    0 14px 34px rgba(21, 25, 35, 0.055),
    inset 0 1px 0 rgba(255, 255, 255, 0.9);
}

.player-museum__card::before,
.player-museum__card::after,
.player-museum__card-wrap::before,
.player-museum__card-wrap::after {
  display: none;
}

.player-museum__card:hover,
.player-museum__card:active {
  border-color: rgba(21, 25, 35, 0.09);
  background:
    radial-gradient(circle at 92% 8%, rgba(21, 25, 35, 0.045), transparent 34%),
    linear-gradient(145deg, #ffffff 0%, #f7f8fb 58%, #ffffff 100%);
  box-shadow:
    0 18px 40px rgba(21, 25, 35, 0.075),
    inset 0 1px 0 rgba(255, 255, 255, 0.94);
}

.player-museum__card-kicker {
  color: rgba(21, 25, 35, 0.48);
}

.player-museum__card-title {
  color: rgba(21, 25, 35, 0.94);
}

.player-museum__card-meta {
  color: rgba(21, 25, 35, 0.62);
}

.player-museum__career {
  overflow: hidden;
  padding: 24px 20px;
  border: 1px solid rgba(21, 25, 35, 0.055);
  border-radius: 24px;
  background:
    radial-gradient(circle at 100% 0%, rgba(21, 25, 35, 0.035), transparent 26rem),
    linear-gradient(145deg, rgba(255, 255, 255, 0.98) 0%, rgba(249, 250, 252, 0.94) 60%, rgba(255, 255, 255, 0.98) 100%);
  box-shadow: 0 16px 40px rgba(21, 25, 35, 0.055);
}

.player-museum__career .player-museum__section-head {
  margin-bottom: 16px;
}

.player-museum__career-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 0;
}

.player-museum__career-card,
.player-museum__career-card:nth-child(1),
.player-museum__career-card:nth-child(n) {
  display: grid;
  grid-template-columns: 72px minmax(0, 1fr);
  grid-template-areas:
    "year title"
    "kind detail";
  align-items: start;
  min-height: 0;
  padding: 15px 0;
  color: rgba(21, 25, 35, 0.94);
  border: 0;
  border-top: 1px solid rgba(21, 25, 35, 0.06);
  border-radius: 0;
  background: transparent;
  box-shadow: none;
}

.player-museum__career-card:first-child {
  border-top: 0;
  padding-top: 2px;
}

.player-museum__career-year,
.player-museum__career-card:nth-child(1) .player-museum__career-year {
  grid-area: year;
  color: rgba(21, 25, 35, 0.9);
  font-size: 20px;
  line-height: 1;
  font-weight: 950;
}

.player-museum__career-kind,
.player-museum__career-card:nth-child(2) .player-museum__career-kind,
.player-museum__career-card:nth-child(3) .player-museum__career-kind,
.player-museum__career-card:nth-child(1) .player-museum__career-kind {
  grid-area: kind;
  width: fit-content;
  margin-top: 9px;
  padding: 3px 8px;
  color: rgba(21, 25, 35, 0.5);
  background: rgba(21, 25, 35, 0.06);
  font-size: 12px;
  line-height: 1.35;
}

.player-museum__career-card h3,
.player-museum__career-card:nth-child(1) h3 {
  grid-area: title;
  margin: 0;
  color: rgba(21, 25, 35, 0.94);
  font-size: 18px;
  line-height: 1.28;
  font-weight: 950;
}

.player-museum__career-card p,
.player-museum__career-card:nth-child(1) p {
  grid-area: detail;
  margin: 6px 0 0;
  color: rgba(21, 25, 35, 0.62);
  font-size: 14px;
  line-height: 1.45;
}

@media (max-width: 767px) {
  .player-museum__career {
    padding: 22px 18px;
    border-radius: 22px;
  }

  .player-museum__career-card,
  .player-museum__career-card:nth-child(1),
  .player-museum__career-card:nth-child(n) {
    grid-template-columns: 58px minmax(0, 1fr);
    padding: 14px 0;
  }

  .player-museum__career-year,
  .player-museum__career-card:nth-child(1) .player-museum__career-year {
    font-size: 18px;
  }

  .player-museum__career-card h3,
  .player-museum__career-card:nth-child(1) h3 {
    font-size: 17px;
  }
}

.player-museum__career {
  position: relative;
  padding: 0;
  border: 0;
  border-radius: 28px;
  background: transparent;
  box-shadow: none;
}

.player-museum__career-swiper {
  overflow: hidden;
  border-radius: 28px;
  background:
    radial-gradient(circle at 80% 8%, rgba(255, 208, 87, 0.22), transparent 26%),
    radial-gradient(circle at 16% 92%, rgba(21, 25, 35, 0.06), transparent 32%),
    linear-gradient(145deg, rgba(255, 255, 255, 0.98) 0%, rgba(248, 250, 253, 0.94) 56%, rgba(255, 255, 255, 0.98) 100%);
  box-shadow:
    0 18px 46px rgba(21, 25, 35, 0.075),
    inset 0 0 0 1px rgba(21, 25, 35, 0.055);
}

.player-museum__career-track {
  display: flex;
  overflow-x: auto;
  scroll-snap-type: x mandatory;
  scroll-behavior: smooth;
  scrollbar-width: none;
}

.player-museum__career-track::-webkit-scrollbar {
  display: none;
}

.player-museum__career-card,
.player-museum__career-card:nth-child(1),
.player-museum__career-card:nth-child(n) {
  position: relative;
  display: flex;
  flex: 0 0 100%;
  min-height: clamp(280px, 38vw, 380px);
  flex-direction: column;
  justify-content: flex-end;
  padding: clamp(28px, 6vw, 54px);
  border: 0;
  background: transparent;
  box-shadow: none;
  scroll-snap-align: start;
}

.player-museum__career-card::before {
  content: "";
  position: absolute;
  top: clamp(22px, 5vw, 42px);
  right: clamp(22px, 6vw, 64px);
  width: clamp(120px, 28vw, 260px);
  aspect-ratio: 1;
  border-radius: 50%;
  background:
    radial-gradient(circle at 40% 34%, rgba(255, 255, 255, 0.92) 0 12%, transparent 13%),
    linear-gradient(145deg, rgba(21, 25, 35, 0.08), rgba(21, 25, 35, 0.02));
  opacity: 0.8;
}

.player-museum__career-card::after {
  content: "";
  position: absolute;
  right: clamp(22px, 6vw, 74px);
  bottom: clamp(24px, 5vw, 50px);
  width: clamp(150px, 36vw, 320px);
  height: 2px;
  border-radius: 999px;
  background: rgba(21, 25, 35, 0.12);
  transform: rotate(-8deg);
}

.player-museum__career-kind,
.player-museum__career-card:nth-child(1) .player-museum__career-kind,
.player-museum__career-card:nth-child(2) .player-museum__career-kind,
.player-museum__career-card:nth-child(3) .player-museum__career-kind {
  position: relative;
  width: fit-content;
  margin: 0 0 12px;
  padding: 5px 10px;
  color: rgba(21, 25, 35, 0.58);
  border-radius: 999px;
  background: rgba(21, 25, 35, 0.06);
  font-size: 12px;
  line-height: 1.2;
  font-weight: 950;
}

.player-museum__career-year,
.player-museum__career-card:nth-child(1) .player-museum__career-year {
  position: absolute;
  top: clamp(28px, 6vw, 54px);
  left: clamp(28px, 6vw, 54px);
  color: rgba(21, 25, 35, 0.22);
  font-size: clamp(52px, 14vw, 116px);
  line-height: 0.88;
  font-weight: 950;
  letter-spacing: 0;
}

.player-museum__career-card h2 {
  position: relative;
  max-width: min(680px, 78%);
  margin: 0;
  color: rgba(21, 25, 35, 0.96);
  font-size: clamp(30px, 8vw, 72px);
  line-height: 0.98;
  font-weight: 950;
  letter-spacing: 0;
}

.player-museum__career-card p,
.player-museum__career-card:nth-child(1) p {
  position: relative;
  max-width: min(560px, 82%);
  margin: 16px 0 0;
  color: rgba(21, 25, 35, 0.62);
  font-size: clamp(15px, 2.8vw, 20px);
  line-height: 1.45;
  font-weight: 800;
}

.player-museum__career-indicator {
  position: absolute;
  right: 22px;
  bottom: 20px;
  z-index: 3;
  display: flex;
  align-items: center;
  gap: 8px;
  color: rgba(21, 25, 35, 0.48);
  font-size: 12px;
  font-weight: 950;
}

.player-museum__career-indicator i {
  width: 34px;
  height: 2px;
  border-radius: 999px;
  background: rgba(21, 25, 35, 0.16);
}

@media (max-width: 767px) {
  .player-museum__career,
  .player-museum__career-swiper {
    border-radius: 24px;
  }

  .player-museum__career-card,
  .player-museum__career-card:nth-child(1),
  .player-museum__career-card:nth-child(n) {
    min-height: 300px;
    padding: 28px 24px 58px;
  }

  .player-museum__career-card h2 {
    max-width: 86%;
  }

  .player-museum__career-card p,
  .player-museum__career-card:nth-child(1) p {
    max-width: 88%;
  }
}
</style>

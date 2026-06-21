<template>
  <div class="portal-surface event-detail">
    <van-loading v-if="loading && !ev.id" class="portal-loading" vertical>加载中…</van-loading>
    <template v-else>
      <section class="portal-hero event-detail__hero">
        <div class="portal-hero__content">
          <h1 class="portal-hero__title">{{ ev.name || '赛事详情' }}</h1>
          <p class="portal-hero__text">{{ detailSummary }}</p>
          <div class="portal-meta-row">
            <span v-if="ev.status" class="portal-badge" :class="statusClass(ev.status)">{{ eventStatusText(ev.status) }}</span>
            <span v-if="ev.edition != null && ev.edition !== ''" class="portal-badge portal-badge--warm">第 {{ ev.edition }} 届</span>
            <span v-if="ev.location" class="portal-badge portal-badge--green">{{ ev.location }}</span>
          </div>
        </div>
      </section>

      <section class="portal-section event-overview">
        <div class="overview-card">
          <span>赛事地点</span>
          <strong>{{ ev.location || '待公布' }}</strong>
        </div>
      </section>

      <section class="portal-section">
        <div class="portal-section__head">
          <div>
            <h2 class="portal-section__title">赛程与成绩</h2>
            <p class="portal-section__meta">未赛显示时间与场地，已赛显示比分和状态。</p>
          </div>
        </div>

        <div v-if="!matches.length" class="portal-empty">暂无赛程，组委会发布后会同步显示。</div>
        <ol v-else class="match-list">
          <li v-for="m in matches" :key="m.id" class="match-card">
            <div class="match-card__time">
              <span>{{ datePart(m.scheduled_at) }}</span>
              <strong>{{ timePart(m.scheduled_at) }}</strong>
            </div>
            <div class="match-card__body">
              <h3>{{ matchTitle(m) }}</h3>
              <p>{{ matchMeta(m) }}</p>
              <div class="portal-meta-row">
                <span v-if="m.status" class="portal-badge" :class="matchStatusClass(m.status)">{{ matchStatusText(m.status) }}</span>
                <span v-if="scoreText(m.result)" class="portal-badge portal-badge--coral">比分 {{ scoreText(m.result) }}</span>
                <span v-if="m.table_no" class="portal-badge portal-badge--warm">{{ m.table_no }}</span>
              </div>
            </div>
          </li>
        </ol>
      </section>
    </template>
  </div>
</template>

<script>
import api from '../api.js';
import { eventStatusText, matchStatusText, scoreText } from '../../shared/statusLabels.js';

export default {
  name: 'EventDetailPage',
  props: { id: { type: [String, Number], required: true } },
  data() {
    return { loading: true, ev: {}, matches: [] };
  },
  computed: {
    detailSummary() {
      const parts = [];
      const start = this.ev.start_date || this.ev.startDate;
      const end = this.ev.end_date || this.ev.endDate;
      if (start && end && start !== end) parts.push(start + ' - ' + end);
      else if (start || end) parts.push(start || end);
      if (this.ev.location) parts.push(this.ev.location);
      return parts.join(' · ') || '赛事介绍、赛程和成绩将在这里集中呈现。';
    }
  },
  watch: { id: 'fetch' },
  mounted() {
    this.fetch();
  },
  methods: {
    eventStatusText,
    matchStatusText,
    scoreText,
    statusClass(status) {
      if (status === 'published') return 'portal-badge--green';
      if (status === 'archived') return 'portal-badge--coral';
      return 'portal-badge--warm';
    },
    matchStatusClass(status) {
      return status === 'finished' ? 'portal-badge--green' : 'portal-badge--warm';
    },
    datePart(value) {
      if (!value) return '日期待定';
      return String(value).slice(0, 10);
    },
    timePart(value) {
      if (!value) return '--:--';
      const m = String(value).match(/\d{2}:\d{2}/);
      return m ? m[0] : '时间待定';
    },
    matchTitle(m) {
      return m.round_label || m.stage_name || '赛程场次';
    },
    matchMeta(m) {
      const parts = [];
      if (m.stage_name && m.stage_name !== m.round_label) parts.push(m.stage_name);
      if (m.venue_name) parts.push(m.venue_name);
      if (m.table_no) parts.push(m.table_no);
      return parts.join(' · ') || '对阵信息待公布';
    },
    async fetch() {
      this.loading = true;
      try {
        const eid = this.id;
        const [eRes, mRes] = await Promise.all([
          api.get('/events/' + eid),
          api.get('/events/' + eid + '/matches')
        ]);
        this.ev = eRes.data || {};
        this.matches = (mRes.data && mRes.data.list) || [];
      } catch (e) {
        this.$message.error('加载失败');
      } finally {
        this.loading = false;
      }
    }
  }
};
</script>

<style scoped>
.event-detail__hero {
  background:
    radial-gradient(circle at 12% 22%, rgba(255, 75, 89, 0.16), transparent 26%),
    radial-gradient(circle at 90% 10%, rgba(47, 125, 255, 0.2), transparent 28%),
    linear-gradient(135deg, #ffffff 0%, #f1f8ff 62%, #fff8ee 100%);
}

.event-overview {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.overview-card {
  padding: 16px;
  border: 1px solid var(--portal-line);
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.86);
  box-shadow: 0 14px 34px rgba(21, 25, 35, 0.07);
}

.overview-card span {
  display: block;
  color: var(--portal-muted);
  font-size: 12px;
  font-weight: 800;
}

.overview-card strong {
  display: block;
  margin-top: 8px;
  color: var(--portal-ink);
  font-size: 20px;
  line-height: 1.25;
}

.match-list {
  display: grid;
  gap: 12px;
  margin: 0;
  padding: 0;
  list-style: none;
}

.match-card {
  display: grid;
  grid-template-columns: 92px minmax(0, 1fr);
  gap: 14px;
  padding: 14px;
  border: 1px solid var(--portal-line);
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.88);
  box-shadow: 0 14px 36px rgba(21, 25, 35, 0.07);
}

.match-card__time {
  display: flex;
  min-height: 86px;
  flex-direction: column;
  justify-content: center;
  padding: 10px;
  border-radius: 18px;
  color: #175cd3;
  background: rgba(47, 125, 255, 0.09);
  text-align: center;
}

.match-card__time span {
  font-size: 12px;
  font-weight: 800;
}

.match-card__time strong {
  margin-top: 6px;
  font-size: 18px;
  line-height: 1.2;
}

.match-card__body h3 {
  margin: 3px 0 6px;
  color: var(--portal-ink);
  font-size: 17px;
  line-height: 1.35;
}

.match-card__body p {
  margin: 0;
  color: var(--portal-muted);
  font-size: 14px;
  line-height: 1.6;
}

@media (max-width: 520px) {
  .event-overview {
    grid-template-columns: 1fr;
  }

  .match-card {
    grid-template-columns: 1fr;
  }

  .match-card__time {
    min-height: auto;
    align-items: center;
    flex-direction: row;
    gap: 10px;
  }
}
</style>

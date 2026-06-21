<template>
  <div class="portal-surface events-page">
    <section class="portal-hero events-hero">
      <div class="portal-hero__content">
        <h1 class="portal-hero__title">赛事赛果</h1>
        <p class="portal-hero__text">
          浏览 TT 青春赛场公开赛事，快速查看赛程、状态与成绩入口。
        </p>
        <div class="portal-hero__actions">
          <a class="portal-action portal-action--primary" href="#event-list">查看赛事</a>
          <router-link class="portal-action" to="/news">阅读战报</router-link>
        </div>
      </div>
    </section>

    <section id="event-list" class="portal-section">
      <div class="portal-section__head">
        <div>
          <h2 class="portal-section__title">赛事资料库</h2>
          <p class="portal-section__meta">共 {{ total || list.length }} 项公开赛事</p>
        </div>
      </div>

      <van-loading v-if="loading && !list.length" class="portal-loading" vertical>加载中…</van-loading>
      <template v-else>
        <div v-if="!list.length" class="portal-empty">暂无赛事，新的赛程正在整理中。</div>
        <div v-else class="portal-card-grid events-grid">
          <router-link
            v-for="(item, index) in list"
            :key="item.id"
            class="portal-card event-card"
            :to="'/events/' + item.id"
          >
            <span class="event-card__number">{{ numberText(index) }}</span>
            <div class="portal-card__body">
              <div class="event-card__topline">
                <span class="portal-badge" :class="statusClass(item.status)">{{ eventStatusText(item.status) }}</span>
                <span v-if="item.edition != null && item.edition !== ''" class="portal-badge portal-badge--warm">第 {{ item.edition }} 届</span>
              </div>
              <h3 class="portal-card__title">{{ item.name || '未命名赛事' }}</h3>
              <p class="portal-card__desc">{{ eventSummary(item) }}</p>
              <div class="portal-meta-row">
                <span v-if="dateRange(item)" class="portal-badge">{{ dateRange(item) }}</span>
                <span v-if="item.location" class="portal-badge portal-badge--green">{{ item.location }}</span>
              </div>
              <span class="event-card__link">进入赛事详情</span>
            </div>
          </router-link>
        </div>

        <div v-if="total > pageSize" class="portal-pager">
          <van-pagination
            v-model="page"
            :total-items="total"
            :items-per-page="pageSize"
            force-ellipses
            @change="onPageChange"
          />
        </div>
      </template>
    </section>
  </div>
</template>

<script>
import api from '../api.js';
import { eventStatusText } from '../../shared/statusLabels.js';

export default {
  name: 'EventsPage',
  data() {
    return { loading: false, list: [], total: 0, page: 1, pageSize: 20 };
  },
  mounted() {
    this.load(1);
  },
  methods: {
    eventStatusText,
    numberText(index) {
      const n = (this.page - 1) * this.pageSize + index + 1;
      return String(n).padStart(2, '0');
    },
    statusClass(status) {
      if (status === 'published') return 'portal-badge--green';
      if (status === 'archived') return 'portal-badge--coral';
      return 'portal-badge--warm';
    },
    dateRange(item) {
      const start = item.start_date || item.startDate;
      const end = item.end_date || item.endDate;
      if (start && end && start !== end) return start + ' - ' + end;
      return start || end || '';
    },
    eventSummary(item) {
      const parts = [];
      if (this.dateRange(item)) parts.push(this.dateRange(item));
      if (item.location) parts.push(item.location);
      if (item.status) parts.push(eventStatusText(item.status));
      return parts.join(' · ') || '赛程、对阵与成绩将在详情页同步更新。';
    },
    onPageChange(p) {
      this.load(p);
    },
    async load(p) {
      this.page = p || this.page;
      this.loading = true;
      try {
        const { data } = await api.get('/events', { params: { page: this.page, pageSize: this.pageSize } });
        this.list = data.list || [];
        this.total = data.total || 0;
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
.events-page {
  padding-bottom: 18px;
}

.events-hero {
  background:
    radial-gradient(circle at 14% 20%, rgba(255, 208, 87, 0.42), transparent 26%),
    radial-gradient(circle at 78% 18%, rgba(85, 210, 162, 0.2), transparent 30%),
    linear-gradient(135deg, #ffffff 0%, #edf6ff 54%, #fff3f2 100%);
}

.events-grid {
  align-items: stretch;
}

.event-card {
  min-height: 210px;
  transition: transform 180ms ease, box-shadow 180ms ease;
}

.event-card:active {
  transform: translateY(2px);
}

.event-card::before {
  content: "";
  position: absolute;
  inset: auto -36px -64px auto;
  width: 152px;
  height: 152px;
  border-radius: 999px;
  background: rgba(47, 125, 255, 0.1);
}

.event-card__number {
  position: absolute;
  top: 16px;
  right: 18px;
  color: rgba(21, 25, 35, 0.08);
  font-size: 52px;
  line-height: 1;
  font-weight: 900;
}

.event-card__topline {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding-right: 72px;
}

.event-card .portal-card__title {
  margin-top: 18px;
  padding-right: 30px;
}

.event-card__link {
  display: inline-flex;
  margin-top: 18px;
  color: #175cd3;
  font-size: 13px;
  font-weight: 900;
}

@media (hover: hover) {
  .event-card:hover {
    transform: translateY(-3px);
    box-shadow: 0 24px 58px rgba(21, 25, 35, 0.12);
  }
}
</style>

<template>
  <div class="portal-page portal-page--detail">
    <van-loading v-if="loading && !ev.id" class="portal-page__loading" vertical>加载中…</van-loading>
    <template v-else>
      <h1 class="portal-page__headline">{{ ev.name || '赛事详情' }}</h1>
      <van-cell-group inset class="portal-page__group">
        <van-cell v-if="ev.edition != null && ev.edition !== ''" title="届次" :value="String(ev.edition)" />
        <van-cell v-if="ev.location" title="地点" :value="ev.location" />
        <van-cell v-if="ev.status" title="状态" :value="ev.status" />
      </van-cell-group>
      <h2 class="portal-page__subtitle">赛程</h2>
      <van-cell-group v-if="matches.length" inset class="portal-page__group">
        <van-cell
          v-for="m in matches"
          :key="m.id"
          :title="m.scheduled_at || '时间待定'"
          :label="matchLabel(m)"
        />
      </van-cell-group>
      <van-empty v-else description="暂无赛程" />
    </template>
  </div>
</template>

<script>
import api from '../api.js';

export default {
  name: 'EventDetailPage',
  props: { id: { type: [String, Number], required: true } },
  data() {
    return { loading: true, ev: {}, matches: [] };
  },
  watch: { id: 'fetch' },
  mounted() {
    this.fetch();
  },
  methods: {
    matchLabel(m) {
      const parts = [];
      if (m.round_label) parts.push(m.round_label);
      if (m.status) parts.push(m.status);
      return parts.join(' · ') || '—';
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
.portal-page--detail {
  padding-top: 0;
}

.portal-page__headline {
  margin: 0 0 12px;
  font-size: 22px;
  font-weight: 600;
  line-height: 1.35;
  color: #323233;
}

.portal-page__subtitle {
  margin: 20px 0 10px;
  font-size: 16px;
  font-weight: 600;
  color: #646566;
}

.portal-page__loading {
  padding: 48px 0;
  text-align: center;
}

.portal-page__group {
  margin-bottom: 8px;
}
</style>

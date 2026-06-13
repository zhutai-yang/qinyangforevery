<template>
  <div class="portal-page">
    <h1 class="portal-page__title">赛事列表</h1>
    <van-loading v-if="loading && !list.length" class="portal-page__loading" vertical>加载中…</van-loading>
    <template v-else>
      <van-empty v-if="!list.length" description="暂无赛事" />
      <van-cell-group v-else inset class="portal-page__group">
        <van-cell
          v-for="item in list"
          :key="item.id"
          :title="item.name"
          :label="cellLabel(item)"
          is-link
          :to="'/events/' + item.id"
        />
      </van-cell-group>
      <div v-if="total > pageSize" class="portal-page__pager">
        <van-pagination
          v-model="page"
          :total-items="total"
          :items-per-page="pageSize"
          force-ellipses
          @change="onPageChange"
        />
      </div>
    </template>
  </div>
</template>

<script>
import api from '../api.js';

export default {
  name: 'EventsPage',
  data() {
    return { loading: false, list: [], total: 0, page: 1, pageSize: 20 };
  },
  mounted() {
    this.load(1);
  },
  methods: {
    cellLabel(item) {
      const parts = [];
      if (item.edition != null && item.edition !== '') parts.push('届次：' + item.edition);
      if (item.status) parts.push('状态：' + item.status);
      return parts.join(' · ') || '查看详情';
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
.portal-page__title {
  margin: 0 0 12px;
  font-size: 20px;
  font-weight: 600;
  color: #323233;
}

.portal-page__loading {
  padding: 48px 0;
  text-align: center;
}

.portal-page__group {
  margin-bottom: 8px;
}

.portal-page__pager {
  display: flex;
  justify-content: center;
  padding: 16px 0 8px;
  overflow-x: auto;
  -webkit-overflow-scrolling: touch;
}
</style>

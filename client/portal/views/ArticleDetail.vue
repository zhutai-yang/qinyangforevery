<template>
  <div class="article portal-page--detail">
    <van-loading v-if="loading && !art.title" class="portal-page__loading" vertical>加载中…</van-loading>
    <template v-else>
      <h1 class="article__title">{{ art.title }}</h1>
      <p v-if="art.published_at" class="article__meta">{{ art.published_at }}</p>
      <div class="article__body" v-html="art.body"></div>
    </template>
  </div>
</template>

<script>
import api from '../api.js';

export default {
  name: 'ArticleDetailPage',
  props: { slugOrId: { type: String, required: true } },
  data() {
    return { loading: true, art: {} };
  },
  watch: { slugOrId: 'fetch' },
  mounted() {
    this.fetch();
  },
  methods: {
    async fetch() {
      this.loading = true;
      try {
        const { data } = await api.get('/articles/' + encodeURIComponent(this.slugOrId));
        this.art = data || {};
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
.portal-page__loading {
  padding: 48px 0;
  text-align: center;
}

.article__title {
  margin: 0 0 8px;
  font-size: 22px;
  font-weight: 600;
  line-height: 1.35;
  color: #323233;
}

.article__meta {
  margin: 0 0 16px;
  color: #969799;
  font-size: 14px;
}

.article__body {
  line-height: 1.7;
  font-size: 16px;
  color: #323233;
  word-break: break-word;
}

.article__body >>> img {
  max-width: 100%;
  height: auto;
}

.article__body >>> pre {
  overflow-x: auto;
  -webkit-overflow-scrolling: touch;
}
</style>

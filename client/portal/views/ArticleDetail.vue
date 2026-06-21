<template>
  <div class="portal-surface article-detail">
    <van-loading v-if="loading && !art.title" class="portal-loading" vertical>加载中…</van-loading>
    <template v-else>
      <article class="article-shell">
        <header class="article-hero">
          <div v-if="art.cover_url || art.cover" class="article-hero__cover">
            <img :src="art.cover_url || art.cover" alt="" />
          </div>
          <div class="article-hero__content">
            <span class="portal-badge portal-badge--coral">TT 资讯</span>
            <h1>{{ art.title || '资讯详情' }}</h1>
            <p v-if="art.summary">{{ art.summary }}</p>
            <div class="portal-meta-row">
              <span v-if="art.published_at" class="portal-badge">{{ art.published_at }}</span>
              <span v-if="art.author" class="portal-badge portal-badge--green">{{ art.author }}</span>
            </div>
          </div>
        </header>

        <div v-if="art.body" class="article__body" v-html="art.body"></div>
        <div v-else class="portal-empty">正文内容正在整理中。</div>
      </article>
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
.article-shell {
  max-width: 820px;
  margin: 0 auto;
}

.article-hero {
  overflow: hidden;
  margin: -2px 0 18px;
  border: 1px solid var(--portal-line);
  border-radius: 28px;
  background:
    radial-gradient(circle at 8% 12%, rgba(255, 208, 87, 0.24), transparent 28%),
    rgba(255, 255, 255, 0.9);
  box-shadow: var(--portal-shadow);
}

.article-hero__cover {
  height: clamp(190px, 38vw, 340px);
  background: linear-gradient(135deg, rgba(47, 125, 255, 0.12), rgba(255, 75, 89, 0.12));
}

.article-hero__cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.article-hero__content {
  padding: 22px;
}

.article-hero h1 {
  margin: 14px 0 0;
  color: var(--portal-ink);
  font-size: clamp(28px, 7vw, 46px);
  line-height: 1.12;
  font-weight: 900;
}

.article-hero p {
  max-width: 680px;
  margin: 12px 0 0;
  color: var(--portal-muted);
  font-size: 15px;
  line-height: 1.75;
}

.article__body {
  padding: 22px;
  border: 1px solid var(--portal-line);
  border-radius: 28px;
  background: rgba(255, 255, 255, 0.9);
  box-shadow: 0 14px 38px rgba(21, 25, 35, 0.07);
  color: #293241;
  font-size: 16px;
  line-height: 1.85;
  word-break: break-word;
}

.article__body >>> h1,
.article__body >>> h2,
.article__body >>> h3 {
  color: var(--portal-ink);
  line-height: 1.25;
}

.article__body >>> a {
  color: #175cd3;
  font-weight: 800;
}

.article__body >>> img {
  max-width: 100%;
  height: auto;
  border-radius: 18px;
}

.article__body >>> pre {
  overflow-x: auto;
  padding: 14px;
  border-radius: 16px;
  background: #101828;
  color: #f7fbff;
  -webkit-overflow-scrolling: touch;
}

@media (max-width: 520px) {
  .article-hero__content,
  .article__body {
    padding: 18px;
  }
}
</style>

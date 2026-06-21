<template>
  <div class="portal-surface news-page">
    <section class="portal-hero news-hero">
      <div class="portal-hero__content">
        <h1 class="portal-hero__title">新闻资讯</h1>
        <p class="portal-hero__text">
          追踪 TT 青春赛场的战报、训练故事和外部乒乓动态。
        </p>
      </div>
    </section>

    <van-loading v-if="loading && !list.length" class="portal-loading" vertical>加载中…</van-loading>
    <template v-else>
      <section class="portal-section">
        <div class="portal-section__head">
          <div>
            <h2 class="portal-section__title">本站文章</h2>
            <p class="portal-section__meta">官方战报、选手故事和训练内容</p>
          </div>
        </div>

        <div v-if="!list.length" class="portal-empty">暂无文章，新的内容正在整理中。</div>
        <template v-else>
          <router-link class="portal-card lead-article" :to="articleTo(leadArticle)">
            <span class="lead-article__visual">
              <img v-if="leadArticle.cover_url || leadArticle.cover" :src="leadArticle.cover_url || leadArticle.cover" alt="" />
            </span>
            <span class="lead-article__content">
              <span class="portal-badge portal-badge--coral">头条</span>
              <strong>{{ leadArticle.title }}</strong>
              <em>{{ leadArticle.summary || '从赛场到训练馆，记录每一个值得回看的瞬间。' }}</em>
              <span class="lead-article__meta">{{ leadArticle.published_at || '发布时间待定' }}</span>
            </span>
          </router-link>

          <div class="portal-card-grid article-grid">
            <router-link
              v-for="(item, index) in restArticles"
              :key="item.id"
              class="portal-card article-card"
              :to="articleTo(item)"
            >
              <span class="article-card__cover">
                <img v-if="item.cover_url || item.cover" :src="item.cover_url || item.cover" alt="" />
                <span v-else>{{ articleInitial(item.title) }}</span>
              </span>
              <span class="portal-card__body">
                <span class="portal-badge" :class="articleBadgeClass(index)">{{ articleTag(index) }}</span>
                <strong class="portal-card__title">{{ item.title }}</strong>
                <span class="portal-card__desc">{{ item.summary || item.published_at || '点击查看完整内容' }}</span>
              </span>
            </router-link>
          </div>
        </template>

        <div v-if="total > pageSize" class="portal-pager">
          <van-pagination
            v-model="page"
            :total-items="total"
            :items-per-page="pageSize"
            force-ellipses
            @change="onPageChange"
          />
        </div>
      </section>

      <section class="portal-section ext-section">
        <div class="portal-section__head">
          <div>
            <h2 class="portal-section__title">外部动态</h2>
            <p class="portal-section__meta">来自外部数据源的乒乓资讯摘要</p>
          </div>
        </div>

        <van-loading v-if="extLoading" class="portal-loading ext-loading" vertical>同步外部动态…</van-loading>
        <div v-else-if="!extItems.length" class="portal-empty">
          {{ extDegraded ? '外部动态暂时不可用，本站内容不受影响。' : '暂无外部动态。' }}
        </div>
        <div v-else class="ext-list">
          <a
            v-for="item in extItems"
            :key="item.id || item.external_url || item.title"
            class="ext-item"
            :href="item.external_url || item.url"
            target="_blank"
            rel="noopener noreferrer"
          >
            <span>
              <strong>{{ item.title || '外部动态' }}</strong>
              <em>{{ extMeta(item) }}</em>
            </span>
            <b>打开</b>
          </a>
        </div>
      </section>
    </template>
  </div>
</template>

<script>
import api from '../api.js';

export default {
  name: 'NewsPage',
  data() {
    return {
      loading: false,
      list: [],
      total: 0,
      page: 1,
      pageSize: 20,
      extLoading: false,
      extItems: [],
      extDegraded: false
    };
  },
  computed: {
    leadArticle() {
      return this.list[0] || {};
    },
    restArticles() {
      return this.list.slice(1);
    }
  },
  mounted() {
    this.load(1);
    this.loadExternalFeed();
  },
  methods: {
    articleTo(item) {
      if (!item || !item.id) return '/news';
      return '/news/' + (item.slug || item.id);
    },
    articleInitial(title) {
      return String(title || 'TT').slice(0, 2).toUpperCase();
    },
    articleTag(index) {
      return ['赛事战报', '训练课堂', '选手故事', '城市赛场'][index % 4];
    },
    articleBadgeClass(index) {
      return ['portal-badge--green', 'portal-badge--warm', '', 'portal-badge--coral'][index % 4];
    },
    extMeta(item) {
      const parts = [];
      if (item.source_name) parts.push(item.source_name);
      if (item.published_at) parts.push(item.published_at);
      return parts.join(' · ') || '外部来源';
    },
    onPageChange(p) {
      this.load(p);
    },
    async load(p) {
      this.page = p || this.page;
      this.loading = true;
      try {
        const { data } = await api.get('/articles', { params: { page: this.page, pageSize: this.pageSize } });
        this.list = data.list || [];
        this.total = data.total || 0;
      } catch (e) {
        this.$message.error('加载失败');
      } finally {
        this.loading = false;
      }
    },
    async loadExternalFeed() {
      this.extLoading = true;
      try {
        const { data } = await api.get('/ext-feed', { params: { page: 1, pageSize: 6 } });
        this.extItems = data.list || [];
        this.extDegraded = !!data.degraded;
      } catch (e) {
        this.extItems = [];
        this.extDegraded = true;
      } finally {
        this.extLoading = false;
      }
    }
  }
};
</script>

<style scoped>
.news-hero {
  background:
    radial-gradient(circle at 16% 24%, rgba(255, 75, 89, 0.13), transparent 26%),
    radial-gradient(circle at 78% 0%, rgba(255, 208, 87, 0.36), transparent 26%),
    linear-gradient(135deg, #ffffff 0%, #f0f7ff 58%, #fff7ed 100%);
}

.lead-article {
  display: grid;
  grid-template-columns: minmax(0, 0.95fr) minmax(0, 1.05fr);
  min-height: 260px;
  margin-bottom: 14px;
}

.lead-article__visual {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 220px;
  background:
    radial-gradient(circle at 35% 35%, rgba(255, 208, 87, 0.36), transparent 22%),
    linear-gradient(135deg, rgba(47, 125, 255, 0.14), rgba(255, 75, 89, 0.12));
}

.lead-article__visual img,
.article-card__cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.lead-article__content {
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 24px;
}

.lead-article__content strong {
  margin-top: 14px;
  color: var(--portal-ink);
  font-size: clamp(24px, 5vw, 36px);
  line-height: 1.14;
  font-weight: 900;
}

.lead-article__content em {
  margin-top: 12px;
  color: var(--portal-muted);
  font-size: 15px;
  font-style: normal;
  line-height: 1.7;
}

.lead-article__meta {
  margin-top: 18px;
  color: #175cd3;
  font-size: 13px;
  font-weight: 900;
}

.article-grid {
  grid-template-columns: 1fr;
}

.article-card {
  display: grid;
  grid-template-columns: 112px minmax(0, 1fr);
  min-height: 132px;
}

.article-card__cover {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 132px;
  color: rgba(23, 92, 211, 0.7);
  background: linear-gradient(135deg, rgba(47, 125, 255, 0.12), rgba(85, 210, 162, 0.16));
  font-size: 24px;
  font-weight: 900;
}

.article-card .portal-card__body {
  padding: 16px;
}

.article-card .portal-card__title {
  margin-top: 10px;
  font-size: 16px;
}

.ext-loading {
  padding: 28px 0;
}

.ext-list {
  display: grid;
  gap: 10px;
}

.ext-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 16px 18px;
  border: 1px solid var(--portal-line);
  border-radius: 22px;
  color: inherit;
  background: rgba(255, 255, 255, 0.84);
  text-decoration: none;
  box-shadow: 0 12px 30px rgba(21, 25, 35, 0.06);
}

.ext-item strong,
.ext-item em {
  display: block;
}

.ext-item strong {
  color: var(--portal-ink);
  font-size: 15px;
  line-height: 1.45;
}

.ext-item em {
  margin-top: 5px;
  color: var(--portal-muted);
  font-size: 12px;
  font-style: normal;
}

.ext-item b {
  flex: 0 0 auto;
  color: #175cd3;
  font-size: 13px;
}

@media (max-width: 640px) {
  .lead-article,
  .article-card {
    grid-template-columns: 1fr;
  }

  .lead-article__visual {
    min-height: 170px;
  }

  .article-card__cover {
    min-height: 148px;
  }
}
</style>

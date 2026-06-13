<template>
  <div id="app" class="portal-root">
    <van-nav-bar
      v-if="showNavBar"
      :title="navTitle"
      left-arrow
      fixed
      placeholder
      safe-area-inset-top
      @click-left="goBack"
    />
    <main
      class="main"
      :class="{
        'main--home': $route.path === '/',
        'main--padded': $route.path !== '/',
        'main--tabbar': showTabbar
      }"
    >
      <router-view />
    </main>
    <van-tabbar v-if="showTabbar" route fixed placeholder safe-area-inset-bottom>
      <van-tabbar-item replace to="/" icon="home-o">首页</van-tabbar-item>
      <van-tabbar-item replace to="/events" icon="medal-o">赛事</van-tabbar-item>
      <van-tabbar-item replace to="/news" icon="notes-o">资讯</van-tabbar-item>
    </van-tabbar>
  </div>
</template>

<script>
export default {
  name: 'PortalApp',
  computed: {
    showNavBar() {
      return !!(this.$route.meta && this.$route.meta.navTitle);
    },
    navTitle() {
      return (this.$route.meta && this.$route.meta.navTitle) || '';
    },
    showTabbar() {
      if (this.$route.meta && this.$route.meta.hideTabbar) return false;
      const p = this.$route.path;
      return p === '/' || p === '/events' || p === '/news';
    }
  },
  methods: {
    goBack() {
      if (window.history.length > 1) {
        this.$router.back();
      } else {
        this.$router.push('/');
      }
    }
  }
};
</script>

<style>
html {
  scroll-behavior: smooth;
}

body {
  margin: 0;
  -webkit-tap-highlight-color: transparent;
}

.portal-root {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Helvetica Neue', Arial, sans-serif;
  min-height: 100vh;
  min-height: 100dvh;
  color: #1d1d1f;
  background: #f7f8fa;
}

.main {
  min-height: 200px;
  box-sizing: border-box;
}

.main--home {
  background: transparent;
}

.main--padded {
  padding: 12px 12px 16px;
  max-width: 960px;
  margin: 0 auto;
  width: 100%;
  box-sizing: border-box;
}

@media (min-width: 768px) {
  .main--padded {
    padding: 20px 24px 24px;
  }
}
</style>

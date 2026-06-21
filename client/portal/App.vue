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
        'main--padded': $route.path !== '/'
      }"
    >
      <router-view />
    </main>
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
  color: #172033;
  background:
    radial-gradient(circle at 0% 12%, rgba(255, 75, 89, 0.06), transparent 28%),
    radial-gradient(circle at 100% 0%, rgba(47, 125, 255, 0.08), transparent 30%),
    #f7fbff;
  --van-tabbar-item-active-color: #175cd3;
  --van-tabbar-background-color: rgba(255, 255, 255, 0.92);
  --van-nav-bar-icon-color: #175cd3;
  --van-nav-bar-text-color: #175cd3;
  --van-nav-bar-title-text-color: #172033;
}

.main {
  min-height: 200px;
  box-sizing: border-box;
}

.main--home {
  background: transparent;
}

.portal-root .van-tabbar {
  border-top: 1px solid rgba(47, 125, 255, 0.1);
  box-shadow: 0 -12px 30px rgba(23, 32, 51, 0.06);
  backdrop-filter: blur(18px);
  -webkit-backdrop-filter: blur(18px);
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

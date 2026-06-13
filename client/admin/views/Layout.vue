<template>
  <el-container class="layout" :class="{ 'layout--mobile': isMobile }">
    <el-aside
      :width="asideWidth"
      class="aside"
      :class="{
        'aside--mobile-open': mobileAsideOpen,
        'aside--collapsed': asideCollapsed && !isMobile
      }"
    >
      <div class="logo" :class="{ 'logo--collapsed': asideCollapsed && !isMobile }">
        <i class="el-icon-s-platform logo__mark" />
        <span v-show="!asideCollapsed" class="logo__text">ttf_admin</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        :collapse="asideCollapsed && !isMobile"
        :collapse-transition="true"
        router
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409eff"
        @select="onMenuSelect"
      >
        <el-menu-item index="/dashboard" :route="{ path: '/dashboard' }">
          <i class="el-icon-s-home" />
          <span slot="title">工作台</span>
        </el-menu-item>

        <el-submenu index="group-ops">
          <template slot="title">
            <i class="el-icon-s-flag" />
            <span>赛事运营</span>
          </template>
          <el-menu-item index="/events" :route="{ path: '/events' }">
            <span slot="title">赛事</span>
          </el-menu-item>
          <el-menu-item index="/venues" :route="{ path: '/venues' }">
            <span slot="title">场馆</span>
          </el-menu-item>
          <el-menu-item index="/matches" :route="{ path: '/matches' }">
            <span slot="title">赛程</span>
          </el-menu-item>
          <el-menu-item index="/athletes" :route="{ path: '/athletes' }">
            <span slot="title">运动员</span>
          </el-menu-item>
        </el-submenu>

        <el-submenu index="group-content">
          <template slot="title">
            <i class="el-icon-document" />
            <span>内容运营</span>
          </template>
          <el-menu-item index="/articles" :route="{ path: '/articles' }">
            <span slot="title">文章</span>
          </el-menu-item>
          <el-menu-item index="/featured" :route="{ path: '/featured' }">
            <span slot="title">关注球员</span>
          </el-menu-item>
          <el-menu-item index="/home-config" :route="{ path: '/home-config' }">
            <span slot="title">首页配置</span>
          </el-menu-item>
          <el-menu-item index="/image-library" :route="{ path: '/image-library' }">
            <span slot="title">公用图片库</span>
          </el-menu-item>
        </el-submenu>

        <el-submenu index="group-sys">
          <template slot="title">
            <i class="el-icon-setting" />
            <span>系统</span>
          </template>
          <el-menu-item index="/ext" :route="{ path: '/ext' }">
            <span slot="title">外部数据源</span>
          </el-menu-item>
          <el-menu-item index="/dict-audit" :route="{ path: '/dict-audit' }">
            <span slot="title">字典与审计</span>
          </el-menu-item>
        </el-submenu>
      </el-menu>
    </el-aside>
    <div v-if="isMobile && mobileAsideOpen" class="mobile-mask" @click="closeMobileAside" />
    <el-container direction="vertical" class="main-wrap">
      <el-header height="auto" class="header">
        <div class="header__left">
          <el-button
            type="text"
            class="header__collapse"
            :icon="isMobile ? 'el-icon-s-operation' : (asideCollapsed ? 'el-icon-s-unfold' : 'el-icon-s-fold')"
            @click="toggleAside"
          />
          <el-breadcrumb separator-class="el-icon-arrow-right">
            <el-breadcrumb-item :to="{ path: '/dashboard' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="pageTitle">{{ pageTitle }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header__right">
          <el-dropdown trigger="click" @command="onUserCommand">
            <span class="header__user">
              <i class="el-icon-user-solid" />
              <span class="header__user-label">管理员</span>
              <i class="el-icon-arrow-down el-icon--right" />
            </span>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </div>
      </el-header>
      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script>
export default {
  name: 'AdminLayout',
  data() {
    return {
      asideCollapsed: false,
      isMobile: false,
      mobileAsideOpen: false
    };
  },
  computed: {
    asideWidth() {
      return this.asideCollapsed ? '64px' : '220px';
    },
    activeMenu() {
      return this.$route.path;
    },
    pageTitle() {
      const t = this.$route.meta && this.$route.meta.title;
      return t && this.$route.path !== '/dashboard' ? t : '';
    }
  },
  methods: {
    checkViewport() {
      this.isMobile = window.innerWidth < 768;
      if (!this.isMobile) this.mobileAsideOpen = false;
    },
    toggleAside() {
      if (this.isMobile) {
        this.mobileAsideOpen = !this.mobileAsideOpen;
      } else {
        this.asideCollapsed = !this.asideCollapsed;
      }
    },
    closeMobileAside() {
      this.mobileAsideOpen = false;
    },
    onMenuSelect() {
      if (this.isMobile) this.closeMobileAside();
    },
    onUserCommand(cmd) {
      if (cmd === 'logout') this.logout();
    },
    logout() {
      localStorage.removeItem('tt_token');
      this.$router.push('/login');
    }
  },
  watch: {
    '$route.path'() {
      if (this.isMobile) this.closeMobileAside();
    }
  },
  mounted() {
    this.checkViewport();
    window.addEventListener('resize', this.checkViewport);
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.checkViewport);
  }
};
</script>

<style scoped>
.layout {
  flex: 1;
  min-height: 0;
  width: 100%;
  overflow: hidden;
}

.aside {
  flex-shrink: 0;
  align-self: stretch;
  display: flex;
  flex-direction: column;
  min-height: 100%;
  background-color: #304156;
  overflow: hidden;
  transition: width 0.22s ease;
}

.aside >>> .el-menu {
  flex: 1;
  min-height: 0;
  border-right: none;
}

.main-wrap {
  flex: 1;
  min-width: 0;
  min-height: 0;
  overflow: hidden;
}

.logo {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px 16px;
  color: #fff;
  background: #263445;
  font-weight: 600;
  font-size: 15px;
  overflow: hidden;
}

.logo--collapsed {
  justify-content: center;
  padding: 14px 8px;
}

.logo__mark {
  font-size: 22px;
  color: #409eff;
  flex-shrink: 0;
}

.logo__text {
  white-space: nowrap;
}

.header {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 12px;
  padding: 10px 16px;
  min-height: 48px;
  background: #fff;
  border-bottom: 1px solid #ebeef5;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.06);
}

.header__left {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
  flex: 1;
}

.header__collapse {
  font-size: 18px;
  padding: 6px;
  color: #606266;
}

.header__collapse:hover {
  color: #409eff;
}

.header__left >>> .el-breadcrumb {
  font-size: 13px;
  line-height: 1;
}

.header__right {
  flex-shrink: 0;
}

.header__user {
  display: inline-flex;
  align-items: center;
  cursor: pointer;
  color: #606266;
  font-size: 14px;
  padding: 4px 8px;
  border-radius: 4px;
  transition: background 0.2s;
}

.header__user:hover {
  background: #f5f7fa;
  color: #303133;
}

.header__user-label {
  margin: 0 4px;
}

.main-content {
  flex: 1;
  min-height: 0;
  overflow: auto;
  padding: 16px;
  background: #f0f2f5;
}

.mobile-mask {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.42);
  z-index: 1100;
}

@media (max-width: 767px) {
  .layout {
    position: relative;
  }

  .aside {
    position: fixed;
    top: 0;
    bottom: 0;
    left: 0;
    width: 220px !important;
    z-index: 1200;
    transform: translateX(-100%);
    transition: transform 0.22s ease;
    box-shadow: 6px 0 20px rgba(0, 0, 0, 0.2);
  }

  .aside.aside--mobile-open {
    transform: translateX(0);
  }

  .main-wrap {
    width: 100%;
  }

  .header {
    padding: 10px 12px;
  }

  .header__left >>> .el-breadcrumb {
    display: none;
  }

  .header__user-label {
    display: none;
  }

  .main-content {
    padding: 12px;
  }
}
</style>

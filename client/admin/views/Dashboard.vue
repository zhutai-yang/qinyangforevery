<template>
  <div class="admin-page dashboard">
    <h1 class="admin-page__title">工作台</h1>
    <p class="admin-page__desc">快速进入各业务模块，管理赛事、内容与系统配置。</p>

    <el-row :gutter="16" class="dashboard__shortcuts">
      <el-col v-for="item in shortcuts" :key="item.path" :xs="24" :sm="12" :md="8" :lg="6">
        <div class="dashboard__card" role="button" tabindex="0" @click="go(item.path)" @keyup.enter="go(item.path)">
          <div class="dashboard__card-icon" :class="'dashboard__card-icon--' + item.tone">
            <i :class="item.icon" />
          </div>
          <div class="dashboard__card-body">
            <h3 class="dashboard__card-title">{{ item.title }}</h3>
            <p class="dashboard__card-desc">{{ item.desc }}</p>
          </div>
          <i class="el-icon-arrow-right dashboard__card-arrow" />
        </div>
      </el-col>
    </el-row>

    <el-card shadow="never" class="admin-page__card dashboard__tips">
      <div slot="header" class="dashboard__tips-header">
        <span>使用说明</span>
      </div>
      <ul class="dashboard__tips-list">
        <li><strong>赛事运营</strong>：先维护「赛事」「场馆」，再在「赛程」下按赛事筛选场次。</li>
        <li><strong>内容运营</strong>：文章、关注球员与门户首页区块可独立配置。</li>
        <li><strong>系统</strong>：外部数据源用于抓取或同步；字典与审计用于枚举与操作留痕。</li>
      </ul>
    </el-card>
  </div>
</template>

<script>
export default {
  name: 'AdminDashboard',
  data() {
    return {
      shortcuts: [
        { path: '/events', title: '赛事', desc: '赛事与届次、发布状态', icon: 'el-icon-trophy', tone: 'gold' },
        { path: '/venues', title: '场馆', desc: '比赛场馆档案', icon: 'el-icon-location-outline', tone: 'blue' },
        { path: '/matches', title: '赛程', desc: '场次与轮次', icon: 'el-icon-date', tone: 'teal' },
        { path: '/athletes', title: '运动员', desc: '人员档案', icon: 'el-icon-user', tone: 'purple' },
        { path: '/articles', title: '文章', desc: '资讯与公告', icon: 'el-icon-document', tone: 'indigo' },
        { path: '/featured', title: '关注球员', desc: '门户展示顺序', icon: 'el-icon-star-on', tone: 'amber' },
        { path: '/home-config', title: '首页配置', desc: '门户区块开关', icon: 'el-icon-menu', tone: 'gray' },
        { path: '/ext', title: '外部数据源', desc: '抓取与同步任务', icon: 'el-icon-link', tone: 'cyan' },
        { path: '/dict-audit', title: '字典与审计', desc: '枚举与日志', icon: 'el-icon-setting', tone: 'slate' }
      ]
    };
  },
  methods: {
    go(path) {
      this.$router.push(path);
    }
  }
};
</script>

<style scoped>
.dashboard__shortcuts {
  margin-bottom: 16px;
}

.dashboard__card {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
  padding: 16px;
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  cursor: pointer;
  transition: box-shadow 0.2s ease, border-color 0.2s ease, transform 0.15s ease;
}

.dashboard__card:hover {
  border-color: #c6e2ff;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  transform: translateY(-1px);
}

.dashboard__card:focus {
  outline: none;
  border-color: #409eff;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
}

.dashboard__card-icon {
  flex-shrink: 0;
  width: 44px;
  height: 44px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  color: #fff;
}

.dashboard__card-icon--gold {
  background: linear-gradient(135deg, #f6d365 0%, #fda085 100%);
}
.dashboard__card-icon--blue {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}
.dashboard__card-icon--teal {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}
.dashboard__card-icon--purple {
  background: linear-gradient(135deg, #a18cd1 0%, #fbc2eb 100%);
}
.dashboard__card-icon--indigo {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}
.dashboard__card-icon--amber {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}
.dashboard__card-icon--gray {
  background: linear-gradient(135deg, #89f7fe 0%, #66a6ff 100%);
}
.dashboard__card-icon--cyan {
  background: linear-gradient(135deg, #00c9ff 0%, #92fe9d 100%);
}
.dashboard__card-icon--slate {
  background: linear-gradient(135deg, #868f96 0%, #596164 100%);
}

.dashboard__card-body {
  flex: 1;
  min-width: 0;
}

.dashboard__card-title {
  margin: 0 0 4px;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.dashboard__card-desc {
  margin: 0;
  font-size: 12px;
  color: #909399;
  line-height: 1.4;
}

.dashboard__card-arrow {
  flex-shrink: 0;
  color: #c0c4cc;
  font-size: 14px;
}

.dashboard__tips >>> .el-card__header {
  padding: 12px 16px;
  font-weight: 600;
  color: #303133;
}

.dashboard__tips-list {
  margin: 0;
  padding-left: 18px;
  color: #606266;
  font-size: 13px;
  line-height: 1.8;
}

.dashboard__tips-list strong {
  color: #303133;
}
</style>

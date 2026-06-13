import Vue from 'vue';
import VueRouter from 'vue-router';
import ElementUI from 'element-ui';
import 'element-ui/lib/theme-chalk/index.css';
import './styles/admin-common.css';
import App from './App.vue';
import Login from './views/Login.vue';
import Layout from './views/Layout.vue';
import Dashboard from './views/Dashboard.vue';
import EventsAdmin from './views/EventsAdmin.vue';
import VenuesAdmin from './views/VenuesAdmin.vue';
import MatchesAdmin from './views/MatchesAdmin.vue';
import AthletesAdmin from './views/AthletesAdmin.vue';
import ArticlesAdmin from './views/ArticlesAdmin.vue';
import FeaturedAdmin from './views/FeaturedAdmin.vue';
import HomeConfigAdmin from './views/HomeConfigAdmin.vue';
import ExtAdmin from './views/ExtAdmin.vue';
import DictAudit from './views/DictAudit.vue';
import AthleteCareerAdmin from './views/AthleteCareerAdmin.vue';
import ImageLibraryAdmin from './views/ImageLibraryAdmin.vue';

Vue.use(VueRouter);
Vue.use(ElementUI);

const router = new VueRouter({
  mode: 'history',
  base: '/admin/',
  routes: [
    { path: '/login', component: Login },
    {
      path: '/',
      component: Layout,
      meta: { requiresAuth: true },
      children: [
        { path: '', redirect: 'dashboard' },
        { path: 'dashboard', component: Dashboard, meta: { title: '工作台' } },
        { path: 'events', component: EventsAdmin, meta: { title: '赛事管理' } },
        { path: 'venues', component: VenuesAdmin, meta: { title: '场馆管理' } },
        { path: 'matches', component: MatchesAdmin, meta: { title: '赛程管理' } },
        { path: 'athletes', component: AthletesAdmin, meta: { title: '运动员' } },
        { path: 'athletes/:id/career', component: AthleteCareerAdmin, props: true, meta: { title: '球员职业生涯' } },
        { path: 'articles', component: ArticlesAdmin, meta: { title: '文章管理' } },
        { path: 'featured', component: FeaturedAdmin, meta: { title: '关注球员' } },
        { path: 'home-config', component: HomeConfigAdmin, meta: { title: '首页配置' } },
        { path: 'ext', component: ExtAdmin, meta: { title: '外部数据源' } },
        { path: 'image-library', component: ImageLibraryAdmin, meta: { title: '图片库管理' } },
        { path: 'dict-audit', component: DictAudit, meta: { title: '字典与审计' } }
      ]
    }
  ]
});

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('tt_token');
  if (to.path === '/login') {
    next();
    return;
  }
  if (to.matched.some((r) => r.meta.requiresAuth) && !token) {
    next('/login');
    return;
  }
  next();
});

new Vue({
  router,
  render: (h) => h(App)
}).$mount('#app');

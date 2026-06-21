import Vue from 'vue';
import VueRouter from 'vue-router';
import Vant from 'vant';
import 'vant/lib/index.css';
import './styles/portal-common.css';
import { Toast } from 'vant';
import App from './App.vue';
import Home from './views/Home.vue';
import Events from './views/Events.vue';
import EventDetail from './views/EventDetail.vue';
import News from './views/News.vue';
import ArticleDetail from './views/ArticleDetail.vue';
import Player from './views/Player.vue';

Vue.use(VueRouter);
Vue.use(Vant);

// 兼容原 Element 写法（官网页面临时提示）
Vue.prototype.$message = {
  error(msg) {
    Toast.fail(msg || '操作失败');
  },
  success(msg) {
    Toast.success(msg || '成功');
  }
};

const router = new VueRouter({
  mode: 'history',
  base: '/',
  routes: [
    { path: '/', component: Home, meta: { tab: 'home' } },
    { path: '/events', component: Events, meta: { tab: 'events' } },
    {
      path: '/events/:id',
      component: EventDetail,
      props: true,
      meta: { navTitle: '赛事详情', hideTabbar: true }
    },
    { path: '/news', component: News, meta: { tab: 'news' } },
    {
      path: '/news/:slugOrId',
      component: ArticleDetail,
      props: true,
      meta: { navTitle: '资讯详情', hideTabbar: true }
    },
    {
      path: '/players/:id',
      component: Player,
      props: true,
      meta: { navTitle: '球员展厅', hideTabbar: true }
    }
  ],
  scrollBehavior() {
    return { x: 0, y: 0 };
  }
});

new Vue({
  router,
  render: (h) => h(App)
}).$mount('#app');

import Vue from 'vue';
import VueRouter from 'vue-router';
import Aside from 'element-ui/lib/aside';
import Breadcrumb from 'element-ui/lib/breadcrumb';
import BreadcrumbItem from 'element-ui/lib/breadcrumb-item';
import Button from 'element-ui/lib/button';
import Card from 'element-ui/lib/card';
import Col from 'element-ui/lib/col';
import Container from 'element-ui/lib/container';
import DatePicker from 'element-ui/lib/date-picker';
import Dialog from 'element-ui/lib/dialog';
import Dropdown from 'element-ui/lib/dropdown';
import DropdownItem from 'element-ui/lib/dropdown-item';
import DropdownMenu from 'element-ui/lib/dropdown-menu';
import Form from 'element-ui/lib/form';
import FormItem from 'element-ui/lib/form-item';
import Header from 'element-ui/lib/header';
import Input from 'element-ui/lib/input';
import InputNumber from 'element-ui/lib/input-number';
import Loading from 'element-ui/lib/loading';
import Main from 'element-ui/lib/main';
import Menu from 'element-ui/lib/menu';
import MenuItem from 'element-ui/lib/menu-item';
import Message from 'element-ui/lib/message';
import MessageBox from 'element-ui/lib/message-box';
import Option from 'element-ui/lib/option';
import Pagination from 'element-ui/lib/pagination';
import Row from 'element-ui/lib/row';
import Select from 'element-ui/lib/select';
import Submenu from 'element-ui/lib/submenu';
import Switch from 'element-ui/lib/switch';
import TabPane from 'element-ui/lib/tab-pane';
import Table from 'element-ui/lib/table';
import TableColumn from 'element-ui/lib/table-column';
import Tabs from 'element-ui/lib/tabs';
import Upload from 'element-ui/lib/upload';
import 'element-ui/lib/theme-chalk/index.css';
import './styles/admin-common.css';
import App from './App.vue';
import Layout from './views/Layout.vue';

const Login = () => import('./views/Login.vue');
const Dashboard = () => import('./views/Dashboard.vue');
const EventsAdmin = () => import('./views/EventsAdmin.vue');
const VenuesAdmin = () => import('./views/VenuesAdmin.vue');
const MatchesAdmin = () => import('./views/MatchesAdmin.vue');
const AthletesAdmin = () => import('./views/AthletesAdmin.vue');
const ArticlesAdmin = () => import('./views/ArticlesAdmin.vue');
const FeaturedAdmin = () => import('./views/FeaturedAdmin.vue');
const HomeConfigAdmin = () => import('./views/HomeConfigAdmin.vue');
const ExtAdmin = () => import('./views/ExtAdmin.vue');
const DictAudit = () => import('./views/DictAudit.vue');
const AthleteCareerAdmin = () => import('./views/AthleteCareerAdmin.vue');
const ImageLibraryAdmin = () => import('./views/ImageLibraryAdmin.vue');

Vue.use(VueRouter);
[
  Aside,
  Breadcrumb,
  BreadcrumbItem,
  Button,
  Card,
  Col,
  Container,
  DatePicker,
  Dialog,
  Dropdown,
  DropdownItem,
  DropdownMenu,
  Form,
  FormItem,
  Header,
  Input,
  InputNumber,
  Main,
  Menu,
  MenuItem,
  Option,
  Pagination,
  Row,
  Select,
  Submenu,
  Switch,
  TabPane,
  Table,
  TableColumn,
  Tabs,
  Upload
].forEach((component) => Vue.use(component));
Vue.use(Loading.directive);
Vue.prototype.$loading = Loading.service;
Vue.prototype.$message = Message;
Vue.prototype.$msgbox = MessageBox;
Vue.prototype.$alert = MessageBox.alert;
Vue.prototype.$confirm = MessageBox.confirm;
Vue.prototype.$prompt = MessageBox.prompt;

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
        { path: 'athletes', component: AthletesAdmin, meta: { title: '明星选手' } },
        { path: 'athletes/:id/career', component: AthleteCareerAdmin, props: true, meta: { title: '明星内容运营' } },
        { path: 'articles', component: ArticlesAdmin, meta: { title: '内容中心' } },
        { path: 'featured', component: FeaturedAdmin, meta: { title: '首页明星' } },
        { path: 'home-config', component: HomeConfigAdmin, meta: { title: '首页编排' } },
        { path: 'ext', component: ExtAdmin, meta: { title: '外部动态' } },
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

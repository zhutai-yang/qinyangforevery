<template>
  <div class="admin-page">
    <h1 class="admin-page__title">运动员</h1>
    <p class="admin-page__desc">维护运动员档案，供赛程、关注列表与门户引用。</p>
    <el-card shadow="never" class="admin-page__card">
      <div class="admin-toolbar">
        <el-button type="primary" icon="el-icon-plus" @click="openEdit()">新建运动员</el-button>
      </div>
      <el-table v-loading="loading" :data="list" stripe border size="small">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="name" label="姓名" />
      <el-table-column prop="gender" label="性别" width="80" />
      <el-table-column prop="association" label="协会" />
      <el-table-column label="操作" width="160">
        <template slot-scope="scope">
          <el-button type="text" @click="openEdit(scope.row)">编辑</el-button>
          <el-button type="text" @click="goCareer(scope.row)">职业生涯</el-button>
          <el-button type="text" @click="remove(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-dialog :title="form.id ? '编辑' : '新建'" :visible.sync="dialog" width="480px">
      <el-form label-width="100px">
        <el-form-item label="姓名"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="性别">
          <el-select v-model="form.gender" placeholder="选择性别" clearable>
            <el-option label="男" value="男" />
            <el-option label="女" value="女" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="出生日期">
          <el-date-picker
            v-model="form.birth_date"
            type="date"
            placeholder="选择出生日期"
            value-format="yyyy-MM-dd"
            format="yyyy-MM-dd"
            :editable="false"
            clearable
          />
        </el-form-item>
        <el-form-item label="协会（国家）">
          <el-select
            v-model="form.association"
            placeholder="选择国家"
            filterable
            clearable
          >
            <el-option
              v-for="c in countries"
              :key="c.iso2"
              :label="c.cn + ' / ' + c.en"
              :value="c.cn + ' / ' + c.en"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="dialog = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </span>
    </el-dialog>
    </el-card>
  </div>
</template>

<script>
import api from '../api.js';
import i18nIsoCountries from 'i18n-iso-countries';
import enLocale from 'i18n-iso-countries/langs/en.json';
import zhLocale from 'i18n-iso-countries/langs/zh.json';

export default {
  name: 'AthletesAdmin',
  data() {
    return {
      loading: false,
      list: [],
      dialog: false,
      form: {},

      countries: []
    };
  },
  async mounted() {
    await this.initCountries();
    await this.load();
  },
  methods: {
    async initCountries() {
      // 预加载所有国家，支持同时展示中文/英文名称，并用同一格式回写到 association
      i18nIsoCountries.registerLocale(enLocale);
      i18nIsoCountries.registerLocale(zhLocale);

      const cnMap = i18nIsoCountries.getNames('zh');
      const enMap = i18nIsoCountries.getNames('en');

      this.countries = Object.keys(cnMap)
        .map((iso2) => ({
          iso2,
          cn: cnMap[iso2],
          en: enMap[iso2]
        }))
        .filter((c) => c.cn && c.en)
        .sort((a, b) => a.cn.localeCompare(b.cn, 'zh-CN'));
    },

    normalizeAssociationValue(raw) {
      if (raw == null) return '';
      const s = String(raw).trim();
      if (!s) return '';
      // 新格式：`中文 / English`
      if (s.includes(' / ')) return s;

      // 兼容旧数据：可能只存了中文或英文
      const hitCn = (this.countries || []).find((c) => c.cn === s);
      if (hitCn) return hitCn.cn + ' / ' + hitCn.en;
      const hitEn = (this.countries || []).find((c) => c.en === s);
      if (hitEn) return hitEn.cn + ' / ' + hitEn.en;

      // 找不到就保留原样（避免旧数据丢失）
      return s;
    },

    async load() {
      this.loading = true;
      try {
        const { data } = await api.get('/athletes', { params: { page: 1, pageSize: 200 } });
        this.list = data.list || [];
      } catch (e) {
        this.$message.error('加载失败');
      } finally {
        this.loading = false;
      }
    },
    goCareer(row) {
      if (!row || !row.id) return;
      this.$router.push('/athletes/' + row.id + '/career');
    },
    openEdit(row) {
      this.form = row
        ? { ...row, association: this.normalizeAssociationValue(row.association) }
        : {
            name: '',
            gender: null,
            birth_date: null,
            association: ''
          };
      this.dialog = true;
    },
    async save() {
      try {
        if (this.form.id) {
          await api.put('/athletes/' + this.form.id, this.form);
        } else {
          await api.post('/athletes', this.form);
        }
        this.$message.success('已保存');
        this.dialog = false;
        this.load();
      } catch (e) {
        this.$message.error('保存失败');
      }
    },
    async remove(row) {
      try {
        await this.$confirm('确认删除？');
        await api.delete('/athletes/' + row.id);
        this.load();
      } catch (e) {
        if (e !== 'cancel') this.$message.error('删除失败');
      }
    }
  }
};
</script>


<template>
  <div class="admin-page">
    <h1 class="admin-page__title">明星选手</h1>
    <p class="admin-page__desc">维护选手档案、官网展示资料与内容运营入口。</p>
    <el-card shadow="never" class="admin-page__card">
      <div class="admin-toolbar">
        <el-button type="primary" icon="el-icon-plus" @click="openEdit()">新建选手</el-button>
        <el-button icon="el-icon-upload2" @click="openQuickImport">快捷导入/更新</el-button>
      </div>
      <el-table v-loading="loading" :data="list" stripe border size="small">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="name" label="姓名" />
      <el-table-column prop="name_en" label="英文名" min-width="130" show-overflow-tooltip />
      <el-table-column prop="profile_title" label="展示定位" min-width="160" show-overflow-tooltip />
      <el-table-column prop="gender" label="性别" width="80" />
      <el-table-column prop="current_world_rank" label="世界排名" width="90" />
      <el-table-column prop="association" label="协会" />
      <el-table-column label="操作" width="160">
        <template slot-scope="scope">
          <el-button type="text" @click="openEdit(scope.row)">编辑</el-button>
          <el-button type="text" @click="goCareer(scope.row)">内容运营</el-button>
          <el-button type="text" @click="remove(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-dialog title="快捷导入/更新运动员" :visible.sync="quickImportDialog" width="760px">
      <div class="quick-import">
        <p class="quick-import__hint">粘贴 <code>athlete-data-import</code> 输出的 JSON。系统会按 ID 或“姓名+协会”匹配，找不到则新建。</p>
        <el-input
          v-model="quickImportText"
          type="textarea"
          :rows="14"
          placeholder='{"athlete":{"name":"王楚钦"},"achievements":[],"results":[],"upcoming_events":[],"sources":[]}'
        />
        <div v-if="quickImportPreview" class="quick-import__preview">
          <strong>{{ quickImportPreview.name || '未命名运动员' }}</strong>
          <span v-if="quickImportPreview.name_en"> / {{ quickImportPreview.name_en }}</span>
          <span> · 荣誉 {{ quickImportPreview.achievements }} 条</span>
          <span> · 成绩 {{ quickImportPreview.results }} 条</span>
          <span> · 预告 {{ quickImportPreview.upcoming }} 条</span>
          <span> · 来源 {{ quickImportPreview.sources }} 条</span>
        </div>
      </div>
      <span slot="footer">
        <el-button @click="quickImportDialog = false">取消</el-button>
        <el-button @click="previewQuickImport">预览</el-button>
        <el-button type="primary" :loading="quickImportSaving" @click="submitQuickImport">导入/更新</el-button>
      </span>
    </el-dialog>
    <el-dialog :title="form.id ? '编辑' : '新建'" :visible.sync="dialog" width="720px">
      <el-form label-width="100px">
        <el-form-item label="姓名"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="英文名"><el-input v-model="form.name_en" /></el-form-item>
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
        <el-form-item label="出生地"><el-input v-model="form.birth_place" /></el-form-item>
        <el-form-item label="国籍"><el-input v-model="form.nationality" placeholder="例如：中国" /></el-form-item>
        <el-form-item label="身高(cm)"><el-input-number v-model="form.height_cm" :min="0" :max="260" /></el-form-item>
        <el-form-item label="惯用手"><el-input v-model="form.dominant_hand" placeholder="左手/右手" /></el-form-item>
        <el-form-item label="打法"><el-input v-model="form.playing_style" placeholder="例如：左手横拍" /></el-form-item>
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
        <el-form-item label="展示定位">
          <el-input v-model="form.profile_title" placeholder="例如：国乒新生代核心" />
        </el-form-item>
        <el-form-item label="展示摘要">
          <el-input v-model="form.profile_summary" type="textarea" rows="3" placeholder="用于官网球员页首屏展示" />
        </el-form-item>
        <el-form-item label="世界排名"><el-input-number v-model="form.current_world_rank" :min="0" :max="9999" /></el-form-item>
        <el-form-item label="最高排名"><el-input-number v-model="form.highest_world_rank" :min="0" :max="9999" /></el-form-item>
        <el-form-item label="排名积分"><el-input-number v-model="form.ranking_points" :min="0" :max="999999" /></el-form-item>
        <el-form-item label="主要身份">
          <el-input v-model="form.major_identity" placeholder="例如：奥运冠军、世乒赛冠军" />
        </el-form-item>
        <el-form-item label="头图URL">
          <ImageLibraryPicker v-model="form.hero_image_url" placeholder="https://..." />
        </el-form-item>
        <el-form-item label="外部链接">
          <el-input v-model="form.social_url" placeholder="社媒、商务页或官方主页" />
        </el-form-item>
        <el-form-item label="来源URL">
          <el-input v-model="form.source_urls" type="textarea" rows="2" placeholder="每行一个来源链接" />
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
import ImageLibraryPicker from '../components/ImageLibraryPicker.vue';

export default {
  name: 'AthletesAdmin',
  components: { ImageLibraryPicker },
  data() {
    return {
      loading: false,
      list: [],
      dialog: false,
      form: {},
      quickImportDialog: false,
      quickImportText: '',
      quickImportPreview: null,
      quickImportSaving: false,

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
            name_en: '',
            gender: null,
            birth_date: null,
            birth_place: '',
            nationality: '',
            height_cm: null,
            dominant_hand: '',
            playing_style: '',
            association: '',
            profile_title: '',
            profile_summary: '',
            hero_image_url: '',
            social_url: '',
            current_world_rank: null,
            highest_world_rank: null,
            ranking_points: null,
            major_identity: '',
            source_urls: ''
          };
      this.dialog = true;
    },
    openQuickImport() {
      this.quickImportDialog = true;
      this.quickImportPreview = null;
      if (!this.quickImportText) {
        this.quickImportText = '{\n  "athlete": {\n    "name": "",\n    "association": ""\n  },\n  "ranking": {},\n  "achievements": [],\n  "results": [],\n  "upcoming_events": [],\n  "sources": []\n}';
      }
    },
    parseQuickImport() {
      try {
        return JSON.parse(this.quickImportText || '{}');
      } catch (e) {
        this.$message.error('JSON 格式不正确');
        return null;
      }
    },
    previewQuickImport() {
      const payload = this.parseQuickImport();
      if (!payload) return;
      const athlete = payload.athlete || payload;
      this.quickImportPreview = {
        name: athlete.name,
        name_en: athlete.name_en,
        achievements: Array.isArray(payload.achievements) ? payload.achievements.length : 0,
        results: Array.isArray(payload.results) ? payload.results.length : 0,
        upcoming: Array.isArray(payload.upcoming_events) ? payload.upcoming_events.length : 0,
        sources: Array.isArray(payload.sources) ? payload.sources.length : 0
      };
    },
    async submitQuickImport() {
      const payload = this.parseQuickImport();
      if (!payload) return;
      this.quickImportSaving = true;
      try {
        const { data } = await api.post('/athletes/import', payload);
        const mode = data && data.mode === 'created' ? '已新建' : '已更新';
        const name = data && data.athlete ? data.athlete.name : '';
        this.$message.success(mode + (name ? '：' + name : ''));
        this.quickImportDialog = false;
        await this.load();
      } catch (e) {
        this.$message.error('导入失败，请检查 JSON 字段和后端日志');
      } finally {
        this.quickImportSaving = false;
      }
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

<style scoped>
.quick-import__hint {
  margin: 0 0 10px;
  color: #606266;
  font-size: 13px;
  line-height: 1.6;
}

.quick-import__hint code {
  padding: 1px 5px;
  border-radius: 4px;
  background: #f5f7fa;
  color: #303133;
}

.quick-import__preview {
  margin-top: 12px;
  padding: 10px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 6px;
  background: #f8fafc;
  color: #303133;
  line-height: 1.7;
}
</style>


<template>
  <div class="admin-page">
    <h1 class="admin-page__title">球员职业生涯运营</h1>
    <p class="admin-page__desc">维护该球员的高光展示与商务预告（按时间/排序在官网球员页聚合展示）。</p>
    <el-card shadow="never" class="admin-page__card">
      <el-tabs v-model="tab">
        <el-tab-pane label="高光展示" name="highlights">
          <div class="career-admin__toolbar">
            <el-button type="primary" icon="el-icon-plus" @click="addHighlight">新增高光</el-button>
            <el-button type="success" icon="el-icon-check" :loading="savingHighlights" @click="saveHighlights">
              保存高光
            </el-button>
          </div>

          <el-table :data="highlights" stripe border size="small" style="width: 100%">
            <el-table-column label="排序" width="90">
              <template slot-scope="scope">
                <el-input v-model.number="scope.row.sort_order" size="small" />
              </template>
            </el-table-column>
            <el-table-column label="状态" width="120">
              <template slot-scope="scope">
                <el-select v-model="scope.row.status" size="small" clearable>
                  <el-option label="草稿" value="draft" />
                  <el-option label="已发布" value="published" />
                </el-select>
              </template>
            </el-table-column>
            <el-table-column label="发布时间" width="200">
              <template slot-scope="scope">
                <el-date-picker
                  v-model="scope.row.published_at"
                  type="datetime"
                  placeholder="发布时间"
                  value-format="yyyy-MM-dd HH:mm:ss"
                  format="yyyy-MM-dd HH:mm:ss"
                  :editable="false"
                  clearable
                />
              </template>
            </el-table-column>
            <el-table-column label="标题" min-width="180">
              <template slot-scope="scope">
                <el-input v-model="scope.row.title" size="small" placeholder="例如：年度最佳表现" />
              </template>
            </el-table-column>
            <el-table-column label="封面URL" min-width="200">
              <template slot-scope="scope">
                <ImageLibraryPicker v-model="scope.row.cover_url" size="small" placeholder="https://..." />
              </template>
            </el-table-column>
            <el-table-column label="摘要" min-width="240">
              <template slot-scope="scope">
                <el-input v-model="scope.row.summary" size="small" type="textarea" rows="2" placeholder="展示在球员页的简短说明" />
              </template>
            </el-table-column>
            <el-table-column label="操作" width="90">
              <template slot-scope="scope">
                <el-button type="text" @click="removeHighlight(scope.$index)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="商务预告" name="business">
          <div class="career-admin__toolbar">
            <el-button type="primary" icon="el-icon-plus" @click="addBusiness">新增商务预告</el-button>
            <el-button type="success" icon="el-icon-check" :loading="savingBusiness" @click="saveBusiness">
              保存商务预告
            </el-button>
          </div>

          <el-table :data="business_previews" stripe border size="small" style="width: 100%">
            <el-table-column label="排序" width="90">
              <template slot-scope="scope">
                <el-input v-model.number="scope.row.sort_order" size="small" />
              </template>
            </el-table-column>
            <el-table-column label="状态" width="120">
              <template slot-scope="scope">
                <el-select v-model="scope.row.status" size="small" clearable>
                  <el-option label="草稿" value="draft" />
                  <el-option label="已发布" value="published" />
                </el-select>
              </template>
            </el-table-column>
            <el-table-column label="发布时间" width="200">
              <template slot-scope="scope">
                <el-date-picker
                  v-model="scope.row.published_at"
                  type="datetime"
                  placeholder="发布时间"
                  value-format="yyyy-MM-dd HH:mm:ss"
                  format="yyyy-MM-dd HH:mm:ss"
                  :editable="false"
                  clearable
                />
              </template>
            </el-table-column>
            <el-table-column label="计划时间" width="200">
              <template slot-scope="scope">
                <el-date-picker
                  v-model="scope.row.scheduled_at"
                  type="datetime"
                  placeholder="计划时间"
                  value-format="yyyy-MM-dd HH:mm:ss"
                  format="yyyy-MM-dd HH:mm:ss"
                  :editable="false"
                  clearable
                />
              </template>
            </el-table-column>
            <el-table-column label="标题" min-width="180">
              <template slot-scope="scope">
                <el-input v-model="scope.row.title" size="small" placeholder="例如：品牌合作活动预告" />
              </template>
            </el-table-column>
            <el-table-column label="封面URL" min-width="200">
              <template slot-scope="scope">
                <ImageLibraryPicker v-model="scope.row.cover_url" size="small" placeholder="https://..." />
              </template>
            </el-table-column>
            <el-table-column label="摘要" min-width="240">
              <template slot-scope="scope">
                <el-input v-model="scope.row.summary" size="small" type="textarea" rows="2" placeholder="展示在球员页的简短说明" />
              </template>
            </el-table-column>
            <el-table-column label="外链URL" min-width="200">
              <template slot-scope="scope">
                <el-input v-model="scope.row.link_url" size="small" placeholder="https://合作方详情/报名页" />
              </template>
            </el-table-column>
            <el-table-column label="链接文案" min-width="160">
              <template slot-scope="scope">
                <el-input v-model="scope.row.link_text" size="small" placeholder="例如：查看详情" />
              </template>
            </el-table-column>
            <el-table-column label="操作" width="90">
              <template slot-scope="scope">
                <el-button type="text" @click="removeBusiness(scope.$index)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script>
import api from '../api.js';
import ImageLibraryPicker from '../components/ImageLibraryPicker.vue';

export default {
  name: 'AthleteCareerAdmin',
  props: { id: { type: [String, Number], required: true } },
  components: { ImageLibraryPicker },
  data() {
    return {
      tab: 'highlights',
      athleteName: '',
      loading: false,
      highlights: [],
      business_previews: [],
      savingHighlights: false,
      savingBusiness: false
    };
  },
  watch: { id: 'loadAll' },
  mounted() {
    this.loadAll();
  },
  methods: {
    normalizeTs(v) {
      if (!v) return null;
      const s = String(v);
      // 兼容 MySQL Timestamp -> JSON（可能是 ISO 字符串，也可能是 'yyyy-MM-dd HH:mm:ss'）
      const ss = s.replace('T', ' ').replace('Z', ' ');
      return ss.length >= 19 ? ss.slice(0, 19) : ss;
    },
    async loadAthleteName() {
      // 后端目前仅提供列表接口，这里用 pageSize 较大兜底展示名称
      try {
        const { data } = await api.get('/athletes', { params: { page: 1, pageSize: 500 } });
        const list = data && data.list ? data.list : [];
        const row = list.find((x) => String(x.id) === String(this.id));
        this.athleteName = row ? row.name : '';
      } catch (e) {
        this.athleteName = '';
      }
    },
    async loadAll() {
      this.loading = true;
      try {
        await this.loadAthleteName();
        await Promise.all([this.loadHighlights(), this.loadBusiness()]);
      } finally {
        this.loading = false;
      }
    },
    async loadHighlights() {
      try {
        const { data } = await api.get('/athletes/' + this.id + '/highlights');
        const list = (data && data.list) || [];
        this.highlights = list.map((r) => ({
          ...r,
          sort_order: r.sort_order != null ? Number(r.sort_order) : 0,
          published_at: this.normalizeTs(r.published_at)
        }));
      } catch (e) {
        this.$message.error('加载高光展示失败');
        this.highlights = [];
      }
    },
    async loadBusiness() {
      try {
        const { data } = await api.get('/athletes/' + this.id + '/business-previews');
        const list = (data && data.list) || [];
        this.business_previews = list.map((r) => ({
          ...r,
          sort_order: r.sort_order != null ? Number(r.sort_order) : 0,
          published_at: this.normalizeTs(r.published_at),
          scheduled_at: this.normalizeTs(r.scheduled_at)
        }));
      } catch (e) {
        this.$message.error('加载商务预告失败');
        this.business_previews = [];
      }
    },
    addHighlight() {
      const maxSort = (this.highlights || []).reduce((acc, r) => Math.max(acc, Number(r.sort_order) || 0), -1);
      this.highlights.push({
        title: '',
        cover_url: '',
        summary: '',
        sort_order: maxSort + 1,
        status: 'published',
        published_at: null
      });
    },
    addBusiness() {
      const maxSort = (this.business_previews || []).reduce((acc, r) => Math.max(acc, Number(r.sort_order) || 0), -1);
      this.business_previews.push({
        title: '',
        cover_url: '',
        summary: '',
        link_url: '',
        link_text: '',
        scheduled_at: null,
        sort_order: maxSort + 1,
        status: 'published',
        published_at: null
      });
    },
    removeHighlight(i) {
      this.highlights.splice(i, 1);
    },
    removeBusiness(i) {
      this.business_previews.splice(i, 1);
    },
    async saveHighlights() {
      this.savingHighlights = true;
      try {
        const body = this.highlights.map((r) => ({
          title: r.title,
          cover_url: r.cover_url || null,
          summary: r.summary || null,
          sort_order: r.sort_order != null ? Number(r.sort_order) : 0,
          status: r.status || 'draft',
          published_at: r.published_at || null
        }));
        await api.put('/athletes/' + this.id + '/highlights', body);
        this.$message.success('高光展示已保存');
        await this.loadHighlights();
      } catch (e) {
        this.$message.error('保存高光展示失败');
      } finally {
        this.savingHighlights = false;
      }
    },
    async saveBusiness() {
      this.savingBusiness = true;
      try {
        const body = this.business_previews.map((r) => ({
          title: r.title,
          cover_url: r.cover_url || null,
          summary: r.summary || null,
          link_url: r.link_url || null,
          link_text: r.link_text || null,
          scheduled_at: r.scheduled_at || null,
          sort_order: r.sort_order != null ? Number(r.sort_order) : 0,
          status: r.status || 'draft',
          published_at: r.published_at || null
        }));
        await api.put('/athletes/' + this.id + '/business-previews', body);
        this.$message.success('商务预告已保存');
        await this.loadBusiness();
      } catch (e) {
        this.$message.error('保存商务预告失败');
      } finally {
        this.savingBusiness = false;
      }
    }
  }
};
</script>

<style scoped>
.career-admin__toolbar {
  display: flex;
  gap: 12px;
  align-items: center;
  margin-bottom: 12px;
}
</style>


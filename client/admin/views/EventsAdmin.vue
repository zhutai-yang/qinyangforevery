<template>
  <div class="admin-page">
    <h1 class="admin-page__title">赛事管理</h1>
    <p class="admin-page__desc">维护赛事档案、届次、地点与发布状态，供赛程与门户引用。</p>
    <el-card shadow="never" class="admin-page__card">
      <el-form inline class="admin-toolbar">
        <el-form-item label="状态">
          <el-select v-model="filters.status" placeholder="全部状态" clearable @change="load(1)">
            <el-option label="草稿" value="draft" />
            <el-option label="已发布" value="published" />
            <el-option label="已归档" value="archived" />
          </el-select>
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="filters.dateRange"
            type="daterange"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="yyyy-MM-dd"
            format="yyyy-MM-dd"
            :editable="false"
            clearable
            @change="load(1)"
          />
        </el-form-item>
        <el-button @click="resetFilters">重置</el-button>
        <el-button type="primary" icon="el-icon-plus" @click="openEdit()">新建赛事</el-button>
      </el-form>
      <el-table v-loading="loading" :data="list" stripe border size="small">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="name" label="名称" />
      <el-table-column prop="edition" label="届次" width="100" />
      <el-table-column label="时间范围" min-width="180">
        <template slot-scope="scope">
          {{ dateRangeText(scope.row) }}
        </template>
      </el-table-column>
      <el-table-column prop="location" label="地点" min-width="140" show-overflow-tooltip />
      <el-table-column label="状态" width="100">
        <template slot-scope="scope">{{ statusText(scope.row.status) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="220">
        <template slot-scope="scope">
          <el-button type="text" @click="openEdit(scope.row)">编辑</el-button>
          <el-button v-if="scope.row.status !== 'published'" type="text" @click="setStatus(scope.row, 'published')">
            发布
          </el-button>
          <el-button v-else type="text" @click="setStatus(scope.row, 'draft')">下架</el-button>
          <el-button type="text" @click="remove(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <div class="admin-toolbar admin-toolbar--end">
      <el-pagination
        layout="total, sizes, prev, pager, next"
        :current-page.sync="page"
        :page-size.sync="pageSize"
        :page-sizes="[20, 50, 100]"
        :total="total"
        @current-change="load"
        @size-change="onPageSizeChange"
      />
    </div>
    <el-dialog :title="form.id ? '编辑' : '新建'" :visible.sync="dialog" width="640px" @close="resetForm">
      <el-form label-width="100px">
        <el-form-item label="名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="届次"><el-input v-model="form.edition" /></el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="form.dateRange"
            type="daterange"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="yyyy-MM-dd"
            format="yyyy-MM-dd"
            :editable="false"
            clearable
          />
        </el-form-item>
        <el-form-item label="级别代码"><el-input v-model="form.level_code" /></el-form-item>
        <el-form-item label="地点"><el-input v-model="form.location" /></el-form-item>
        <el-form-item label="关联场馆">
          <el-select v-model="form.venueIds" placeholder="选择场馆" multiple filterable clearable>
            <el-option v-for="v in venues" :key="v.id" :label="venueLabel(v)" :value="v.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status" placeholder="状态">
            <el-option label="草稿" value="draft" />
            <el-option label="已发布" value="published" />
            <el-option label="已归档" value="archived" />
          </el-select>
        </el-form-item>
        <el-form-item label="描述"><el-input v-model="form.description" type="textarea" rows="3" /></el-form-item>
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
import { eventStatusText } from '../../shared/statusLabels.js';

export default {
  name: 'EventsAdmin',
  data() {
    return {
      loading: false,
      list: [],
      total: 0,
      page: 1,
      pageSize: 20,
      filters: { status: '', dateRange: [] },
      venues: [],
      dialog: false,
      form: {}
    };
  },
  async mounted() {
    await this.loadVenues();
    await this.load();
  },
  methods: {
    statusText(status) {
      return eventStatusText(status);
    },
    dateRangeText(row) {
      const start = row.start_date || '未定';
      const end = row.end_date || '未定';
      return start + ' 至 ' + end;
    },
    venueLabel(row) {
      return row.address ? row.name + '（' + row.address + '）' : row.name;
    },
    async loadVenues() {
      try {
        const { data } = await api.get('/venues');
        this.venues = data.list || [];
      } catch (e) {
        this.venues = [];
      }
    },
    async load(p) {
      if (p) this.page = p;
      this.loading = true;
      try {
        const params = {
          page: this.page,
          pageSize: this.pageSize
        };
        if (this.filters.status) params.status = this.filters.status;
        if (this.filters.dateRange && this.filters.dateRange.length === 2) {
          params.startDate = this.filters.dateRange[0];
          params.endDate = this.filters.dateRange[1];
        }
        const { data } = await api.get('/events', { params });
        this.list = data.list || [];
        this.total = data.total || 0;
      } catch (e) {
        this.$message.error('加载失败');
      } finally {
        this.loading = false;
      }
    },
    resetFilters() {
      this.filters = { status: '', dateRange: [] };
      this.load(1);
    },
    onPageSizeChange(size) {
      this.pageSize = size;
      this.load(1);
    },
    async openEdit(row) {
      if (row && row.id) {
        try {
          const { data } = await api.get('/events/' + row.id);
          this.form = {
            ...data,
            dateRange: data.start_date || data.end_date ? [data.start_date, data.end_date] : [],
            venueIds: (data.venues || []).map((v) => v.id)
          };
        } catch (e) {
          this.form = {
            ...row,
            dateRange: row.start_date || row.end_date ? [row.start_date, row.end_date] : [],
            venueIds: []
          };
        }
      } else {
        this.form = { name: '', status: 'draft', dateRange: [], venueIds: [] };
      }
      this.dialog = true;
    },
    resetForm() {
      this.form = {};
    },
    normalizeForm() {
      const body = { ...this.form };
      const range = body.dateRange || [];
      body.start_date = range[0] || null;
      body.end_date = range[1] || null;
      delete body.dateRange;
      delete body.venueIds;
      delete body.venues;
      return body;
    },
    async save() {
      try {
        const body = this.normalizeForm();
        let saved;
        if (this.form.id) {
          const { data } = await api.put('/events/' + this.form.id, body);
          saved = data;
        } else {
          const { data } = await api.post('/events', body);
          saved = data;
        }
        if (saved && saved.id) {
          await api.put('/events/' + saved.id + '/venues', { venueIds: this.form.venueIds || [] });
        }
        this.$message.success('已保存');
        this.dialog = false;
        this.load();
      } catch (e) {
        this.$message.error('保存失败');
      }
    },
    async setStatus(row, status) {
      try {
        const body = { ...row, status };
        await api.put('/events/' + row.id, body);
        this.$message.success(status === 'published' ? '已发布' : '已下架');
        this.load();
      } catch (e) {
        this.$message.error('状态更新失败');
      }
    },
    async remove(row) {
      try {
        await this.$confirm('确认删除？');
        await api.delete('/events/' + row.id);
        this.$message.success('已删除');
        this.load();
      } catch (e) {
        if (e !== 'cancel') this.$message.error('删除失败');
      }
    }
  }
};
</script>


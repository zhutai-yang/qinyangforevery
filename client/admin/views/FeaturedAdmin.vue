<template>
  <div class="admin-page">
    <h1 class="admin-page__title">关注球员</h1>
    <p class="admin-page__desc">在表格中维护关注球员（需先存在运动员档案）。保存会覆盖服务端列表。</p>
    <el-card shadow="never" class="admin-page__card">
      <div class="admin-toolbar">
        <el-button type="primary" icon="el-icon-plus" @click="addRow">添加一行</el-button>
        <el-button type="success" icon="el-icon-check" @click="save">保存</el-button>
      </div>
      <el-table :data="rows" stripe border size="small">
      <el-table-column label="运动员">
        <template slot-scope="scope">
          <el-select
            v-model.number="scope.row.athlete_id"
            size="small"
            filterable
            clearable
            placeholder="选择运动员"
          >
            <el-option
              v-for="a in athletes"
              :key="a.id"
              :label="a.name + '（' + a.id + '）'"
              :value="a.id"
            />
          </el-select>
        </template>
      </el-table-column>
      <el-table-column label="排序">
        <template slot-scope="scope">
          <el-input v-model.number="scope.row.sort_order" size="small" />
        </template>
      </el-table-column>
      <el-table-column label="启用" width="80">
        <template slot-scope="scope">
          <el-switch v-model="scope.row.enabled" />
        </template>
      </el-table-column>
      <el-table-column label="操作" width="80">
        <template slot-scope="scope">
          <el-button type="text" @click="rows.splice(scope.$index, 1)">删</el-button>
        </template>
      </el-table-column>
    </el-table>
    </el-card>
  </div>
</template>

<script>
import api from '../api.js';

export default {
  name: 'FeaturedAdmin',
  data() {
    return { rows: [], athletes: [] };
  },
  mounted() {
    this.load();
    this.loadAthletes();
  },
  methods: {
    async loadAthletes() {
      try {
        const { data } = await api.get('/athletes', { params: { page: 1, pageSize: 200 } });
        this.athletes = data.list || [];
      } catch (e) {
        this.$message.error('加载运动员失败');
        this.athletes = [];
      }
    },
    async load() {
      try {
        const { data } = await api.get('/featured-athletes');
        const list = data.list || [];
        this.rows = list.map((r) => ({
          athlete_id: r.athlete_id != null ? Number(r.athlete_id) : null,
          sort_order: r.sort_order || 0,
          enabled: r.enabled !== 0 && r.enabled !== false
        }));
        console.log(data);
      } catch (e) {
        this.$message.error('加载失败');
      }
    },
    addRow() {
      this.rows.push({ athlete_id: null, sort_order: 0, enabled: true });
    },
    async save() {
      try {
        const body = this.rows
          .filter((r) => r.athlete_id)
          .map((r) => ({
            athlete_id: r.athlete_id,
            sort_order: r.sort_order || 0,
            enabled: r.enabled
          }));
        await api.put('/featured-athletes', body);
        this.$message.success('已保存');
        this.load();
      } catch (e) {
        this.$message.error('保存失败');
      }
    }
  }
};
</script>

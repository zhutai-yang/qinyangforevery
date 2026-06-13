<template>
  <div class="admin-page">
    <h1 class="admin-page__title">字典与审计</h1>
    <p class="admin-page__desc">维护赛事级别等枚举字典，并分页查看后台操作审计日志。</p>
    <el-card shadow="never" class="admin-page__card">
      <el-tabs v-model="tab">
        <el-tab-pane label="字典 event_level" name="dict">
          <div class="admin-toolbar">
            <el-button type="primary" icon="el-icon-check" @click="saveDict">保存字典项</el-button>
          </div>
          <el-table :data="dictItems" stripe border size="small">
            <el-table-column label="code">
              <template slot-scope="scope">
                <el-input v-model="scope.row.code" size="small" />
              </template>
            </el-table-column>
            <el-table-column label="label">
              <template slot-scope="scope">
                <el-input v-model="scope.row.label" size="small" />
              </template>
            </el-table-column>
            <el-table-column label="sort">
              <template slot-scope="scope">
                <el-input v-model.number="scope.row.sort_order" size="small" />
              </template>
            </el-table-column>
            <el-table-column width="60">
              <template slot-scope="scope">
                <el-button type="text" @click="dictItems.splice(scope.$index, 1)">删</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-button @click="dictItems.push({ code: '', label: '', sort_order: 0 })">添加项</el-button>
        </el-tab-pane>
        <el-tab-pane label="审计日志" name="audit">
          <el-table v-loading="auditLoading" :data="auditList" stripe border size="small">
            <el-table-column prop="id" label="ID" width="70" />
            <el-table-column prop="action" label="动作" width="120" />
            <el-table-column prop="entity" label="实体" width="100" />
            <el-table-column prop="username" label="用户" width="120" />
            <el-table-column prop="created_at" label="时间" width="180" />
          </el-table>
          <el-pagination
            v-if="auditTotal > auditPageSize"
            layout="prev, pager, next"
            class="dict-audit__pager"
            :total="auditTotal"
            :page-size="auditPageSize"
            :current-page.sync="auditPage"
            @current-change="loadAudit"
          />
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script>
import api from '../api.js';

export default {
  name: 'DictAudit',
  data() {
    return {
      tab: 'dict',
      dictItems: [],
      auditLoading: false,
      auditList: [],
      auditTotal: 0,
      auditPage: 1,
      auditPageSize: 20
    };
  },
  mounted() {
    this.loadDict();
    this.loadAudit(1);
  },
  methods: {
    async loadDict() {
      try {
        const { data } = await api.get('/dict/event_level');
        this.dictItems = (data.items || []).map((i) => ({
          code: i.code,
          label: i.label,
          sort_order: i.sort_order
        }));
      } catch (e) {
        this.$message.error('加载字典失败');
      }
    },
    async saveDict() {
      try {
        await api.put('/dict/event_level', { items: this.dictItems });
        this.$message.success('已保存');
        this.loadDict();
      } catch (e) {
        this.$message.error('保存失败');
      }
    },
    async loadAudit(p) {
      this.auditPage = p || this.auditPage;
      this.auditLoading = true;
      try {
        const { data } = await api.get('/audit-logs', {
          params: { page: this.auditPage, pageSize: this.auditPageSize }
        });
        this.auditList = data.list || [];
        this.auditTotal = data.total || 0;
      } catch (e) {
        this.$message.error('加载审计失败');
      } finally {
        this.auditLoading = false;
      }
    }
  }
};
</script>

<style scoped>
.dict-audit__pager {
  margin-top: 16px;
}
</style>

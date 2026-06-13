<template>
  <div class="admin-page">
    <h1 class="admin-page__title">赛事管理</h1>
    <p class="admin-page__desc">维护赛事档案、届次、地点与发布状态，供赛程与门户引用。</p>
    <el-card shadow="never" class="admin-page__card">
      <div class="admin-toolbar">
        <el-button type="primary" icon="el-icon-plus" @click="openEdit()">新建赛事</el-button>
      </div>
      <el-table v-loading="loading" :data="list" stripe border size="small">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="name" label="名称" />
      <el-table-column label="状态" width="100">
        <template slot-scope="scope">{{ statusText(scope.row.status) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="160">
        <template slot-scope="scope">
          <el-button type="text" @click="openEdit(scope.row)">编辑</el-button>
          <el-button type="text" @click="remove(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-dialog :title="form.id ? '编辑' : '新建'" :visible.sync="dialog" width="520px" @close="resetForm">
      <el-form label-width="100px">
        <el-form-item label="名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="届次"><el-input v-model="form.edition" /></el-form-item>
        <el-form-item label="级别代码"><el-input v-model="form.level_code" /></el-form-item>
        <el-form-item label="地点"><el-input v-model="form.location" /></el-form-item>
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

export default {
  name: 'EventsAdmin',
  data() {
    return {
      loading: false,
      list: [],
      dialog: false,
      form: {}
    };
  },
  mounted() {
    this.load();
  },
  methods: {
    statusText(status) {
      switch (status) {
        case 'draft':
          return '草稿';
        case 'published':
          return '已发布';
        case 'archived':
          return '已归档';
        default:
          return status || '—';
      }
    },
    async load() {
      this.loading = true;
      try {
        const { data } = await api.get('/events', { params: { page: 1, pageSize: 200 } });
        this.list = data.list || [];
      } catch (e) {
        this.$message.error('加载失败');
      } finally {
        this.loading = false;
      }
    },
    openEdit(row) {
      this.form = row ? { ...row } : { name: '', status: 'draft' };
      this.dialog = true;
    },
    resetForm() {
      this.form = {};
    },
    async save() {
      try {
        if (this.form.id) {
          await api.put('/events/' + this.form.id, this.form);
        } else {
          await api.post('/events', this.form);
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


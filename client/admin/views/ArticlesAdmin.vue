<template>
  <div class="admin-page">
    <h1 class="admin-page__title">文章管理</h1>
    <p class="admin-page__desc">发布资讯、公告与赛况，控制草稿与已发布状态。</p>
    <el-card shadow="never" class="admin-page__card">
      <div class="admin-toolbar">
        <el-button type="primary" icon="el-icon-plus" @click="openEdit()">新建文章</el-button>
      </div>
      <el-table v-loading="loading" :data="list" stripe border size="small">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="title" label="标题" />
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
    <el-dialog :title="form.id ? '编辑文章' : '新建文章'" :visible.sync="dialog" width="640px">
      <el-form label-width="80px">
        <el-form-item label="标题"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="slug"><el-input v-model="form.slug" /></el-form-item>
        <el-form-item label="摘要"><el-input v-model="form.summary" type="textarea" rows="2" /></el-form-item>
        <el-form-item label="正文"><el-input v-model="form.body" type="textarea" rows="6" /></el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status">
            <el-option label="草稿" value="draft" />
            <el-option label="已发布" value="published" />
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

export default {
  name: 'ArticlesAdmin',
  data() {
    return { loading: false, list: [], dialog: false, form: {} };
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
        default:
          return status || '—';
      }
    },
    async load() {
      this.loading = true;
      try {
        const { data } = await api.get('/articles', { params: { page: 1, pageSize: 200 } });
        this.list = data.list || [];
      } catch (e) {
        this.$message.error('加载失败');
      } finally {
        this.loading = false;
      }
    },
    openEdit(row) {
      this.form = row ? { ...row } : { title: '', status: 'draft' };
      this.dialog = true;
    },
    async save() {
      try {
        if (this.form.id) {
          await api.put('/articles/' + this.form.id, this.form);
        } else {
          await api.post('/articles', this.form);
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
        await api.delete('/articles/' + row.id);
        this.load();
      } catch (e) {
        if (e !== 'cancel') this.$message.error('删除失败');
      }
    }
  }
};
</script>


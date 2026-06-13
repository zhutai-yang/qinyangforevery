<template>
  <div class="admin-page">
    <h1 class="admin-page__title">场馆管理</h1>
    <p class="admin-page__desc">维护比赛场馆名称与地址，供赛程与展示使用。</p>
    <el-card shadow="never" class="admin-page__card">
      <div class="admin-toolbar">
        <el-button type="primary" icon="el-icon-plus" @click="openEdit()">新建场馆</el-button>
      </div>
      <el-table v-loading="loading" :data="list" stripe border size="small">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="name" label="名称" />
      <el-table-column prop="address" label="地址" />
      <el-table-column label="操作" width="160">
        <template slot-scope="scope">
          <el-button type="text" @click="openEdit(scope.row)">编辑</el-button>
          <el-button type="text" @click="remove(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-dialog :title="form.id ? '编辑场馆' : '新建场馆'" :visible.sync="dialog" width="480px">
      <el-form label-width="80px">
        <el-form-item label="名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="地址"><el-input v-model="form.address" /></el-form-item>
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
  name: 'VenuesAdmin',
  data() {
    return { loading: false, list: [], dialog: false, form: {} };
  },
  mounted() {
    this.load();
  },
  methods: {
    async load() {
      this.loading = true;
      try {
        const { data } = await api.get('/venues');
        this.list = data.list || [];
      } catch (e) {
        this.$message.error('加载失败');
      } finally {
        this.loading = false;
      }
    },
    openEdit(row) {
      this.form = row ? { ...row } : { name: '' };
      this.dialog = true;
    },
    async save() {
      try {
        if (this.form.id) {
          await api.put('/venues/' + this.form.id, this.form);
        } else {
          await api.post('/venues', this.form);
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
        await api.delete('/venues/' + row.id);
        this.$message.success('已删除');
        this.load();
      } catch (e) {
        if (e !== 'cancel') this.$message.error('删除失败');
      }
    }
  }
};
</script>


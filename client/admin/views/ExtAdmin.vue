<template>
  <div class="admin-page">
    <h1 class="admin-page__title">外部数据源</h1>
    <p class="admin-page__desc">配置外部站点地址、解析方式与同步频率；启用后系统按间隔(分)自动同步，也可手动触发。</p>
    <el-card shadow="never" class="admin-page__card">
      <div class="admin-toolbar">
        <el-button type="primary" icon="el-icon-plus" @click="openEdit()">新建数据源</el-button>
      </div>
      <el-table v-loading="loading" :data="list" stripe border size="small">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="name" label="名称" />
      <el-table-column prop="base_url" label="URL" min-width="200" show-overflow-tooltip />
      <el-table-column prop="interval_minutes" label="间隔(分)" width="90" />
      <el-table-column prop="parser_type" label="解析器" width="100" />
      <el-table-column prop="enabled" label="启用" width="70">
        <template slot-scope="scope">{{ scope.row.enabled ? '是' : '否' }}</template>
      </el-table-column>
      <el-table-column label="操作" width="220">
        <template slot-scope="scope">
          <el-button type="text" @click="openEdit(scope.row)">编辑</el-button>
          <el-button type="text" @click="sync(scope.row)">同步</el-button>
          <el-button type="text" @click="remove(scope.row)">删</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-dialog :title="form.id ? '编辑' : '新建'" :visible.sync="dialog" width="560px">
      <el-form label-width="120px">
        <el-form-item label="名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="基础 URL"><el-input v-model="form.base_url" /></el-form-item>
        <el-form-item label="解析器">
          <el-select v-model="form.parser_type" placeholder="html_v1">
            <el-option label="HTML (html_v1)" value="html_v1" />
            <el-option label="RSS (rss)" value="rss" />
          </el-select>
        </el-form-item>
        <el-form-item label="间隔(分)"><el-input v-model.number="form.interval_minutes" /></el-form-item>
        <el-form-item label="启用"><el-switch v-model="form.enabled" /></el-form-item>
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
  name: 'ExtAdmin',
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
        const { data } = await api.get('/ext-sources');
        this.list = data.list || [];
      } catch (e) {
        this.$message.error('加载失败');
      } finally {
        this.loading = false;
      }
    },
    openEdit(row) {
      this.form = row
        ? { ...row, enabled: !!row.enabled }
        : { name: '', base_url: '', parser_type: 'html_v1', interval_minutes: 60, enabled: true };
      this.dialog = true;
    },
    async save() {
      try {
        const body = { ...this.form };
        if (this.form.id) {
          await api.put('/ext-sources/' + this.form.id, body);
        } else {
          await api.post('/ext-sources', body);
        }
        this.$message.success('已保存');
        this.dialog = false;
        this.load();
      } catch (e) {
        this.$message.error('保存失败');
      }
    },
    async sync(row) {
      try {
        const { data } = await api.post('/ext-sources/' + row.id + '/sync');
        this.$message.success(data.message || '同步已触发');
      } catch (e) {
        this.$message.error('同步失败');
      }
    },
    async remove(row) {
      try {
        await this.$confirm('确认删除？');
        await api.delete('/ext-sources/' + row.id);
        this.load();
      } catch (e) {
        if (e !== 'cancel') this.$message.error('删除失败');
      }
    }
  }
};
</script>


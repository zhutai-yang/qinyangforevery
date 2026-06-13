<template>
  <div class="login-wrap">
    <el-card class="box">
      <h2>管理后台登录</h2>
      <el-form label-width="80px" @submit.native.prevent="submit">
        <el-form-item label="用户名">
          <el-input v-model="username" autocomplete="username" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="password" type="password" autocomplete="current-password" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" native-type="submit" :loading="loading">登录</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script>
import api from '../api.js';

export default {
  name: 'AdminLogin',
  data() {
    return { username: '', password: '', loading: false };
  },
  methods: {
    async submit() {
      this.loading = true;
      try {
        const { data } = await api.post('/auth/login', {
          username: this.username,
          password: this.password
        });
        if (data.token) {
          localStorage.setItem('tt_token', data.token);
          this.$router.replace('/dashboard');
        } else {
          this.$message.error('登录失败');
        }
      } catch (e) {
        const msg = (e.response && e.response.data && e.response.data.message) || '登录失败';
        this.$message.error(msg);
      } finally {
        this.loading = false;
      }
    }
  }
};
</script>

<style scoped>
.login-wrap {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  box-sizing: border-box;
  background: linear-gradient(145deg, #eef2f6 0%, #e8edf2 45%, #dfe6ee 100%);
}

.box {
  width: 100%;
  max-width: 400px;
  border: none;
  border-radius: 8px;
  box-shadow: 0 12px 40px rgba(15, 37, 64, 0.12);
}

.box >>> .el-card__body {
  padding: 28px 28px 24px;
}

h2 {
  margin: 0 0 20px;
  font-size: 20px;
  font-weight: 600;
  color: #303133;
  letter-spacing: -0.02em;
}
</style>

<template>
  <div class="login-page">
    <aside class="login-page__brand">
      <div class="login-page__brand-inner">
        <div class="login-page__logo">
          <i class="el-icon-s-platform" />
        </div>
        <h1 class="login-page__brand-title">ttf_admin</h1>
        <p class="login-page__brand-desc">乒乓球赛事管理后台</p>
      </div>
      <div class="login-page__brand-decor" aria-hidden="true">
        <span class="login-page__orb login-page__orb--1" />
        <span class="login-page__orb login-page__orb--2" />
      </div>
    </aside>

    <main class="login-page__main">
      <div class="login-panel">
        <header class="login-panel__header">
          <h2 class="login-panel__title">欢迎登录</h2>
          <p class="login-panel__hint">请使用管理员账号登录系统</p>
        </header>

        <el-form class="login-form" @submit.native.prevent="submit">
          <el-form-item>
            <el-input
              v-model="username"
              prefix-icon="el-icon-user"
              placeholder="用户名"
              size="medium"
              autocomplete="username"
            />
          </el-form-item>
          <el-form-item>
            <el-input
              v-model="password"
              prefix-icon="el-icon-lock"
              placeholder="密码"
              type="password"
              size="medium"
              show-password
              autocomplete="current-password"
              @keyup.enter.native="submit"
            />
          </el-form-item>
          <el-button
            type="primary"
            class="login-form__submit"
            native-type="submit"
            :loading="loading"
          >
            登 录
          </el-button>
        </el-form>
      </div>
    </main>
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
.login-page {
  display: flex;
  min-height: 100vh;
  min-height: 100dvh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 45%, #5b21b6 100%);
}

.login-page__brand {
  position: relative;
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  background: linear-gradient(160deg, rgba(91, 33, 182, 0.55) 0%, rgba(124, 58, 237, 0.25) 100%);
  color: #fff;
}

.login-page__brand-inner {
  position: relative;
  z-index: 1;
  text-align: center;
  padding: 40px 32px;
}

.login-page__logo {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 72px;
  height: 72px;
  margin-bottom: 20px;
  border-radius: 18px;
  background: rgba(196, 181, 253, 0.2);
  border: 1px solid rgba(221, 214, 254, 0.45);
  font-size: 36px;
  color: #ddd6fe;
}

.login-page__brand-title {
  margin: 0 0 10px;
  font-size: 28px;
  font-weight: 600;
  letter-spacing: -0.02em;
}

.login-page__brand-desc {
  margin: 0;
  font-size: 15px;
  line-height: 1.6;
  color: rgba(255, 255, 255, 0.72);
}

.login-page__brand-decor {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.login-page__orb {
  position: absolute;
  border-radius: 50%;
  background: radial-gradient(circle at 30% 30%, rgba(196, 181, 253, 0.45), rgba(124, 58, 237, 0));
}

.login-page__orb--1 {
  width: 320px;
  height: 320px;
  top: -80px;
  right: -60px;
  opacity: 0.55;
}

.login-page__orb--2 {
  width: 240px;
  height: 240px;
  bottom: -40px;
  left: -40px;
  opacity: 0.4;
}

.login-page__main {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 32px 24px;
  background: transparent;
}

.login-panel {
  width: 100%;
  max-width: 400px;
  padding: 36px 32px 32px;
  border-radius: 12px;
  background: #fff;
  box-shadow: 0 8px 32px rgba(15, 37, 64, 0.08);
  border: 1px solid #ebeef5;
  box-sizing: border-box;
}

.login-panel__header {
  margin-bottom: 28px;
}

.login-panel__title {
  margin: 0 0 8px;
  font-size: 22px;
  font-weight: 600;
  color: #303133;
  letter-spacing: -0.02em;
}

.login-panel__hint {
  margin: 0;
  font-size: 14px;
  line-height: 1.5;
  color: #909399;
}

.login-form >>> .el-form-item {
  margin-bottom: 20px;
}

.login-form >>> .el-form-item:last-child {
  margin-bottom: 0;
}

.login-form >>> .el-input__inner {
  height: 42px;
  line-height: 42px;
  border-radius: 6px;
}

.login-form >>> .el-input__prefix {
  left: 12px;
}

.login-form >>> .el-input--prefix .el-input__inner {
  padding-left: 38px;
}

.login-form__submit {
  width: 100%;
  height: 42px;
  margin-top: 4px;
  font-size: 15px;
  font-weight: 500;
  letter-spacing: 0.08em;
  border-radius: 6px;
}

@media (max-width: 767px) {
  .login-page {
    flex-direction: column;
  }

  .login-page__brand {
    flex: none;
    min-height: 200px;
  }

  .login-page__brand-inner {
    padding: 32px 24px 28px;
  }

  .login-page__logo {
    width: 56px;
    height: 56px;
    margin-bottom: 14px;
    font-size: 28px;
    border-radius: 14px;
  }

  .login-page__brand-title {
    font-size: 22px;
  }

  .login-page__brand-desc {
    font-size: 14px;
  }

  .login-page__main {
    flex: 1;
    align-items: flex-start;
    padding: 24px 16px 32px;
  }

  .login-panel {
    padding: 28px 20px 24px;
    border-radius: 10px;
    box-shadow: 0 4px 20px rgba(15, 37, 64, 0.06);
  }

  .login-panel__title {
    font-size: 20px;
  }
}
</style>

const path = require('path');
const { defineConfig, loadEnv } = require('vite');
const { createVuePlugin } = require('vite-plugin-vue2');

/** 与 e:\\miduo\\mdimp\\imp-admin 一致：用 loadEnv 集中读 VITE_ 前缀，开发代理目标可改 */
module.exports = defineConfig(({ mode }) => {
  const projectRoot = path.resolve(__dirname, '..');
  const env = loadEnv(mode, projectRoot, 'VITE_');
  const apiTarget = (env.VITE_API_TARGET || 'http://127.0.0.1:8096').replace(/\/$/, '');
  const port = parseInt(env.VITE_ADMIN_PORT || '5174', 10) || 5174;

  return {
    envDir: projectRoot,
    plugins: [createVuePlugin()],
    root: path.resolve(__dirname, 'admin'),
    base: '/admin/',
    resolve: {
      alias: { vue: 'vue/dist/vue.esm.js' }
    },
    server: {
      port,
      proxy: {
        // 只代理真实接口前缀，避免把模块文件 `/api.js` 也误代理到后端
        '/api/': { target: apiTarget, changeOrigin: true }
      }
    },
    build: {
      outDir: path.resolve(__dirname, '../dist/admin'),
      emptyOutDir: true
    }
  };
});

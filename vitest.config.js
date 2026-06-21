const path = require('path');
const { defineConfig } = require('vitest/config');
const { createVuePlugin } = require('vite-plugin-vue2');

module.exports = defineConfig({
  plugins: [createVuePlugin()],
  resolve: {
    alias: { vue: 'vue/dist/vue.esm.js' }
  },
  test: {
    include: ['client/**/*.test.js']
  },
  root: path.resolve(__dirname)
});

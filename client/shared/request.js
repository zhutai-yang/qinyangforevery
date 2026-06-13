import axios from 'axios';

/**
 * 创建 axios 实例（IMP 风格公共请求层）。
 * @param {string} baseURL API 前缀，如 /api/admin 或 /api/public
 * @param {{ onUnauthorized?: () => void }} options
 */
export function createHttp(baseURL, options = {}) {
  const http = axios.create({ baseURL, timeout: 60000 });

  http.interceptors.request.use((config) => {
    const t = localStorage.getItem('tt_token');
    if (t) {
      config.headers.Authorization = 'Bearer ' + t;
    }
    return config;
  });

  http.interceptors.response.use(
    (r) => r,
    (err) => {
      if (err.response && err.response.status === 401 && options.onUnauthorized) {
        localStorage.removeItem('tt_token');
        options.onUnauthorized();
      }
      return Promise.reject(err);
    }
  );

  return http;
}

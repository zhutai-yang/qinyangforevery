import { createHttp } from '../shared/request.js';

const http = createHttp('/api/admin', {
  onUnauthorized() {
    if (!window.location.pathname.endsWith('/login')) {
      window.location.href = '/admin/login';
    }
  }
});

export default http;

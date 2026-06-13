<template>
  <div class="admin-page">
    <h1 class="admin-page__title">公用图片库管理</h1>
    <p class="admin-page__desc">上传图片后，可在各运营页面直接选择图片作为封面等素材。</p>

    <el-card shadow="never" class="admin-page__card">
      <div class="image-lib-toolbar">
        <el-button size="small" type="primary" icon="el-icon-upload" @click="reload">
          刷新
        </el-button>

        <el-upload
          class="image-lib-toolbar__upload"
          action="/api/admin/files/upload"
          :show-file-list="false"
          :auto-upload="true"
          accept="image/*"
          :http-request="uploadImage"
        >
          <el-button size="small" type="success" icon="el-icon-upload2">上传图片</el-button>
        </el-upload>

        <el-input
          v-model="keyword"
          size="small"
          placeholder="搜索：文件名/扩展名"
          style="width: 320px; margin-left: auto"
          clearable
        />
      </div>

      <div v-if="filteredItems.length" class="image-lib-grid">
        <div
          v-for="it in filteredItems"
          :key="it.fileId"
          class="image-lib-tile"
        >
          <div class="image-lib-tile__img">
            <img :src="publicFileUrl(it.fileId)" :alt="it.originalName || it.fileId" />
          </div>
          <div class="image-lib-tile__meta">
            <div class="image-lib-tile__name" :title="it.originalName">
              {{ it.originalName || it.fileId }}
            </div>
            <div class="image-lib-tile__actions">
              <el-button type="text" size="small" @click="copyUrl(it)">复制URL</el-button>
              <el-button
                type="text"
                size="small"
                style="color: #f56c6c"
                @click="confirmDelete(it)"
              >
                删除
              </el-button>
            </div>
          </div>
        </div>
      </div>

      <div v-else class="image-lib-empty">
        暂无图片素材，请先上传。
      </div>
    </el-card>
  </div>
</template>

<script>
import api from '../api.js';

export default {
  name: 'ImageLibraryAdmin',
  data() {
    return {
      loading: false,
      items: [],
      keyword: ''
    };
  },
  computed: {
    filteredItems() {
      const kw = (this.keyword || '').trim().toLowerCase();
      const list = Array.isArray(this.items) ? this.items : [];
      if (!kw) return list;
      return list.filter((it) => {
        const name = (it.originalName || '').toLowerCase();
        const ext = (it.ext || '').toLowerCase();
        return name.includes(kw) || ext.includes(kw);
      });
    }
  },
  mounted() {
    this.reload();
  },
  methods: {
    publicFileUrl(fileId) {
      const origin = window.location.origin || '';
      return origin + '/api/public/files/' + fileId;
    },
    isImageItem(it) {
      if (!it) return false;
      const ct = it.contentType || '';
      const ext = (it.ext || '').toLowerCase();
      if (ct.startsWith('image/')) return true;
      return ['jpg', 'jpeg', 'png', 'gif', 'webp', 'bmp', 'svg'].includes(ext);
    },
    async reload() {
      this.loading = true;
      try {
        const { data } = await api.get('/files/list', { params: { limit: 200 } });
        const items = (data && data.items) || [];
        this.items = items.filter((it) => this.isImageItem(it));
      } catch (e) {
        this.$message.error('加载图片库失败');
        this.items = [];
      } finally {
        this.loading = false;
      }
    },
    uploadImage(options) {
      const file = options.file;
      if (!file) return;
      const formData = new FormData();
      formData.append('file', file);

      api
        .post('/files/upload', formData, { headers: { 'Content-Type': 'multipart/form-data' } })
        .then((res) => {
          this.$message.success('上传成功');
          this.reload();
          if (options.onSuccess) options.onSuccess(res && res.data ? res.data : {});
        })
        .catch((err) => {
          this.$message.error('上传失败');
          if (options.onError) options.onError(err);
        });
    },
    copyUrl(it) {
      const url = this.publicFileUrl(it.fileId);
      try {
        navigator.clipboard.writeText(url);
        this.$message.success('URL已复制');
      } catch (e) {
        // 兼容老浏览器：直接提示
        this.$alert(url, '下载地址', { confirmButtonText: '确定' });
      }
    },
    async confirmDelete(it) {
      try {
        await this.$confirm('确认删除该图片？', '删除确认', { type: 'warning' });
        await api.delete('/files/' + it.fileId);
        this.$message.success('删除成功');
        await this.reload();
      } catch (e) {
        if (e !== 'cancel') this.$message.error('删除失败');
      }
    }
  }
};
</script>

<style scoped>
.image-lib-toolbar {
  display: flex;
  gap: 10px;
  align-items: center;
  margin-bottom: 14px;
  flex-wrap: wrap;
}

.image-lib-toolbar__upload {
  display: inline-flex;
}

.image-lib-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
  max-height: 640px;
  overflow: auto;
  padding-right: 4px;
}

.image-lib-tile {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  background: #fff;
  overflow: hidden;
}

.image-lib-tile__img {
  height: 130px;
  background: #f5f7fa;
}

.image-lib-tile__img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.image-lib-tile__meta {
  padding: 10px;
}

.image-lib-tile__name {
  font-size: 12px;
  color: #606266;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-bottom: 8px;
}

.image-lib-tile__actions {
  display: flex;
  justify-content: space-between;
}

.image-lib-empty {
  padding: 30px 0;
  text-align: center;
  color: #909399;
  background: #fff;
  border: 1px dashed #ebeef5;
  border-radius: 8px;
}
</style>


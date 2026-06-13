<template>
  <div class="image-picker">
    <div class="image-picker__row">
      <div v-if="previewUrl" class="image-picker__preview">
        <img :src="previewUrl" alt="" />
      </div>
      <el-input
        v-model="innerValue"
        :size="size"
        :placeholder="placeholder"
        clearable
      />
      <el-button
        :size="size"
        type="primary"
        icon="el-icon-picture"
        style="margin-left: 8px"
        @click="openDialog"
      >
        选择
      </el-button>
    </div>

    <el-dialog
      title="图片库"
      :visible.sync="dialogVisible"
      width="920px"
      :close-on-click-modal="false"
      @open="loadImages"
    >
      <div class="image-picker__dialog-toolbar">
        <el-input
          v-model="keyword"
          size="small"
          placeholder="搜索：文件名/扩展名"
          style="width: 320px"
          clearable
        />
        <el-button size="small" @click="loadImages" :loading="loading">刷新</el-button>
        <el-upload
          class="image-picker__upload"
          action="/api/admin/files/upload"
          :show-file-list="false"
          :auto-upload="true"
          accept="image/*"
          :http-request="uploadImage"
        >
          <el-button size="small" type="success" icon="el-icon-upload">上传并选用</el-button>
        </el-upload>
      </div>

      <div v-if="filteredItems.length" class="image-picker__grid">
        <div
          v-for="it in filteredItems"
          :key="it.fileId"
          class="image-picker__tile"
          @click="select(it)"
        >
          <img class="image-picker__tile-img" :src="publicFileUrl(it.fileId)" :alt="it.originalName || it.fileId" />
          <div class="image-picker__tile-name" :title="it.originalName || ''">
            {{ it.originalName || it.fileId }}
          </div>
        </div>
      </div>

      <div v-else class="image-picker__empty">
        暂无图片，请先上传。
      </div>

      <div slot="footer" class="image-picker__footer">
        <el-button @click="dialogVisible = false">关闭</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import api from '../api.js';

export default {
  name: 'ImageLibraryPicker',
  props: {
    value: { type: String, default: '' },
    size: { type: String, default: 'small' },
    placeholder: { type: String, default: 'https://...' }
  },
  data() {
    return {
      dialogVisible: false,
      keyword: '',
      loading: false,
      items: []
    };
  },
  computed: {
    innerValue: {
      get() {
        return this.value || '';
      },
      set(v) {
        this.$emit('input', v);
      }
    },
    previewUrl() {
      if (!this.value) return '';
      // 允许直接显示任意 URL（历史数据可能不是本图片库）
      return this.value;
    },
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
  methods: {
    openDialog() {
      this.dialogVisible = true;
    },
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
    async loadImages() {
      // 弹窗每次打开都刷新，保证新上传可见
      this.loading = true;
      try {
        const { data } = await api.get('/files/list', { params: { limit: 80 } });
        const items = (data && data.items) || [];
        this.items = items.filter((it) => this.isImageItem(it));
      } catch (e) {
        this.items = [];
        this.$message.error('加载图片库失败');
      } finally {
        this.loading = false;
      }
    },
    select(it) {
      const url = this.publicFileUrl(it.fileId);
      this.$emit('input', url);
      this.dialogVisible = false;
    },
    uploadImage(options) {
      const file = options.file;
      if (!file) return;

      const formData = new FormData();
      formData.append('file', file);

      api
        .post('/files/upload', formData, { headers: { 'Content-Type': 'multipart/form-data' } })
        .then((res) => {
          const body = res && res.data ? res.data : {};
          const url = body.downloadUrl || this.publicFileUrl(body.fileId);
          if (url) this.$emit('input', url);
          this.dialogVisible = false;
          this.$message.success('上传成功');
          this.loadImages();
          if (options.onSuccess) options.onSuccess(body);
        })
        .catch((err) => {
          this.$message.error('上传失败');
          if (options.onError) options.onError(err);
        });
    }
  }
};
</script>

<style scoped>
.image-picker__row {
  display: flex;
  align-items: center;
}

.image-picker__preview {
  width: 44px;
  height: 44px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  overflow: hidden;
  background: #fff;
  margin-right: 8px;
  flex-shrink: 0;
}

.image-picker__preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-picker__dialog-toolbar {
  display: flex;
  gap: 10px;
  align-items: center;
  margin-bottom: 12px;
  flex-wrap: wrap;
}

.image-picker__grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
  max-height: 520px;
  overflow: auto;
  padding-right: 4px;
}

.image-picker__tile {
  border: 1px solid #ebeef5;
  border-radius: 6px;
  background: #fff;
  cursor: pointer;
  overflow: hidden;
  transition: border-color 0.2s;
}

.image-picker__tile:hover {
  border-color: #409eff;
}

.image-picker__tile-img {
  width: 100%;
  height: 120px;
  object-fit: cover;
  display: block;
  background: #f5f7fa;
}

.image-picker__tile-name {
  padding: 8px 8px;
  font-size: 12px;
  color: #606266;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.image-picker__empty {
  padding: 24px 0;
  text-align: center;
  color: #909399;
  background: #fff;
  border: 1px dashed #ebeef5;
  border-radius: 8px;
}

.image-picker__footer {
  text-align: right;
}
</style>


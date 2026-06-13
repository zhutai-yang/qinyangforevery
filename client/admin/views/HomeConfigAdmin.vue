<template>
  <div class="admin-page">
    <h1 class="admin-page__title">首页配置</h1>
    <p class="admin-page__desc">控制门户首页各区块开关、排序与 JSON 配置。</p>
    <el-card shadow="never" class="admin-page__card">
      <div class="admin-toolbar">
        <el-button type="primary" icon="el-icon-check" @click="save">保存首页区块</el-button>
      </div>
      <el-table :data="blocks" stripe border size="small" row-key="block_key">
        <el-table-column label="拖拽" width="90">
          <template slot-scope="scope">
            <span
              class="drag-handle"
              draggable="true"
              @dragstart="onDragStart(scope.$index, $event)"
              @dragover.prevent
              @drop="onDrop(scope.$index, $event)"
            >
              拖拽
            </span>
          </template>
        </el-table-column>
      <el-table-column prop="block_key" label="区块键" width="160" />
      <el-table-column label="启用" width="100">
        <template slot-scope="scope">
          <el-switch v-model="scope.row.enabled" :active-value="1" :inactive-value="0" />
        </template>
      </el-table-column>
      <el-table-column label="排序">
        <template slot-scope="scope">
          <el-input v-model.number="scope.row.sort_order" size="small" />
        </template>
      </el-table-column>
      <el-table-column label="config_json">
        <template slot-scope="scope">
          <el-input v-model="scope.row.config_json" type="textarea" rows="2" />
        </template>
      </el-table-column>
    </el-table>
    </el-card>
  </div>
</template>

<script>
import api from '../api.js';

export default {
  name: 'HomeConfigAdmin',
  data() {
    return { blocks: [], draggingFromIndex: null };
  },
  mounted() {
    this.load();
  },
  methods: {
    onDragStart(fromIndex, e) {
      this.draggingFromIndex = fromIndex;
      try {
        e.dataTransfer.effectAllowed = 'move';
        e.dataTransfer.setData('text/plain', String(fromIndex));
      } catch (err) {
        // 某些环境可能不允许 dataTransfer 设置，但我们仍可依赖组件状态
      }
    },

    onDrop(toIndex, e) {
      e.preventDefault();
      const fromIndex = this.draggingFromIndex;
      this.draggingFromIndex = null;
      if (fromIndex == null || fromIndex === toIndex) return;
      if (fromIndex < 0 || toIndex < 0) return;
      if (fromIndex >= this.blocks.length || toIndex >= this.blocks.length) return;

      const moved = this.blocks.splice(fromIndex, 1)[0];
      this.blocks.splice(toIndex, 0, moved);
      // 直接重写 sort_order，后端保存时按 sort_order 排序生效
      this.blocks.forEach((b, i) => {
        b.sort_order = i;
      });
    },

    async load() {
      try {
        const { data } = await api.get('/home-config');
        this.blocks = (data.blocks || []).map((b) => ({
          ...b,
          enabled: b.enabled ? 1 : 0,
          config_json: b.config_json || ''
        }));
      } catch (e) {
        this.$message.error('加载失败');
      }
    },
    async save() {
      try {
        await api.put('/home-config', {
          blocks: this.blocks.map((b) => ({
            block_key: b.block_key,
            enabled: !!b.enabled,
            sort_order: Number(b.sort_order) || 0,
            config_json: b.config_json || null
          }))
        });
        this.$message.success('已保存');
        this.load();
      } catch (e) {
        this.$message.error('保存失败');
      }
    }
  }
};
</script>

<style scoped>
.drag-handle {
  cursor: grab;
  user-select: none;
  color: #409eff;
}

.drag-handle:active {
  cursor: grabbing;
}
</style>

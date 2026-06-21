<template>
  <div class="admin-page">
    <h1 class="admin-page__title">赛程管理</h1>
    <p class="admin-page__desc">先选择赛事，再维护该赛事下的场次、时间与轮次标签。</p>
    <el-card shadow="never" class="admin-page__card">
      <el-form inline class="admin-toolbar">
        <el-form-item label="赛事">
          <el-select v-model="eventId" placeholder="选择赛事" filterable clearable @change="loadMatches">
            <el-option v-for="e in events" :key="e.id" :label="e.name" :value="e.id" />
          </el-select>
        </el-form-item>
        <el-button type="primary" icon="el-icon-plus" :disabled="!eventId" @click="openEdit()">新建场次</el-button>
      </el-form>
      <el-table v-loading="loading" :data="matches" stripe border size="small">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="scheduled_at" label="计划时间" width="180" />
      <el-table-column label="阶段" width="120">
        <template slot-scope="scope">{{ stageName(scope.row.stage_id) }}</template>
      </el-table-column>
      <el-table-column label="场地/台号" min-width="160">
        <template slot-scope="scope">
          {{ venueName(scope.row.venue_id) || '未定' }}
          <span v-if="scope.row.table_no"> / {{ scope.row.table_no }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="round_label" label="轮次" width="100" />
      <el-table-column label="双方" min-width="180">
        <template slot-scope="scope">{{ participantsText(scope.row) }}</template>
      </el-table-column>
      <el-table-column label="比分" width="100">
        <template slot-scope="scope">{{ scoreText(scope.row.result) || '—' }}</template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template slot-scope="scope">{{ statusText(scope.row.status) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="120">
        <template slot-scope="scope">
          <el-button type="text" @click="openEdit(scope.row)">编辑</el-button>
          <el-button type="text" @click="removeMatch(scope.row)">删</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-dialog title="场次" :visible.sync="dialog" width="520px">
      <el-form label-width="100px">
        <el-form-item label="阶段">
          <div class="matches-admin__stage-select">
            <el-select v-model.number="form.stage_id" placeholder="选择阶段" filterable clearable>
              <el-option v-for="s in stages" :key="s.id" :label="s.name" :value="s.id" />
            </el-select>
            <el-button
              type="primary"
              size="mini"
              plain
              icon="el-icon-plus"
              :disabled="!eventId"
              @click="openStageDialog"
            >
              新建阶段
            </el-button>
          </div>
        </el-form-item>
        <el-form-item label="计划时间">
          <el-date-picker
            v-model="form.scheduled_at"
            type="datetime"
            placeholder="选择计划时间"
            value-format="yyyy-MM-dd HH:mm:ss"
            format="yyyy-MM-dd HH:mm:ss"
            :editable="false"
            clearable
          />
        </el-form-item>
        <el-form-item label="场馆">
          <el-select v-model.number="form.venue_id" placeholder="选择场馆" filterable clearable>
            <el-option v-for="v in venues" :key="v.id" :label="venueLabel(v)" :value="v.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="台号"><el-input v-model="form.table_no" placeholder="例如：T1" /></el-form-item>
        <el-form-item label="轮次"><el-input v-model="form.round_label" /></el-form-item>
        <el-form-item label="双方选手">
          <div class="matches-admin__participants">
            <el-select v-model.number="form.home_athlete_id" placeholder="主队/左侧" filterable clearable>
              <el-option v-for="a in athletes" :key="a.id" :label="athleteLabel(a)" :value="a.id" />
            </el-select>
            <span class="matches-admin__vs">vs</span>
            <el-select v-model.number="form.away_athlete_id" placeholder="客队/右侧" filterable clearable>
              <el-option v-for="a in athletes" :key="a.id" :label="athleteLabel(a)" :value="a.id" />
            </el-select>
          </div>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status">
            <el-option label="已安排" value="scheduled" />
            <el-option label="已结束" value="finished" />
          </el-select>
        </el-form-item>
        <template v-if="form.status === 'finished'">
          <el-form-item label="比分">
            <div class="matches-admin__score">
              <el-input-number v-model="form.score_home" :min="0" :max="99" controls-position="right" />
              <span class="matches-admin__vs">:</span>
              <el-input-number v-model="form.score_away" :min="0" :max="99" controls-position="right" />
            </div>
          </el-form-item>
          <el-form-item label="胜者">
            <el-select v-model.number="form.winner_athlete_id" placeholder="选择胜者" clearable>
              <el-option
                v-for="a in selectedAthletes"
                :key="a.id"
                :label="athleteLabel(a)"
                :value="a.id"
              />
            </el-select>
          </el-form-item>
        </template>
      </el-form>
      <span slot="footer">
        <el-button @click="dialog = false">取消</el-button>
        <el-button type="primary" @click="saveMatch">保存</el-button>
      </span>
    </el-dialog>

    <el-dialog title="阶段管理（绑定赛事）" :visible.sync="stageDialog" width="520px">
      <el-form label-width="110px">
        <el-form-item label="阶段名称" required>
          <el-input v-model="stageForm.name" placeholder="例如：第一阶段/小组赛/淘汰赛" />
        </el-form-item>
        <el-form-item label="赛段类型(可选)">
          <el-input v-model="stageForm.stage_type" placeholder="可留空" />
        </el-form-item>
        <el-form-item label="排序(可选)">
          <el-input v-model.number="stageForm.sort_order" placeholder="数值越小越靠前" />
        </el-form-item>
        <el-form-item>
          <span class="matches-admin__hint">
            当前赛事：{{ eventId }}
          </span>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="stageDialog = false">取消</el-button>
        <el-button type="primary" :loading="stageSaving" @click="saveStage">保存阶段</el-button>
      </span>
    </el-dialog>
    </el-card>
  </div>
</template>

<script>
import api from '../api.js';
import { matchStatusText, scoreText } from '../../shared/statusLabels.js';

export default {
  name: 'MatchesAdmin',
  data() {
    return {
      events: [],
      venues: [],
      athletes: [],
      eventId: null,
      stages: [],
      matches: [],
      loading: false,
      dialog: false,
      form: {},

      // stage management dialog (bound to current eventId)
      stageDialog: false,
      stageSaving: false,
      stageForm: { name: '', stage_type: '', sort_order: 0 }
    };
  },
  async mounted() {
    await Promise.all([this.loadVenues(), this.loadAthletes(), this.loadEvents()]);
  },
  computed: {
    selectedAthletes() {
      const ids = [this.form.home_athlete_id, this.form.away_athlete_id].filter(Boolean);
      return this.athletes.filter((a) => ids.includes(a.id));
    }
  },
  methods: {
    scoreText,
    openStageDialog() {
      if (!this.eventId) return;
      const maxSort =
        (this.stages || []).reduce((acc, s) => Math.max(acc, Number(s.sort_order) || 0), -1) + 1;
      this.stageForm = { name: '', stage_type: '', sort_order: maxSort };
      this.stageDialog = true;
    },

    async saveStage() {
      if (!this.eventId) {
        this.$message.error('请先选择赛事');
        return;
      }
      const name = (this.stageForm.name || '').trim();
      if (!name) {
        this.$message.error('请填写阶段名称');
        return;
      }

      this.stageSaving = true;
      try {
        const body = {
          name,
          stage_type: this.stageForm.stage_type ? this.stageForm.stage_type.trim() : null,
          sort_order: this.stageForm.sort_order != null ? Number(this.stageForm.sort_order) : 0
        };
        const { data: created } = await api.post('/events/' + this.eventId + '/stages', body);
        this.$message.success('阶段已创建');
        this.stageDialog = false;
        this.stageForm = { name: '', stage_type: '', sort_order: 0 };

        await this.loadStages();
        // 创建成功后，尽量把新阶段回填到当前编辑的场次里
        if (this.dialog && created && created.id) {
          this.form.stage_id = created.id;
        }
      } catch (e) {
        this.$message.error('保存阶段失败');
      } finally {
        this.stageSaving = false;
      }
    },

    async loadStages() {
      if (!this.eventId) {
        this.stages = [];
        return;
      }
      try {
        const { data } = await api.get('/events/' + this.eventId + '/stages');
        this.stages = data.list || [];
      } catch (e) {
        this.$message.error('加载阶段失败');
        this.stages = [];
      }
    },
    statusText(status) {
      return matchStatusText(status);
    },
    stageName(id) {
      const row = this.stages.find((s) => s.id === id);
      return row ? row.name : '—';
    },
    venueName(id) {
      const row = this.venues.find((v) => v.id === id);
      return row ? row.name : '';
    },
    venueLabel(row) {
      return row.address ? row.name + '（' + row.address + '）' : row.name;
    },
    athleteLabel(row) {
      return row.association ? row.name + '（' + row.association + '）' : row.name;
    },
    participantsText(row) {
      const parts = row.participants || [];
      if (!parts.length) return '待定';
      return parts.map((p) => p.athlete_name || p.athlete_id).join(' vs ');
    },
    async loadVenues() {
      try {
        const { data } = await api.get('/venues');
        this.venues = data.list || [];
      } catch (e) {
        this.venues = [];
      }
    },
    async loadAthletes() {
      try {
        const { data } = await api.get('/athletes', { params: { page: 1, pageSize: 500 } });
        this.athletes = data.list || [];
      } catch (e) {
        this.athletes = [];
      }
    },
    async loadEvents() {
      try {
        const { data } = await api.get('/events', { params: { page: 1, pageSize: 500 } });
        this.events = data.list || [];
        if (this.events.length && !this.eventId) {
          this.eventId = this.events[0].id;
          this.loadMatches();
        }
      } catch (e) {
        this.$message.error('加载赛事失败');
      }
    },
    async loadMatches() {
      if (!this.eventId) return;
      this.loading = true;
      try {
        await this.loadStages();
        const { data } = await api.get('/events/' + this.eventId + '/matches');
        this.matches = data.list || [];
      } catch (e) {
        this.$message.error('加载赛程失败');
      } finally {
        this.loading = false;
      }
    },
    openEdit(row) {
      const participants = row && row.participants ? row.participants : [];
      const result = row && row.result ? row.result : {};
      this.form = row
        ? {
          ...row,
          home_athlete_id: participants[0] ? participants[0].athlete_id : null,
          away_athlete_id: participants[1] ? participants[1].athlete_id : null,
          score_home: result.score_home != null ? result.score_home : 0,
          score_away: result.score_away != null ? result.score_away : 0,
          winner_athlete_id: result.winner_athlete_id || null
        }
        : {
          stage_id: this.stages.length ? this.stages[0].id : null,
          scheduled_at: null,
          venue_id: null,
          table_no: '',
          round_label: '',
          status: 'scheduled',
          home_athlete_id: null,
          away_athlete_id: null,
          score_home: 0,
          score_away: 0,
          winner_athlete_id: null
        };
      this.dialog = true;
    },
    matchBody() {
      return {
        stage_id: this.form.stage_id,
        scheduled_at: this.form.scheduled_at,
        venue_id: this.form.venue_id || null,
        table_no: this.form.table_no || null,
        round_label: this.form.round_label,
        status: this.form.status
      };
    },
    participantIds() {
      return [this.form.home_athlete_id, this.form.away_athlete_id].filter(Boolean);
    },
    validateParticipants() {
      const ids = this.participantIds();
      if (ids.length === 1) {
        this.$message.error('双方选手需同时填写或同时留空');
        return false;
      }
      if (ids.length === 2 && ids[0] === ids[1]) {
        this.$message.error('双方选手不能相同');
        return false;
      }
      return true;
    },
    async saveMatch() {
      try {
        if (!this.validateParticipants()) return;
        let saved;
        if (this.form.id) {
          const { data } = await api.put('/matches/' + this.form.id, this.matchBody());
          saved = data;
        } else {
          const { data } = await api.post('/events/' + this.eventId + '/matches', this.matchBody());
          saved = data;
        }
        const matchId = this.form.id || (saved && saved.id);
        const ids = this.participantIds();
        if (matchId && ids.length === 2) {
          await api.put('/matches/' + matchId + '/participants', { athleteIds: ids });
        }
        if (matchId && this.form.status === 'finished') {
          await api.put('/matches/' + matchId + '/result', {
            score_home: this.form.score_home || 0,
            score_away: this.form.score_away || 0,
            winner_athlete_id: this.form.winner_athlete_id || null
          });
        }
        this.$message.success('已保存');
        this.dialog = false;
        this.loadMatches();
      } catch (e) {
        this.$message.error('保存失败（需先有阶段 stage_id）');
      }
    },
    async removeMatch(row) {
      try {
        await this.$confirm('确认删除场次？');
        await api.delete('/matches/' + row.id);
        this.loadMatches();
      } catch (e) {
        if (e !== 'cancel') this.$message.error('删除失败');
      }
    }
  }
};
</script>

<style scoped>
.matches-admin__stage-select {
  display: flex;
  align-items: center;
  gap: 8px;
}

.matches-admin__participants,
.matches-admin__score {
  display: flex;
  align-items: center;
  gap: 8px;
}

.matches-admin__participants .el-select {
  flex: 1;
}

.matches-admin__vs {
  color: #909399;
  font-size: 12px;
}

.matches-admin__hint {
  color: #909399;
  font-size: 12px;
  padding-left: 6px;
}
</style>


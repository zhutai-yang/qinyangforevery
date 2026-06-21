-- TT 主 DDL 基线 (MySQL 8.0)
-- 说明：本文件为完整权威源；schema-slice*.sql 仅作历史参照；migration/ 为 Flyway 部署载体
-- 乒乓球赛事管理系统
CREATE DATABASE IF NOT EXISTS tt_event DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE tt_event;

CREATE TABLE IF NOT EXISTS fnd_role (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  code VARCHAR(64) NOT NULL UNIQUE,
  name VARCHAR(128) NOT NULL
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS fnd_user (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(64) NOT NULL UNIQUE,
  password_hash VARCHAR(256) NOT NULL,
  status VARCHAR(32) NOT NULL DEFAULT 'active',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS fnd_user_role (
  user_id BIGINT NOT NULL,
  role_id BIGINT NOT NULL,
  PRIMARY KEY (user_id, role_id),
  CONSTRAINT fk_fnd_user_role_user FOREIGN KEY (user_id) REFERENCES fnd_user(id),
  CONSTRAINT fk_fnd_user_role_role FOREIGN KEY (role_id) REFERENCES fnd_role(id)
) ENGINE=InnoDB;

-- 登录限流（按 IP，与 LoginRateLimiter 对应；不依赖 Redis）
CREATE TABLE IF NOT EXISTS sys_login_rate (
  ip VARCHAR(64) NOT NULL PRIMARY KEY,
  attempt_count INT NOT NULL DEFAULT 0,
  window_start_ms BIGINT NOT NULL,
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS fnd_dict_type (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  code VARCHAR(64) NOT NULL UNIQUE,
  name VARCHAR(128) NOT NULL
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS fnd_dict_item (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  type_id BIGINT NOT NULL,
  code VARCHAR(64) NOT NULL,
  label VARCHAR(128) NOT NULL,
  sort_order INT NOT NULL DEFAULT 0,
  CONSTRAINT fk_dict_item_type FOREIGN KEY (type_id) REFERENCES fnd_dict_type(id),
  UNIQUE KEY uq_dict_type_code (type_id, code)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS evt_event (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(256) NOT NULL,
  edition VARCHAR(64) NULL,
  level_code VARCHAR(64) NULL,
  start_date DATE NULL,
  end_date DATE NULL,
  location VARCHAR(512) NULL,
  status VARCHAR(32) NOT NULL DEFAULT 'draft',
  description TEXT NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  KEY ix_evt_event_status (status, start_date)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS evt_venue (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(256) NOT NULL,
  address VARCHAR(512) NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS evt_event_venue (
  event_id BIGINT NOT NULL,
  venue_id BIGINT NOT NULL,
  PRIMARY KEY (event_id, venue_id),
  CONSTRAINT fk_evv_event FOREIGN KEY (event_id) REFERENCES evt_event(id) ON DELETE CASCADE,
  CONSTRAINT fk_evv_venue FOREIGN KEY (venue_id) REFERENCES evt_venue(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS reg_athlete (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(128) NOT NULL,
  name_en VARCHAR(128) NULL,
  gender VARCHAR(16) NULL,
  birth_date DATE NULL,
  birth_place VARCHAR(256) NULL,
  nationality VARCHAR(128) NULL,
  height_cm INT NULL,
  dominant_hand VARCHAR(32) NULL,
  playing_style VARCHAR(256) NULL,
  association VARCHAR(256) NULL,
  profile_title VARCHAR(256) NULL,
  profile_summary VARCHAR(1024) NULL,
  hero_image_url VARCHAR(512) NULL,
  social_url VARCHAR(512) NULL,
  current_world_rank INT NULL,
  highest_world_rank INT NULL,
  ranking_points INT NULL,
  major_identity VARCHAR(512) NULL,
  source_urls TEXT NULL,
  extra_profile_json MEDIUMTEXT NULL,
  data_collected_at DATETIME(3) NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  KEY ix_reg_athlete_name (name),
  KEY ix_reg_athlete_rank (current_world_rank)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS reg_athlete_achievement (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  athlete_id BIGINT NOT NULL,
  year INT NULL,
  event_name VARCHAR(256) NOT NULL,
  category VARCHAR(128) NULL,
  result_label VARCHAR(128) NULL,
  partner_or_team VARCHAR(256) NULL,
  opponent VARCHAR(256) NULL,
  score VARCHAR(64) NULL,
  source_url VARCHAR(2048) NULL,
  sort_order INT NOT NULL DEFAULT 0,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  CONSTRAINT fk_athlete_achievement_athlete FOREIGN KEY (athlete_id) REFERENCES reg_athlete(id) ON DELETE CASCADE,
  KEY ix_athlete_achievement_athlete_sort (athlete_id, sort_order, id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS reg_athlete_result (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  athlete_id BIGINT NOT NULL,
  match_date DATE NULL,
  event_name VARCHAR(256) NOT NULL,
  category VARCHAR(128) NULL,
  result_label VARCHAR(128) NULL,
  partner_or_team VARCHAR(256) NULL,
  opponent VARCHAR(256) NULL,
  score VARCHAR(64) NULL,
  source_url VARCHAR(2048) NULL,
  sort_order INT NOT NULL DEFAULT 0,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  CONSTRAINT fk_athlete_result_athlete FOREIGN KEY (athlete_id) REFERENCES reg_athlete(id) ON DELETE CASCADE,
  KEY ix_athlete_result_athlete_sort (athlete_id, sort_order, id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS reg_athlete_upcoming_event (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  athlete_id BIGINT NOT NULL,
  start_date DATE NULL,
  end_date DATE NULL,
  event_name VARCHAR(256) NOT NULL,
  level_label VARCHAR(128) NULL,
  location VARCHAR(256) NULL,
  venue VARCHAR(256) NULL,
  status VARCHAR(64) NULL,
  note VARCHAR(1024) NULL,
  source_url VARCHAR(2048) NULL,
  sort_order INT NOT NULL DEFAULT 0,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  CONSTRAINT fk_athlete_upcoming_event_athlete FOREIGN KEY (athlete_id) REFERENCES reg_athlete(id) ON DELETE CASCADE,
  KEY ix_athlete_upcoming_event_athlete_date (athlete_id, start_date, id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS reg_athlete_data_source (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  athlete_id BIGINT NOT NULL,
  source_name VARCHAR(256) NOT NULL,
  source_url VARCHAR(2048) NOT NULL,
  retrieved_at DATETIME(3) NULL,
  note VARCHAR(1024) NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  CONSTRAINT fk_athlete_data_source_athlete FOREIGN KEY (athlete_id) REFERENCES reg_athlete(id) ON DELETE CASCADE,
  KEY ix_athlete_data_source_athlete (athlete_id, id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS reg_athlete_import_snapshot (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  athlete_id BIGINT NOT NULL,
  source_name VARCHAR(128) NOT NULL DEFAULT 'quick_import',
  payload_json MEDIUMTEXT NOT NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  CONSTRAINT fk_athlete_import_snapshot_athlete FOREIGN KEY (athlete_id) REFERENCES reg_athlete(id) ON DELETE CASCADE,
  KEY ix_athlete_import_snapshot_athlete (athlete_id, created_at)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS sch_stage (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  event_id BIGINT NOT NULL,
  name VARCHAR(128) NOT NULL,
  stage_type VARCHAR(64) NULL,
  sort_order INT NOT NULL DEFAULT 0,
  CONSTRAINT fk_sch_stage_event FOREIGN KEY (event_id) REFERENCES evt_event(id) ON DELETE CASCADE,
  KEY ix_sch_stage_event (event_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `sch_match` (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  event_id BIGINT NOT NULL,
  stage_id BIGINT NOT NULL,
  scheduled_at DATETIME(3) NULL,
  venue_id BIGINT NULL,
  round_label VARCHAR(64) NULL,
  table_no VARCHAR(32) NULL,
  status VARCHAR(32) NOT NULL DEFAULT 'scheduled',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  CONSTRAINT fk_sch_match_event FOREIGN KEY (event_id) REFERENCES evt_event(id) ON DELETE CASCADE,
  CONSTRAINT fk_sch_match_stage FOREIGN KEY (stage_id) REFERENCES sch_stage(id),
  CONSTRAINT fk_sch_match_venue FOREIGN KEY (venue_id) REFERENCES evt_venue(id),
  KEY ix_sch_match_event_time (event_id, scheduled_at),
  KEY ix_sch_match_stage (stage_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `sch_match_participant` (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  match_id BIGINT NOT NULL,
  athlete_id BIGINT NOT NULL,
  side_order INT NOT NULL,
  CONSTRAINT fk_smp_match FOREIGN KEY (match_id) REFERENCES `sch_match`(id) ON DELETE CASCADE,
  CONSTRAINT fk_smp_athlete FOREIGN KEY (athlete_id) REFERENCES reg_athlete(id),
  UNIQUE KEY uq_smp_match_side (match_id, side_order),
  KEY ix_smp_athlete (athlete_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS rec_result (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  match_id BIGINT NOT NULL UNIQUE,
  winner_athlete_id BIGINT NULL,
  score_home INT NOT NULL DEFAULT 0,
  score_away INT NOT NULL DEFAULT 0,
  finished_at DATETIME(3) NULL,
  CONSTRAINT fk_rec_match FOREIGN KEY (match_id) REFERENCES `sch_match`(id) ON DELETE CASCADE,
  CONSTRAINT fk_rec_winner FOREIGN KEY (winner_athlete_id) REFERENCES reg_athlete(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS gov_audit_log (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NULL,
  action VARCHAR(64) NOT NULL,
  entity VARCHAR(64) NOT NULL,
  entity_id VARCHAR(64) NULL,
  payload_summary TEXT NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  CONSTRAINT fk_audit_user FOREIGN KEY (user_id) REFERENCES fnd_user(id),
  KEY ix_audit_user_time (user_id, created_at)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS cms_article (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  title VARCHAR(512) NOT NULL,
  slug VARCHAR(256) NULL,
  summary VARCHAR(1024) NULL,
  body MEDIUMTEXT NULL,
  cover_url VARCHAR(512) NULL,
  status VARCHAR(32) NOT NULL DEFAULT 'draft',
  published_at DATETIME(3) NULL,
  pinned TINYINT(1) NOT NULL DEFAULT 0,
  event_id BIGINT NULL,
  athlete_id BIGINT NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  CONSTRAINT fk_cms_event FOREIGN KEY (event_id) REFERENCES evt_event(id) ON DELETE SET NULL,
  CONSTRAINT fk_cms_athlete FOREIGN KEY (athlete_id) REFERENCES reg_athlete(id) ON DELETE SET NULL,
  KEY ix_cms_published (status, published_at DESC)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS cfg_featured_athlete (
  athlete_id BIGINT NOT NULL PRIMARY KEY,
  sort_order INT NOT NULL DEFAULT 0,
  enabled TINYINT(1) NOT NULL DEFAULT 1,
  CONSTRAINT fk_cfg_fa_athlete FOREIGN KEY (athlete_id) REFERENCES reg_athlete(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS cfg_home_block (
  block_key VARCHAR(64) NOT NULL PRIMARY KEY,
  enabled TINYINT(1) NOT NULL DEFAULT 1,
  sort_order INT NOT NULL DEFAULT 0,
  config_json TEXT NULL
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS ext_data_source (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(256) NOT NULL,
  base_url VARCHAR(1024) NOT NULL,
  parser_type VARCHAR(64) NOT NULL DEFAULT 'html_v1',
  enabled TINYINT(1) NOT NULL DEFAULT 1,
  interval_minutes INT NOT NULL DEFAULT 60,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS ext_sync_log (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  source_id BIGINT NOT NULL,
  started_at DATETIME(3) NOT NULL,
  ended_at DATETIME(3) NULL,
  status VARCHAR(32) NOT NULL,
  error_message TEXT NULL,
  fetched_count INT NOT NULL DEFAULT 0,
  CONSTRAINT fk_ext_log_source FOREIGN KEY (source_id) REFERENCES ext_data_source(id) ON DELETE CASCADE,
  KEY ix_ext_log_source (source_id, started_at DESC)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS ext_feed_item (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  source_id BIGINT NOT NULL,
  external_url VARCHAR(2048) NOT NULL,
  title VARCHAR(512) NOT NULL,
  summary TEXT NULL,
  published_at_ext DATETIME(3) NULL,
  raw_hash VARCHAR(64) NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  CONSTRAINT fk_ext_feed_source FOREIGN KEY (source_id) REFERENCES ext_data_source(id) ON DELETE CASCADE,
  UNIQUE KEY uq_ext_feed_url (source_id, external_url(512)),
  KEY ix_ext_feed_source_time (source_id, created_at DESC)
) ENGINE=InnoDB;

-- 球员高光展示（与球员一对多关联）
CREATE TABLE IF NOT EXISTS cfg_athlete_highlight (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  athlete_id BIGINT NOT NULL,
  title VARCHAR(512) NOT NULL,
  cover_url VARCHAR(512) NULL,
  summary VARCHAR(1024) NULL,
  sort_order INT NOT NULL DEFAULT 0,
  status VARCHAR(32) NOT NULL DEFAULT 'draft',
  published_at DATETIME(3) NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  CONSTRAINT fk_highlight_athlete FOREIGN KEY (athlete_id) REFERENCES reg_athlete(id) ON DELETE CASCADE,
  KEY ix_highlight_athlete_sort (athlete_id, sort_order, id)
) ENGINE=InnoDB;

-- 球员商务预告（与球员一对多关联）
CREATE TABLE IF NOT EXISTS cfg_athlete_business_preview (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  athlete_id BIGINT NOT NULL,
  title VARCHAR(512) NOT NULL,
  cover_url VARCHAR(512) NULL,
  summary VARCHAR(1024) NULL,
  link_url VARCHAR(2048) NULL,
  link_text VARCHAR(128) NULL,
  scheduled_at DATETIME(3) NULL,
  sort_order INT NOT NULL DEFAULT 0,
  status VARCHAR(32) NOT NULL DEFAULT 'draft',
  published_at DATETIME(3) NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  CONSTRAINT fk_business_preview_athlete FOREIGN KEY (athlete_id) REFERENCES reg_athlete(id) ON DELETE CASCADE,
  KEY ix_business_preview_athlete_sort (athlete_id, sort_order, id)
) ENGINE=InnoDB;

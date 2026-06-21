ALTER TABLE reg_athlete
  ADD COLUMN name_en VARCHAR(128) NULL AFTER name,
  ADD COLUMN birth_place VARCHAR(256) NULL AFTER birth_date,
  ADD COLUMN nationality VARCHAR(128) NULL AFTER birth_place,
  ADD COLUMN height_cm INT NULL AFTER nationality,
  ADD COLUMN dominant_hand VARCHAR(32) NULL AFTER height_cm,
  ADD COLUMN playing_style VARCHAR(256) NULL AFTER dominant_hand,
  ADD COLUMN current_world_rank INT NULL AFTER social_url,
  ADD COLUMN highest_world_rank INT NULL AFTER current_world_rank,
  ADD COLUMN ranking_points INT NULL AFTER highest_world_rank,
  ADD COLUMN major_identity VARCHAR(512) NULL AFTER ranking_points,
  ADD COLUMN source_urls TEXT NULL AFTER major_identity,
  ADD COLUMN extra_profile_json MEDIUMTEXT NULL AFTER source_urls,
  ADD COLUMN data_collected_at DATETIME(3) NULL AFTER extra_profile_json,
  ADD COLUMN updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) AFTER created_at,
  ADD KEY ix_reg_athlete_name (name),
  ADD KEY ix_reg_athlete_rank (current_world_rank);

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

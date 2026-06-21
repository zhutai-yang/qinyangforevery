ALTER TABLE reg_athlete
  ADD COLUMN profile_title VARCHAR(256) NULL AFTER association,
  ADD COLUMN profile_summary VARCHAR(1024) NULL AFTER profile_title,
  ADD COLUMN hero_image_url VARCHAR(512) NULL AFTER profile_summary,
  ADD COLUMN social_url VARCHAR(512) NULL AFTER hero_image_url;

-- Add article_views table to track view counts
create table article_views (
  article_id varchar(255) primary key,
  view_count integer not null default 0
);

-- Add created_at to article_favorites for trending calculation
-- SQLite does not support non-constant defaults in ALTER TABLE, so we use a literal default
alter table article_favorites add column created_at TIMESTAMP DEFAULT '2025-01-01 00:00:00';

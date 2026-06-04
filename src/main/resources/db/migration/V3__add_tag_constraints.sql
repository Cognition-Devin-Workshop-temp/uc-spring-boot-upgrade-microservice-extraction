-- Add UNIQUE index on tags.name to enforce tag name uniqueness
CREATE UNIQUE INDEX IF NOT EXISTS idx_tags_name ON tags(name);

-- Recreate article_tags with composite primary key
CREATE TABLE article_tags_new (
  article_id varchar(255) NOT NULL,
  tag_id varchar(255) NOT NULL,
  PRIMARY KEY (article_id, tag_id)
);
INSERT OR IGNORE INTO article_tags_new (article_id, tag_id) SELECT article_id, tag_id FROM article_tags;
DROP TABLE article_tags;
ALTER TABLE article_tags_new RENAME TO article_tags;

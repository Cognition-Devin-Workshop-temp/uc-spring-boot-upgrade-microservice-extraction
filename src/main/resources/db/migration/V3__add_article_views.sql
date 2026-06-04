create table article_views (
  id varchar(255) primary key,
  article_id varchar(255) not null,
  user_id varchar(255),
  view_count integer not null default 1,
  viewed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

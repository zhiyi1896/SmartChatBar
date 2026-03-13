CREATE TABLE IF NOT EXISTS user (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  email VARCHAR(100) NOT NULL UNIQUE,
  password VARCHAR(255) NOT NULL,
  nickname VARCHAR(50) NOT NULL,
  avatar VARCHAR(255) DEFAULT NULL,
  bio VARCHAR(255) DEFAULT NULL,
  status TINYINT DEFAULT 1,
  role VARCHAR(20) DEFAULT 'USER',
  received_like_count INT DEFAULT 0,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_user_role (role)
);

CREATE TABLE IF NOT EXISTS post (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  title VARCHAR(100) NOT NULL,
  content TEXT NOT NULL,
  status TINYINT DEFAULT 1,
  is_top TINYINT DEFAULT 0,
  is_wonderful TINYINT DEFAULT 0,
  score DOUBLE DEFAULT 0,
  view_count INT DEFAULT 0,
  like_count INT DEFAULT 0,
  comment_count INT DEFAULT 0,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_post_user (user_id),
  KEY idx_post_status_time (status, create_time),
  KEY idx_post_hot (is_top, is_wonderful, score)
);

CREATE TABLE IF NOT EXISTS comment (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  target_id BIGINT NOT NULL,
  parent_id BIGINT DEFAULT 0,
  user_id BIGINT NOT NULL,
  reply_user_id BIGINT DEFAULT NULL,
  type TINYINT NOT NULL,
  content TEXT NOT NULL,
  like_count INT DEFAULT 0,
  status TINYINT DEFAULT 1,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_comment_target (target_id, type, status),
  KEY idx_comment_parent (parent_id),
  KEY idx_comment_user (user_id)
);

CREATE TABLE IF NOT EXISTS conversation (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user1_id BIGINT NOT NULL,
  user2_id BIGINT NOT NULL,
  last_message VARCHAR(500) DEFAULT NULL,
  last_message_time DATETIME DEFAULT NULL,
  unread_count_user1 INT DEFAULT 0,
  unread_count_user2 INT DEFAULT 0,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_users (user1_id, user2_id),
  KEY idx_conv_user1 (user1_id, update_time),
  KEY idx_conv_user2 (user2_id, update_time)
);

CREATE TABLE IF NOT EXISTS message (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  conversation_id BIGINT NOT NULL,
  from_user_id BIGINT NOT NULL,
  to_user_id BIGINT NOT NULL,
  content TEXT NOT NULL,
  is_read TINYINT DEFAULT 0,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  KEY idx_message_conv_time (conversation_id, create_time),
  KEY idx_message_to_user (to_user_id, is_read)
);

CREATE TABLE IF NOT EXISTS notification (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  sender_id BIGINT DEFAULT NULL,
  type VARCHAR(30) NOT NULL,
  content VARCHAR(500) NOT NULL,
  related_id BIGINT DEFAULT NULL,
  is_read TINYINT DEFAULT 0,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  KEY idx_notice_user_read (user_id, is_read, create_time)
);

CREATE TABLE IF NOT EXISTS sensitive_word (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  word VARCHAR(100) NOT NULL UNIQUE,
  level TINYINT DEFAULT 1,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

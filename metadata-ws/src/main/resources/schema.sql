CREATE TABLE IF NOT EXISTS security_user (
  preferred_username VARCHAR(255) NOT NULL,
  role  VARCHAR(255),
  PRIMARY KEY (preferred_username)
);
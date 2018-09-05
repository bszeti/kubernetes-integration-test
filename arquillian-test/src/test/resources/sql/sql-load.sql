CREATE TABLE users (
  email VARCHAR(256),
  phone VARCHAR(64),
  ts_created DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (email)
);
INSERT INTO users (email, phone) VALUES ('testSucc@test.com', '5551234567');
INSERT INTO users (email, phone) VALUES ('testRetry@test.com', '5551230000');
select * from users;
exit

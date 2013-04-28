# --- For heroku deployment - use PostgreSQL - see http://www.postgresql.org/
# --- Must also work with H2 - test locally

# --- !Ups

CREATE TABLE
  users
(
  id SERIAL PRIMARY KEY,
  first_name VARCHAR(127) NOT NULL,
  last_name VARCHAR(127) DEFAULT '' NOT NULL,
  email VARCHAR(127) NOT NULL,
  phone VARCHAR(127) DEFAULT '',
  permission VARCHAR(20) DEFAULT 'NormalUser' NOT NULL,
  pwd VARCHAR(127) DEFAULT '*',
  CONSTRAINT unique_email UNIQUE (email)
);


# --- !Downs

DROP TABLE IF EXISTS users;

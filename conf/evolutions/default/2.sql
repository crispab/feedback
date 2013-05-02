# --- For heroku deployment - use PostgreSQL - see http://www.postgresql.org/
# --- Must also work with H2 - test locally

# --- !Ups

CREATE TABLE
  polls
(
  id SERIAL PRIMARY KEY,
  uuid VARCHAR(127) NOT NULL,
  customer VARCHAR(127) NOT NULL,
  contact_person VARCHAR(127) DEFAULT '' NOT NULL,
  assignment VARCHAR(127) DEFAULT '',
  consultant INTEGER NOT NULL,
  is_open BOOLEAN DEFAULT TRUE NOT NULL,
  FOREIGN KEY(consultant) REFERENCES users(id),
  CONSTRAINT unique_uuid UNIQUE (uuid)
);


# --- !Downs

DROP TABLE IF EXISTS polls;

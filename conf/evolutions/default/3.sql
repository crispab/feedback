# --- For heroku deployment - use PostgreSQL - see http://www.postgresql.org/
# --- Must also work with H2 - test locally

# --- !Ups

CREATE TABLE
  metrics
(
  id SERIAL PRIMARY KEY,
  whenx TIMESTAMP NOT NULL,
  poll INTEGER NOT NULL,
  score INTEGER NOT NULL,
  comment VARCHAR(256) DEFAULT '' NOT NULL,
  FOREIGN KEY(poll) REFERENCES polls(id)
);


# --- !Downs

DROP TABLE IF EXISTS metrics;

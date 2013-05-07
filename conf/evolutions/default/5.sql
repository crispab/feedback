# --- For heroku deployment - use PostgreSQL - see http://www.postgresql.org/
# --- Must also work with H2 - test locally

# --- !Ups

ALTER TABLE metrics ADD COLUMN name VARCHAR(127) DEFAULT '';


# --- !Downs

ALTER TABLE metrics DROP COLUMN name;

# --- For heroku deployment - use PostgreSQL - see http://www.postgresql.org/
# --- Must also work with H2 - test locally

# --- !Ups

CREATE INDEX metrics_poll ON metrics(poll);


# --- !Downs

DROP INDEX IF EXISTS metrics_poll;

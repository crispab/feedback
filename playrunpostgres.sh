#!/bin/sh
# Start play overriding database config with config for PostgreSQL (rather than H2)

play debug -Dconfig.file=conf/postgres.conf run


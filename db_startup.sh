#!/bin/bash
set -e
export PGPASSWORD=$POSTGRES_PASSWORD

until pg_isready -h $POSTGRES_HOST -U $POSTGRES_USER; do
  echo 'Waiting for postgres...'
  sleep 1
done

# Create Keycloak user and database
psql -h $POSTGRES_HOST -U $POSTGRES_USER -c "SELECT 1 FROM pg_roles WHERE rolname='$KEYCLOAK_USER'" | grep -q 1 || psql -h $POSTGRES_HOST -U $POSTGRES_USER -c "CREATE USER $KEYCLOAK_USER WITH PASSWORD '$KEYCLOAK_PASSWORD';"
psql -h $POSTGRES_HOST -U $POSTGRES_USER -c "CREATE DATABASE $KEYCLOAK_DB WITH OWNER = $KEYCLOAK_USER ENCODING = 'UTF8' LC_COLLATE = 'en_US.utf8' LC_CTYPE = 'en_US.utf8' TEMPLATE = template0;" || true

# Create Nessie user and database
psql -h $POSTGRES_HOST -U $POSTGRES_USER -c "SELECT 1 FROM pg_roles WHERE rolname='$NESSIE_USER'" | grep -q 1 || psql -h $POSTGRES_HOST -U $POSTGRES_USER -c "CREATE USER $NESSIE_USER WITH PASSWORD '$NESSIE_PASSWORD';"
psql -h $POSTGRES_HOST -U $POSTGRES_USER -c "CREATE DATABASE $NESSIE_DB WITH OWNER = $NESSIE_USER ENCODING = 'UTF8' LC_COLLATE = 'en_US.utf8' LC_CTYPE = 'en_US.utf8' TEMPLATE = template0;" || true
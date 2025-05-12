#!/bin/bash
set -e

until pg_isready -h $POSTGRES_HOST -U $POSTGRES_USER; do
  echo 'Waiting for postgres...'
  sleep 1
done

# Create Keycloak user and database
psql -h $POSTGRES_HOST -U $POSTGRES_USER -c "DO \$\$
  BEGIN
    IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = '$KEYCLOAK_USER') THEN
      CREATE USER $KEYCLOAK_USER WITH PASSWORD '$KEYCLOAK_PASSWORD';
    END IF;
  END
\$\$;"

psql -h $POSTGRES_HOST -U $POSTGRES_USER -c "CREATE DATABASE $KEYCLOAK_DB OWNER $KEYCLOAK_USER WITH ENCODING 'UTF8' LC_COLLATE='en_US.utf8' LC_CTYPE='en_US.utf8' TEMPLATE template0;"

# Create Nessie user and database
psql -h $POSTGRES_HOST -U $POSTGRES_USER -c "DO \$\$
  BEGIN
    IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = '$NESSIE_USER') THEN
      CREATE USER $NESSIE_USER WITH PASSWORD '$NESSIE_PASSWORD';
    END IF;
  END
\$\$;"

psql -h $POSTGRES_HOST -U $POSTGRES_USER -c "CREATE DATABASE $NESSIE_DB OWNER $NESSIE_USER WITH ENCODING 'UTF8' LC_COLLATE='en_US.utf8' LC_CTYPE='en_US.utf8' TEMPLATE template0;"
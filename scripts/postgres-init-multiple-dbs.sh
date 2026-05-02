#!/usr/bin/env sh
set -eu

if [ -z "${POSTGRES_MULTIPLE_DATABASES:-}" ]; then
  exit 0
fi

echo "Creating databases: ${POSTGRES_MULTIPLE_DATABASES}"

echo "${POSTGRES_MULTIPLE_DATABASES}" | tr ',' '\n' | while IFS= read -r db; do
  db="$(echo "$db" | xargs 2>/dev/null || echo "$db" | sed 's/^[[:space:]]*//;s/[[:space:]]*$//')"
  [ -z "$db" ] && continue

  echo "Ensuring database exists: $db"
  exists="$(psql -v ON_ERROR_STOP=1 --username "${POSTGRES_USER}" --dbname postgres -tAc "SELECT 1 FROM pg_database WHERE datname='${db}'" || true)"
  if [ -z "$exists" ]; then
    psql -v ON_ERROR_STOP=1 --username "${POSTGRES_USER}" --dbname postgres -c "CREATE DATABASE \"${db}\";"
  fi

  psql -v ON_ERROR_STOP=1 --username "${POSTGRES_USER}" --dbname postgres -c "GRANT ALL PRIVILEGES ON DATABASE \"${db}\" TO \"${POSTGRES_USER}\";"
done


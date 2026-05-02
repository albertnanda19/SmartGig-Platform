#!/bin/bash
set -e

BOOTSTRAP_SERVER="kafka:29092"

echo "Waiting for Kafka to be ready..."
until docker compose exec -T kafka kafka-topics --bootstrap-server $BOOTSTRAP_SERVER --list > /dev/null 2>&1; do
  echo "Kafka not ready yet, retrying in 5 seconds..."
  sleep 5
done

echo "Kafka is ready. Creating topics..."

create_topic() {
  local topic=$1
  local partitions=$2
  local replication=$3
  local retention_ms=$4

  if docker compose exec -T kafka kafka-topics --bootstrap-server $BOOTSTRAP_SERVER --describe --topic $topic > /dev/null 2>&1; then
    echo "Topic $topic already exists, skipping..."
  else
    docker compose exec -T kafka kafka-topics \
      --bootstrap-server $BOOTSTRAP_SERVER \
      --create \
      --topic $topic \
      --partitions $partitions \
      --replication-factor $replication \
      --config retention.ms=$retention_ms \
      --config cleanup.policy=delete
    echo "Created topic: $topic"
  fi
}

create_topic "user.registered"                    3 1 604800000
create_topic "project.created"                   3 1 604800000
create_topic "project.applied"                   3 1 604800000
create_topic "project.status.changed"            3 1 604800000
create_topic "skill.trending"                    1 1 2592000000
create_topic "notification.send"                 3 1 86400000

create_topic "user.registered.DLT"               1 1 2592000000
create_topic "project.created.DLT"               1 1 2592000000
create_topic "project.applied.DLT"               1 1 2592000000
create_topic "project.status.changed.DLT"        1 1 2592000000
create_topic "notification.send.DLT"             1 1 2592000000

echo "All Kafka topics created successfully!"
echo "Topic list:"
docker compose exec -T kafka kafka-topics --bootstrap-server $BOOTSTRAP_SERVER --list


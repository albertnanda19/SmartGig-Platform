@echo off
set KAFKA_CONTAINER=smartgig-kafka
set BOOTSTRAP=kafka:29092

echo Creating Kafka topics...

docker exec %KAFKA_CONTAINER% kafka-topics --bootstrap-server %BOOTSTRAP% --create --if-not-exists --topic user.registered --partitions 3 --replication-factor 1
docker exec %KAFKA_CONTAINER% kafka-topics --bootstrap-server %BOOTSTRAP% --create --if-not-exists --topic project.created --partitions 3 --replication-factor 1
docker exec %KAFKA_CONTAINER% kafka-topics --bootstrap-server %BOOTSTRAP% --create --if-not-exists --topic project.applied --partitions 3 --replication-factor 1
docker exec %KAFKA_CONTAINER% kafka-topics --bootstrap-server %BOOTSTRAP% --create --if-not-exists --topic project.status.changed --partitions 3 --replication-factor 1
docker exec %KAFKA_CONTAINER% kafka-topics --bootstrap-server %BOOTSTRAP% --create --if-not-exists --topic skill.trending --partitions 1 --replication-factor 1
docker exec %KAFKA_CONTAINER% kafka-topics --bootstrap-server %BOOTSTRAP% --create --if-not-exists --topic notification.send --partitions 3 --replication-factor 1
docker exec %KAFKA_CONTAINER% kafka-topics --bootstrap-server %BOOTSTRAP% --create --if-not-exists --topic user.registered.DLT --partitions 1 --replication-factor 1
docker exec %KAFKA_CONTAINER% kafka-topics --bootstrap-server %BOOTSTRAP% --create --if-not-exists --topic project.applied.DLT --partitions 1 --replication-factor 1

echo Done!


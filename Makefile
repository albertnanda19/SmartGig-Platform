.PHONY: help up down build test init-kafka logs clean monitoring

help:
	@echo "SmartGig Platform - Available Commands"
	@echo "========================================"
	@echo "make up          - Start all infrastructure (Docker)"
	@echo "make monitoring  - Start monitoring stack (Prometheus + Grafana)"
	@echo "make down        - Stop all containers"
	@echo "make build       - Build all services (skip tests)"
	@echo "make test        - Run all tests"
	@echo "make init-kafka  - Create all Kafka topics"
	@echo "make logs        - Tail logs from infrastructure"
	@echo "make clean       - Remove all containers and volumes"

up:
	docker-compose up -d
	@echo "Infrastructure started! Run 'make init-kafka' to create topics."

monitoring:
	docker-compose -f docker-compose.yml -f docker-compose.monitoring.yml up -d

down:
	docker-compose down

build:
	mvn clean package -DskipTests -T 4

test:
	mvn verify -pl common-lib,auth-service,user-service,project-service

init-kafka:
	bash scripts/init-kafka-topics.sh

logs:
	docker-compose logs -f kafka postgres redis

clean:
	docker-compose down -v
	docker system prune -f
	mvn clean

build-common:
	mvn install -pl common-lib -DskipTests

eureka:
	cd eureka-server && mvn spring-boot:run

auth:
	cd auth-service && mvn spring-boot:run -Dspring-boot.run.profiles=dev


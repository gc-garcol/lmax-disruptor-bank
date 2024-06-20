.PHONY: help

help: ## Show all commands
	@grep -E '^[a-zA-Z0-9_-]+:.*?## .*$$' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-20s\033[0m %s\n", $$1, $$2}'

setup-dev: ## Setup development environment
	docker-compose -f docker-infra.yml up -d

down-dev: ## Down development environment
	docker-compose -f docker-infra.yml down -v

build: ## Build jar
	./gradlew clean build

run-leader: ## Run leader
	./gradlew :bank-cluster-app:run-leader

run-follower: ## Run follower
	./gradlew :bank-cluster-app:run-follower

run-learner: ## Run learner
	./gradlew :bank-cluster-app:run-learner

run-admin: ## Run admin app
	./gradlew :bank-client-app-admin:run-admin

run-benchmark-tool: ## Run benchmark tool
	./gradlew :bank-cluster-benchmark:run-benchmark-tool

run-user: ## Run user app
	./gradlew :bank-client-app-admin:run-user

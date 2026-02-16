.DEFAULT_GOAL := help

.PHONY: help build run test

help: ## Show this help
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-20s\033[0m %s\n", $$1, $$2}'

build: ## Build release APK
	./gradlew assembleRelease

run: ## Install and run on connected device
	./gradlew installDebug

test: ## Run unit tests
	./gradlew test

.DEFAULT_GOAL := help

KTLINT_VERSION := 1.5.0
KTLINT := .ktlint/ktlint

.PHONY: help build run test format

help: ## Show this help
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-20s\033[0m %s\n", $$1, $$2}'

build: ## Build release APK
	./gradlew assembleRelease

run: ## Install and run on connected device
	./gradlew installDebug


run_release: ## Install and run on connected device on release
	./gradlew installRelease

test: ## Run unit tests
	./gradlew test

$(KTLINT):
	@mkdir -p .ktlint
	@curl -sSLO "https://github.com/pinterest/ktlint/releases/download/$(KTLINT_VERSION)/ktlint" && chmod +x ktlint && mv ktlint $(KTLINT)

format: $(KTLINT) ## Format all Kotlin code with ktlint
	$(KTLINT) --format "app/src/**/*.kt"

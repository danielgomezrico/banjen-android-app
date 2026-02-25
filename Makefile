.DEFAULT_GOAL := help

KTLINT_VERSION := 1.5.0
KTLINT := .ktlint/ktlint

.PHONY: help build run test format deploy-metadata

help: ## Show this help
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-20s\033[0m %s\n", $$1, $$2}'

build: ## Build release APK
	./gradlew assembleRelease

run: ## Uninstall, install and run on connected device (use DEVICE=<serial> for multiple devices)
	adb $(if $(DEVICE),-s $(DEVICE)) uninstall com.makingiants.android.banjotuner || true
	$(if $(DEVICE),ANDROID_SERIAL=$(DEVICE) )./gradlew installDebug


run_release: ## Uninstall, install and run on connected device on release (use DEVICE=<serial> for multiple devices)
	adb $(if $(DEVICE),-s $(DEVICE)) uninstall com.makingiants.android.banjotuner || true
	$(if $(DEVICE),ANDROID_SERIAL=$(DEVICE) )./gradlew installRelease

test: ## Run unit tests
	./gradlew test

$(KTLINT):
	@mkdir -p .ktlint
	@curl -sSLO "https://github.com/pinterest/ktlint/releases/download/$(KTLINT_VERSION)/ktlint" && chmod +x ktlint && mv ktlint $(KTLINT)

format: $(KTLINT) ## Format all Kotlin code with ktlint
	$(KTLINT) --format "app/src/**/*.kt"

deploy-metadata: ## Upload metadata to Play Store via fastlane (no build)
	bundle exec fastlane upload_metadata

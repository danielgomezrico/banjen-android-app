#!/usr/bin/env bash
#
# Create apk for release
#

cd "$(dirname "$0")/../android" || exit 1

./gradlew assembleRelease
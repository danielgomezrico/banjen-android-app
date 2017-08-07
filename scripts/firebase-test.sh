#!/usr/bin/env bash
#
#
#

./gradlew assembleAndroidTest

RESULT_PATH=app/build/outputs/apk

gcloud config set project banjen-9ff86

gcloud firebase test android run \
  --type instrumentation \
  --app $RESULT_PATH/app-debug.apk \
  --test $RESULT_PATH/app-debug-androidTest-unaligned.apk \
  --device-ids Nexus5X,Nexus6,Nexus7,Nexus9,athene,D6603,herolte \
  --os-version-ids 21 \
  --locales en \
  --orientations portrait
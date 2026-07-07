#!/usr/bin/env bash
#
# Setup Firebase apps + download platform config files.
#
# - Registers the Android / iOS app in the Firebase project if it does not exist yet.
# - Always fetches the latest google-services.json and GoogleService-Info.plist.
#
# Requirements:
#   - firebase CLI installed (npm i -g firebase-tools)
#   - You must be logged in (`firebase login`)
#
# Usage:
#   ./scripts/setup-firebase.sh
#   FIREBASE_PROJECT=banjen-xxx ./scripts/setup-firebase.sh
#

set -euo pipefail

FIREBASE_PROJECT="${FIREBASE_PROJECT:-banjen-9ff86}"
ANDROID_PACKAGE="com.makingiants.android.banjotuner"
IOS_BUNDLE="com.banjen.ios"

ANDROID_OUT="android/app/google-services.json"
IOS_OUT="ios/Banjen/GoogleService-Info.plist"

command -v firebase >/dev/null 2>&1 || {
  echo "ERROR: firebase CLI not found in PATH."
  echo "Install it: npm install -g firebase-tools"
  echo "Then run: firebase login"
  exit 1
}

echo "==> Firebase setup (project: $FIREBASE_PROJECT)"
echo

get_app_id() {
  local platform="$1"
  local namespace="$2"

  firebase apps:list --json --project "$FIREBASE_PROJECT" 2>/dev/null | \
    python3 -c '
import json, sys
try:
    data = json.load(sys.stdin)
    for app in data.get("result", []):
        if (app.get("platform") or "").upper() == "'"$platform"'".upper() and app.get("namespace") == "'"$namespace"'":
            print(app.get("appId", ""))
            sys.exit(0)
except Exception:
    pass
' || true
}

register_if_missing() {
  local platform="$1"
  local display_name="$2"
  local namespace="$3"

  local app_id
  app_id=$(get_app_id "$platform" "$namespace")

  if [[ -z "$app_id" ]]; then
    echo "App for $platform ($namespace) not found. Registering..." >&2
    if [[ "$platform" == "ANDROID" ]]; then
      firebase apps:create ANDROID "$display_name" --package-name "$namespace" --project "$FIREBASE_PROJECT" || true
    else
      firebase apps:create IOS "$display_name" --bundle-id "$namespace" --project "$FIREBASE_PROJECT" || true
    fi
    app_id=$(get_app_id "$platform" "$namespace")
  else
    echo "$platform app already registered ($app_id)" >&2
  fi

  echo "$app_id"
}

fetch_config() {
  local platform="$1"
  local app_id="$2"
  local out_file="$3"

  if [[ -z "$app_id" ]]; then
    echo "ERROR: No app ID for $platform. Registration may have failed."
    return 1
  fi

  echo "Fetching $platform config → $out_file"
  mkdir -p "$(dirname "$out_file")"

  # Retry a couple times in case the app was just created
  for attempt in 1 2 3; do
    if firebase apps:sdkconfig "$platform" "$app_id" --project "$FIREBASE_PROJECT" > "$out_file" 2>/dev/null; then
      echo "  ✓ $out_file updated"
      break
    fi
    echo "  Attempt $attempt failed, retrying..."
    sleep 2
  done

  if [[ ! -s "$out_file" ]]; then
    echo "  WARNING: $out_file may be empty or fetch failed. Run the command again."
  fi
}

# Android
echo "=== Android ==="
ANDROID_APP_ID=$(register_if_missing ANDROID "Banjen" "$ANDROID_PACKAGE")
fetch_config ANDROID "$ANDROID_APP_ID" "$ANDROID_OUT"
echo

# iOS
echo "=== iOS ==="
IOS_APP_ID=$(register_if_missing IOS "Banjen" "$IOS_BUNDLE")
fetch_config IOS "$IOS_APP_ID" "$IOS_OUT"
echo

echo "==> Firebase setup complete."
echo "   Android: $ANDROID_OUT"
echo "   iOS:     $IOS_OUT"
echo
echo "If you changed the bundle ID / package name, update the constants at the top of this script."
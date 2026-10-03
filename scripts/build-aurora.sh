#!/usr/bin/env bash
# Build in separate processes so native image-packing memory is released before R8.
set -euo pipefail
cd "$(dirname "$0")/.."
: "${JAVA_HOME:?Set JAVA_HOME to JDK 17}"
: "${ANDROID_HOME:?Set ANDROID_HOME to an installed Android SDK}"
./gradlew --no-daemon --max-workers=2 tools:pack -Pbuildversion=160.5
./gradlew --no-daemon --max-workers=1 tests:test -Pbuildversion=160.5
./gradlew --no-daemon --max-workers=1 android:assembleRelease -x tools:pack -Pbuildversion=160.5 "$@"
printf '\nUnsigned release APK: android/build/outputs/apk/release/android-release-unsigned.apk\n'

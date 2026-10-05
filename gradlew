#!/usr/bin/env sh
# Minimal wrapper that uses system gradle if available for CI
set -e
if [ -f "./gradle/wrapper/gradle-wrapper.jar" ]; then
  exec java -jar ./gradle/wrapper/gradle-wrapper.jar "$@"
else
  # Fallback for GitHub Actions setup-gradle
  exec gradle "$@"
fi

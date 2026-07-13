#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
SDK_DIR="${ANDROID_HOME:-$HOME/Android/Sdk}"
CMDLINE_URL="https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip"

echo "Family Scam Shield Android bootstrap"
echo "Repo: $ROOT_DIR"

if ! command -v java >/dev/null 2>&1; then
  cat <<'MSG'
Missing: java
Install JDK 17 first, then rerun this script.
Examples:
  Ubuntu/Debian: sudo apt-get install openjdk-17-jdk
  macOS: brew install openjdk@17
MSG
  exit 1
fi

mkdir -p "$SDK_DIR/cmdline-tools"

if [ ! -x "$SDK_DIR/cmdline-tools/latest/bin/sdkmanager" ]; then
  TMP_ZIP="$(mktemp)"
  TMP_DIR="$(mktemp -d)"
  echo "Downloading Android command-line tools..."
  curl -L "$CMDLINE_URL" -o "$TMP_ZIP"
  unzip -q "$TMP_ZIP" -d "$TMP_DIR"
  rm -rf "$SDK_DIR/cmdline-tools/latest"
  mv "$TMP_DIR/cmdline-tools" "$SDK_DIR/cmdline-tools/latest"
  rm -f "$TMP_ZIP"
  rm -rf "$TMP_DIR"
fi

export ANDROID_HOME="$SDK_DIR"
export PATH="$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$PATH"

# sdkmanager closes stdin after accepting the needed license prompts; under pipefail,
# `yes` may exit with SIGPIPE (141). That is harmless, so do not let it stop setup.
yes | sdkmanager --licenses >/dev/null || true
sdkmanager "platform-tools" "platforms;android-35" "build-tools;35.0.0"

echo
echo "Android SDK ready at $ANDROID_HOME"
echo "Add this to your shell profile if needed:"
echo "  export ANDROID_HOME=\"$ANDROID_HOME\""
echo "  export PATH=\"\$ANDROID_HOME/cmdline-tools/latest/bin:\$ANDROID_HOME/platform-tools:\$PATH\""

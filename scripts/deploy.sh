#!/bin/bash
#
# deploy.sh — Build release AAB and upload to Play Store internal testing track
#
# Prerequisites:
#   1. keystore.properties configured with signing credentials
#   2. play-store-key.json service account key in project root
#   3. Python 3 with google-api-python-client & oauth2client installed:
#        pip install google-api-python-client oauth2client
#
# Usage:
#   ./scripts/deploy.sh
#

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
AAB_PATH="$PROJECT_DIR/app/build/outputs/bundle/release/app-release.aab"
SERVICE_ACCOUNT_KEY="$PROJECT_DIR/play-store-key.json"
PACKAGE_NAME="com.rokas.showuswhatyougot"

echo "=============================="
echo " Play Store Deploy (internal)"
echo "=============================="

# --- Validate prerequisites ---
if [ ! -f "$PROJECT_DIR/keystore.properties" ]; then
  echo "❌ keystore.properties not found in project root."
  echo "   Copy keystore.properties.example and fill in your values."
  exit 1
fi

if [ ! -f "$SERVICE_ACCOUNT_KEY" ]; then
  echo "❌ play-store-key.json not found in project root."
  echo "   See README for instructions on creating a service account key."
  exit 1
fi

if ! python3 -c "import googleapiclient; import oauth2client" 2>/dev/null; then
  echo "❌ Missing Python dependencies. Install them with:"
  echo "   pip install google-api-python-client oauth2client"
  exit 1
fi

# --- Build release AAB ---
echo ""
echo "🔨 Building release AAB..."
cd "$PROJECT_DIR"
./gradlew :app:bundleRelease --no-daemon

if [ ! -f "$AAB_PATH" ]; then
  echo "❌ AAB not found at $AAB_PATH"
  exit 1
fi

echo "✅ AAB built: $AAB_PATH"

# --- Upload to Play Store ---
echo ""
echo "🚀 Uploading to Play Store (internal track)..."

python3 "$SCRIPT_DIR/upload_to_play_store.py" \
  --package-name "$PACKAGE_NAME" \
  --aab "$AAB_PATH" \
  --track internal \
  --key "$SERVICE_ACCOUNT_KEY"

echo ""
echo "✅ Done! Build uploaded to internal testing track."


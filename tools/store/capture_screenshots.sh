#!/bin/zsh
# Captures raw store screenshots from the iOS simulators, one per screen, into build/store-raw/.
# Then: python3 tools/store/make_store_images.py build/store-raw .   (needs Pillow)
#
# usage: tools/store/capture_screenshots.sh     (macOS, run from the project root)
# Builds the Release app for the simulator, opens each screen with the -startScreen launch argument
# and uses a 09:41 status bar. Live prices need an internet connection.
set -eu
ROOT=$(cd "$(dirname "$0")/../.." && pwd)
BUNDLE=com.suer.vatankuyumculuk
OUT="$ROOT/build/store-raw"
APP="$ROOT/build/sim/Build/Products/Release-iphonesimulator/VatanKuyumculuk.app"
SCREENS=(PRICES PRICES:COINS CALCULATOR STORE)
mkdir -p "$OUT"

xcodebuild -project "$ROOT/iosApp/iosApp.xcodeproj" -scheme iosApp -configuration Release -sdk iphonesimulator \
  -destination 'generic/platform=iOS Simulator' -derivedDataPath "$ROOT/build/sim" build -quiet

udid() {  # newest available simulator with this exact name
  xcrun simctl list devices available -j | python3 -c "
import json, sys
devices = json.load(sys.stdin)['devices']
ids = [d['udid'] for rt in sorted(devices) for d in devices[rt] if d['name'] == sys.argv[1]]
print(ids[-1] if ids else '')" "$1"
}

shoot() {
  local name=$1 prefix=$2 id
  id=$(udid "$name")
  [ -n "$id" ] || { echo "simulator not found: $name" >&2; return 1; }
  xcrun simctl boot "$id" 2>/dev/null || true
  xcrun simctl bootstatus "$id" -b > /dev/null
  xcrun simctl ui "$id" appearance dark
  xcrun simctl status_bar "$id" override --time "09:41" --dataNetwork wifi --wifiMode active --wifiBars 3 \
    --cellularMode active --cellularBars 4 --operatorName "" --batteryState charged --batteryLevel 100
  xcrun simctl install "$id" "$APP"
  local i=1
  for s in $SCREENS; do
    xcrun simctl terminate "$id" $BUNDLE 2>/dev/null || true
    xcrun simctl launch "$id" $BUNDLE -startScreen "$s" > /dev/null
    sleep 12  # live prices arrive and the tick highlight fades
    xcrun simctl io "$id" screenshot --type=png "$OUT/$prefix-$i.png" > /dev/null 2>&1
    echo "$prefix-$i ($s)"
    i=$((i + 1))
  done
  xcrun simctl shutdown "$id"
}

shoot "iPhone 17 Pro" iphone        # 1206x2622, 6.3"
shoot "iPhone 17 Pro Max" iphone69  # 1320x2868, 6.9"
shoot "iPad Pro 13-inch (M5)" ipad  # 2064x2752

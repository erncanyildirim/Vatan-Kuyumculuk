#!/bin/sh
# Builds, signs and uploads the iOS app to App Store Connect with Xcode's automatic signing.
#
# One-time setup on the release Mac: Xcode > Settings > Accounts signed in with the Apple ID of
# team 4BQQTXY4NF (Eren Can YILDIRIM). Xcode then creates the distribution certificate and the
# App Store profile itself (-allowProvisioningUpdates), and uploads with that account.
#
# usage: tools/ios/release.sh            (bump CURRENT_PROJECT_VERSION in Config.xcconfig first)
#        tools/ios/release.sh export     (only builds the signed .ipa into build/ios, no upload)
# The archive is kept in Xcode's archive folder, so it also shows in Window > Organizer.
set -eu

ROOT=$(cd "$(dirname "$0")/../.." && pwd)
TEAM=4BQQTXY4NF
MODE=${1:-upload}
OUT="$ROOT/build/ios"
ARCHIVE="$HOME/Library/Developer/Xcode/Archives/$(date +%F)/VatanKuyumculuk $(date +%H.%M.%S).xcarchive"
WORK=$(mktemp -d)
trap 'rm -rf "$WORK"' EXIT

case "$MODE" in
  upload) DESTINATION=upload ;;
  export) DESTINATION=export ;;
  *) echo "usage: $0 [upload|export]" >&2; exit 2 ;;
esac

cat > "$WORK/ExportOptions.plist" <<PLIST
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0"><dict>
  <key>method</key><string>app-store-connect</string>
  <key>destination</key><string>$DESTINATION</string>
  <key>teamID</key><string>$TEAM</string>
  <key>signingStyle</key><string>automatic</string>
  <key>uploadSymbols</key><true/>
  <key>manageAppVersionAndBuildNumber</key><false/>
</dict></plist>
PLIST

echo "Archiving…"
xcodebuild -project "$ROOT/iosApp/iosApp.xcodeproj" -scheme iosApp -configuration Release \
  -destination 'generic/platform=iOS' -archivePath "$ARCHIVE" \
  -allowProvisioningUpdates DEVELOPMENT_TEAM="$TEAM" archive -quiet

if [ "$MODE" = upload ]; then echo "Signing and uploading…"; else echo "Signing…"; fi
mkdir -p "$OUT"
xcodebuild -exportArchive -archivePath "$ARCHIVE" -exportOptionsPlist "$WORK/ExportOptions.plist" \
  -exportPath "$OUT" -allowProvisioningUpdates -quiet
echo "Done ($MODE). Archive: $ARCHIVE  Output: $OUT"

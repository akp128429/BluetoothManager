# Bluetooth Manager
Native Android 12+ app. No login, backend, Internet permission, ads, or data upload.

## Features
- Request Nearby devices permission through Android.
- List devices paired to this phone; paired does not mean connected.
- User-initiated, single discovery pass for discoverable nearby devices.
- Search by device name or address (e.g. IPL08).
- Select an unpaired device and request Android pairing confirmation.
- Open Android Bluetooth settings to connect/disconnect audio devices.
- No third-party connection detection, forced disconnections, hidden APIs or automated scan loops.

## Build APK
Use JDK 17, Android SDK 35 and Gradle 8.9. Open this folder in Android Studio and configure Gradle 8.9, or run:

    gradle :app:assembleDebug :app:lintDebug

Output: app/build/outputs/apk/debug/app-debug.apk
No Gradle wrapper binary is included. With GitHub, push this folder's contents to a new repository. The included Actions workflow builds an installable debug APK downloadable from the run's Artifacts section. Debug APK is for personal testing; release distribution needs your own signing key.

## Use
1. Grant Nearby devices permission and enable Bluetooth.
2. Refresh paired devices, or put your accessory in discoverable/pairing mode and tap Scan nearby.
3. Search IPL08, select it and request pairing if needed.
4. Open Bluetooth settings and select the accessory to connect. Android controls audio-profile connections and disconnections.

Scanning does not reveal all devices or which other phone an accessory is connected to. Discovery can temporarily affect Bluetooth audio; stop scanning before connecting. The app cancels scanning when it leaves the foreground. No background reconnect is implemented.

## Verification
Manifest/XML and workflow structure checked during creation. APK compilation and physical Bluetooth tests were not possible in the creation environment because the Android SDK and Gradle were unavailable. Run the workflow and test permission denial, Bluetooth off, pairing rejection and a real accessory before relying on it.

## Hosting
Render cannot run an Android APK or control a phone's Bluetooth. It could host a separate APK download page after a build; no server is needed by this app. Source is published in https://github.com/akp128429/BluetoothManager. No Render service is required.

## References
https://developer.android.com/develop/connectivity/bluetooth/bt-permissions
https://developer.android.com/reference/android/bluetooth/BluetoothAdapter
https://developer.android.com/reference/android/bluetooth/BluetoothDevice
https://developer.android.com/build/releases/agp-8-7-0-release-notes

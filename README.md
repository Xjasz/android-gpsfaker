# android-gpsfaker (Fake GPS Controller)

Legacy Android app written 2014 that fakes the device location.
It replaces Android's network location provider with its own (based on microG's NetworkLocation)
and feeds GPS and network locations from a point you pick on a Google Map.
A floating on-screen joystick nudges the point up/down/left/right while you use other apps.

**Archived code, provided as-is.** It needs a rooted device and the APK installed in the system
partition. It targets SDK 29 with an old Gradle plugin and does not build on a modern toolchain
without work (see *Building*).

## Setup

1. Google Maps key: replace `YOUR_GOOGLE_MAPS_API_KEY` in `app/src/main/AndroidManifest.xml`.
2. OpenCellID key (optional): replace `YOUR_OPENCELLID_API_KEY` in
   `app/src/main/java/org/microg/networklocation/backends/opencellid/OpenCellIdLocationSource.java`.
3. Your own server (optional): set the two URLs at the top of
   `app/src/main/java/com/delvedapps/core/webservice/WebService.java`.
   They default to `https://example.com/...`, so the app shows a short error toast
   (such as `Client Error: 404`) on launch and on the "move to system directory" screen.
4. On the device: allow the location permission the app asks for, and allow
   "Display over other apps" for it in Settings, or the floating joystick cannot open.

## Server JSON

The app sends a `GET` to each URL and reads a JSON object (or an array of objects).
A trigger key has to hold a **string** value; the app then reads the other fields from the same object.

### AppData (`URL_APP_DATA`): fetched 2 s after launch

Forces the update screen when `required` is `"true"` and `latest_version` is higher than the app's `versionCode`.
`MESSAGE1` / `MESSAGE2` show as toasts when not empty.

```json
{
  "VERSION": "1",
  "latest_version": "2",
  "required": "true",
  "title": "Update available",
  "message1": "A new version is out.",
  "message2": "Please update to continue.",
  "url": "https://example.com/fakegps/latest.apk",
  "MESSAGE1": "",
  "MESSAGE2": ""
}
```

### Commands (`URL_APP_COMMANDS`): fetched on the "move to system directory" screen

The app runs these lines **as root** (`su`) to copy itself into the system partition and reboot.
Use `POST_JELLYBEAN` for Android 4.3+ and `PRE_JELLYBEAN` for older devices.
The app runs them in this order (`<pkg>` is the package name, `<file>` is `<pkg>.apk`):

1. `path + <pkg>`: its output from the first `/` onward is the APK's current path (`<dir>`)
2. `string1`
3. `string2`
4. `string3_part1 + <dir> + string3_part2 + <file>`
5. `string4 + <file>`
6. `string5`, after a 5 s pause

Example for a device where `/system` can be remounted:

```json
{
  "POST_JELLYBEAN": "1",
  "path": "pm path ",
  "string1": "mount -o rw,remount /system",
  "string2": "mkdir -p /system/priv-app",
  "string3_part1": "cp ",
  "string3_part2": " /system/priv-app/",
  "string4": "chmod 644 /system/priv-app/",
  "string5": "reboot"
}
```

> **Security:** whoever controls this URL gets root on every device running the app.
> Serve it only over HTTPS from a server you control, or skip the server and copy the APK into
> `/system/priv-app/` yourself.

## Building

- Fix before the first build:
  - Add a Gradle wrapper (`gradle wrapper`).
  - Use an Android Gradle plugin that matches it; `build.gradle` pins 3.5.4.
  - Switch the four `android.support.v4.app.*` imports to `androidx.fragment.app.*`.
- `app/src/main/aidl` and the `android/`, `com/android/internal/` sources are stubs for hidden
  framework APIs that the location provider binds to. Keep them.

## Credits

`org/microg/networklocation` comes from [microG NetworkLocation](https://github.com/microg/NetworkLocation) (Apache 2.0).
Most of its online backends (Apple Wi-Fi, Vodafone CellAPI, Mozilla Location Service, MapQuest Nominatim) have since shut down.

# simple-wifi-connector
 custom plugin to connect to a WiFi network on Android and iOS
## Versions
### Version 1.0
Initial draft of this plugin
---
## Supported platforms
This plugin is available for Android 7+ and iOS 11+
### Android
Below Android version 10 this plugin uses the WifiManager. For Android 10+ it uses the ConnectivityManager.
### iOS
Uses the NEHotspotConfigurationManager
---
## Install Plugin
Install Plugman globally using the following command:
```
$ cordova plugin add ...
```
---
## Usage
Example:
```
const connectToNetwork = async (ssid, pass) => {
  return new Promise((resolve, reject) => {
    window.cordova.plugins.simplewificonnector.connect(
      ssid,
      pass,
      function(res) {
        resolve(res);
      },
      function(err) {
        reject(err);
      }
    );
  });
}
```

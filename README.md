<img src="https://image.flaticon.com/icons/png/512/36/36366.png" width="150px" />

# Treehouse - Mobile App Toolchain

Treehouse is a hybrid mobile app toolchain that combines the best open source libraries to form the easiest, fastest and most complete way to build, test, deploy and publish hybrid mobile apps!

Treehouse = Cordova + Fastlane

### Schedule
 - [x] v0.1.0 - Support run command with Cordova (including continous / development mode)
 - [x] v0.2.0 - Support build command with Cordova for Android
 - [x] v0.3.0 - Support publish command with Fastlane for Play store
 - [x] v0.4.0 - Support build command with Corova for iOS 
 - [x] v0.5.0 - Support publish command with Faslane for Apple iTunes store
 - [ ] v0.6.0 - Support create command
 - [ ] v0.7.0 - Support setup command
 
### Commands
- treehouse list
- treehouse run [--verbose=true]
- treehouse develop [--watch=source|browser|both default=both] [--verbose=true]
- treehouse build [platform=ios|android|* default=*] [--verbose=true]
- treehouse publish [platform=ios|android|* default=*] [--key=`location` default=resources/android/play.json] [--username=`username`] [--password=`password`] [--verbose=true]

##### Coming Soon...
- treehouse setup [platform]
- treehouse create [name] [id]

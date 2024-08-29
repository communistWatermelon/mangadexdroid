# mangadexdroid

Latest release APK can be downloaded [here](https://github.com/communistWatermelon/mangadexdroid/releases). 
New commits on `main` are built using Github Actions.


## Preview

Main Screen             |  Notification
:-------------------------:|:-------------------------:
<img src="https://github.com/user-attachments/assets/e34f6a18-bb55-4326-b2c4-6e948f751ce2" width="300">  |  <img src="https://user-images.githubusercontent.com/3271813/198822770-9a124ecb-5215-4dc2-9029-50a5909eb014.png" width="200">



## Summary
An Android project that notifies you of new chapters to manga series followed on mangadex.org. Mostly created so I could avoid using third-party RSS feeds, but also as a testing ground for new architecture patterns.
Mostly only maintained for my own use, feel free to fork the project and build on it if necessary. I'll be adding features at my own leisure, and will not build an iOS version.

The app will only notify you of chapters that have been released *after* you've installed the app. This is to prevent notification spam on first run.


### Working Functionality
- Logging in
- Tracking/Displaying followed Manga updates from MangaDex
- Notifying of chapters released after app install date
- Opening a new manga chapter from the Android notification or from the home screen
- Light and Dark theme (following Android's system setting)
- Material You theming (on supported devices)
- Automatically caching new chapters, to improve performance at read-time
- Ability to read manga chapters using a native image renderer or the system webview


### Missing Functionality
- Log out (clear app data if you want to log out)
- Various settings that you might expect with a full consumer app
- Everything else related to MangaDex's site functionality


## General Architecture
Developed using Jetpack Compose for the UI
Ktor for HTTP requests and file downloads
Koin for Dependency Injection

App architecture is modelled after Google's recommended approach: Model-View-ViewModel (MVVM) with Repositories.


## API
Working directly with the MangaDex API. 
Using global rate-limiting in Ktor, matching MangaDex's 5 per second limit.


## Analytics/Logging

All logs + analytics are logged to my personal Firebase Crashlytics project, so I have information to debug crashes or non-fatal events in the app. 


## Sync Process

Every ~30 minutes (while the app is foregrounded OR background), the app will go through a sync process:
1. Refresh Auth Token
2. Fetch Followed Chapters
3. Fetch new Manga info for unknown manga series
4. Fetch covers for new manga found in step 3
5. Fetch chapter read status markers for the authenticated user

Background refreshes are using WorkManager, which means there may be some delay in the refresh.


## Storage

Manga series infor, chapter info, and read status is all stored in a local DB, powered by Room
Cover images are cached and stored on device.


## Modules

This app is split into many modules, which fall into the following categories
- **lib**: Various segments of the architecture, expected to be used in multiple places. Should not depend on any data or feature modules. Examples: lib-navigation, lib-networking
- **data**: Responsible for holding services that features may need to interact with. Should not hold state. Should not depend on any feature modules. Examples: data-authentication, data-user  
- **feature**: Responsible for the UI and business logic for a feature. Features should not depend on each other. Some examples: feature-authentication, feature-manga-list
- **app**: Application lifecycle logic. Should only depend on libs and features

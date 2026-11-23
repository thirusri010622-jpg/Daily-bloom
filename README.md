# Daily Bloom – Android app source

This is an Android Studio project for the routine, gratitude, water, reading and progress tracker.

## Build an APK
1. Install Android Studio (current stable version) on a computer.
2. Open this `DailyBloomAndroid` folder in Android Studio.
3. Allow Gradle sync and install the Android SDK platform 35 if prompted.
4. On your Android phone, enable Developer options / USB debugging if you want to test directly.
5. Choose **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
6. Android Studio creates the debug APK under `app/build/outputs/apk/debug/app-debug.apk`.

## Important setup on your phone
- Open the app and allow notifications.
- In Android Settings, allow **Alarms & reminders** / exact alarms for Daily Bloom if your phone asks.
- Some phone brands restrict background activity; set battery use for Daily Bloom to unrestricted for dependable reminders.
- The app uses local device storage; it does not upload your routine.

## Current starter content
- Daily routine and gratitude times, including 4:05 AM and 8:55 PM.
- Water tracker: eight 200 ml check-ins (1.6 L target).
- Simple reading list.
- Daily completion summary.
- Daily notification reminders for the listed routine items.

## Notes
- This is a starter project, not a signed release APK. It must be built in Android Studio.
- Routine check-ins are stored locally. The initial version does not yet provide full weekly/monthly history, book notes, supplement-specific tick boxes, or custom alarm editing.
- The original handwritten time/activity entries were transcribed as a starter schedule and can be edited in a later version. Please review the schedule before relying on reminders.

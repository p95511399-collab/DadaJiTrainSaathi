# Dada Ji Train Saathi (Android starter project)

This is a starter Android app with a Hindi one-tap interface and Hindi text-to-speech.
It does **not** promise an exact GPS dot. RailRadar's documented example reports a station
name/code, distance from origin/last station and speed; optional telemetry fields can vary
by source. Its independent data combines public railway information with passenger-shared
GPS. Always inspect `lastUpdatedAt`; a stale/missing update is not presented as confirmed
live data.

## What is included
- Large “ट्रेन कहाँ है?” button.
- Save a train number on the phone.
- Hindi spoken status.
- Shows the provider-reported current station/location, next halt, delay and update timestamp. It does not fabricate latitude/longitude.
- Backend proxy keeps the RailRadar API key off the Android app.
- Backend refuses to label a response live unless `isLive` and `lastUpdatedAt` are present.

## Required before it can run
1. Create an API key with RailRadar and review its current pricing/usage terms.
2. Deploy the `backend` folder to a server that provides HTTPS.
3. Set the server environment variable `RAILRADAR_API_KEY` to your key.
4. In `MainActivity.kt`, replace `https://YOUR-BACKEND-DOMAIN.example` with your backend HTTPS URL.
5. Open this folder in Android Studio, sync Gradle, then build/install the app.

## Run backend locally
- Install Node.js 18 or newer.
- In `backend`: `npm install`
- Set `RAILRADAR_API_KEY` in the server environment.
- Run: `npm start`

Do not put the API key in the Android source code, screenshots, or public GitHub repository.

## Important limitations
- RailRadar is independent, not an official Indian Railways/CRIS/IRCTC service.
- Its terms say location combines public railway information and passenger-shared GPS; accuracy and freshness can vary.
- If live data is missing, this starter app shows an error rather than inventing a location from the timetable. It currently shows the provider-reported location text, not a map or guaranteed exact GPS coordinates.
- This is source code, not a compiled APK. Building/installing requires Android Studio and a deployed backend.
- Station-nearby voice alerts (Gonda/Babhnan) and background notifications are not included in this first starter version.

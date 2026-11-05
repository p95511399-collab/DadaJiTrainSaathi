# दादाजी ट्रेन साथी (Android source + backend)

इस starter project में ये सुविधाएँ हैं:
- चार saved train buttons: 15204, 15032, 15203, 15031
- हिंदी voice input (फोन की speech recognition सुविधा से)
- ट्रेन नंबर बोलने/लिखने पर live status
- ट्रेन के नाम से search, फिर मिले हुए नंबर से live status
- हिंदी Text-to-Speech जवाब
- API key केवल backend environment variable में रहती है, Android app/GitHub में नहीं

## बहुत जरूरी: यह अभी APK नहीं है
यह source code है। इसे build और deploy करना बाकी है। वास्तविक live data तभी आएगा जब RailRadar API key सक्रिय हो, Render backend deploy हो, और app में उसका URL भरा जाए। Data provider के last-updated समय और `isLive` को ध्यान से देखें; stale या non-live data को exact GPS न समझें।

## GitHub में upload
1. GitHub repository खोलें।
2. `Add file` → `Upload files` चुनें।
3. इस ZIP को अपने कंप्यूटर पर extract करके **अंदर की सारी files/folders** repository के root में upload करें। (फोन पर GitHub mobile site में upload मुश्किल हो तो कंप्यूटर/desktop mode बेहतर है।)
4. Commit changes करें।

## Render पर backend deploy
1. Render → New → Web Service → अपनी GitHub repository चुनें।
2. Root Directory: `backend`
3. Runtime: Node
4. Build Command: `npm install`
5. Start Command: `npm start`
6. Environment Variables में `RAILRADAR_API_KEY` नाम और अपनी key value जोड़ें। Key को GitHub file, screenshot या chat में कभी न डालें।
7. Deploy होने पर Render URL मिलेगा, जैसे `https://your-service.onrender.com`। Render HTTPS देता है।

## Android app URL
`app/src/main/java/com/dadaji/trainsaathi/MainActivity.kt` में:
`https://YOUR-RENDER-SERVICE.onrender.com`
को अपने Render URL से बदलें। फिर Android Studio में project खोलकर build करें।

## API details
Backend RailRadar के documented endpoints इस्तेमाल करता है:
- `GET /v1/trains/{number}/live`
- `GET /v1/lookup/search/trains?q=...&limit=5&lang=hi`

Docs: https://railradar.in/docs/live-train-status , https://railradar.in/docs/search-trains , https://railradar.in/docs/authentication

## Notes
- Android voice recognition device/Google speech service पर निर्भर है; microphone permission माँगी जाएगी।
- यदि नाम से कई ट्रेनें मिलती हैं, app candidates दिखाता है; अभी user को number चुनकर फिर search करना होगा।
- Free API quota सीमित हो सकती है। बार-बार refresh न करें।
- App में train location provider द्वारा दी गई जानकारी है, फोन की GPS से train को track नहीं किया जाता।

package com.dadaji.trainsaathi

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.view.Gravity
import android.view.ViewGroup
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.thread

class MainActivity : Activity(), TextToSpeech.OnInitListener {
    private val saved = listOf("15204", "15032", "15203", "15031")
    // Replace after deploying the backend on Render. Keep the trailing /
    private val backend = "https://dadajitrainsaathi-1.onrender.com"
    private lateinit var result: TextView
    private lateinit var input: EditText
    private lateinit var tts: TextToSpeech
    private var speech: SpeechRecognizer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tts = TextToSpeech(this, this)
        buildScreen()
    }

    private fun buildScreen() {
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(24, 24, 24, 20); gravity = Gravity.TOP }
        root.addView(TextView(this).apply { text = "🚆 दादाजी ट्रेन साथी"; textSize = 27f; gravity = Gravity.CENTER; setPadding(0, 12, 0, 24) })
        root.addView(TextView(this).apply { text = "अपनी ट्रेन चुनें"; textSize = 20f; setPadding(0, 8, 0, 12) })
        saved.forEach { number ->
            root.addView(Button(this).apply { text = "ट्रेन $number कहाँ है?"; textSize = 18f; setOnClickListener { fetchLive(number) } }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 58).apply { bottomMargin = 8 })
        }
        root.addView(TextView(this).apply { text = "या ट्रेन का नाम / नंबर बोलें"; textSize = 19f; setPadding(0, 22, 0, 8) })
        input = EditText(this).apply { hint = "ट्रेन नंबर या नाम लिखें"; textSize = 18f; isSingleLine = true }
        root.addView(input)
        root.addView(Button(this).apply { text = "🎙️ बोलकर पूछें"; textSize = 19f; setOnClickListener { startVoice() } }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 58))
        root.addView(Button(this).apply { text = "खोजें"; setOnClickListener { resolveQuery(input.text.toString()) } }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 52))
        result = TextView(this).apply { text = "ट्रेन चुनें या बोलकर पूछें।"; textSize = 18f; setPadding(4, 24, 4, 8) }
        root.addView(result)
        setContentView(ScrollView(this).apply { addView(root) })
    }

    private fun startVoice() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) { say("इस फोन में आवाज़ पहचानने की सुविधा उपलब्ध नहीं है।"); return }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 41); return
        }
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi-IN")
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "hi-IN")
            putExtra(RecognizerIntent.EXTRA_PROMPT, "ट्रेन का नाम या नंबर बोलिए")
        }
        startActivityForResult(intent, 42)
    }

    @Deprecated("Legacy activity result used for broad Android compatibility")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 42 && resultCode == RESULT_OK) {
            val spoken = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull().orEmpty()
            input.setText(spoken); resolveQuery(spoken)
        }
    }

    private fun resolveQuery(raw: String) {
        val q = raw.trim()
        if (q.isEmpty()) { say("कृपया ट्रेन का नाम या नंबर बोलिए।"); return }
        val number = Regex("[0-9]{5}").find(q)?.value
        if (number != null) { fetchLive(number); return }
        result.text = "ट्रेन का नाम खोज रहे हैं: $q"
        thread {
            try {
                val conn = URL("$backend/api/search?q=" + URLEncoder.encode(q, "UTF-8")).openConnection() as HttpURLConnection
                conn.connectTimeout = 15000; conn.readTimeout = 20000
                val body = conn.inputStream.bufferedReader().use { it.readText() }; conn.disconnect()
                val arr = JSONObject(body).optJSONArray("results")
                runOnUiThread {
                    if (arr == null || arr.length() == 0) { result.text = "ट्रेन नहीं मिली। कृपया उसका नंबर बोलें।"; say("ट्रेन नहीं मिली। कृपया ट्रेन नंबर बताइए।") }
                    else if (arr.length() == 1) fetchLive(arr.getJSONObject(0).optString("number"))
                    else {
                        val first = arr.getJSONObject(0); val second = arr.getJSONObject(1)
                        result.text = "कई ट्रेनें मिलीं:\n${first.optString("number")} ${first.optString("name")}\n${second.optString("number")} ${second.optString("name")}\nकृपया सही ट्रेन नंबर चुनें।"
                        say("कई ट्रेनें मिली हैं। स्क्रीन पर नंबर देखकर सही ट्रेन चुनिए।")
                    }
                }
            } catch (_: Exception) { runOnUiThread { result.text = "नाम से खोज नहीं हो पाई। ट्रेन नंबर बोलकर कोशिश करें।"; say("नाम से खोज नहीं हो पाई। ट्रेन नंबर बताइए।") } }
        }
    }

    private fun fetchLive(number: String) {
        result.text = "ट्रेन $number का live status लोड हो रहा है…"
        thread {
            try {
                val conn = URL("$backend/api/live/$number").openConnection() as HttpURLConnection
                conn.connectTimeout = 15000; conn.readTimeout = 20000
                val body = conn.inputStream.bufferedReader().use { it.readText() }; conn.disconnect()
                val j = JSONObject(body)
                val name = j.optString("trainName", "ट्रेन $number")
                val loc = j.optString("location", "जानकारी उपलब्ध नहीं")
                val next = j.optString("nextStation", "जानकारी उपलब्ध नहीं")
                val delay = j.optInt("delayMinutes", Int.MIN_VALUE)
                val updated = j.optString("lastUpdatedAt", "समय उपलब्ध नहीं")
                val live = j.optBoolean("isLive", false)
                val message = buildString {
                    append("$name ($number)\n")
                    append("स्थिति: ${if (live) "लाइव डेटा" else "लाइव डेटा की पुष्टि नहीं"}\n")
                    append("अभी की जानकारी: $loc\nअगला स्टेशन: $next\n")
                    append("देरी: ${if (delay == Int.MIN_VALUE) "उपलब्ध नहीं" else "$delay मिनट"}\n")
                    append("आखिरी अपडेट: $updated")
                }
                runOnUiThread { result.text = message; say("$name. $loc. अगला स्टेशन $next. ${if (delay == Int.MIN_VALUE) "देरी की जानकारी उपलब्ध नहीं है" else "देरी $delay मिनट है"}.") }
            } catch (_: Exception) { runOnUiThread { result.text = "डेटा नहीं मिल पाया। इंटरनेट, backend URL और API key जाँचें।"; say("अभी ट्रेन की जानकारी नहीं मिल पाई।") } }
        }
    }

    private fun say(text: String) { if (::tts.isInitialized) tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "train-status") }
    override fun onInit(status: Int) { if (status == TextToSpeech.SUCCESS) tts.language = Locale("hi", "IN") }
    override fun onDestroy() { if (::tts.isInitialized) tts.shutdown(); speech?.destroy(); super.onDestroy() }
}

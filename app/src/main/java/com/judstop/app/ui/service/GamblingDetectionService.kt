package com.judstop.app.ui.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.SharedPreferences
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.core.app.NotificationCompat
import androidx.core.content.edit
import com.judstop.app.MainActivity
import com.judstop.app.R
import java.time.Instant

class GamblingDetectionService : AccessibilityService() {

    private lateinit var sharedPreferences: SharedPreferences

    companion object {
        private const val TAG = "GamblingDetectionSvc"
        const val PREFS_NAME = "JudStopPrefs"
        const val KEY_BLOCKED_COUNT = "blockedCount"
        private const val NOTIFICATION_CHANNEL_ID = "gambling_detection_channel" // ID Channel Tetap
    }

    override fun onCreate() {
        super.onCreate()
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
    }

    override fun onServiceConnected() {
        val info = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED or
                    AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
            packageNames = arrayOf(
                "com.android.chrome",
                "org.mozilla.firefox",
                "com.UCMobile.intl",
                "com.opera.browser",
                "com.opera.gx",
                "com.opera.mini.native",
                "com.duckduckgo.mobile.android",
                "com.vivo.browser",
                "com.mi.globalbrowser",
                "com.sec.android.app.sbrowser",
                "com.android.browser",
                "com.android.chrome.beta",
                "com.android.chrome.dev",
                "com.microsoft.emmx",
                "secure.unblock.unlimited.proxy.snap.hotspot.shield"
            )
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            notificationTimeout = 100
        }
        serviceInfo = info
        Log.d(TAG, "Accessibility Service connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event ?: return

        val rootNode = rootInActiveWindow ?: return

        // Coba cari URL dari address bar (lebih efektif untuk beberapa browser)
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED || event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            findUrlInNode(rootNode, event.packageName?.toString())
        }

        detectGamblingKeywords(rootNode)
    }

    // Fungsi untuk mencoba mengambil URL dari address bar (ini sangat bergantung pada struktur UI browser)
    private fun findUrlInNode(nodeInfo: AccessibilityNodeInfo?, browserPackageName: String?) {
        nodeInfo ?: return

        // Heuristik untuk address bar (ID resource bisa berbeda antar versi browser)
        // Chrome: com.android.chrome:id/url_bar
        // Firefox: org.mozilla.firefox:id/url_bar_title atau org.mozilla.firefox:id/mozac_browser_toolbar_url_view
        val commonUrlBarIds = listOf(
            "com.android.chrome:id/url_bar",
            "org.mozilla.firefox:id/mozac_browser_toolbar_url_view",
            "org.mozilla.firefox:id/url_bar_title",
            // Tambahkan ID lain jika diketahui
        )

        for (id in commonUrlBarIds) {
            if (browserPackageName != null && id.startsWith(browserPackageName)) {
                val urlNodes = nodeInfo.findAccessibilityNodeInfosByViewId(id)
                for (urlNode in urlNodes) {
                    val urlText = urlNode.text?.toString()
                    if (urlText != null && isGamblingUrl(urlText)) {
                        Log.d(TAG, "Gambling URL detected in address bar: $urlText")
                        performBlockAction("URL Judi Terdeteksi: $urlText")
                        return // Sudah diblokir, keluar
                    }
                }
            }
        }

        // Fallback jika tidak ditemukan di ID spesifik, iterasi anak
        for (i in 0 until nodeInfo.childCount) {
            findUrlInNode(nodeInfo.getChild(i), browserPackageName)
        }
    }


    private fun isGamblingUrl(url: String): Boolean {
        val keywords = listOf(
            "slot", "casino", "poker", "togel", "judol", "gacor", "rtp",
            "sbobet", "betting", "gambling", "taruhan", "lottery", "toto"
            // Tambahkan keyword domain umum situs judi jika diketahui
        )
        return keywords.any { keyword -> url.contains(keyword, ignoreCase = true) }
    }


    private fun detectGamblingKeywords(node: AccessibilityNodeInfo?) {
        if (node == null) {
            Log.d(TAG, "Node is null, returning")
            return
        }

        // Log.d(TAG, "Processing node: ${node.className}, Text: ${node.text}, ContentDescription: ${node.contentDescription}")

        val textToCheck = node.text?.toString() ?: node.contentDescription?.toString()

        if (textToCheck != null) {
            val gamblingKeywords = listOf(
                "slot", "lottery", "slots", "wager", "slot online", "casino",
                "poker", "togel", "gacor", "rtp live", "situs judi", "bandar", "taruhan"
            )
            if (gamblingKeywords.any { keyword ->
                    textToCheck.contains(
                        keyword,
                        ignoreCase = true
                    )
                }) {
                Log.d(TAG, "Found gambling-related text: $textToCheck")
                performBlockAction("Konten Judi Terdeteksi: $textToCheck")
                return // Sudah diblokir, tidak perlu proses anak lagi
            }
        }

        for (i in 0 until node.childCount) {
            val childNode = node.getChild(i)
            detectGamblingKeywords(childNode) // Rekursif
            // Jika sudah diblokir oleh anak, bisa dipertimbangkan untuk return lebih awal
            // Namun, karena performGlobalAction(GLOBAL_ACTION_BACK) mungkin butuh waktu,
            // membiarkan iterasi selesai bisa jadi tidak masalah.
        }
    }

    private fun performBlockAction(detectedContent: String) {
        incrementBlockedCount()
        buildNotification(detectedContent)
        performGlobalAction(GLOBAL_ACTION_BACK) // Kembali ke halaman sebelumnya
        // Alternatif: Buka aplikasi JudStop
        // val intent = Intent(this, MainActivity::class.java)
        // intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        // startActivity(intent)
    }


    private fun incrementBlockedCount() {
        val currentCount = sharedPreferences.getInt(KEY_BLOCKED_COUNT, 0)
        sharedPreferences.edit { putInt(KEY_BLOCKED_COUNT, currentCount + 1) }
        Log.d(TAG, "Blocked count incremented to: ${currentCount + 1}")
    }

    private fun buildNotification(text: String) {
        val resultIntent = Intent(this, MainActivity::class.java)
        val resultPendingIntent: PendingIntent? =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.getActivity(
                    this,
                    0,
                    resultIntent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
            } else {
                PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT)
            }

        val channelName = "Notifikasi Deteksi Judi"
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            channelName,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notifikasi ketika aktivitas judi online terdeteksi dan dicegah."
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 500, 200, 500, 200, 500)
        }
        notificationManager.createNotificationChannel(channel)

        val builder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher) // Ganti dengan ikon notifikasi Anda
            .setContentTitle("JudStop: Aktivitas Judi Dicegah  !")
            .setContentText(text.take(100) + if (text.length > 100) "..." else "") // Batasi panjang teks
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Konten terkait judi terdeteksi: \"$text\". Akses dicegah untuk keamanan Anda.")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setContentIntent(resultPendingIntent)
            .setAutoCancel(true)
            .setSound(alarmSound)
            .setColorized(true)

        val notificationId = (1000 + Instant.now().toEpochMilli() % 1000).toInt()
        notificationManager.notify(notificationId, builder.build())
        Log.d(TAG, "Notification built and sent for: $text")
    }

    override fun onInterrupt() {
        Log.d(TAG, "Accessibility Service interrupted")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Accessibility Service destroyed")
    }
}
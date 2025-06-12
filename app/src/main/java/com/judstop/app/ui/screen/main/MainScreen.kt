package com.judstop.app.ui.screen.main

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.ComponentName
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.view.accessibility.AccessibilityManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.judstop.app.R
import com.judstop.app.ui.service.GamblingDetectionService
import com.judstop.app.ui.theme.AccentGreen
import com.judstop.app.ui.theme.DarkerBlueText
import com.judstop.app.ui.theme.JudStopTheme
import com.judstop.app.ui.theme.LightBlueBackground
import com.judstop.app.ui.theme.LightBluePrimary

fun isAccessibilityServiceEnabled(context: Context, serviceClass: Class<*>): Boolean {
    val accessibilityManager =
        context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
    val enabledServices =
        accessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
    val expectedComponentName = ComponentName(context, serviceClass)
    for (service in enabledServices) {
        val serviceComponentName = ComponentName.unflattenFromString(service.id)
        if (serviceComponentName != null && serviceComponentName == expectedComponentName) {
            return true
        }
    }
    return false
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onEnableServiceClick: () -> Unit,
    onChatbotClick: () -> Unit,
    onWatchTutorialClick: () -> Unit,
    refreshKey: Int,
    initialLaunch: Boolean, // Untuk menandai peluncuran awal
    onInitialLaunchHandled: () -> Unit, // Callback setelah dialog awal ditangani
) {
    val context = LocalContext.current
    var isServiceEnabled by remember { mutableStateOf(false) }
    val sharedPrefs = remember {
        context.getSharedPreferences(
            GamblingDetectionService.PREFS_NAME,
            Context.MODE_PRIVATE
        )
    }
    var blockedCount by remember { mutableIntStateOf(0) }
    var showPermissionDialog by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { sp, key ->
            if (key == GamblingDetectionService.KEY_BLOCKED_COUNT) {
                blockedCount = sp.getInt(GamblingDetectionService.KEY_BLOCKED_COUNT, 0)
            }
        }
        sharedPrefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose { sharedPrefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    LaunchedEffect(refreshKey) {
        Log.d("MainScreen", "LaunchedEffect triggered by refreshKey: $refreshKey")
        val serviceCurrentlyEnabled =
            isAccessibilityServiceEnabled(context, GamblingDetectionService::class.java)
        val currentBlockedCount = sharedPrefs.getInt(GamblingDetectionService.KEY_BLOCKED_COUNT, 0)

        isServiceEnabled = serviceCurrentlyEnabled
        blockedCount = currentBlockedCount

        if (initialLaunch && !serviceCurrentlyEnabled) {
            showPermissionDialog = true
            onInitialLaunchHandled() // Tandai bahwa dialog awal sudah coba ditampilkan
        }
        Log.d(
            "MainScreen",
            "Status updated - Service enabled: $isServiceEnabled, Blocked: $blockedCount, ShowDialog: $showPermissionDialog"
        )
    }

    if (showPermissionDialog) {
        AccessibilityPermissionDialog(
            onDismiss = { showPermissionDialog = false },
            onConfirm = {
                showPermissionDialog = false
                onEnableServiceClick()
            }
        )
    }

    Scaffold(
        containerColor = LightBlueBackground, // Warna background utama
        floatingActionButton = {
            FloatingActionButton(
                onClick = onChatbotClick,
                shape = CircleShape,
                containerColor = DarkerBlueText,
                contentColor = Color.White
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_chatbot), "Chatbot AI",
                    modifier = Modifier.size(24.dp) // Ukuran ikon
                )
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    bottom = paddingValues.calculateBottomPadding(),
                    start = paddingValues.calculateStartPadding(androidx.compose.ui.unit.LayoutDirection.Ltr),
                    end = paddingValues.calculateEndPadding(androidx.compose.ui.unit.LayoutDirection.Ltr)
                )
                .verticalScroll(rememberScrollState()) // Agar bisa di-scroll jika konten panjang
                .padding(bottom = 80.dp) // Beri ruang ekstra di bawah untuk FAB jika konten panjang
        ) {
            // Hero Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp + paddingValues.calculateTopPadding()) // Lebih tinggi
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(LightBluePrimary, LightBluePrimary.copy(alpha = 0.7f))
                        )
                    )
                    .padding(16.dp)
                    .padding(top = paddingValues.calculateTopPadding()),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Filled.Shield, // Menggunakan ikon Material Shield
                        contentDescription = "JudStop Logo",
                        tint = Color.White,
                        modifier = Modifier.size(80.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "JudStop",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Perlindungan Proaktif Judi Online Anda",
                        fontSize = 16.sp,
                        color = Color.White.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Konten Utama dengan sedikit jarak dari atas
            Column(
                modifier = Modifier
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Activation Card
                ActivationCard(
                    isServiceEnabled = isServiceEnabled,
                    onToggle = {
                        if (!isServiceEnabled) {
                            // Jika mencoba mengaktifkan dan belum ada izin, arahkan ke settings
                            // Jika izin sudah ada tapi service belum "jalan" (kasus jarang), ini akan jadi visual
                            onEnableServiceClick()
                        } else {
                            // Jika pengguna mencoba menonaktifkan dari sini, informasikan
                            // bahwa itu harus dilakukan dari System Settings.
                            // Untuk saat ini, Switch akan readonly jika enabled.
                            // Atau, bisa juga tetap mengarah ke settings untuk menonaktifkan.
                            onEnableServiceClick() // Arahkan ke settings untuk manage
                        }
                    }
                )

                // Stats Card
                val density = LocalDensity.current
                AnimatedVisibility(
                    visible = true, // Selalu tampilkan, animasi hanya saat masuk
                    enter = slideInVertically { with(density) { +40 } } + // Slide dari bawah
                            scaleIn(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) +
                            fadeIn(animationSpec = spring(stiffness = Spring.StiffnessLow)),
                    exit = fadeOut()
                ) {
                    StatsCard(blockedCount = blockedCount)
                }

                // Info/Tutorial (opsional)
                InfoBox()

                // Watch Tutorial Link
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp)) // Sedikit lengkungan
                        .clickable { onWatchTutorialClick() }
                        .padding(
                            vertical = 12.dp,
                            horizontal = 8.dp
                        ), // Padding agar area klik lebih luas
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center // Pusatkan
                ) {
                    Icon(
                        imageVector = Icons.Filled.PlayCircleOutline, // Ikon tutorial
                        contentDescription = "Tutorial",
                        tint = LightBluePrimary,
                        modifier = Modifier.size(22.dp) // Sesuaikan ukuran ikon
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Lihat Cara Penggunaan",
                        color = LightBluePrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun ActivationCard(isServiceEnabled: Boolean, onToggle: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle) // Membuat seluruh card bisa diklik
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Perlindungan JudStop",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = DarkerBlueText
                )
                Text(
                    text = if (isServiceEnabled) "Layanan sedang aktif" else "Ketuk untuk mengaktifkan",
                    fontSize = 14.sp,
                    color = if (isServiceEnabled) AccentGreen else DarkerBlueText.copy(alpha = 0.7f)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Switch(
                checked = isServiceEnabled,
                onCheckedChange = { onToggle() }, // Biarkan onToggle card yang menghandle logika
                colors = SwitchDefaults.colors(
                    checkedThumbColor = LightBluePrimary,
                    checkedTrackColor = LightBluePrimary.copy(alpha = 0.5f),
                    uncheckedThumbColor = Color(0xFFB0BEC5),
                    uncheckedTrackColor = Color(0xFFCFD8DC),
                    uncheckedBorderColor = Color(0xFFB0BEC5)
                ),
                thumbContent = if (isServiceEnabled) {
                    { Icon(Icons.Filled.CheckCircle, "Enabled", tint = Color.White) }
                } else null
            )
        }
    }
}

@Composable
fun StatsCard(blockedCount: Int) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = LightBluePrimary)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Shield, // Atau ikon statistik lain
                contentDescription = "Statistik Blokir",
                tint = Color.White,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = "$blockedCount Situs Dicegah",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Perlindungan berjalan optimal",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun InfoBox() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(LightBluePrimary.copy(alpha = 0.1f))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.Info,
            contentDescription = "Informasi",
            tint = LightBluePrimary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = "JudStop memerlukan izin Aksesibilitas untuk mencegah membuka situs. Data Anda tetap aman.",
            fontSize = 13.sp,
            color = DarkerBlueText.copy(alpha = 0.9f),
            lineHeight = 18.sp
        )
    }
}


@Composable
fun AccessibilityPermissionDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Settings, // Ikon pengaturan
                    contentDescription = "Perlu Izin",
                    tint = LightBluePrimary,
                    modifier = Modifier.size(48.dp)
                )
                Text(
                    text = "Aktifkan JudStop",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkerBlueText
                )
                Text(
                    text = "Untuk mencegah membuka situs judi online, JudStop memerlukan izin Aksesibilitas. Ketuk 'Pengaturan' untuk melanjutkan.",
                    textAlign = TextAlign.Center,
                    fontSize = 14.sp,
                    color = DarkerBlueText.copy(alpha = 0.8f)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("Nanti", color = DarkerBlueText.copy(alpha = 0.7f))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = onConfirm,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = LightBluePrimary,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Pengaturan")
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreviewEnabled() {
    JudStopTheme { // Ganti dengan tema Anda jika berbeda
        var key by remember { mutableIntStateOf(0) }
        MainScreen(
            onEnableServiceClick = {},
            onChatbotClick = {},
            refreshKey = key++,
            initialLaunch = false,
            onInitialLaunchHandled = {},
            onWatchTutorialClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreviewDisabled() {
    JudStopTheme {
        var key by remember { mutableIntStateOf(0) }
        MainScreen(
            onEnableServiceClick = {},
            onChatbotClick = {},
            refreshKey = key++,
            initialLaunch = true, // Simulate initial launch for dialog
            onInitialLaunchHandled = {},
            onWatchTutorialClick = {}
        )
    }
}

@Preview
@Composable
fun AccessibilityDialogPreview() {
    JudStopTheme {
        AccessibilityPermissionDialog(onDismiss = { }, onConfirm = { })
    }
}
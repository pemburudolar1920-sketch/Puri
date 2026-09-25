package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.CctvCamera
import com.example.ui.theme.CoralDanger
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.viewmodel.RtViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CctvScreen(
    viewModel: RtViewModel
) {
    val context = LocalContext.current
    val cameras = viewModel.cctvCameras

    var selectedCamera by remember { mutableStateOf(cameras.first()) }
    var isNightVision by remember { mutableStateOf(false) }
    var isAudioEnabled by remember { mutableStateOf(false) }
    var isSnapshotTaken by remember { mutableStateOf(false) }
    var showAddCameraDialog by remember { mutableStateOf(false) }
    var customCamerasList by remember { mutableStateOf<List<CctvCamera>>(emptyList()) }

    // Real-time ticking clock for CCTV OSD (On-Screen Display)
    var currentTimeString by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        while (true) {
            currentTimeString = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            delay(1000)
        }
    }

    // Blinking REC dot animation
    val infiniteTransition = rememberInfiniteTransition(label = "rec")
    val alphaRec by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alphaRec"
    )

    val allCameras = cameras + customCamerasList

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("cctv_screen"),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // Title & Status
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Koneksi CCTV Lingkungan",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Pantau keamanan 24 jam • Terkoneksi ke semua aplikasi CCTV",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Live CCTV Stream Monitor Player (HUD)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column {
                    // Video Canvas Screen
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(230.dp)
                            .background(if (isNightVision) Color(0xFF1B2E24) else Color(0xFF090D16))
                    ) {
                        // Simulated Camera Scenery Layer
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.Videocam,
                                    contentDescription = null,
                                    tint = if (isNightVision) Color(0xFF4ADE80).copy(alpha = 0.6f) else Color.White.copy(alpha = 0.4f),
                                    modifier = Modifier.size(54.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = selectedCamera.namaKamera,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "Lokasi: ${selectedCamera.lokasi}",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "IP: ${selectedCamera.ipAddress} • RTSP Port: 554",
                                    color = Color.White.copy(alpha = 0.5f),
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        // Top HUD Overlay (REC dot + Camera ID + Timestamp)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(CoralDanger.copy(alpha = alphaRec))
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "LIVE REC",
                                    color = CoralDanger,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = selectedCamera.channel,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp
                                )
                            }

                            Text(
                                text = currentTimeString,
                                color = Color.White,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Bottom HUD Overlay (Bitrate + Quality)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomCenter)
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isNightVision) "🌙 NIGHT VISION (IR ON)" else "☀️ DAYLIGHT (COLOR)",
                                color = if (isNightVision) Color(0xFF4ADE80) else Color.White,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${selectedCamera.resolusi} | 2480 Kbps",
                                color = Color.White.copy(alpha = 0.8f),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp
                            )
                        }
                    }

                    // Interactive Camera Controls Toolbar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF1E293B))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                isNightVision = !isNightVision
                                Toast.makeText(
                                    context,
                                    if (isNightVision) "Night Vision Aktif" else "Mode Normal",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        ) {
                            Icon(
                                imageVector = if (isNightVision) Icons.Default.Bedtime else Icons.Default.Brightness5,
                                contentDescription = "Night Vision",
                                tint = if (isNightVision) Color(0xFF4ADE80) else Color.White
                            )
                        }

                        IconButton(
                            onClick = {
                                isAudioEnabled = !isAudioEnabled
                                Toast.makeText(
                                    context,
                                    if (isAudioEnabled) "Suara Audio CCTV Aktif" else "Audio Dimatikan",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        ) {
                            Icon(
                                imageVector = if (isAudioEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                                contentDescription = "Audio Stream",
                                tint = if (isAudioEnabled) Color(0xFF38BDF8) else Color.White
                            )
                        }

                        IconButton(
                            onClick = {
                                isSnapshotTaken = true
                                Toast.makeText(context, "📸 Foto Tangkapan Layar CCTV Berhasil Disimpan!", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = "Snapshot", tint = Color.White)
                        }

                        IconButton(
                            onClick = {
                                launchExternalCctvApp(context, "generic", selectedCamera.rtspUrl)
                            }
                        ) {
                            Icon(Icons.Default.OpenInNew, contentDescription = "Buka di App Luar", tint = Color.White)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Camera Feed Selector (Horizontal list)
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = "PILIH TITIK KAMERA CCTV (${allCameras.size} Titik)",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(allCameras) { cam ->
                        val isSelected = cam.id == selectedCamera.id
                        Card(
                            modifier = Modifier
                                .width(160.dp)
                                .clickable { selectedCamera = cam },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 3.dp else 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = cam.channel,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = if (isSelected) EmeraldPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF22C55E))
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = cam.namaKamera,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    maxLines = 2
                                )
                                Text(
                                    text = cam.lokasi,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // Feature: Konek CCTV ALL APLIKASI
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = "KONEKSIKAN KE APLIKASI CCTV FAVORIT ANDA",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Buka langsung stream RTSP kamera ini pada aplikasi CCTV pihak ketiga pilihan warga.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 10.dp)
                )

                // Grid of supported apps
                AppLaunchRow(
                    appName = "Ezviz",
                    appPkg = "com.mcu.iSmartLife",
                    description = "Buka streaming via Ezviz Cloud / IP Device",
                    icon = Icons.Default.Visibility,
                    rtspUrl = selectedCamera.rtspUrl,
                    context = context
                )
                AppLaunchRow(
                    appName = "V380 / V380 Pro",
                    appPkg = "com.macrovideo.v380pro",
                    description = "Koneksi IPCam V380 lingkungan RT",
                    icon = Icons.Default.Videocam,
                    rtspUrl = selectedCamera.rtspUrl,
                    context = context
                )
                AppLaunchRow(
                    appName = "Tuya Smart / Smart Life",
                    appPkg = "com.tuya.smartlife",
                    description = "Perangkat Smart Home & CCTV Tuya",
                    icon = Icons.Default.Home,
                    rtspUrl = selectedCamera.rtspUrl,
                    context = context
                )
                AppLaunchRow(
                    appName = "VLC Media Player",
                    appPkg = "org.videolan.vlc",
                    description = "Putar stream RTSP murni tanpa kompresi",
                    icon = Icons.Default.PlayCircle,
                    rtspUrl = selectedCamera.rtspUrl,
                    context = context
                )
                AppLaunchRow(
                    appName = "IP Webcam / RTSP Player",
                    appPkg = "com.pas.webcam",
                    description = "Universal Stream Player & Web Browser",
                    icon = Icons.Default.Cast,
                    rtspUrl = selectedCamera.rtspUrl,
                    context = context
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Button to Add Custom Community Camera
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                OutlinedButton(
                    onClick = { showAddCameraDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.AddLocationAlt, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Tambah Titik CCTV Baru di Lingkungan RT")
                }
            }
        }
    }

    if (showAddCameraDialog) {
        AddCctvDialog(
            onSave = { newCam ->
                customCamerasList = customCamerasList + newCam
                selectedCamera = newCam
                showAddCameraDialog = false
                Toast.makeText(context, "Kamera CCTV berhasil ditambahkan!", Toast.LENGTH_SHORT).show()
            },
            onDismiss = { showAddCameraDialog = false }
        )
    }
}

@Composable
fun AppLaunchRow(
    appName: String,
    appPkg: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    rtspUrl: String,
    context: Context
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable {
                launchExternalCctvApp(context, appPkg, rtspUrl)
            },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = appName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    Text(text = description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            FilledTonalButton(
                onClick = { launchExternalCctvApp(context, appPkg, rtspUrl) },
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text("Buka", fontSize = 11.sp)
            }
        }
    }
}

fun launchExternalCctvApp(context: Context, packageName: String, streamUrl: String) {
    try {
        if (packageName == "generic") {
            // Generic RTSP video intent
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(Uri.parse(streamUrl), "video/*")
            }
            context.startActivity(Intent.createChooser(intent, "Pilih Aplikasi Pemutar CCTV"))
            return
        }

        // Try opening installed app
        val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
        if (launchIntent != null) {
            context.startActivity(launchIntent)
        } else {
            // Fallback: Open Play Store for this CCTV app
            val storeIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName"))
            context.startActivity(storeIntent)
        }
    } catch (e: Exception) {
        Toast.makeText(context, "Membuka stream: $streamUrl", Toast.LENGTH_LONG).show()
    }
}

@Composable
fun AddCctvDialog(
    onSave: (CctvCamera) -> Unit,
    onDismiss: () -> Unit
) {
    var nama by remember { mutableStateOf("") }
    var lokasi by remember { mutableStateOf("") }
    var rtsp by remember { mutableStateOf("") }
    var ip by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier.fillMaxWidth(0.95f),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "Tambah Kamera CCTV RT",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = nama,
                    onValueChange = { nama = it },
                    label = { Text("Nama Kamera") },
                    placeholder = { Text("misal: CCTV 06 - Lapangan Tenis RT") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = lokasi,
                    onValueChange = { lokasi = it },
                    label = { Text("Lokasi Pemasangan") },
                    placeholder = { Text("misal: Sudut Timur Lapangan Fasum") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = rtsp,
                    onValueChange = { rtsp = it },
                    label = { Text("RTSP Stream URL") },
                    placeholder = { Text("rtsp://admin:pass@192.168.1.xxx:554/live") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = ip,
                    onValueChange = { ip = it },
                    label = { Text("IP Address / Port") },
                    placeholder = { Text("192.168.1.206") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Batal") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (nama.isNotBlank()) {
                                onSave(
                                    CctvCamera(
                                        id = "cctv-custom-${System.currentTimeMillis()}",
                                        namaKamera = nama.trim(),
                                        lokasi = lokasi.trim().ifBlank { "Lingkungan RT Puri Pratama" },
                                        channel = "CH-0" + (6..9).random(),
                                        rtspUrl = rtsp.trim().ifBlank { "rtsp://192.168.1.206:554/live" },
                                        ipAddress = ip.trim().ifBlank { "192.168.1.206" }
                                    )
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                    ) {
                        Text("Simpan Titik CCTV")
                    }
                }
            }
        }
    }
}

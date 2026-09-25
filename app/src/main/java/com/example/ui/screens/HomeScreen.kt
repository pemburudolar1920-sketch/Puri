package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserRole
import com.example.ui.components.EmergencyDialog
import com.example.ui.theme.*
import com.example.ui.viewmodel.RtViewModel
import com.example.ui.viewmodel.ScreenRoute
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: RtViewModel,
    onOpenEmergency: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val wargaList by viewModel.wargaList.collectAsState()
    val kasList by viewModel.kasTransactions.collectAsState()
    val iuranConfig by viewModel.iuranConfig.collectAsState()
    val pengumumanList by viewModel.pengumumanList.collectAsState()
    val cctvList = viewModel.cctvCameras

    val rupiahFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))

    val totalPemasukan = kasList.filter { it.jenis == "PEMASUKAN" }.sumOf { it.nominal }
    val totalPengeluaran = kasList.filter { it.jenis == "PENGELUARAN" }.sumOf { it.nominal }
    val saldoKas = totalPemasukan - totalPengeluaran

    val wargaLunasCount = wargaList.count { it.statusIuranBulanIni }
    val totalWargaCount = wargaList.size

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("home_screen"),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // Top Banner / Header
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                EmeraldPrimaryDark,
                                EmeraldPrimary
                            )
                        )
                    )
                    .padding(horizontal = 20.dp, vertical = 24.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF4ADE80))
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "PORTAL DIGITAL WARGA",
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                            }
                            Text(
                                text = "RT PURI PRATAMA",
                                color = Color.White,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }

                        // Role Badge & Switcher
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color.White.copy(alpha = 0.18f),
                            modifier = Modifier
                                .clickable {
                                    if (currentUser?.role == UserRole.PENGURUS_RT) {
                                        viewModel.switchRole(UserRole.WARGA)
                                    } else {
                                        viewModel.switchRole(UserRole.PENGURUS_RT)
                                    }
                                }
                                .testTag("role_switcher_chip")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (currentUser?.role == UserRole.PENGURUS_RT) Icons.Default.AdminPanelSettings else Icons.Default.Person,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (currentUser?.role == UserRole.PENGURUS_RT) "PENGURUS RT" else "WARGA",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    Icons.Default.Sync,
                                    contentDescription = "Ganti Peran",
                                    tint = Color.White.copy(alpha = 0.8f),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Halo, ${currentUser?.namaLengkap ?: "Warga RT"}",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Lokasi: ${currentUser?.blokRumah ?: "Perumahan RT Puri Pratama"}",
                        color = Color.White.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        // Emergency SOS Button (Prominent Banner)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .offset(y = (-12).dp)
                    .clickable { onOpenEmergency() }
                    .testTag("emergency_sos_button"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CoralDanger),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Campaign,
                                contentDescription = null,
                                tint = CoralDanger,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "TOMBOL DARURAT (SOS)",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "Sirine, Panggilan Satpam, & Kirim WA Darurat",
                                color = Color.White.copy(alpha = 0.9f),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = Color.White
                    )
                }
            }
        }

        // Ringkasan Kas RT Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.AccountBalanceWallet,
                                contentDescription = null,
                                tint = EmeraldPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Laporan Kas RT Real-Time",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        TextButton(
                            onClick = { viewModel.navigateTo(ScreenRoute.KAS_IURAN) },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("Rincian Kas", fontSize = 12.sp, color = EmeraldPrimary)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Saldo Bersih Terkini",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = rupiahFormat.format(saldoKas),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = EmeraldPrimary
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.TrendingUp,
                                    contentDescription = null,
                                    tint = Color(0xFF16A34A),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Pemasukan Bulan Ini", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text(
                                text = rupiahFormat.format(totalPemasukan),
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF16A34A)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.TrendingDown,
                                    contentDescription = null,
                                    tint = CoralDanger,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Pengeluaran Bulan Ini", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text(
                                text = rupiahFormat.format(totalPengeluaran),
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium,
                                color = CoralDanger
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    // Progress bar Iuran Warga
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Kepatuhan Iuran Warga: $wargaLunasCount dari $totalWargaCount KK Lunas",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        val percent = if (totalWargaCount > 0) (wargaLunasCount * 100 / totalWargaCount) else 0
                        Text(
                            text = "$percent%",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldPrimary
                        )
                    }
                    LinearProgressIndicator(
                        progress = { if (totalWargaCount > 0) wargaLunasCount.toFloat() / totalWargaCount else 0f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = EmeraldPrimary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }
        }

        // Quick Feature Menus (Grid)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "MENU UTAMA RT PURI PRATAMA",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    FeatureCard(
                        title = "Kas & Iuran",
                        subtitle = "Edit iuran, kas, Excel",
                        icon = Icons.Default.ReceiptLong,
                        color = EmeraldPrimary,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.navigateTo(ScreenRoute.KAS_IURAN) }
                    )
                    FeatureCard(
                        title = "Data Warga",
                        subtitle = "NIK, KK, Pengingat WA",
                        icon = Icons.Default.PeopleAlt,
                        color = BlueInfo,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.navigateTo(ScreenRoute.DATA_WARGA) }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    FeatureCard(
                        title = "CCTV RT",
                        subtitle = "Live stream all apps",
                        icon = Icons.Default.Videocam,
                        color = Color(0xFF7C3AED),
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.navigateTo(ScreenRoute.CCTV) }
                    )
                    FeatureCard(
                        title = "Lapor Warga",
                        subtitle = "Sampah, lampu, got",
                        icon = Icons.Default.ReportProblem,
                        color = AmberAccent,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.navigateTo(ScreenRoute.PELAPORAN) }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    FeatureCard(
                        title = "Forum Terbuka",
                        subtitle = "Saran & masukan warga",
                        icon = Icons.Default.Forum,
                        color = TealSecondary,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.navigateTo(ScreenRoute.FORUM) }
                    )
                    FeatureCard(
                        title = "Pengumuman",
                        subtitle = "Kegiatan & RSVP",
                        icon = Icons.Default.EventAvailable,
                        color = Color(0xFF0284C7),
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.navigateTo(ScreenRoute.PENGUMUMAN) }
                    )
                }
            }
        }

        // Live CCTV Quick Peek
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Videocam, contentDescription = null, tint = Color(0xFF7C3AED), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Pantauan CCTV Lingkungan",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    TextButton(onClick = { viewModel.navigateTo(ScreenRoute.CCTV) }) {
                        Text("Lihat Semua", fontSize = 12.sp, color = Color(0xFF7C3AED))
                    }
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.navigateTo(ScreenRoute.CCTV) },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .background(Color(0xFF0F172A))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = CoralDanger
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(Color.White)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("LIVE STREAM", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Text(
                                    text = "5 Kamera Online",
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 11.sp
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                Column {
                                    Text(
                                        text = "Gerbang Masuk Utama RT",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = "Support: Ezviz • V380 • Tuya • RTSP • VLC",
                                        color = Color.White.copy(alpha = 0.7f),
                                        fontSize = 10.sp
                                    )
                                }
                                FilledTonalButton(
                                    onClick = { viewModel.navigateTo(ScreenRoute.CCTV) },
                                    colors = ButtonDefaults.filledTonalButtonColors(containerColor = Color.White.copy(alpha = 0.2f)),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text("Buka CCTV", color = Color.White, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Pengumuman Terkini
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Campaign, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Agenda & Pengumuman RT",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    TextButton(onClick = { viewModel.navigateTo(ScreenRoute.PENGUMUMAN) }) {
                        Text("Lihat Agenda", fontSize = 12.sp, color = EmeraldPrimary)
                    }
                }

                pengumumanList.take(2).forEach { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { viewModel.navigateTo(ScreenRoute.PENGUMUMAN) },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Event,
                                    contentDescription = null,
                                    tint = EmeraldPrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.judul,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 1
                                )
                                Text(
                                    text = "${item.tanggalKegiatan} • ${item.lokasi}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer
                            ) {
                                Text(
                                    text = "${item.rsvpCount} Hadir",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FeatureCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clickable { onClick() }
            .testTag("feature_card_${title.lowercase().replace(" ", "_")}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = title, tint = color, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
            )
        }
    }
}

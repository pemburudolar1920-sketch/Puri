package com.example.ui.components

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.User
import com.example.ui.theme.CoralDanger
import com.example.ui.theme.DangerContainer
import com.example.util.EmergencyHelper

@Composable
fun EmergencyDialog(
    user: User?,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var isAlarmActive by remember { mutableStateOf(false) }
    var selectedReason by remember { mutableStateOf("Keamanan / Maling / Orang Mencurigakan") }
    val reasons = listOf(
        "Keamanan / Maling / Orang Mencurigakan",
        "Kebakaran / Korsleting Listrik",
        "Medis / Sakit Parah / Butuh Ambulans",
        "Bencana / Pohon Tumbang / Banjir",
        "Keributan / Gangguan Ketertiban"
    )

    // Pulsing animation for siren
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    DisposableEffect(Unit) {
        onDispose {
            EmergencyHelper.stopSirenSound()
        }
    }

    Dialog(
        onDismissRequest = {
            EmergencyHelper.stopSirenSound()
            onDismiss()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f)
                .testTag("emergency_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Peringatan",
                            tint = CoralDanger,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "TOMBOL DARURAT (SOS)",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = CoralDanger
                        )
                    }
                    IconButton(
                        onClick = {
                            EmergencyHelper.stopSirenSound()
                            onDismiss()
                        }
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup")
                    }
                }

                Text(
                    text = "Gunakan saat terjadi kondisi darurat di lingkungan RT Puri Pratama.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                )

                // Big Siren Button
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .padding(vertical = 12.dp)
                        .size(140.dp)
                        .scale(if (isAlarmActive) scale else 1f)
                        .clip(CircleShape)
                        .background(if (isAlarmActive) CoralDanger else CoralDanger.copy(alpha = 0.85f))
                        .clickable {
                            if (isAlarmActive) {
                                EmergencyHelper.stopSirenSound()
                                isAlarmActive = false
                            } else {
                                EmergencyHelper.playSirenSound()
                                EmergencyHelper.triggerVibration(context)
                                isAlarmActive = true
                            }
                        }
                        .testTag("siren_toggle_button")
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = if (isAlarmActive) Icons.Default.VolumeUp else Icons.Default.Campaign,
                            contentDescription = "Sirine Darurat",
                            tint = Color.White,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (isAlarmActive) "MATIKAN SIRINE" else "BUNYIKAN SIRINE",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                if (isAlarmActive) {
                    Text(
                        text = "🔊 Sirine darurat sedang berbunyi keras!",
                        color = CoralDanger,
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }

                // Pilih Situasi Darurat
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = DangerContainer)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Kategori Situasi Darurat:",
                            fontWeight = FontWeight.Bold,
                            color = CoralDanger,
                            style = MaterialTheme.typography.labelLarge
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        reasons.forEach { reason ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedReason = reason }
                                    .padding(vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = (selectedReason == reason),
                                    onClick = { selectedReason = reason },
                                    colors = RadioButtonDefaults.colors(selectedColor = CoralDanger)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = reason,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                // Kirim Pesan Darurat WhatsApp
                Button(
                    onClick = {
                        val senderName = user?.namaLengkap ?: "Warga RT Puri Pratama"
                        val senderBlock = user?.blokRumah ?: "Area RT Puri Pratama"
                        EmergencyHelper.sendSosWhatsApp(
                            context = context,
                            targetPhone = "6281234567890", // Pos satpam / Grup WA
                            userName = senderName,
                            blok = senderBlock,
                            jenisDarurat = selectedReason
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .testTag("broadcast_sos_wa_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = CoralDanger)
                ) {
                    Icon(Icons.Default.Send, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "KIRIM PESAN SOS KE WA GRUP / SATPAM",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Divider(modifier = Modifier.padding(vertical = 12.dp))

                // Kontak Darurat Cepat (Call)
                Text(
                    text = "PANGGILAN CEPAT NOMOR DARURAT",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                EmergencyContactRow(
                    title = "Pos Satpam & Security RT",
                    subtitle = "Siaga 24 Jam - Pos Utama Depan",
                    phone = "081234567890",
                    icon = Icons.Default.Security,
                    color = Color(0xFF0F766E),
                    onCall = { EmergencyHelper.openDialer(context, "081234567890") }
                )

                EmergencyContactRow(
                    title = "Ketua RT (Bpk. Budi Santoso)",
                    subtitle = "Blok A1 No. 01",
                    phone = "081287654321",
                    icon = Icons.Default.Person,
                    color = Color(0xFF0F6E47),
                    onCall = { EmergencyHelper.openDialer(context, "081287654321") }
                )

                EmergencyContactRow(
                    title = "Polsek / Polisi Terdekat",
                    subtitle = "Layanan Kepolisian Siaga 110",
                    phone = "110",
                    icon = Icons.Default.LocalPolice,
                    color = Color(0xFF1E3A8A),
                    onCall = { EmergencyHelper.openDialer(context, "110") }
                )

                EmergencyContactRow(
                    title = "Ambulans & Medis Darurat",
                    subtitle = "Siaga Darurat Kesehatan 119",
                    phone = "119",
                    icon = Icons.Default.LocalHospital,
                    color = CoralDanger,
                    onCall = { EmergencyHelper.openDialer(context, "119") }
                )

                EmergencyContactRow(
                    title = "Pemadam Kebakaran (Damkar)",
                    subtitle = "Pos Sektor Damkar Wilayah 113",
                    phone = "113",
                    icon = Icons.Default.FireTruck,
                    color = Color(0xFFC2410C),
                    onCall = { EmergencyHelper.openDialer(context, "113") }
                )
            }
        }
    }
}

@Composable
fun EmergencyContactRow(
    title: String,
    subtitle: String,
    phone: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    onCall: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onCall() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(12.dp)
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
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = title, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                    Text(text = "$subtitle • $phone", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            FilledTonalIconButton(
                onClick = onCall,
                colors = IconButtonDefaults.filledTonalIconButtonColors(containerColor = color.copy(alpha = 0.15f))
            ) {
                Icon(Icons.Default.Phone, contentDescription = "Telepon", tint = color)
            }
        }
    }
}

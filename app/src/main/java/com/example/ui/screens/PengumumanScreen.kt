package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Pengumuman
import com.example.data.model.UserRole
import com.example.ui.theme.CoralDanger
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.viewmodel.RtViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PengumumanScreen(
    viewModel: RtViewModel
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val pengumumanList by viewModel.pengumumanList.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }
    val isPengurus = currentUser?.role == UserRole.PENGURUS_RT

    Scaffold(
        floatingActionButton = {
            if (isPengurus) {
                ExtendedFloatingActionButton(
                    onClick = { showCreateDialog = true },
                    containerColor = EmeraldPrimary,
                    contentColor = Color.White,
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("Buat Pengumuman Baru", fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("create_pengumuman_fab")
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .testTag("pengumuman_screen")
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Pengumuman Kegiatan RT",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Informasi agenda kerja bakti, posyandu, rapat warga, dan jadwal siskamling RT Puri Pratama.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            if (pengumumanList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Belum ada pengumuman kegiatan aktif.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(pengumumanList, key = { it.id }) { item ->
                        PengumumanCard(
                            item = item,
                            userName = currentUser?.namaLengkap ?: "Warga",
                            isPengurus = isPengurus,
                            onToggleRsvp = {
                                viewModel.toggleRsvp(item, currentUser?.namaLengkap ?: "Warga")
                            },
                            onDelete = {
                                viewModel.deletePengumuman(item)
                            }
                        )
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        CreatePengumumanDialog(
            onSubmit = { newPengumuman ->
                viewModel.createPengumuman(newPengumuman)
                showCreateDialog = false
            },
            onDismiss = { showCreateDialog = false }
        )
    }
}

@Composable
fun PengumumanCard(
    item: Pengumuman,
    userName: String,
    isPengurus: Boolean,
    onToggleRsvp: () -> Unit,
    onDelete: () -> Unit
) {
    val isAttending = item.isUserAttending(userName)
    var showAttendeesList by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = item.kategori,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    if (item.isPrioritas) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = CoralDanger.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "PENTING",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = CoralDanger,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                if (isPengurus) {
                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = item.judul,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = item.isi,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Time & Location Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${item.tanggalKegiatan} • ${item.jamKegiatan}",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Place, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = item.lokasi,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // RSVP Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onToggleRsvp,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isAttending) EmeraldPrimary else MaterialTheme.colorScheme.primaryContainer
                    ),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Icon(
                        imageVector = if (isAttending) Icons.Default.CheckCircle else Icons.Default.Check,
                        contentDescription = null,
                        tint = if (isAttending) Color.White else MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isAttending) "Sudah Konfirmasi Hadir" else "Konfirmasi Kehadiran",
                        color = if (isAttending) Color.White else MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }

                TextButton(
                    onClick = { showAttendeesList = !showAttendeesList }
                ) {
                    Text(
                        text = "${item.rsvpCount} Warga Hadir",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldPrimary
                    )
                }
            }

            // Attendee names dropdown
            AnimatedVisibility(visible = showAttendeesList) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(10.dp)
                ) {
                    Text("Daftar Warga yang Hadir:", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (item.rsvpWargaList.isBlank()) "Belum ada yang konfirmasi." else item.rsvpWargaList,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun CreatePengumumanDialog(
    onSubmit: (Pengumuman) -> Unit,
    onDismiss: () -> Unit
) {
    var judul by remember { mutableStateOf("") }
    var isi by remember { mutableStateOf("") }
    var tanggalKegiatan by remember { mutableStateOf("") }
    var jamKegiatan by remember { mutableStateOf("") }
    var lokasi by remember { mutableStateOf("") }
    var kategori by remember { mutableStateOf("Kerja Bakti") }
    var isPrioritas by remember { mutableStateOf(false) }

    val categories = listOf("Kerja Bakti", "Rapat Warga", "Sosial / Posyandu", "Keamanan / Ronda", "Informasi Penting")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier.fillMaxWidth(0.95f),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "Buat Pengumuman Kegiatan RT",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = judul,
                    onValueChange = { judul = it },
                    label = { Text("Nama Kegiatan / Judul") },
                    placeholder = { Text("misal: Kerja Bakti Akbar & Bersih Selokan") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = tanggalKegiatan,
                        onValueChange = { tanggalKegiatan = it },
                        label = { Text("Hari / Tanggal") },
                        placeholder = { Text("Minggu, 4 Okt 2026") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = jamKegiatan,
                        onValueChange = { jamKegiatan = it },
                        label = { Text("Waktu Pelaksanaan") },
                        placeholder = { Text("07:30 - 10:00 WIB") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = lokasi,
                    onValueChange = { lokasi = it },
                    label = { Text("Lokasi Acara") },
                    placeholder = { Text("Lapangan Fasum RT Puri Pratama") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = isi,
                    onValueChange = { isi = it },
                    label = { Text("Rincian / Keterangan Kegiatan") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text("Kategori:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Column {
                    categories.chunked(2).forEach { rowCats ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            rowCats.forEach { cat ->
                                FilterChip(
                                    selected = kategori == cat,
                                    onClick = { kategori = cat },
                                    label = { Text(cat, fontSize = 11.sp) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 6.dp)
                ) {
                    Checkbox(
                        checked = isPrioritas,
                        onCheckedChange = { isPrioritas = it }
                    )
                    Text("Tandai sebagai Agenda Prioritas / Wajib", fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Batal") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (judul.isNotBlank() && isi.isNotBlank()) {
                                val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                                onSubmit(
                                    Pengumuman(
                                        judul = judul.trim(),
                                        isi = isi.trim(),
                                        tanggalKegiatan = tanggalKegiatan.trim().ifBlank { "Segera Ditentukan" },
                                        jamKegiatan = jamKegiatan.trim().ifBlank { "08:00 WIB" },
                                        lokasi = lokasi.trim().ifBlank { "Bale Warga RT Puri Pratama" },
                                        kategori = kategori,
                                        tanggalPost = today,
                                        isPrioritas = isPrioritas
                                    )
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                    ) {
                        Text("Terbitkan Pengumuman")
                    }
                }
            }
        }
    }
}

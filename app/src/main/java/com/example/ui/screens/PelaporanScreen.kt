package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.model.PelaporanLingkungan
import com.example.data.model.UserRole
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.CoralDanger
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.viewmodel.RtViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PelaporanScreen(
    viewModel: RtViewModel
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val pelaporanList by viewModel.pelaporanList.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }
    var selectedLaporanForResponse by remember { mutableStateOf<PelaporanLingkungan?>(null) }
    var filterKategori by remember { mutableStateOf("SEMUA") }

    val isPengurus = currentUser?.role == UserRole.PENGURUS_RT

    val filteredList = if (filterKategori == "SEMUA") {
        pelaporanList
    } else {
        pelaporanList.filter { it.status == filterKategori }
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = EmeraldPrimary,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.AddComment, contentDescription = null) },
                text = { Text("Buat Laporan Lingkungan", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("create_laporan_fab")
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .testTag("pelaporan_screen")
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Pelaporan Lingkungan RT",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Laporkan lampu jalan padam, sampah menumpuk, got tersumbat, atau gangguan fasilitas umum.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Filter status chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = filterKategori == "SEMUA",
                    onClick = { filterKategori = "SEMUA" },
                    label = { Text("Semua (${pelaporanList.size})") }
                )
                FilterChip(
                    selected = filterKategori == "TERKIRIM",
                    onClick = { filterKategori = "TERKIRIM" },
                    label = { Text("Menunggu") }
                )
                FilterChip(
                    selected = filterKategori == "DIPROSES",
                    onClick = { filterKategori = "DIPROSES" },
                    label = { Text("Diproses") }
                )
                FilterChip(
                    selected = filterKategori == "SELESAI",
                    onClick = { filterKategori = "SELESAI" },
                    label = { Text("Selesai") }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (filteredList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Tidak ada laporan lingkungan dalam kategori ini.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(filteredList, key = { it.id }) { item ->
                        LaporanCard(
                            laporan = item,
                            isPengurus = isPengurus,
                            onUpdateStatus = {
                                selectedLaporanForResponse = item
                            },
                            onDelete = {
                                viewModel.deleteLaporan(item)
                            }
                        )
                    }
                }
            }
        }
    }

    // Buat Laporan Dialog
    if (showCreateDialog) {
        CreateLaporanDialog(
            reporterName = currentUser?.namaLengkap ?: "Warga RT",
            reporterPhone = currentUser?.noHp ?: "",
            defaultLocation = currentUser?.blokRumah ?: "",
            onSubmit = { laporan ->
                viewModel.submitLaporan(laporan)
                showCreateDialog = false
            },
            onDismiss = { showCreateDialog = false }
        )
    }

    // Response Dialog (Pengurus RT updates status & gives response)
    selectedLaporanForResponse?.let { laporan ->
        UpdateLaporanStatusDialog(
            laporan = laporan,
            onSave = { newStatus, responseText ->
                viewModel.updateLaporanStatus(laporan.id, newStatus, responseText)
                selectedLaporanForResponse = null
            },
            onDismiss = { selectedLaporanForResponse = null }
        )
    }
}

@Composable
fun LaporanCard(
    laporan: PelaporanLingkungan,
    isPengurus: Boolean,
    onUpdateStatus: () -> Unit,
    onDelete: () -> Unit
) {
    val statusColor = when (laporan.status) {
        "SELESAI" -> EmeraldPrimary
        "DIPROSES" -> AmberAccent
        else -> CoralDanger
    }

    val statusText = when (laporan.status) {
        "SELESAI" -> "SELESAI / TUNTAS"
        "DIPROSES" -> "SEDANG DIPROSES"
        else -> "MENUNGGU RESPON"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Category & Status Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = laporan.kategori,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = statusColor.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = statusText,
                        color = statusColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = laporan.judul,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = laporan.deskripsi,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                modifier = Modifier.padding(vertical = 4.dp)
            )

            // Attached Photo (if any)
            if (laporan.fotoUri.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                AsyncImage(
                    model = laporan.fotoUri,
                    contentDescription = "Foto Laporan",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(10.dp))
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = EmeraldPrimary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = laporan.lokasiDetail,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = "Pelapor: ${laporan.namaPelapor} • Tgl: ${laporan.tanggalLapor}",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp)
            )

            // Tanggapan Pengurus Box (if any)
            if (laporan.tanggapanPengurus.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Verified, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Tanggapan Pengurus RT:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = EmeraldPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = laporan.tanggapanPengurus,
                            fontSize = 12.sp,
                            style = MaterialTheme.typography.bodySmall
                        )
                        if (laporan.tanggalSelesai.isNotBlank()) {
                            Text(
                                text = "Diselesaikan: ${laporan.tanggalSelesai}",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Pengurus update action
            if (isPengurus) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onUpdateStatus,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Icon(Icons.Default.EditNote, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Tanggapi & Ubah Status", fontSize = 11.sp)
                    }

                    IconButton(onClick = onDelete, modifier = Modifier.size(34.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun CreateLaporanDialog(
    reporterName: String,
    reporterPhone: String,
    defaultLocation: String,
    onSubmit: (PelaporanLingkungan) -> Unit,
    onDismiss: () -> Unit
) {
    var judul by remember { mutableStateOf("") }
    var deskripsi by remember { mutableStateOf("") }
    var lokasi by remember { mutableStateOf(defaultLocation) }
    var selectedKategori by remember { mutableStateOf("Lampu Jalan") }
    var selectedPhotoUri by remember { mutableStateOf<Uri?>(null) }

    val categories = listOf(
        "Lampu Jalan",
        "Sampah & Kebersihan",
        "Saluran Got/Drainase",
        "Keamanan & Ronda",
        "Fasum & Taman",
        "Jalan Rusak",
        "Lainnya"
    )

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        selectedPhotoUri = uri
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier.fillMaxWidth(0.95f),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                Text(
                    text = "Buat Laporan Lingkungan",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = judul,
                    onValueChange = { judul = it },
                    label = { Text("Judul Laporan") },
                    placeholder = { Text("misal: Lampu jalan padam di Blok A") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = lokasi,
                    onValueChange = { lokasi = it },
                    label = { Text("Lokasi Spesifik Kejadian") },
                    placeholder = { Text("misal: Depan tiang listrik Blok B2 No. 04") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = deskripsi,
                    onValueChange = { deskripsi = it },
                    label = { Text("Uraian / Deskripsi Keluhan") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text("Kategori Masalah:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Column {
                    categories.chunked(2).forEach { rowCats ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            rowCats.forEach { cat ->
                                FilterChip(
                                    selected = selectedKategori == cat,
                                    onClick = { selectedKategori = cat },
                                    label = { Text(cat, fontSize = 11.sp) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Lampirkan Foto
                OutlinedButton(
                    onClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.PhotoCamera, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (selectedPhotoUri != null) "Foto Terlampir (Klik Ganti)" else "Lampirkan Foto Kondisi")
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Batal") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (judul.isNotBlank() && deskripsi.isNotBlank()) {
                                val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                                onSubmit(
                                    PelaporanLingkungan(
                                        judul = judul.trim(),
                                        deskripsi = deskripsi.trim(),
                                        kategori = selectedKategori,
                                        lokasiDetail = lokasi.trim(),
                                        fotoUri = selectedPhotoUri?.toString() ?: "",
                                        namaPelapor = reporterName,
                                        kontakPelapor = reporterPhone,
                                        status = "TERKIRIM",
                                        tanggalLapor = today
                                    )
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                    ) {
                        Text("Kirim Laporan")
                    }
                }
            }
        }
    }
}

@Composable
fun UpdateLaporanStatusDialog(
    laporan: PelaporanLingkungan,
    onSave: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var newStatus by remember { mutableStateOf(laporan.status) }
    var tanggapan by remember { mutableStateOf(laporan.tanggapanPengurus) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier.fillMaxWidth(0.95f),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "Tanggapi & Update Status Laporan",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = laporan.judul,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = EmeraldPrimary
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text("Pilih Status Pengerjaan:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(
                        selected = newStatus == "TERKIRIM",
                        onClick = { newStatus = "TERKIRIM" },
                        label = { Text("Menunggu", fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = newStatus == "DIPROSES",
                        onClick = { newStatus = "DIPROSES" },
                        label = { Text("Diproses", fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = newStatus == "SELESAI",
                        onClick = { newStatus = "SELESAI" },
                        label = { Text("Selesai", fontSize = 11.sp) }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = tanggapan,
                    onValueChange = { tanggapan = it },
                    label = { Text("Tanggapan Resmi Pengurus RT") },
                    placeholder = { Text("misal: Petugas telah memperbaiki lampu dan sudah menyala kembali.") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Batal") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onSave(newStatus, tanggapan.trim())
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                    ) {
                        Text("Simpan Tanggapan")
                    }
                }
            }
        }
    }
}

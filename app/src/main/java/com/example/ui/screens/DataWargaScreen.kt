package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.IuranConfig
import com.example.data.model.UserRole
import com.example.data.model.Warga
import com.example.ui.theme.CoralDanger
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.viewmodel.RtViewModel
import com.example.util.EmergencyHelper
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataWargaScreen(
    viewModel: RtViewModel
) {
    val context = LocalContext.current
    val wargaList by viewModel.wargaList.collectAsState()
    val iuranConfig by viewModel.iuranConfig.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var filterStatus by remember { mutableStateOf("SEMUA") } // SEMUA, BELUM_LUNAS, LUNAS
    var selectedWargaForEdit by remember { mutableStateOf<Warga?>(null) }
    var showAddWargaDialog by remember { mutableStateOf(false) }

    val isPengurus = currentUser?.role == UserRole.PENGURUS_RT

    val filteredList = wargaList.filter { w ->
        val matchesSearch = w.nama.contains(searchQuery, ignoreCase = true) ||
                w.blokRumah.contains(searchQuery, ignoreCase = true) ||
                w.nik.contains(searchQuery) ||
                w.noHp.contains(searchQuery)

        val matchesStatus = when (filterStatus) {
            "BELUM_LUNAS" -> !w.statusIuranBulanIni
            "LUNAS" -> w.statusIuranBulanIni
            else -> true
        }
        matchesSearch && matchesStatus
    }

    val belumLunasCount = wargaList.count { !it.statusIuranBulanIni }
    val lunasCount = wargaList.count { it.statusIuranBulanIni }

    Scaffold(
        floatingActionButton = {
            if (isPengurus) {
                FloatingActionButton(
                    onClick = { showAddWargaDialog = true },
                    containerColor = EmeraldPrimary,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("add_warga_fab")
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = "Tambah Warga")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .testTag("data_warga_screen")
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Screen Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Data Warga RT Puri Pratama",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Total: ${wargaList.size} KK • $lunasCount Lunas • $belumLunasCount Belum Bayar",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Cari nama, blok rumah, NIK, atau no HP...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = null)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_warga_input"),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Filter Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = filterStatus == "SEMUA",
                    onClick = { filterStatus = "SEMUA" },
                    label = { Text("Semua (${wargaList.size})") }
                )
                FilterChip(
                    selected = filterStatus == "BELUM_LUNAS",
                    onClick = { filterStatus = "BELUM_LUNAS" },
                    label = { Text("Belum Bayar ($belumLunasCount)") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CoralDanger.copy(alpha = 0.15f),
                        selectedLabelColor = CoralDanger
                    )
                )
                FilterChip(
                    selected = filterStatus == "LUNAS",
                    onClick = { filterStatus = "LUNAS" },
                    label = { Text("Lunas ($lunasCount)") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = EmeraldPrimary.copy(alpha = 0.15f),
                        selectedLabelColor = EmeraldPrimary
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Citizens List
            if (filteredList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Tidak ditemukan data warga.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(filteredList, key = { it.id }) { warga ->
                        WargaCard(
                            warga = warga,
                            isPengurus = isPengurus,
                            iuranConfig = iuranConfig ?: IuranConfig(),
                            onToggleStatus = {
                                viewModel.togglePaymentStatus(warga, "September 2026")
                            },
                            onSendWaReminder = {
                                val cfg = iuranConfig ?: IuranConfig()
                                val bankInfo = "${cfg.bankRekening} ${cfg.noRekening} a.n ${cfg.namaRekening}\n(Bendahara: ${cfg.namaBendahara} - ${cfg.kontakBendaharaWa})"
                                EmergencyHelper.sendIuranReminderWhatsApp(
                                    context = context,
                                    targetPhone = warga.noHp,
                                    wargaName = warga.nama,
                                    blok = warga.blokRumah,
                                    periode = "September 2026",
                                    nominal = warga.totalIuranNominal,
                                    bankInfo = bankInfo
                                )
                            },
                            onEdit = {
                                selectedWargaForEdit = warga
                            },
                            onDelete = {
                                viewModel.deleteWarga(warga)
                                Toast.makeText(context, "Data warga dihapus", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }
        }
    }

    // Edit Warga Dialog
    selectedWargaForEdit?.let { warga ->
        AddOrEditWargaDialog(
            initialWarga = warga,
            onSave = { updated ->
                viewModel.saveWarga(updated)
                selectedWargaForEdit = null
            },
            onDismiss = { selectedWargaForEdit = null }
        )
    }

    // Add Warga Dialog
    if (showAddWargaDialog) {
        AddOrEditWargaDialog(
            initialWarga = null,
            onSave = { newWarga ->
                viewModel.saveWarga(newWarga)
                showAddWargaDialog = false
            },
            onDismiss = { showAddWargaDialog = false }
        )
    }
}

@Composable
fun WargaCard(
    warga: Warga,
    isPengurus: Boolean,
    iuranConfig: IuranConfig,
    onToggleStatus: () -> Unit,
    onSendWaReminder: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val isLunas = warga.statusIuranBulanIni
    val rupiahFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("warga_card_${warga.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Row 1: Name & Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(if (isLunas) EmeraldPrimary.copy(alpha = 0.15f) else CoralDanger.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isLunas) Icons.Default.CheckCircle else Icons.Default.Person,
                            contentDescription = null,
                            tint = if (isLunas) EmeraldPrimary else CoralDanger,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = warga.nama,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "${warga.blokRumah} • ${warga.statusHunian}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isLunas) EmeraldPrimary.copy(alpha = 0.12f) else CoralDanger.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = if (isLunas) "LUNAS" else "BELUM BAYAR",
                        color = if (isLunas) EmeraldPrimary else CoralDanger,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(8.dp))

            // Detail Data Warga: NIK, KK, HP, Keluarga
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("NIK (KTP):", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(warga.nik, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Nomor KK:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(warga.noKk, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("No. HP (WhatsApp):", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(warga.noHp, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Anggota Keluarga:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${warga.jumlahKeluarga} Jiwa", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Actions Row: WA Reminder (if not paid) + Status toggle (if pengurus)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // If Belum Bayar: WhatsApp Reminder Button
                if (!isLunas) {
                    Button(
                        onClick = onSendWaReminder,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)), // WhatsApp Green
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("send_wa_reminder_${warga.id}")
                    ) {
                        Icon(Icons.Default.Chat, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Kirim Pengingat WA",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                } else {
                    Text(
                        text = "Lunas tgl: ${warga.tanggalBayar.ifEmpty { "-" }}",
                        fontSize = 11.sp,
                        color = EmeraldPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Pengurus Controls: Toggle Lunas / Edit / Delete
                if (isPengurus) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedButton(
                            onClick = onToggleStatus,
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text(
                                text = if (isLunas) "Batalkan Lunas" else "Tandai Lunas",
                                fontSize = 11.sp
                            )
                        }

                        IconButton(onClick = onEdit, modifier = Modifier.size(34.dp)) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(18.dp))
                        }

                        IconButton(onClick = onDelete, modifier = Modifier.size(34.dp)) {
                            Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddOrEditWargaDialog(
    initialWarga: Warga?,
    onSave: (Warga) -> Unit,
    onDismiss: () -> Unit
) {
    val isEdit = initialWarga != null
    var nama by remember { mutableStateOf(initialWarga?.nama ?: "") }
    var nik by remember { mutableStateOf(initialWarga?.nik ?: "") }
    var noKk by remember { mutableStateOf(initialWarga?.noKk ?: "") }
    var noHp by remember { mutableStateOf(initialWarga?.noHp ?: "") }
    var blok by remember { mutableStateOf(initialWarga?.blokRumah ?: "") }
    var statusHunian by remember { mutableStateOf(initialWarga?.statusHunian ?: "Pemilik Tetap") }
    var jumlahKeluarga by remember { mutableStateOf(initialWarga?.jumlahKeluarga?.toString() ?: "3") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .wrapContentHeight(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = if (isEdit) "Edit Data Warga" else "Tambah Data Warga Baru",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = nama,
                    onValueChange = { nama = it },
                    label = { Text("Nama Lengkap Kepala Keluarga") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = nik,
                        onValueChange = { if (it.length <= 16) nik = it },
                        label = { Text("NIK (16 Digit)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = noKk,
                        onValueChange = { if (it.length <= 16) noKk = it },
                        label = { Text("Nomor KK (16 Digit)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = noHp,
                        onValueChange = { noHp = it },
                        label = { Text("No. WhatsApp") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = blok,
                        onValueChange = { blok = it },
                        label = { Text("Blok & No Rumah") },
                        placeholder = { Text("misal: Blok A1 No. 04") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = jumlahKeluarga,
                        onValueChange = { jumlahKeluarga = it },
                        label = { Text("Jumlah Anggota") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Status Hunian:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Row {
                            FilterChip(
                                selected = statusHunian == "Pemilik Tetap",
                                onClick = { statusHunian = "Pemilik Tetap" },
                                label = { Text("Tetap", fontSize = 11.sp) }
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            FilterChip(
                                selected = statusHunian == "Kontrak / Sewa",
                                onClick = { statusHunian = "Kontrak / Sewa" },
                                label = { Text("Sewa", fontSize = 11.sp) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Batal") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (nama.isNotBlank() && blok.isNotBlank()) {
                                val wargaToSave = initialWarga?.copy(
                                    nama = nama.trim(),
                                    nik = nik.trim(),
                                    noKk = noKk.trim(),
                                    noHp = noHp.trim(),
                                    blokRumah = blok.trim(),
                                    statusHunian = statusHunian,
                                    jumlahKeluarga = jumlahKeluarga.toIntOrNull() ?: 3
                                ) ?: Warga(
                                    nama = nama.trim(),
                                    nik = nik.trim().ifBlank { "3276010000000000" },
                                    noKk = noKk.trim().ifBlank { "3276010000000000" },
                                    noHp = noHp.trim(),
                                    blokRumah = blok.trim(),
                                    statusHunian = statusHunian,
                                    jumlahKeluarga = jumlahKeluarga.toIntOrNull() ?: 3,
                                    statusIuranBulanIni = false
                                )
                                onSave(wargaToSave)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                    ) {
                        Text(if (isEdit) "Simpan Perubahan" else "Tambah Warga")
                    }
                }
            }
        }
    }
}

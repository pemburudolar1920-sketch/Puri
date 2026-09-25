package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.IuranConfig
import com.example.data.model.KasTransaction
import com.example.data.model.UserRole
import com.example.ui.components.EditIuranDialog
import com.example.ui.components.ExportExcelDialog
import com.example.ui.theme.CoralDanger
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.viewmodel.RtViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IuranKasScreen(
    viewModel: RtViewModel
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val kasTransactions by viewModel.kasTransactions.collectAsState()
    val wargaList by viewModel.wargaList.collectAsState()
    val iuranConfig by viewModel.iuranConfig.collectAsState()

    var showEditIuranDialog by remember { mutableStateOf(false) }
    var showAddTransactionDialog by remember { mutableStateOf(false) }
    var showExportExcelDialog by remember { mutableStateOf(false) }

    var filterType by remember { mutableStateOf("SEMUA") } // SEMUA, PEMASUKAN, PENGELUARAN
    val selectedMonth by remember { mutableStateOf("September 2026") }

    val rupiahFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))

    val filteredList = when (filterType) {
        "PEMASUKAN" -> kasTransactions.filter { it.jenis == "PEMASUKAN" }
        "PENGELUARAN" -> kasTransactions.filter { it.jenis == "PENGELUARAN" }
        else -> kasTransactions
    }

    val totalPemasukan = kasTransactions.filter { it.jenis == "PEMASUKAN" }.sumOf { it.nominal }
    val totalPengeluaran = kasTransactions.filter { it.jenis == "PENGELUARAN" }.sumOf { it.nominal }
    val saldoKas = totalPemasukan - totalPengeluaran

    val isPengurus = currentUser?.role == UserRole.PENGURUS_RT

    Scaffold(
        floatingActionButton = {
            if (isPengurus) {
                ExtendedFloatingActionButton(
                    onClick = { showAddTransactionDialog = true },
                    containerColor = EmeraldPrimary,
                    contentColor = Color.White,
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("Catat Kas RT", fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("add_kas_fab")
                )
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp)
        ) {
            // Header Title & Excel Export
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Laporan Kas RT Real-Time",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Periode: $selectedMonth",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    FilledTonalButton(
                        onClick = { showExportExcelDialog = true },
                        colors = ButtonDefaults.filledTonalButtonColors(containerColor = Color(0xFF107C41).copy(alpha = 0.15f)),
                        modifier = Modifier.testTag("open_excel_export_button")
                    ) {
                        Icon(
                            Icons.Default.FileDownload,
                            contentDescription = null,
                            tint = Color(0xFF107C41),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "Export Excel",
                            color = Color(0xFF107C41),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Summary Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = EmeraldPrimary),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "TOTAL SALDO KAS RT PURI PRATAMA",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = rupiahFormat.format(saldoKas),
                            color = Color.White,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Total Pemasukan", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
                                Text(
                                    rupiahFormat.format(totalPemasukan),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Total Pengeluaran", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
                                Text(
                                    rupiahFormat.format(totalPengeluaran),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Config Iuran Bulanan Card (Feature: Edit besar iuran perbulan)
            item {
                val cfg = iuranConfig ?: IuranConfig()
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.PriceCheck,
                                    contentDescription = null,
                                    tint = EmeraldPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Besaran Iuran Warga Perbulan",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleSmall
                                )
                            }
                            if (isPengurus) {
                                TextButton(
                                    onClick = { showEditIuranDialog = true },
                                    modifier = Modifier.testTag("edit_iuran_button")
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Edit Iuran", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Text(
                            text = "Total Iuran: ${rupiahFormat.format(cfg.totalIuran)} / KK per bulan",
                            fontWeight = FontWeight.ExtraBold,
                            color = EmeraldPrimary,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        // Breakdown items
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("• Keamanan (Satpam):", fontSize = 12.sp)
                            Text(rupiahFormat.format(cfg.iuranKeamanan), fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("• Kebersihan (Sampah):", fontSize = 12.sp)
                            Text(rupiahFormat.format(cfg.iuranKebersihan), fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("• Kas RT & Operasional:", fontSize = 12.sp)
                            Text(rupiahFormat.format(cfg.iuranKasRt), fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("• Kas Sosial & Kematian:", fontSize = 12.sp)
                            Text(rupiahFormat.format(cfg.iuranSosial), fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                        }

                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        Text(
                            text = "Pembayaran via Rekening: ${cfg.bankRekening} ${cfg.noRekening} a.n ${cfg.namaRekening}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Filter Tabs
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = filterType == "SEMUA",
                        onClick = { filterType = "SEMUA" },
                        label = { Text("Semua (${kasTransactions.size})") }
                    )
                    FilterChip(
                        selected = filterType == "PEMASUKAN",
                        onClick = { filterType = "PEMASUKAN" },
                        label = { Text("Pemasukan") }
                    )
                    FilterChip(
                        selected = filterType == "PENGELUARAN",
                        onClick = { filterType = "PENGELUARAN" },
                        label = { Text("Pengeluaran") }
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Transaction items
            if (filteredList.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Belum ada catatan transaksi pada filter ini.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(filteredList, key = { it.id }) { trans ->
                    KasTransactionItem(
                        transaction = trans,
                        rupiahFormat = rupiahFormat,
                        canDelete = isPengurus,
                        onDelete = { viewModel.deleteKasTransaction(trans) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }

    // Edit Iuran Dialog
    if (showEditIuranDialog) {
        val cfg = iuranConfig ?: IuranConfig()
        EditIuranDialog(
            config = cfg,
            onSave = { updated ->
                viewModel.updateIuranConfig(updated)
            },
            onDismiss = { showEditIuranDialog = false }
        )
    }

    // Export Excel Dialog
    if (showExportExcelDialog) {
        ExportExcelDialog(
            periode = selectedMonth,
            wargaList = wargaList,
            kasTransactions = kasTransactions,
            onDismiss = { showExportExcelDialog = false }
        )
    }

    // Add Transaction Dialog (Pengurus)
    if (showAddTransactionDialog) {
        AddKasTransactionDialog(
            onSave = { trans ->
                viewModel.addKasTransaction(trans)
                showAddTransactionDialog = false
            },
            onDismiss = { showAddTransactionDialog = false }
        )
    }
}

@Composable
fun KasTransactionItem(
    transaction: KasTransaction,
    rupiahFormat: NumberFormat,
    canDelete: Boolean,
    onDelete: () -> Unit
) {
    val isPemasukan = transaction.jenis == "PEMASUKAN"
    val color = if (isPemasukan) Color(0xFF16A34A) else CoralDanger

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
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
                        .background(color.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPemasukan) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = transaction.keterangan,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "${transaction.kategori} • ${transaction.tanggal}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = (if (isPemasukan) "+ " else "- ") + rupiahFormat.format(transaction.nominal),
                    fontWeight = FontWeight.Bold,
                    color = color,
                    style = MaterialTheme.typography.bodyMedium
                )
                if (canDelete) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Default.DeleteOutline,
                            contentDescription = "Hapus",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AddKasTransactionDialog(
    onSave: (KasTransaction) -> Unit,
    onDismiss: () -> Unit
) {
    var jenis by remember { mutableStateOf("PENGELUARAN") } // PEMASUKAN or PENGELUARAN
    var kategori by remember { mutableStateOf("Gaji Satpam") }
    var keterangan by remember { mutableStateOf("") }
    var nominalStr by remember { mutableStateOf("") }
    val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    val kategoriList = if (jenis == "PENGELUARAN") {
        listOf("Gaji Satpam", "Kebersihan & Sampah", "Perbaikan Lampu Jalan", "Konsumsi Kerja Bakti", "Perbaikan Fasum", "Listrik Fasum & CCTV", "Sosial & Kematian", "Operasional RT")
    } else {
        listOf("Iuran Warga", "Donasi & Partisipasi", "Sponsorship RT", "Sewa Fasum RT", "Saldo Awal Kas")
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier.fillMaxWidth(0.95f),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "Catat Transaksi Kas RT",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Jenis Pemasukan / Pengeluaran
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            jenis = "PENGELUARAN"
                            kategori = "Gaji Satpam"
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (jenis == "PENGELUARAN") CoralDanger else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            "PENGELUARAN",
                            color = if (jenis == "PENGELUARAN") Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Button(
                        onClick = {
                            jenis = "PEMASUKAN"
                            kategori = "Iuran Warga"
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (jenis == "PEMASUKAN") Color(0xFF16A34A) else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            "PEMASUKAN",
                            color = if (jenis == "PEMASUKAN") Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = nominalStr,
                    onValueChange = { nominalStr = it },
                    label = { Text("Nominal (Rp)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = keterangan,
                    onValueChange = { keterangan = it },
                    label = { Text("Keterangan / Uraian") },
                    placeholder = { Text("misal: Pembelian kabel & bohlam lampu jalan") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text("Kategori:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Column {
                    kategoriList.chunked(2).forEach { rowItems ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            rowItems.forEach { cat ->
                                FilterChip(
                                    selected = (kategori == cat),
                                    onClick = { kategori = cat },
                                    label = { Text(cat, fontSize = 11.sp) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) {
                        Text("Batal")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val nominal = nominalStr.toLongOrNull() ?: 0L
                            if (nominal > 0 && keterangan.isNotBlank()) {
                                onSave(
                                    KasTransaction(
                                        jenis = jenis,
                                        kategori = kategori,
                                        keterangan = keterangan.trim(),
                                        nominal = nominal,
                                        tanggal = today,
                                        bulanTahun = "September 2026",
                                        dicatatOleh = "Pengurus RT"
                                    )
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                    ) {
                        Text("Simpan Transaksi")
                    }
                }
            }
        }
    }
}

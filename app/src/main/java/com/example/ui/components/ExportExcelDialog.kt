package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.KasTransaction
import com.example.data.model.Warga
import com.example.ui.theme.EmeraldPrimary
import com.example.util.ExcelExporter
import java.io.File
import java.text.NumberFormat
import java.util.Locale

@Composable
fun ExportExcelDialog(
    periode: String,
    wargaList: List<Warga>,
    kasTransactions: List<KasTransaction>,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var exportedFile by remember { mutableStateOf<File?>(null) }
    var isExporting by remember { mutableStateOf(false) }

    val wargaLunas = wargaList.filter { it.statusIuranBulanIni }
    val wargaBelumLunas = wargaList.filter { !it.statusIuranBulanIni }
    val totalPemasukan = kasTransactions.filter { it.jenis == "PEMASUKAN" }.sumOf { it.nominal }
    val totalPengeluaran = kasTransactions.filter { it.jenis == "PENGELUARAN" }.sumOf { it.nominal }
    val saldo = totalPemasukan - totalPengeluaran

    val rupiahFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .wrapContentHeight()
                .testTag("export_excel_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = Color(0xFF107C41), // Excel Green
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Download Laporan Excel",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup")
                    }
                }

                Text(
                    text = "Laporan Kas RT lengkap terstruktur rapi dan siap dibuka di Microsoft Excel, Google Spreadsheet, atau WPS Office.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                )

                // Preview Info
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "KONTEN YANG TERMUAT DALAM FILE EXCEL:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        ExportContentItem(
                            icon = Icons.Default.CheckCircle,
                            title = "Daftar Warga LUNAS Iuran",
                            detail = "${wargaLunas.size} KK lengkap dengan NIK, No KK, HP, Nominal & Tanggal Bayar"
                        )
                        ExportContentItem(
                            icon = Icons.Default.Warning,
                            title = "Daftar Warga BELUM LUNAS",
                            detail = "${wargaBelumLunas.size} KK lengkap dengan nomor WA untuk follow up"
                        )
                        ExportContentItem(
                            icon = Icons.Default.TrendingDown,
                            title = "Rincian PENGELUARAN Kas RT",
                            detail = "${kasTransactions.filter { it.jenis == "PENGELUARAN" }.size} transaksi (${rupiahFormat.format(totalPengeluaran)})"
                        )
                        ExportContentItem(
                            icon = Icons.Default.AccountBalanceWallet,
                            title = "Ringkasan Saldo Kas RT",
                            detail = "Pemasukan: ${rupiahFormat.format(totalPemasukan)} | Saldo: ${rupiahFormat.format(saldo)}"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action button: Download / Generate & Open
                Button(
                    onClick = {
                        isExporting = true
                        try {
                            val file = ExcelExporter.generateKasAndWargaCsv(
                                context = context,
                                periode = periode,
                                wargaList = wargaList,
                                kasList = kasTransactions
                            )
                            exportedFile = file
                            Toast.makeText(context, "File Excel/CSV berhasil dibuat: ${file.name}", Toast.LENGTH_LONG).show()
                            ExcelExporter.shareExportedFile(context, file)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Gagal mengexport file: ${e.message}", Toast.LENGTH_SHORT).show()
                        } finally {
                            isExporting = false
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("generate_excel_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF107C41)) // Excel green
                ) {
                    Icon(Icons.Default.FileDownload, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isExporting) "MEMBUAT DOKUMEN..." else "DOWNLOAD & BUKA DI EXCEL",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                if (exportedFile != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = {
                            exportedFile?.let { ExcelExporter.shareExportedFile(context, it) }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("BAGIKAN KE WA / EMAIL / DRIVE")
                    }
                }
            }
        }
    }
}

@Composable
fun ExportContentItem(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, detail: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = EmeraldPrimary,
            modifier = Modifier
                .size(18.dp)
                .padding(top = 2.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(title, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
            Text(detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

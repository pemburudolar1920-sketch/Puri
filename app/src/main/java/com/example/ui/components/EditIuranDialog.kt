package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.IuranConfig
import com.example.ui.theme.EmeraldPrimary
import java.text.NumberFormat
import java.util.Locale

@Composable
fun EditIuranDialog(
    config: IuranConfig,
    onSave: (IuranConfig) -> Unit,
    onDismiss: () -> Unit
) {
    var keamanan by remember { mutableStateOf(config.iuranKeamanan.toString()) }
    var kebersihan by remember { mutableStateOf(config.iuranKebersihan.toString()) }
    var kasRt by remember { mutableStateOf(config.iuranKasRt.toString()) }
    var sosial by remember { mutableStateOf(config.iuranSosial.toString()) }
    var bank by remember { mutableStateOf(config.bankRekening) }
    var noRek by remember { mutableStateOf(config.noRekening) }
    var namaRek by remember { mutableStateOf(config.namaRekening) }
    var bendahara by remember { mutableStateOf(config.namaBendahara) }
    var waBendahara by remember { mutableStateOf(config.kontakBendaharaWa) }
    var catatan by remember { mutableStateOf(config.keteranganTambahan) }

    val calculatedTotal = (keamanan.toLongOrNull() ?: 0L) +
            (kebersihan.toLongOrNull() ?: 0L) +
            (kasRt.toLongOrNull() ?: 0L) +
            (sosial.toLongOrNull() ?: 0L)

    val rupiahFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
                .testTag("edit_iuran_dialog"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                // Title Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Payments,
                            contentDescription = null,
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Edit Besaran Iuran RT",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup")
                    }
                }

                Text(
                    text = "Atur rincian pos iuran bulanan yang berlaku untuk seluruh warga RT Puri Pratama.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Total Preview Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "TOTAL IURAN / BULAN",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = rupiahFormat.format(calculatedTotal),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldPrimary
                            )
                        }
                        Text(
                            text = "per KK / Rumah",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                // Pos Iuran Inputs
                Text(
                    text = "RINCIAN KOMPONEN IURAN",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = keamanan,
                    onValueChange = { keamanan = it },
                    label = { Text("1. Iuran Keamanan / Satpam (Rp)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .testTag("input_iuran_keamanan"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = kebersihan,
                    onValueChange = { kebersihan = it },
                    label = { Text("2. Iuran Kebersihan / Sampah (Rp)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .testTag("input_iuran_kebersihan"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = kasRt,
                    onValueChange = { kasRt = it },
                    label = { Text("3. Iuran Kas Operasional RT (Rp)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .testTag("input_iuran_kas"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = sosial,
                    onValueChange = { sosial = it },
                    label = { Text("4. Iuran Sosial & Kematian (Rp)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .testTag("input_iuran_sosial"),
                    singleLine = true
                )

                Divider(modifier = Modifier.padding(vertical = 12.dp))

                // Rekening Pembayaran
                Text(
                    text = "INFORMASI REKENING KAS RT",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = bank,
                    onValueChange = { bank = it },
                    label = { Text("Nama Bank (misal: Bank Mandiri / BCA)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = noRek,
                    onValueChange = { noRek = it },
                    label = { Text("Nomor Rekening Kas") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = namaRek,
                    onValueChange = { namaRek = it },
                    label = { Text("Atas Nama Rekening") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = bendahara,
                    onValueChange = { bendahara = it },
                    label = { Text("Nama Bendahara RT") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = waBendahara,
                    onValueChange = { waBendahara = it },
                    label = { Text("No. WhatsApp Bendahara RT") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = catatan,
                    onValueChange = { catatan = it },
                    label = { Text("Catatan / Instruksi Pembayaran") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    minLines = 2
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val newConfig = config.copy(
                            iuranKeamanan = keamanan.toLongOrNull() ?: 0L,
                            iuranKebersihan = kebersihan.toLongOrNull() ?: 0L,
                            iuranKasRt = kasRt.toLongOrNull() ?: 0L,
                            iuranSosial = sosial.toLongOrNull() ?: 0L,
                            bankRekening = bank.trim(),
                            noRekening = noRek.trim(),
                            namaRekening = namaRek.trim(),
                            namaBendahara = bendahara.trim(),
                            kontakBendaharaWa = waBendahara.trim(),
                            keteranganTambahan = catatan.trim()
                        )
                        onSave(newConfig)
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("save_iuran_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("SIMPAN PERUBAHAN IURAN", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

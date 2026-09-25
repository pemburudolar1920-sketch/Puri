package com.example.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.data.model.KasTransaction
import com.example.data.model.Warga
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ExcelExporter {

    private val rupiahFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))

    fun generateKasAndWargaCsv(
        context: Context,
        periode: String,
        wargaList: List<Warga>,
        kasList: List<KasTransaction>
    ): File {
        val exportDir = File(context.cacheDir, "exports")
        if (!exportDir.exists()) exportDir.mkdirs()

        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "Laporan_Kas_RT_Puri_Pratama_${periode.replace(" ", "_")}_$timeStamp.csv"
        val file = File(exportDir, fileName)

        val wargaLunas = wargaList.filter { it.statusIuranBulanIni }
        val wargaBelumLunas = wargaList.filter { !it.statusIuranBulanIni }

        val totalPemasukan = kasList.filter { it.jenis == "PEMASUKAN" }.sumOf { it.nominal }
        val totalPengeluaran = kasList.filter { it.jenis == "PENGELUARAN" }.sumOf { it.nominal }
        val saldoKas = totalPemasukan - totalPengeluaran

        val sb = StringBuilder()
        // UTF-8 BOM so Excel opens accented & Indonesian characters properly
        sb.append("\uFEFF")

        // 1. HEADER
        sb.append("LAPORAN KAS DAN IURAN WARGA RT PURI PRATAMA\n")
        sb.append("Periode;\"$periode\"\n")
        sb.append("Dicetak Pada;\"${SimpleDateFormat("dd MMMM yyyy HH:mm", Locale("id", "ID")).format(Date())}\"\n")
        sb.append("\n")

        // 2. RINGKASAN KAS
        sb.append("=== RINGKASAN KAS RT ===\n")
        sb.append("Indikator;Nominal (Rp)\n")
        sb.append("Total Pemasukan Kas;\"${rupiahFormat.format(totalPemasukan)}\"\n")
        sb.append("Total Pengeluaran Kas;\"${rupiahFormat.format(totalPengeluaran)}\"\n")
        sb.append("Saldo Kas Bersih;\"${rupiahFormat.format(saldoKas)}\"\n")
        sb.append("Total Kepala Keluarga (KK);${wargaList.size}\n")
        sb.append("KK Lunas Iuran;${wargaLunas.size} KK\n")
        sb.append("KK Belum Lunas;${wargaBelumLunas.size} KK\n")
        sb.append("\n")

        // 3. DAFTAR WARGA LUNAS
        sb.append("=== DAFTAR WARGA LUNAS IURAN ($periode) ===\n")
        sb.append("No;Nama Warga;Blok/No Rumah;No HP / WA;NIK;No KK;Status Hunian;Nominal (Rp);Tanggal Bayar;Status\n")
        wargaLunas.forEachIndexed { index, w ->
            sb.append("${index + 1};\"${w.nama}\";\"${w.blokRumah}\";\"${w.noHp}\";\"'${w.nik}\";\"'${w.noKk}\";\"${w.statusHunian}\";\"${rupiahFormat.format(w.totalIuranNominal)}\";\"${w.tanggalBayar}\";\"LUNAS\"\n")
        }
        sb.append("\n")

        // 4. DAFTAR WARGA BELUM LUNAS
        sb.append("=== DAFTAR WARGA BELUM LUNAS IURAN ($periode) ===\n")
        sb.append("No;Nama Warga;Blok/No Rumah;No HP / WA;NIK;No KK;Status Hunian;Kewajiban Iuran (Rp);Status;Keterangan\n")
        wargaBelumLunas.forEachIndexed { index, w ->
            sb.append("${index + 1};\"${w.nama}\";\"${w.blokRumah}\";\"${w.noHp}\";\"'${w.nik}\";\"'${w.noKk}\";\"${w.statusHunian}\";\"${rupiahFormat.format(w.totalIuranNominal)}\";\"BELUM LUNAS\";\"Perlu Pengingat WA\"\n")
        }
        sb.append("\n")

        // 5. RINCIAN PENGELUARAN KAS RT
        sb.append("=== RINCIAN PENGELUARAN KAS RT ===\n")
        sb.append("No;Tanggal;Kategori Pengeluaran;Uraian / Keterangan;Nominal (Rp);Dicatat Oleh\n")
        val listPengeluaran = kasList.filter { it.jenis == "PENGELUARAN" }
        listPengeluaran.forEachIndexed { index, p ->
            sb.append("${index + 1};\"${p.tanggal}\";\"${p.kategori}\";\"${p.keterangan}\";\"${rupiahFormat.format(p.nominal)}\";\"${p.dicatatOleh}\"\n")
        }
        sb.append(";TOTAL PENGELUARAN;;;\"${rupiahFormat.format(totalPengeluaran)}\";\n")
        sb.append("\n")

        // 6. RINCIAN PEMASUKAN KAS RT
        sb.append("=== RINCIAN PEMASUKAN KAS RT ===\n")
        sb.append("No;Tanggal;Kategori Pemasukan;Uraian / Keterangan;Nominal (Rp);Dicatat Oleh\n")
        val listPemasukan = kasList.filter { it.jenis == "PEMASUKAN" }
        listPemasukan.forEachIndexed { index, p ->
            sb.append("${index + 1};\"${p.tanggal}\";\"${p.kategori}\";\"${p.keterangan}\";\"${rupiahFormat.format(p.nominal)}\";\"${p.dicatatOleh}\"\n")
        }
        sb.append(";TOTAL PEMASUKAN;;;\"${rupiahFormat.format(totalPemasukan)}\";\n")

        FileOutputStream(file).use { out ->
            out.write(sb.toString().toByteArray(Charsets.UTF_8))
        }

        return file
    }

    fun shareExportedFile(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Laporan Kas & Iuran RT Puri Pratama")
            putExtra(Intent.EXTRA_TEXT, "Berikut terlampir file Laporan Kas & Iuran RT Puri Pratama lengkap dalam format CSV/Excel.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(intent, "Buka atau Bagikan Laporan Excel/CSV via")
        context.startActivity(chooser)
    }
}

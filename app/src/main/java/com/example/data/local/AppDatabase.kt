package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.ForumPost
import com.example.data.model.IuranConfig
import com.example.data.model.KasTransaction
import com.example.data.model.PelaporanLingkungan
import com.example.data.model.Pengumuman
import com.example.data.model.User
import com.example.data.model.UserRole
import com.example.data.model.Warga
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        User::class,
        Warga::class,
        IuranConfig::class,
        KasTransaction::class,
        PelaporanLingkungan::class,
        ForumPost::class,
        Pengumuman::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun wargaDao(): WargaDao
    abstract fun iuranConfigDao(): IuranConfigDao
    abstract fun kasTransactionDao(): KasTransactionDao
    abstract fun pelaporanDao(): PelaporanDao
    abstract fun forumDao(): ForumDao
    abstract fun pengumumanDao(): PengumumanDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "rt_puri_pratama.db"
                ).addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        CoroutineScope(Dispatchers.IO).launch {
                            INSTANCE?.let { database ->
                                seedInitialData(database)
                            }
                        }
                    }
                }).build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun seedInitialData(db: AppDatabase) {
            // Seed Users
            val adminUser = User(
                username = "admin",
                passwordHash = "admin123",
                namaLengkap = "Ir. H. Budi Santoso (Ketua RT)",
                noHp = "081287654321",
                noNik = "3276011205750001",
                blokRumah = "Blok A1 No. 01",
                role = UserRole.PENGURUS_RT
            )
            val wargaUser = User(
                username = "warga",
                passwordHash = "warga123",
                namaLengkap = "Ahmad Zulkarnaen",
                noHp = "081398765432",
                noNik = "3276012408880004",
                blokRumah = "Blok B2 No. 07",
                role = UserRole.WARGA
            )
            db.userDao().insertUser(adminUser)
            db.userDao().insertUser(wargaUser)

            // Seed Iuran Configuration
            val config = IuranConfig(
                id = 1,
                iuranKeamanan = 60000L,
                iuranKebersihan = 40000L,
                iuranKasRt = 30000L,
                iuranSosial = 20000L,
                namaRekening = "KAS RT PURI PRATAMA",
                bankRekening = "Bank Mandiri",
                noRekening = "132-00-889912-3",
                namaBendahara = "Bpk. Bambang Sutrisno",
                kontakBendaharaWa = "6281234567890",
                keteranganTambahan = "Jatuh tempo setiap tanggal 10. Konfirmasi pembayaran otomatis atau kirim bukti via WhatsApp."
            )
            db.iuranConfigDao().insertOrUpdate(config)

            // Seed Warga
            val wargaList = listOf(
                Warga(
                    nama = "Ir. H. Budi Santoso",
                    nik = "3276011205750001",
                    noKk = "3276010011223344",
                    noHp = "081287654321",
                    blokRumah = "Blok A1 No. 01",
                    statusHunian = "Pemilik Tetap",
                    jumlahKeluarga = 4,
                    statusIuranBulanIni = true,
                    tanggalBayar = "2026-09-02",
                    totalIuranNominal = 150000L
                ),
                Warga(
                    nama = "Bambang Sutrisno",
                    nik = "3276011803800002",
                    noKk = "3276010011223345",
                    noHp = "081234567890",
                    blokRumah = "Blok A1 No. 02",
                    statusHunian = "Pemilik Tetap",
                    jumlahKeluarga = 3,
                    statusIuranBulanIni = true,
                    tanggalBayar = "2026-09-03",
                    totalIuranNominal = 150000L
                ),
                Warga(
                    nama = "Dr. Hendra Gunawan",
                    nik = "3276012010820003",
                    noKk = "3276010011223346",
                    noHp = "081122334455",
                    blokRumah = "Blok A2 No. 05",
                    statusHunian = "Pemilik Tetap",
                    jumlahKeluarga = 5,
                    statusIuranBulanIni = true,
                    tanggalBayar = "2026-09-05",
                    totalIuranNominal = 150000L
                ),
                Warga(
                    nama = "Ahmad Zulkarnaen",
                    nik = "3276012408880004",
                    noKk = "3276010011223347",
                    noHp = "081398765432",
                    blokRumah = "Blok B2 No. 07",
                    statusHunian = "Pemilik Tetap",
                    jumlahKeluarga = 3,
                    statusIuranBulanIni = false, // Belum bayar
                    tanggalBayar = "",
                    totalIuranNominal = 150000L
                ),
                Warga(
                    nama = "Siti Nurhaliza",
                    nik = "3276015509920005",
                    noKk = "3276010011223348",
                    noHp = "085712345678",
                    blokRumah = "Blok B1 No. 03",
                    statusHunian = "Kontrak / Sewa",
                    jumlahKeluarga = 2,
                    statusIuranBulanIni = false, // Belum bayar
                    tanggalBayar = "",
                    totalIuranNominal = 150000L
                ),
                Warga(
                    nama = "Rudi Hermawan",
                    nik = "3276011504850006",
                    noKk = "3276010011223349",
                    noHp = "087888999111",
                    blokRumah = "Blok C1 No. 09",
                    statusHunian = "Pemilik Tetap",
                    jumlahKeluarga = 4,
                    statusIuranBulanIni = false, // Belum bayar
                    tanggalBayar = "",
                    totalIuranNominal = 150000L
                ),
                Warga(
                    nama = "Wahyu Prasetyo",
                    nik = "3276010906910007",
                    noKk = "3276010011223350",
                    noHp = "081299887766",
                    blokRumah = "Blok C2 No. 12",
                    statusHunian = "Pemilik Tetap",
                    jumlahKeluarga = 3,
                    statusIuranBulanIni = true,
                    tanggalBayar = "2026-09-08",
                    totalIuranNominal = 150000L
                ),
                Warga(
                    nama = "Dewi Anggraini",
                    nik = "3276016211870008",
                    noKk = "3276010011223351",
                    noHp = "082133445566",
                    blokRumah = "Blok D1 No. 02",
                    statusHunian = "Kontrak / Sewa",
                    jumlahKeluarga = 2,
                    statusIuranBulanIni = false, // Belum bayar
                    tanggalBayar = "",
                    totalIuranNominal = 150000L
                )
            )
            db.wargaDao().insertAll(wargaList)

            // Seed Kas Transactions
            val transactions = listOf(
                KasTransaction(
                    jenis = "PEMASUKAN",
                    kategori = "Iuran Warga",
                    keterangan = "Iuran Bulanan Warga (4 KK Lunas)",
                    nominal = 600000L,
                    tanggal = "2026-09-08",
                    bulanTahun = "September 2026",
                    dicatatOleh = "Bendahara RT"
                ),
                KasTransaction(
                    jenis = "PEMASUKAN",
                    kategori = "Donasi & Partisipasi",
                    keterangan = "Sumbangan Warga untuk Pengecatan Gapura RT",
                    nominal = 750000L,
                    tanggal = "2026-09-05",
                    bulanTahun = "September 2026",
                    dicatatOleh = "Sekretaris RT"
                ),
                KasTransaction(
                    jenis = "PEMASUKAN",
                    kategori = "Saldo Awal Kas",
                    keterangan = "Sisa Saldo Kas Bulan Agustus 2026",
                    nominal = 4850000L,
                    tanggal = "2026-09-01",
                    bulanTahun = "September 2026",
                    dicatatOleh = "Bendahara RT"
                ),
                KasTransaction(
                    jenis = "PENGELUARAN",
                    kategori = "Gaji Satpam",
                    keterangan = "Honor Jaga Malam 2 Petugas Satpam RT (Term 1)",
                    nominal = 1800000L,
                    tanggal = "2026-09-10",
                    bulanTahun = "September 2026",
                    dicatatOleh = "Bendahara RT"
                ),
                KasTransaction(
                    jenis = "PENGELUARAN",
                    kategori = "Kebersihan & Sampah",
                    keterangan = "Biaya Angkut Sampah DLH / Kontainer Mingguan",
                    nominal = 450000L,
                    tanggal = "2026-09-07",
                    bulanTahun = "September 2026",
                    dicatatOleh = "Koord. Lingkungan"
                ),
                KasTransaction(
                    jenis = "PENGELUARAN",
                    kategori = "Perbaikan & Fasum",
                    keterangan = "Penggantian 3 unit Bohlam LED PJU Lampu Jalan Gang B",
                    nominal = 225000L,
                    tanggal = "2026-09-12",
                    bulanTahun = "September 2026",
                    dicatatOleh = "Sie. Pembangunan"
                ),
                KasTransaction(
                    jenis = "PENGELUARAN",
                    kategori = "Konsumsi & Sosial",
                    keterangan = "Snack dan Kopi Kerja Bakti Bersih Drainase Saluran",
                    nominal = 180000L,
                    tanggal = "2026-09-14",
                    bulanTahun = "September 2026",
                    dicatatOleh = "Sie. Sosial"
                )
            )
            db.kasTransactionDao().insertAll(transactions)

            // Seed Pelaporan Lingkungan
            val reports = listOf(
                PelaporanLingkungan(
                    judul = "Lampu Penerangan Jalan Gang B2 Mati",
                    deskripsi = "Lampu jalan di tiang depan rumah B2 No. 05 sudah mati selama 2 hari, kondisi jalan sangat gelap saat malam hari.",
                    kategori = "Lampu Jalan",
                    lokasiDetail = "Depan Blok B2 No. 05",
                    namaPelapor = "Ahmad Zulkarnaen",
                    kontakPelapor = "081398765432",
                    status = "DIPROSES",
                    tanggalLapor = "2026-09-20",
                    tanggapanPengurus = "Sudah dijadwalkan teknisi Sie Pembangunan untuk penggantian fitting dan lampu hari Rabu sore.",
                    tanggalSelesai = ""
                ),
                PelaporanLingkungan(
                    judul = "Tumpukan Ranting Pohon Pasca Angin Kencang",
                    deskripsi = "Ranting pohon tumbang menutupi separuh jalan masuk gang C, mohon bantuan diangkut.",
                    kategori = "Sampah & Kebersihan",
                    lokasiDetail = "Sudut Tikungan Gang Blok C1",
                    namaPelapor = "Rudi Hermawan",
                    kontakPelapor = "087888999111",
                    status = "SELESAI",
                    tanggalLapor = "2026-09-15",
                    tanggapanPengurus = "Telah dieksekusi oleh tim kebersihan dan warga saat kerja bakti. Jalan sudah bersih dan lancar.",
                    tanggalSelesai = "2026-09-16"
                ),
                PelaporanLingkungan(
                    judul = "Saluran Got Air Menggenang di Blok D",
                    deskripsi = "Drainase tersumbat dedaunan dan lumpur tebal sehingga air menggenang dan rawan jentik nyamuk.",
                    kategori = "Saluran Got/Drainase",
                    lokasiDetail = "Parit samping Pos Ronda Blok D",
                    namaPelapor = "Dewi Anggraini",
                    kontakPelapor = "082133445566",
                    status = "TERKIRIM",
                    tanggalLapor = "2026-09-22",
                    tanggapanPengurus = "",
                    tanggalSelesai = ""
                )
            )
            db.pelaporanDao().insertAll(reports)

            // Seed Forum Posts
            val forumList = listOf(
                ForumPost(
                    judul = "Usulan Pengadaan Portal Otomatis & CCTV Tambahan di Pintu Belakang",
                    isi = "Mengingat pintu gerbang belakang sering dilalui kendaraan non-warga saat jam malam, alangkah baiknya jika dipasang portal magnetik atau tambahan CCTV HD untuk memantau keamanan lingkungan bersama.",
                    kategori = "Keamanan",
                    penulisNama = "Dr. Hendra Gunawan",
                    penulisRole = "Warga Blok A2",
                    blokRumah = "Blok A2 No. 05",
                    tanggal = "2026-09-18",
                    upvotes = 14,
                    komentarRaw = "1::Ir. H. Budi Santoso::Ketua RT::2026-09-18 19:30::Terima kasih sarannya dr. Hendra, usulan ini sangat bagus dan akan kita agendakan di rapat musyawarah warga akhir bulan ini.||2::Bambang Sutrisno::Bendahara RT::2026-09-19 08:15::Secara estimasi kas RT saat ini mencukupi untuk biaya pemasangan kamera CCTV outdoor IP Cam 3MP."
                ),
                ForumPost(
                    judul = "Pengelolaan Bank Sampah & Daur Ulang Mandiri RT Puri Pratama",
                    isi = "Bagaimana jika RT kita membuat jadwal penimbangan sampah kardus, botol plastik dan minyak jelantah setiap minggu ke-2? Hasilnya bisa masuk tambahan kas sosial warga atau tabungan bersama.",
                    kategori = "Saran & Masukan",
                    penulisNama = "Siti Nurhaliza",
                    penulisRole = "Warga Blok B1",
                    blokRumah = "Blok B1 No. 03",
                    tanggal = "2026-09-12",
                    upvotes = 9,
                    komentarRaw = "1::Wahyu Prasetyo::Warga Blok C::2026-09-12 11:20::Sangat setuju Bu Siti, di perumahan sebelah program ini sukses dan lingkungan jadi lebih bersih."
                )
            )
            db.forumDao().insertAll(forumList)

            // Seed Pengumuman Kegiatan RT
            val announcements = listOf(
                Pengumuman(
                    judul = "Kerja Bakti Akbar & Fogging Pencegahan DBD",
                    isi = "Menyambut musim penghujan, Pengurus RT mengundang seluruh Kepala Keluarga untuk berpartisipasi dalam kerja bakti membersihkan got, merapikan tanaman liar, dan penyemprotan fogging lingkungan perumahan RT Puri Pratama. Disediakan kopi dan snack bersama.",
                    tanggalKegiatan = "Minggu, 28 September 2026",
                    jamKegiatan = "07:00 - 10:30 WIB",
                    lokasi = "Titik Kumpul Lapangan Fasum RT Puri Pratama",
                    kategori = "Kerja Bakti",
                    dibuatOleh = "Ketua RT Puri Pratama",
                    tanggalPost = "2026-09-21",
                    rsvpWargaList = "Ir. H. Budi Santoso, Bambang Sutrisno, Wahyu Prasetyo, Dr. Hendra Gunawan",
                    isPrioritas = true
                ),
                Pengumuman(
                    judul = "Jadwal Posyandu Balita & Skrining Lansia Sehat",
                    isi = "Pemeriksaan tumbuh kembang balita, imunisasi rutin, penimbangan berat badan serta cek tensi dan gula darah gratis untuk warga lansia RT Puri Pratama bekerjasama dengan Bidan Puskesmas.",
                    tanggalKegiatan = "Kamis, 1 Oktober 2026",
                    jamKegiatan = "08:30 - 11:30 WIB",
                    lokasi = "Bale Warga & Pos RT Puri Pratama",
                    kategori = "Sosial / Posyandu",
                    dibuatOleh = "Ibu-Ibu PKK RT",
                    tanggalPost = "2026-09-20",
                    rsvpWargaList = "Siti Nurhaliza, Dewi Anggraini",
                    isPrioritas = false
                ),
                Pengumuman(
                    judul = "Sosialisasi Sistem Ronda Malam Digital & Siaga Satpam",
                    isi = "Pertemuan ramah tamah dan evaluasi jadwal piket ronda siskamling mandiri untuk meningkatkan kewaspadaan malam hari di lingkungan kita.",
                    tanggalKegiatan = "Sabtu, 3 Oktober 2026",
                    jamKegiatan = "20:00 - 22:00 WIB",
                    lokasi = "Pos Satpam Utama RT",
                    kategori = "Keamanan / Ronda",
                    dibuatOleh = "Sie Keamanan RT",
                    tanggalPost = "2026-09-19",
                    rsvpWargaList = "Ir. H. Budi Santoso, Rudi Hermawan",
                    isPrioritas = false
                )
            )
            db.pengumumanDao().insertAll(announcements)
        }
    }
}

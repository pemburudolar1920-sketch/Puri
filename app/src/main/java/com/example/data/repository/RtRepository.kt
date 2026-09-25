package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.text.SimpleDateFormat
import java.util.*

class RtRepository(private val db: AppDatabase) {

    // Users
    fun getAllUsers(): Flow<List<User>> = db.userDao().getAllUsers()

    suspend fun login(username: String, passwordAttempt: String): User? {
        val user = db.userDao().getUserByUsername(username.trim())
        if (user != null && user.passwordHash == passwordAttempt) {
            return user
        }
        return null
    }

    suspend fun register(
        username: String,
        passwordHash: String,
        namaLengkap: String,
        noHp: String,
        noNik: String,
        blokRumah: String,
        role: UserRole
    ): Long {
        val user = User(
            username = username.trim(),
            passwordHash = passwordHash,
            namaLengkap = namaLengkap.trim(),
            noHp = noHp.trim(),
            noNik = noNik.trim(),
            blokRumah = blokRumah.trim(),
            role = role
        )
        val userId = db.userDao().insertUser(user)

        // If registered as warga, also auto-add to Warga list if not already present
        if (role == UserRole.WARGA) {
            val warga = Warga(
                nama = namaLengkap.trim(),
                nik = noNik.trim(),
                noKk = "3276" + (100000000000L..999999999999L).random().toString(),
                noHp = noHp.trim(),
                blokRumah = blokRumah.trim(),
                statusHunian = "Pemilik Tetap",
                jumlahKeluarga = 3,
                statusIuranBulanIni = false
            )
            db.wargaDao().insertWarga(warga)
        }
        return userId
    }

    suspend fun changePassword(userId: Long, newPassword: String) {
        db.userDao().updatePassword(userId, newPassword)
    }

    // Warga
    fun getAllWarga(): Flow<List<Warga>> = db.wargaDao().getAllWarga()
    fun getWargaBelumBayar(): Flow<List<Warga>> = db.wargaDao().getWargaBelumBayar()
    fun getWargaLunas(): Flow<List<Warga>> = db.wargaDao().getWargaLunas()

    suspend fun saveWarga(warga: Warga): Long = db.wargaDao().insertWarga(warga)
    suspend fun updateWarga(warga: Warga) = db.wargaDao().updateWarga(warga)
    suspend fun deleteWarga(warga: Warga) = db.wargaDao().deleteWarga(warga)

    suspend fun togglePaymentStatus(warga: Warga, currentMonth: String) {
        val newStatus = !warga.statusIuranBulanIni
        val today = if (newStatus) SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) else ""
        db.wargaDao().updatePaymentStatus(warga.id, newStatus, today)

        // If marked as paid, record into Kas Transactions
        if (newStatus) {
            val trans = KasTransaction(
                jenis = "PEMASUKAN",
                kategori = "Iuran Warga",
                keterangan = "Iuran ${warga.nama} (${warga.blokRumah})",
                nominal = warga.totalIuranNominal,
                tanggal = today,
                bulanTahun = currentMonth,
                dicatatOleh = "Sistem Kas RT"
            )
            db.kasTransactionDao().insertTransaction(trans)
        }
    }

    // Iuran Config (Edit besar iuran)
    fun getIuranConfig(): Flow<IuranConfig?> = db.iuranConfigDao().getConfig()

    suspend fun updateIuranConfig(config: IuranConfig) {
        db.iuranConfigDao().insertOrUpdate(config)
    }

    // Kas Transactions
    fun getAllTransactions(): Flow<List<KasTransaction>> = db.kasTransactionDao().getAllTransactions()
    fun getTransactionsByMonth(bulanTahun: String): Flow<List<KasTransaction>> = db.kasTransactionDao().getTransactionsByMonth(bulanTahun)
    suspend fun addTransaction(transaction: KasTransaction): Long = db.kasTransactionDao().insertTransaction(transaction)
    suspend fun deleteTransaction(transaction: KasTransaction) = db.kasTransactionDao().deleteTransaction(transaction)

    // Pelaporan Lingkungan
    fun getAllLaporan(): Flow<List<PelaporanLingkungan>> = db.pelaporanDao().getAllLaporan()
    suspend fun submitLaporan(laporan: PelaporanLingkungan): Long = db.pelaporanDao().insertLaporan(laporan)
    suspend fun updateLaporanStatus(id: Long, status: String, tanggapan: String) {
        val today = if (status == "SELESAI") SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) else ""
        db.pelaporanDao().updateStatusAndResponse(id, status, tanggapan, today)
    }
    suspend fun deleteLaporan(laporan: PelaporanLingkungan) = db.pelaporanDao().deleteLaporan(laporan)

    // Forum
    fun getAllForumPosts(): Flow<List<ForumPost>> = db.forumDao().getAllPosts()
    suspend fun createForumPost(post: ForumPost): Long = db.forumDao().insertPost(post)
    suspend fun upvoteForumPost(id: Long) = db.forumDao().upvote(id)
    suspend fun addForumComment(post: ForumPost, nama: String, role: String, isi: String) {
        val now = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
        val newCommentId = UUID.randomUUID().toString().take(6)
        val newEntry = "$newCommentId::$nama::$role::$now::$isi"
        val updatedRaw = if (post.komentarRaw.isBlank()) newEntry else "${post.komentarRaw}||$newEntry"
        db.forumDao().updateComments(post.id, updatedRaw)
    }

    // Pengumuman
    fun getAllPengumuman(): Flow<List<Pengumuman>> = db.pengumumanDao().getAllPengumuman()
    suspend fun createPengumuman(pengumuman: Pengumuman): Long = db.pengumumanDao().insertPengumuman(pengumuman)
    suspend fun toggleRsvp(pengumuman: Pengumuman, userName: String) {
        val attendees = pengumuman.rsvpWargaList.split(",")
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .toMutableList()

        val existingIndex = attendees.indexOfFirst { it.equals(userName.trim(), ignoreCase = true) }
        if (existingIndex >= 0) {
            attendees.removeAt(existingIndex)
        } else {
            attendees.add(userName.trim())
        }
        db.pengumumanDao().updateRsvpList(pengumuman.id, attendees.joinToString(", "))
    }
    suspend fun deletePengumuman(pengumuman: Pengumuman) = db.pengumumanDao().deletePengumuman(pengumuman)

    // CCTV Feeds for "Konek CCTV all aplikasi"
    fun getCctvCameras(): List<CctvCamera> {
        return listOf(
            CctvCamera(
                id = "cctv-01",
                namaKamera = "CCTV 01 - Gerbang Masuk Utama RT",
                lokasi = "Gapura Utama & Pos Security Depan",
                resolusi = "1080p FHD (30 FPS)",
                status = "ONLINE",
                channel = "CH-01",
                rtspUrl = "rtsp://admin:puri123@192.168.1.201:554/live/ch0",
                supportApp = "Ezviz, V380 Pro, Tuya Smart, IP Cam, VLC Player",
                ipAddress = "192.168.1.201"
            ),
            CctvCamera(
                id = "cctv-02",
                namaKamera = "CCTV 02 - Pos Keamanan & Ruang Satpam",
                lokasi = "Area Parkir Tamu & Pos Pantau",
                resolusi = "1080p FHD (Night Vision)",
                status = "ONLINE",
                channel = "CH-02",
                rtspUrl = "rtsp://admin:puri123@192.168.1.202:554/live/ch0",
                supportApp = "Ezviz, V380 Pro, Tuya Smart, IP Cam, VLC Player",
                ipAddress = "192.168.1.202"
            ),
            CctvCamera(
                id = "cctv-03",
                namaKamera = "CCTV 03 - Lapangan Fasum & Taman RT",
                lokasi = "Pusat Area Olahraga & Bale Warga",
                resolusi = "2K Super HD (Wide Angle)",
                status = "ONLINE",
                channel = "CH-03",
                rtspUrl = "rtsp://admin:puri123@192.168.1.203:554/live/ch0",
                supportApp = "Ezviz, V380 Pro, Tuya Smart, IP Cam, VLC Player",
                ipAddress = "192.168.1.203"
            ),
            CctvCamera(
                id = "cctv-04",
                namaKamera = "CCTV 04 - Pertigaan Blok A & Blok B",
                lokasi = "Jl. Puri Melati I & II",
                resolusi = "1080p FHD (IR Active)",
                status = "ONLINE",
                channel = "CH-04",
                rtspUrl = "rtsp://admin:puri123@192.168.1.204:554/live/ch0",
                supportApp = "Ezviz, V380 Pro, Tuya Smart, IP Cam, VLC Player",
                ipAddress = "192.168.1.204"
            ),
            CctvCamera(
                id = "cctv-05",
                namaKamera = "CCTV 05 - Pintu Keluar Belakang / Musholla",
                lokasi = "Pintu Portal Timur RT Puri Pratama",
                resolusi = "1080p FHD (Motion Detect)",
                status = "ONLINE",
                channel = "CH-05",
                rtspUrl = "rtsp://admin:puri123@192.168.1.205:554/live/ch0",
                supportApp = "Ezviz, V380 Pro, Tuya Smart, IP Cam, VLC Player",
                ipAddress = "192.168.1.205"
            )
        )
    }
}

package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.repository.RtRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class RtViewModel(private val repository: RtRepository) : ViewModel() {

    // Current logged-in user (Default to admin on first load so user immediately sees all admin & citizen powers)
    private val _currentUser = MutableStateFlow<User?>(
        User(
            id = 1,
            username = "admin",
            passwordHash = "admin123",
            namaLengkap = "Ir. H. Budi Santoso (Ketua RT)",
            noHp = "081287654321",
            noNik = "3276011205750001",
            blokRumah = "Blok A1 No. 01",
            role = UserRole.PENGURUS_RT
        )
    )
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    // Navigation Active Screen
    private val _currentScreen = MutableStateFlow(ScreenRoute.HOME)
    val currentScreen: StateFlow<ScreenRoute> = _currentScreen.asStateFlow()

    fun navigateTo(screen: ScreenRoute) {
        _currentScreen.value = screen
    }

    // Role switcher for test / presentation
    fun switchRole(role: UserRole) {
        if (role == UserRole.PENGURUS_RT) {
            _currentUser.value = User(
                id = 1,
                username = "admin",
                passwordHash = "admin123",
                namaLengkap = "Ir. H. Budi Santoso (Ketua RT)",
                noHp = "081287654321",
                noNik = "3276011205750001",
                blokRumah = "Blok A1 No. 01",
                role = UserRole.PENGURUS_RT
            )
        } else {
            _currentUser.value = User(
                id = 2,
                username = "warga",
                passwordHash = "warga123",
                namaLengkap = "Ahmad Zulkarnaen",
                noHp = "081398765432",
                noNik = "3276012408880004",
                blokRumah = "Blok B2 No. 07",
                role = UserRole.WARGA
            )
        }
    }

    // User authentication
    val allUsers: StateFlow<List<User>> = repository.getAllUsers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    suspend fun login(username: String, passwordAttempt: String): Boolean {
        val user = repository.login(username, passwordAttempt)
        return if (user != null) {
            _currentUser.value = user
            true
        } else false
    }

    suspend fun register(
        username: String,
        pass: String,
        nama: String,
        hp: String,
        nik: String,
        blok: String,
        role: UserRole
    ): Boolean {
        return try {
            val id = repository.register(username, pass, nama, hp, nik, blok, role)
            _currentUser.value = User(
                id = id,
                username = username,
                passwordHash = pass,
                namaLengkap = nama,
                noHp = hp,
                noNik = nik,
                blokRumah = blok,
                role = role
            )
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun changePassword(oldPass: String, newPass: String): Boolean {
        val user = _currentUser.value ?: return false
        if (user.passwordHash == oldPass) {
            repository.changePassword(user.id, newPass)
            _currentUser.value = user.copy(passwordHash = newPass)
            return true
        }
        return false
    }

    fun logout() {
        switchRole(UserRole.WARGA)
    }

    // Warga Data
    val wargaList: StateFlow<List<Warga>> = repository.getAllWarga()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun togglePaymentStatus(warga: Warga, currentMonth: String) {
        viewModelScope.launch {
            repository.togglePaymentStatus(warga, currentMonth)
        }
    }

    fun saveWarga(warga: Warga) {
        viewModelScope.launch {
            if (warga.id == 0L) {
                repository.saveWarga(warga)
            } else {
                repository.updateWarga(warga)
            }
        }
    }

    fun deleteWarga(warga: Warga) {
        viewModelScope.launch {
            repository.deleteWarga(warga)
        }
    }

    // Iuran Config
    val iuranConfig: StateFlow<IuranConfig?> = repository.getIuranConfig()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), IuranConfig())

    fun updateIuranConfig(config: IuranConfig) {
        viewModelScope.launch {
            repository.updateIuranConfig(config)
        }
    }

    // Kas Transactions
    val kasTransactions: StateFlow<List<KasTransaction>> = repository.getAllTransactions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addKasTransaction(transaction: KasTransaction) {
        viewModelScope.launch {
            repository.addTransaction(transaction)
        }
    }

    fun deleteKasTransaction(transaction: KasTransaction) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }

    // Pelaporan Lingkungan
    val pelaporanList: StateFlow<List<PelaporanLingkungan>> = repository.getAllLaporan()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun submitLaporan(laporan: PelaporanLingkungan) {
        viewModelScope.launch {
            repository.submitLaporan(laporan)
        }
    }

    fun updateLaporanStatus(id: Long, status: String, tanggapan: String) {
        viewModelScope.launch {
            repository.updateLaporanStatus(id, status, tanggapan)
        }
    }

    fun deleteLaporan(laporan: PelaporanLingkungan) {
        viewModelScope.launch {
            repository.deleteLaporan(laporan)
        }
    }

    // Forum
    val forumPosts: StateFlow<List<ForumPost>> = repository.getAllForumPosts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun createForumPost(post: ForumPost) {
        viewModelScope.launch {
            repository.createForumPost(post)
        }
    }

    fun upvoteForumPost(id: Long) {
        viewModelScope.launch {
            repository.upvoteForumPost(id)
        }
    }

    fun addForumComment(post: ForumPost, nama: String, role: String, komentar: String) {
        viewModelScope.launch {
            repository.addForumComment(post, nama, role, komentar)
        }
    }

    // Pengumuman
    val pengumumanList: StateFlow<List<Pengumuman>> = repository.getAllPengumuman()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun createPengumuman(pengumuman: Pengumuman) {
        viewModelScope.launch {
            repository.createPengumuman(pengumuman)
        }
    }

    fun toggleRsvp(pengumuman: Pengumuman, userName: String) {
        viewModelScope.launch {
            repository.toggleRsvp(pengumuman, userName)
        }
    }

    fun deletePengumuman(pengumuman: Pengumuman) {
        viewModelScope.launch {
            repository.deletePengumuman(pengumuman)
        }
    }

    // CCTV Cameras
    val cctvCameras: List<CctvCamera> = repository.getCctvCameras()
}

enum class ScreenRoute {
    HOME,
    KAS_IURAN,
    DATA_WARGA,
    PELAPORAN,
    CCTV,
    FORUM,
    PENGUMUMAN,
    AKUN
}

class RtViewModelFactory(private val repository: RtRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RtViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RtViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

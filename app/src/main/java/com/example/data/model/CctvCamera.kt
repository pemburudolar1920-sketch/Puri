package com.example.data.model

data class CctvCamera(
    val id: String,
    val namaKamera: String,
    val lokasi: String,
    val resolusi: String = "1080p FHD",
    val status: String = "ONLINE",
    val channel: String = "CH-01",
    val streamUrl: String = "",
    val rtspUrl: String = "",
    val supportApp: String = "All Apps (Ezviz, V380, Tuya, VLC, RTSP)",
    val ipAddress: String = "192.168.1.101"
)

package com.example.domain.model

data class AppInfo(
    val id: String,
    val packageName: String,
    val name: String,
    val category: String,
    val primaryColorHex: Long = 0xFF2563EB,
    val iconType: String = "app",
    val isRealInstalledApp: Boolean = false
)

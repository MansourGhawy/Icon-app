package com.example.model

import android.graphics.Bitmap

data class AppInfo(
    val appName: String,
    val packageName: String,
    val launcherActivity: String,
    val originalIcon: Bitmap,
    val dominantColor: Int = 0xFF4285F4.toInt() // fallback blue
)

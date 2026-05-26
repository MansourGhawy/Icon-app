package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.widget.Toast
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.palette.graphics.Palette
import com.example.model.AppInfo
import com.example.model.IconSettings
import com.example.utils.IconProcessor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AppListViewModel(application: Application) : AndroidViewModel(application) {

    private val _installedApps = MutableStateFlow<List<AppInfo>>(emptyList())
    val installedApps: StateFlow<List<AppInfo>> = _installedApps.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Composite state for filtering apps live
    val filteredApps: StateFlow<List<AppInfo>> = _searchQuery
        .combine(_installedApps) { query, apps ->
            if (query.isBlank()) {
                apps
            } else {
                apps.filter { it.appName.contains(query, ignoreCase = true) || it.packageName.contains(query, ignoreCase = true) }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Currently being edited app
    private val _selectedApp = MutableStateFlow<AppInfo?>(null)
    val selectedApp: StateFlow<AppInfo?> = _selectedApp.asStateFlow()

    // Active icon custom settings
    private val _iconSettings = MutableStateFlow(IconSettings())
    val iconSettings: StateFlow<IconSettings> = _iconSettings.asStateFlow()

    // Real-time parsed high fidelity preview bitmap
    private val _previewBitmap = MutableStateFlow<Bitmap?>(null)
    val previewBitmap: StateFlow<Bitmap?> = _previewBitmap.asStateFlow()

    init {
        loadInstalledApps()
    }

    fun loadInstalledApps() {
        viewModelScope.launch {
            _isLoading.value = true
            val appsList = withContext(Dispatchers.IO) {
                fetchAppsFromSystem(getApplication())
            }
            _installedApps.value = appsList
            _isLoading.value = false
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectApp(app: AppInfo) {
        _selectedApp.value = app
        // Auto-initialize settings based on application's extracted color
        val baseColor = app.dominantColor
        // Calculate corresponding darker secondary tone for background linear/radial gradient
        val darkerTone = adjustColorBrightness(baseColor, 0.3f)
        _iconSettings.value = IconSettings(
            bgColor1 = baseColor,
            bgColor2 = darkerTone
        )
        refreshPreview()
    }

    fun updateSettings(newSettings: IconSettings) {
        _iconSettings.value = newSettings
        refreshPreview()
    }

    fun refreshPreview() {
        val app = _selectedApp.value ?: return
        val settings = _iconSettings.value
        viewModelScope.launch {
            val preview = withContext(Dispatchers.Default) {
                IconProcessor.processIcon(app.originalIcon, settings, targetSize = 512)
            }
            _previewBitmap.value = preview
        }
    }

    /**
     * Pins shortcut using ShortcutManagerCompat
     */
    fun saveShortcut(customName: String) {
        val app = _selectedApp.value ?: return
        val preview = _previewBitmap.value ?: return
        val context = getApplication<Application>()

        if (!ShortcutManagerCompat.isRequestPinShortcutSupported(context)) {
            Toast.makeText(context, "Layout manager does not support pinned shortcuts!", Toast.LENGTH_LONG).show()
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            val finalName = customName.ifBlank { app.appName }
            
            // Build system launcher target intent
            val launchIntent = Intent().apply {
                setClassName(app.packageName, app.launcherActivity)
                action = Intent.ACTION_MAIN
                addCategory(Intent.CATEGORY_LAUNCHER)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }

            val uniqueId = "iconic_${app.packageName}_${System.currentTimeMillis()}"

            val shortcutInfo = ShortcutInfoCompat.Builder(context, uniqueId)
                .setShortLabel(finalName)
                .setLongLabel(finalName)
                .setIcon(IconCompat.createWithBitmap(preview))
                .setIntent(launchIntent)
                .build()

            val success = ShortcutManagerCompat.requestPinShortcut(context, shortcutInfo, null)
            withContext(Dispatchers.Main) {
                if (success) {
                    Toast.makeText(context, "Shortcut successfully pinned!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Request to pin shortcut failed.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Helper to extract system launcher applications
    private fun fetchAppsFromSystem(context: Context): List<AppInfo> {
        val items = mutableListOf<AppInfo>()
        val pm = context.packageManager

        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolveInfos = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.queryIntentActivities(intent, PackageManager.ResolveInfoFlags.of(0L))
        } else {
            pm.queryIntentActivities(intent, 0)
        }

        // Filter out our own package name from the listing to maintain focus
        val selfPkg = context.packageName

        for (resolveInfo in resolveInfos) {
            val packageName = resolveInfo.activityInfo.packageName
            if (packageName == selfPkg) continue

            val appName = resolveInfo.loadLabel(pm).toString()
            val launcherActivity = resolveInfo.activityInfo.name
            val originalDrawable = resolveInfo.loadIcon(pm) ?: continue
            val hdBitmap = drawableToHdBitmap(originalDrawable)

            // Extract dominant color dynamically
            val palette = Palette.from(hdBitmap).generate()
            val dominantColor = palette.getDominantColor(0xFF4285F4.toInt())

            items.add(
                AppInfo(
                    appName = appName,
                    packageName = packageName,
                    launcherActivity = launcherActivity,
                    originalIcon = hdBitmap,
                    dominantColor = dominantColor
                )
            )
        }

        // Sort applications alphabetized
        return items.sortedBy { it.appName.lowercase() }
    }

    private fun drawableToHdBitmap(drawable: Drawable): Bitmap {
        if (drawable is BitmapDrawable && drawable.bitmap != null) {
            return drawable.bitmap
        }
        val width = if (drawable.intrinsicWidth <= 0) 192 else drawable.intrinsicWidth
        val height = if (drawable.intrinsicHeight <= 0) 192 else drawable.intrinsicHeight

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, width, height)
        drawable.draw(canvas)
        return bitmap
    }

    private fun adjustColorBrightness(color: Int, factor: Float): Int {
        val a = (color ushr 24) and 0xFF
        val r = (((color ushr 16) and 0xFF) * factor).toInt().coerceIn(0, 255)
        val g = (((color ushr 8) and 0xFF) * factor).toInt().coerceIn(0, 255)
        val b = ((color and 0xFF) * factor).toInt().coerceIn(0, 255)
        return (a shl 24) or (r shl 16) or (g shl 8) or b
    }
}

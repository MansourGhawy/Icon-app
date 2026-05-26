package com.example.model

enum class IconShape {
    CIRCLE,
    SQUARE,
    SQUIRCLE,
    ROUNDED_RECT,
    HEXAGON,
    OCTAGON
}

enum class BackgroundType {
    SOLID,
    LINEAR_GRADIENT,
    RADIAL_GRADIENT,
    TRANSPARENT
}

data class IconSettings(
    val shape: IconShape = IconShape.ROUNDED_RECT,
    val scale: Float = 1.0f,
    val offsetX: Float = 0.0f, // raw pixels or component offset percentage
    val offsetY: Float = 0.0f,
    val hue: Float = 0.0f, // 0 - 360
    val saturation: Float = 1.0f, // 0.0 - 2.0
    val brightness: Float = 0.0f, // -100 to 100
    val contrast: Float = 1.0f, // 0.5 to 2.0
    val bgType: BackgroundType = BackgroundType.SOLID,
    val bgColor1: Int = 0xFF1E1E1E.toInt(), // Dark slate default
    val bgColor2: Int = 0xFF0D0D0D.toInt(),
    val borderWidth: Float = 0.0f, // pixels
    val borderColor: Int = 0xFFFFFFFF.toInt(),
    val shadowRadius: Float = 0.0f, // pixels
    val shadowColor: Int = 0x80000000.toInt(),
    val grayscale: Boolean = false,
    val invert: Boolean = false,
    val sepia: Boolean = false,
    val blurRadius: Int = 0 // 0 to 25
)

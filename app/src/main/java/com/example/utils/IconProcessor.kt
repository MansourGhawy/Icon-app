package com.example.utils

import android.graphics.*
import com.example.model.BackgroundType
import com.example.model.IconShape
import com.example.model.IconSettings

object IconProcessor {

    /**
     * Processes a source icon bitmap using standard Canvas API and IconSettings.
     * Renders at 512x512 resolution for high-definition output.
     */
    fun processIcon(
        originalIcon: Bitmap,
        settings: IconSettings,
        targetSize: Int = 512
    ): Bitmap {
        // 1. Create native empty ARGB_8888 bitmap
        val outputBitmap = Bitmap.createBitmap(targetSize, targetSize, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(outputBitmap)

        val size = targetSize.toFloat()
        val cx = size / 2f
        val cy = size / 2f

        // 2. Generate the shape mask path
        val path = getShapePath(settings.shape, size)

        // 3. Draw Shadow if requested (soft shadow underneath the shape)
        if (settings.shadowRadius > 0.0f) {
            val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = settings.shadowColor
                style = Paint.Style.FILL
                // Enable shadow layer in pure software rendering or target paint
                setShadowLayer(settings.shadowRadius, 0f, settings.shadowRadius / 2f, settings.shadowColor)
            }
            // Draw a background path representing shadow
            canvas.drawPath(path, shadowPaint)
        }

        // 4. Draw Background
        canvas.save()
        canvas.clipPath(path)

        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
        }

        when (settings.bgType) {
            BackgroundType.SOLID -> {
                bgPaint.color = settings.bgColor1
                canvas.drawPath(path, bgPaint)
            }
            BackgroundType.LINEAR_GRADIENT -> {
                bgPaint.shader = LinearGradient(
                    0f, 0f, size, size,
                    settings.bgColor1, settings.bgColor2,
                    Shader.TileMode.CLAMP
                )
                canvas.drawPath(path, bgPaint)
            }
            BackgroundType.RADIAL_GRADIENT -> {
                bgPaint.shader = RadialGradient(
                    cx, cy, size * 0.7f,
                    settings.bgColor1, settings.bgColor2,
                    Shader.TileMode.CLAMP
                )
                canvas.drawPath(path, bgPaint)
            }
            BackgroundType.TRANSPARENT -> {
                // Do not draw anything, leave transparent
            }
        }

        // 5. Apply image filters to the ORIGINAL icon
        var processedIcon = originalIcon

        // Apply software blur on the original icon if requested before placing
        if (settings.blurRadius > 0) {
            // Scale down original icon a bit to make box blur look smoother and prevent performance drag
            val scaledForBlur = Bitmap.createScaledBitmap(processedIcon, 256, 256, true)
            processedIcon = boxBlur(scaledForBlur, settings.blurRadius)
        }

        // 6. Draw the masked original icon using transforms
        val matrix = Matrix()
        val srcW = processedIcon.width.toFloat()
        val srcH = processedIcon.height.toFloat()

        // Center standard bounds
        matrix.postTranslate(-srcW / 2f, -srcH / 2f)
        matrix.postScale(settings.scale, settings.scale)
        matrix.postTranslate(cx + settings.offsetX, cy + settings.offsetY)

        val iconPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)

        // Generate comprehensive Color Matrix
        val finalColorMatrix = ColorMatrix()

        // Simple Saturation
        if (settings.saturation != 1.0f) {
            val satMat = ColorMatrix()
            satMat.setSaturation(settings.saturation)
            finalColorMatrix.postConcat(satMat)
        }

        // Hue
        if (settings.hue != 0.0f) {
            finalColorMatrix.postConcat(getHueMatrix(settings.hue))
        }

        // Brightness
        if (settings.brightness != 0.0f) {
            finalColorMatrix.postConcat(getBrightnessMatrix(settings.brightness))
        }

        // Contrast
        if (settings.contrast != 1.0f) {
            finalColorMatrix.postConcat(getContrastMatrix(settings.contrast))
        }

        // Filters: Grayscale
        if (settings.grayscale) {
            val grayMat = ColorMatrix()
            grayMat.setSaturation(0f)
            finalColorMatrix.postConcat(grayMat)
        }

        // Filters: Sepia
        if (settings.sepia) {
            finalColorMatrix.postConcat(ColorMatrix(floatArrayOf(
                0.393f, 0.769f, 0.189f, 0f, 0f,
                0.349f, 0.686f, 0.168f, 0f, 0f,
                0.272f, 0.534f, 0.131f, 0f, 0f,
                0f,     0f,     0f,     1f, 0f
            )))
        }

        // Filters: Invert
        if (settings.invert) {
            finalColorMatrix.postConcat(ColorMatrix(floatArrayOf(
                -1f,  0f,  0f,  0f, 255f,
                 0f, -1f,  0f,  0f, 255f,
                 0f,  0f, -1f,  0f, 255f,
                 0f,  0f,  0f,  1f,   0f
            )))
        }

        iconPaint.colorFilter = ColorMatrixColorFilter(finalColorMatrix)
        canvas.drawBitmap(processedIcon, matrix, iconPaint)
        canvas.restore()

        // 7. Draw Border Stroke
        if (settings.borderWidth > 0.0f) {
            val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = settings.borderColor
                style = Paint.Style.STROKE
                strokeWidth = settings.borderWidth
                strokeJoin = Paint.Join.ROUND
                strokeCap = Paint.Cap.ROUND
            }
            canvas.drawPath(path, borderPaint)
        }

        return outputBitmap
    }

    /**
     * Mathematically generates beautiful, geometric adaptive paths.
     */
    private fun getShapePath(shape: IconShape, size: Float): Path {
        val path = Path()
        val cx = size / 2f
        val cy = size / 2f
        val radius = size / 2f

        // Deduct spacing slightly to prevent border cutoff along margins
        val inset = 4f
        val r = radius - inset
        val l = inset
        val t = inset
        val rBound = size - inset
        val bBound = size - inset

        when (shape) {
            IconShape.CIRCLE -> {
                path.addCircle(cx, cy, r, Path.Direction.CW)
            }
            IconShape.SQUARE -> {
                path.addRect(l, t, rBound, bBound, Path.Direction.CW)
            }
            IconShape.ROUNDED_RECT -> {
                val rx = size * 0.2f
                val ry = size * 0.2f
                val rectF = RectF(l, t, rBound, bBound)
                path.addRoundRect(rectF, rx, ry, Path.Direction.CW)
            }
            IconShape.SQUIRCLE -> {
                // Generates squircle via mathematical super-ellipse formula: x^4 + y^4 = r^4 (scaled parameters)
                path.moveTo(cx + r, cy)
                val power = 0.65 // super-ellipse curvature exponent
                for (angleDeg in 1..360) {
                    val angleRad = Math.toRadians(angleDeg.toDouble())
                    val cosVal = Math.cos(angleRad)
                    val sinVal = Math.sin(angleRad)
                    val x = cx + r * Math.signum(cosVal) * Math.pow(Math.abs(cosVal), power)
                    val y = cy + r * Math.signum(sinVal) * Math.pow(Math.abs(sinVal), power)
                    path.lineTo(x.toFloat(), y.toFloat())
                }
                path.close()
            }
            IconShape.HEXAGON -> {
                val rHex = r
                path.moveTo(cx + rHex, cy)
                for (i in 1..5) {
                    val angle = i * Math.PI / 3.0
                    val x = cx + rHex * Math.cos(angle).toFloat()
                    val y = cy + rHex * Math.sin(angle).toFloat()
                    path.lineTo(x, y)
                }
                path.close()
            }
            IconShape.OCTAGON -> {
                val rOct = r
                path.moveTo(cx + rOct, cy)
                for (i in 1..7) {
                    val angle = i * Math.PI / 4.0
                    val x = cx + rOct * Math.cos(angle).toFloat()
                    val y = cy + rOct * Math.sin(angle).toFloat()
                    path.lineTo(x, y)
                }
                path.close()
            }
        }
        return path
    }

    private fun getHueMatrix(hue: Float): ColorMatrix {
        val matrix = ColorMatrix()
        val angle = hue * Math.PI / 180.0
        val cos = Math.cos(angle).toFloat()
        val sin = Math.sin(angle).toFloat()

        val lumR = 0.213f
        val lumG = 0.715f
        val lumB = 0.072f

        val m = floatArrayOf(
            lumR + cos * (1 - lumR) + sin * (-lumR), lumG + cos * (-lumG) + sin * (-lumG), lumB + cos * (-lumB) + sin * (1 - lumB), 0f, 0f,
            lumR + cos * (-lumR) + sin * (0.143f),   lumG + cos * (1 - lumG) + sin * (0.140f),   lumB + cos * (-lumB) + sin * (-0.283f), 0f, 0f,
            lumR + cos * (-lumR) + sin * (-(1 - lumR)), lumG + cos * (-lumG) + sin * (lumG),      lumB + cos * (1 - lumB) + sin * (lumB), 0f, 0f,
            0f, 0f, 0f, 1f, 0f
        )
        matrix.set(m)
        return matrix
    }

    private fun getBrightnessMatrix(brightness: Float): ColorMatrix {
        // brightness is a slider value -100 to 100
        val matrix = ColorMatrix()
        val m = floatArrayOf(
            1f, 0f, 0f, 0f, brightness,
            0f, 1f, 0f, 0f, brightness,
            0f, 0f, 1f, 0f, brightness,
            0f, 0f, 0f, 1f, 0f
        )
        matrix.set(m)
        return matrix
    }

    private fun getContrastMatrix(contrast: Float): ColorMatrix {
        // contrast is 0.5 to 2.0
        val scale = contrast
        val offset = 128f * (1f - scale)
        val matrix = ColorMatrix()
        val m = floatArrayOf(
            scale, 0f, 0f, 0f, offset,
            0f, scale, 0f, 0f, offset,
            0f, 0f, scale, 0f, offset,
            0f, 0f, 0f, 1f, 0f
        )
        matrix.set(m)
        return matrix
    }

    /**
     * O(N) fast software linear box blur for backwards compatibility and high performance.
     */
    private fun boxBlur(src: Bitmap, radius: Int): Bitmap {
        var r = radius
        if (r < 1) return src
        if (r > 25) r = 25
        
        val w = src.width
        val h = src.height
        val pix = IntArray(w * h)
        src.getPixels(pix, 0, w, 0, 0, w, h)

        val dest = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val d = 2 * r + 1
        val pixOut = IntArray(w * h)

        // Horizontal Pass
        for (y in 0 until h) {
            var rSum = 0
            var gSum = 0
            var bSum = 0
            var aSum = 0
            for (i in -r..r) {
                val pxIdx = y * w + clamp(i, 0, w - 1)
                val p = pix[pxIdx]
                aSum += (p ushr 24) and 0xff
                rSum += (p ushr 16) and 0xff
                gSum += (p ushr 8) and 0xff
                bSum += p and 0xff
            }
            for (x in 0 until w) {
                pixOut[y * w + x] = ((aSum / d) shl 24) or ((rSum / d) shl 16) or ((gSum / d) shl 8) or (bSum / d)
                val outIdx = y * w + clamp(x - r, 0, w - 1)
                val inIdx = y * w + clamp(x + r + 1, 0, w - 1)
                val outPixel = pix[outIdx]
                val inPixel = pix[inIdx]

                aSum += ((inPixel ushr 24) and 0xff) - ((outPixel ushr 24) and 0xff)
                rSum += ((inPixel ushr 16) and 0xff) - ((outPixel ushr 16) and 0xff)
                gSum += ((inPixel ushr 8) and 0xff) - ((outPixel ushr 8) and 0xff)
                bSum += (inPixel and 0xff) - (outPixel and 0xff)
            }
        }

        // Vertical Pass
        for (x in 0 until w) {
            var rSum = 0
            var gSum = 0
            var bSum = 0
            var aSum = 0
            for (i in -r..r) {
                val pxIdx = clamp(i, 0, h - 1) * w + x
                val p = pixOut[pxIdx]
                aSum += (p ushr 24) and 0xff
                rSum += (p ushr 16) and 0xff
                gSum += (p ushr 8) and 0xff
                bSum += p and 0xff
            }
            for (y in 0 until h) {
                pix[y * w + x] = ((aSum / d) shl 24) or ((rSum / d) shl 16) or ((gSum / d) shl 8) or (bSum / d)

                val outIdx = clamp(y - r, 0, h - 1) * w + x
                val inIdx = clamp(y + r + 1, 0, h - 1) * w + x
                val outPixel = pixOut[outIdx]
                val inPixel = pixOut[inIdx]

                aSum += ((inPixel ushr 24) and 0xff) - ((outPixel ushr 24) and 0xff)
                rSum += ((inPixel ushr 16) and 0xff) - ((outPixel ushr 16) and 0xff)
                gSum += ((inPixel ushr 8) and 0xff) - ((outPixel ushr 8) and 0xff)
                bSum += (inPixel and 0xff) - (outPixel and 0xff)
            }
        }
        dest.setPixels(pix, 0, w, 0, 0, w, h)
        return dest
    }

    private fun clamp(v: Int, min: Int, max: Int): Int = if (v < min) min else if (v > max) max else v
}

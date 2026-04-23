package com.lerchenflo.taximeter.utilities

import androidx.compose.ui.graphics.Color
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.random.Random

fun Long.toComposeColor(): Color = Color(this.toInt())

fun generateRandomColor(): Long {
    val hue = Random.nextFloat() * 360f
    val saturation = 0.6f + Random.nextFloat() * 0.25f
    val brightness = 0.7f + Random.nextFloat() * 0.2f
    return hsvToArgb(hue, saturation, brightness)
}

private fun hsvToArgb(h: Float, s: Float, v: Float): Long {
    val c = v * s
    val x = c * (1f - abs((h / 60f) % 2f - 1f))
    val m = v - c

    val (r1, g1, b1) = when {
        h < 60f -> Triple(c, x, 0f)
        h < 120f -> Triple(x, c, 0f)
        h < 180f -> Triple(0f, c, x)
        h < 240f -> Triple(0f, x, c)
        h < 300f -> Triple(x, 0f, c)
        else -> Triple(c, 0f, x)
    }

    val r = ((r1 + m) * 255).roundToInt()
    val g = ((g1 + m) * 255).roundToInt()
    val b = ((b1 + m) * 255).roundToInt()

    return (0xFFL shl 24) or (r.toLong() shl 16) or (g.toLong() shl 8) or b.toLong()
}

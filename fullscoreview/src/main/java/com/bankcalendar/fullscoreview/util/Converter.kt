package com.bankcalendar.fullscoreview.util

import android.content.Context
import android.graphics.Canvas
import android.text.StaticLayout
import android.util.TypedValue
import androidx.core.graphics.withTranslation
import java.text.DecimalFormat

fun spToPx(sp: Float, context: Context): Float {
    return sp * context.resources.displayMetrics.scaledDensity
}

fun dpToPx(dp: Float, context: Context): Int {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        dp,
        context.resources.displayMetrics
    ).toInt()
}

fun pxToSp(px: Float, context: Context): Float {
    return px / context.resources.displayMetrics.scaledDensity
}

fun StaticLayout.draw(canvas: Canvas?, x: Float, y: Float) {
    canvas?.withTranslation(x, y) {
        draw(this)
    }
}

fun Int.formatNumber(): String {
    val dec = DecimalFormat("##,###.##")
    return "${dec.format(this)} ₽"
}
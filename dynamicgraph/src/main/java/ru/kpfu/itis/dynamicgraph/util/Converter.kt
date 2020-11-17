package ru.kpfu.itis.dynamicgraph.util

import android.content.Context
import android.graphics.Canvas
import android.text.StaticLayout
import android.util.TypedValue
import androidx.core.graphics.withTranslation
import java.text.DecimalFormat
import java.util.*

fun spToPx(sp: Float, context: Context): Float {
    return sp * context.resources.displayMetrics.scaledDensity
}

fun dpToPx(dp: Float, context: Context): Float {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        dp,
        context.resources.displayMetrics
    )
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

fun Calendar.getFormattedMonth(): String {
    val month = this.get(Calendar.MONTH) + 1
    return if (month < 10) {
        "0$month"
    } else "$month"
}
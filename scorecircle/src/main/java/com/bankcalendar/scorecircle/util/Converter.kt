package com.bankcalendar.scorecircle.util

import android.content.Context
import android.util.TypedValue

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

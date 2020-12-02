package com.bankcalendar.circlesview

import android.graphics.Color
import android.os.Parcel
import android.os.Parcelable
import android.view.View

class SavedState : View.BaseSavedState {

    constructor(parcelable: Parcelable) : super(parcelable)

    private constructor(parcel: Parcel) : super(parcel) {
    }

    private val baseTextColor = Color.parseColor("#34495e")
    private val baseBackgroundCircleColor = Color.parseColor("#e1e5e7")
    private val baseCommentaryLineColor = Color.parseColor("#979797")

    var maxScore: Int = 0
    var secondaryText: String = ""
    var innerTextColor: Int = baseTextColor
    var primaryText: String = ""
    var lineColor: Int = baseCommentaryLineColor
    var dotColor: Int = Color.WHITE
    var backgroundCircleColor: Int = baseBackgroundCircleColor
    var circleWidth: Float = CirclesDiagramView.DEFAULT_WIDTH
    var outerCircleRadius: Float = CirclesDiagramView.DEFAULT_RADIUS
    var circleRadiuses = mutableListOf<Float>()
    var circleValues = mutableListOf<Int>()
    var percents = mutableListOf<Int>()
    var dotRadius = CirclesDiagramView.DEFAULT_WIDTH / 4

    @JvmField
    val CREATOR: Parcelable.Creator<SavedState> = object : Parcelable.Creator<SavedState> {
        override fun createFromParcel(parcel: Parcel): SavedState {
            return SavedState(parcel)
        }

        override fun newArray(size: Int): Array<SavedState?> {
            return arrayOfNulls(size)
        }
    }
}
package com.bankcalendar.scorepoint

import android.graphics.Color
import android.os.Parcel
import android.os.Parcelable
import android.view.View

class SavedState : View.BaseSavedState {

    constructor(parcelable: Parcelable) : super(parcelable)

    private constructor(parcel: Parcel) : super(parcel)

    private val baseTextColor = Color.parseColor("#34495e")
    private val baseCircleColor = Color.parseColor("#53C283")

    var score: Int = 0
    var maxScore: Int = 0

    var innerText: String = ""
    var innerTextColor: Int = baseTextColor

    var secondaryText: String = ""
    var secondaryTextColor: Int = baseTextColor

    var tertiaryText: String = ""
    var tertiaryTextColor: Int = baseTextColor

    var circleWidth: Float = 0f
    var circleRadius: Float = 0f
    var circleColor: Int = baseCircleColor

    var outerCircleWidth: Float = 0f
    var outerCircleRadius: Float = 0f

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
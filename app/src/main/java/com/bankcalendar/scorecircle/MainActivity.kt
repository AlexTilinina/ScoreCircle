package com.bankcalendar.scorecircle

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val list0 = mutableListOf(0, 0, 0, 0)
        val list1 = mutableListOf(0, 0, 0, 12000)
        val list2 = mutableListOf(5000, 0, 0, 12000)
        val list3 = mutableListOf(5000, 30000, 0, 12000)
        val list4 = mutableListOf(5000, 30000, 10000, 12000)

        val score = 550
        val color = when (score) {
            in 0..249 -> R.color.colorScorePointRed
            in 250..499 -> R.color.colorScorePointYellow
            in 500..749 -> R.color.colorScorePointGreen
            in 750..999 -> R.color.colorScorePointBlue
            else -> R.color.colorScorePointPurple
        }
        circlesDiagramView.score = score
        circlesDiagramView.circleColor = ContextCompat.getColor(this, color)
    }
}

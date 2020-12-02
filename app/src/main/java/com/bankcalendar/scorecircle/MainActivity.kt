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

        val colors = intArrayOf(
            ContextCompat.getColor(this, R.color.colorScorePointRed),
            ContextCompat.getColor(this, R.color.colorScorePointYellow),
            ContextCompat.getColor(this, R.color.colorScorePointGreen),
            ContextCompat.getColor(this, R.color.colorScorePointBlue),
            ContextCompat.getColor(this, R.color.colorScorePointPurple)
        )
        val scoreList = intArrayOf(356, 256, 456, 856, 560)
        val listTest = mutableListOf(356, 256, 1000, 856, 560)

        score_circle.setValues(listTest)

        /*val score = scoreList.filter { it != 0 }.average().toInt()
        val color = when (score) {
            in 0..249 -> R.color.colorScorePointRed
            in 250..499 -> R.color.colorScorePointYellow
            in 500..749 -> R.color.colorScorePointGreen
            in 750..999 -> R.color.colorScorePointBlue
            else -> R.color.colorScorePointPurple
        }
        circlesDiagramView.primaryText = score.toString()
        circlesDiagramView.scoreList = scoreList
        circlesDiagramView.circleColors = colors*/
    }
}

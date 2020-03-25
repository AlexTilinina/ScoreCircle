package com.bankcalendar.scorecircle

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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
        circlesDiagramView.setValues(list4)
    }
}

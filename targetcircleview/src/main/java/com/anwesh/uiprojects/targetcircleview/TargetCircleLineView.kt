package com.anwesh.uiprojects.targetcircleview

/**
 * Created by anweshmishra on 12/10/20.
 */

import android.view.View
import android.view.MotionEvent
import android.graphics.Paint
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.RectF
import android.app.Activity
import android.content.Context

val parts : Int = 3
val scGap : Float = 0.02f / parts
val colors : Array<Int> = arrayOf(
        "#9C27B0",
        "#009688",
        "#2196F3",
        "#4CAF50",
        "#03A9F4"
).map({Color.parseColor(it)}).toTypedArray()
val deg : Float = 360f
val strokeFactor : Float = 90f
val sizeFactor : Float = 3.2f
val delay : Long = 20
val backColor : Int = Color.parseColor("#BDBDBD")


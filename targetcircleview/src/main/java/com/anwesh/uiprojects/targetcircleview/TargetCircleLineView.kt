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

fun Int.inverse() : Float = 1f / this
fun Float.maxScale(i : Int, n : Int) : Float = Math.max(0f, this - i * n.inverse())
fun Float.divideScale(i : Int, n : Int) : Float = Math.min(n.inverse(), maxScale(i, n)) * n
fun Float.sinify() : Float = Math.sin(this * Math.PI).toFloat()
fun Int.mirror() : Float = 1f - 2 * this

fun Canvas.drawTargetCircleLine(scale : Float, w : Float, h : Float, paint : Paint) {
    val sf : Float = scale.sinify()
    val sf1 : Float = sf.divideScale(0, parts)
    val sf2 : Float = sf.divideScale(1, parts)
    val size : Float = Math.min(w, h) / sizeFactor
    val r : Float = size / 2
    val endY : Float = -h / 2 + (h / 2 - r) * sf1
    val endX : Float = -w / 2 + (w / 2 - r) * sf1
    save()
    translate(w / 2, h / 2)
    for (j in 0..3) {
        val iy : Int = j % 2
        val jx : Float = (j / 2).mirror()
        save()
        scale(1f, jx)
        rotate(90f * (j % 2))
        drawLine(0f, -h / 2 + (-w / 2 + h / 2) * iy,  0f, endY + (endX - endY) * iy, paint)
        restore()
    }
    drawArc(RectF(-r, -r, r, r), 0f, deg * sf2, true, paint)
    restore()
}

fun Canvas.drawTCLNode(i : Int, scale : Float, paint : Paint) {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    paint.color = colors[i]
    paint.strokeCap = Paint.Cap.ROUND
    paint.strokeWidth = Math.min(w, h) / strokeFactor
    drawTargetCircleLine(scale, w, h, paint)
}

class TargetCircleLineView(ctx : Context) : View(ctx) {

    private val renderer : Renderer = Renderer(this)

    override fun onDraw(canvas : Canvas) {
        renderer.render(canvas)
    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.handleTap()
            }
        }
        return true
    }

    data class State(var scale : Float = 0f, var dir : Float =0f, var prevScale : Float = 0f) {

        fun update(cb : (Float) -> Unit) {
            scale += scGap * dir
            if (Math.abs(scale - prevScale) > 1) {
                scale = prevScale + dir
                dir = 0f
                prevScale = scale
                cb(prevScale)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            if (dir == 0f) {
                dir = 1f - 2 * prevScale
                cb()
            }
        }
    }

    data class Animator(var view : View, var animated : Boolean = false) {


        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(delay)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }

    data class TCLNode(var i : Int, val state : State = State()) {

        private var next : TCLNode? = null
        private var prev : TCLNode? = null

        init {
            addNeighbor()
        }

        fun addNeighbor() {
            if (i < colors.size - 1) {
                next = TCLNode(i + 1)
                next?.prev = this
            }
        }

        fun draw(canvas : Canvas, paint : Paint) {
            canvas.drawTCLNode(i, state.scale, paint)
        }

        fun update(cb : (Float) -> Unit) {
            state.update(cb)
        }

        fun startUpdating(cb : () -> Unit) {
            state.startUpdating(cb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : TCLNode {
            var curr : TCLNode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }
    }

    data class TargetCircleLine(var i : Int) {

        private var curr : TCLNode = TCLNode(0)
        private var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            curr.draw(canvas, paint)
        }

        fun update(cb : (Float) -> Unit) {
            curr.update {
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                cb(it)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            curr.startUpdating(cb)
        }
    }

    data class Renderer(var view : TargetCircleLineView) {

        private val animator : Animator = Animator(view)
        private val tcl : TargetCircleLine = TargetCircleLine(0)
        private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

        fun render(canvas : Canvas) {
            canvas.drawColor(backColor)
            tcl.draw(canvas, paint)
            animator.animate {
                tcl.update {
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            tcl.startUpdating {
                animator.start()
            }
        }
    }

    companion object {

        fun create(activity : Activity) : TargetCircleLineView {
            val view : TargetCircleLineView = TargetCircleLineView(activity)
            activity.setContentView(view)
            return view
        }
    }
}


package com.akribase.archycards.wheelview

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.MotionEvent
import com.akribase.archycards.wheelview.WheelView.OnWheelAngleChangeListener
import java.util.*
import kotlin.math.abs
import kotlin.math.min

class StickyWheelView : WheelView, OnWheelAngleChangeListener {
    var timer: Timer? = null
    var timerTask: TimerTask? = null
    var handleSticky = false
    var uiHandler: Handler? = null
    override var onWheelAngleChangeListener: OnWheelAngleChangeListener? = null

    constructor(context: Context?) : super(context) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(
        context!!, attrs
    ) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(
        context!!, attrs, defStyle
    ) {
        init()
    }

    private fun init() {
        super.onWheelAngleChangeListener = this
        uiHandler = Handler(Looper.getMainLooper())
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            handleSticky = false
        } else if (event.action == MotionEvent.ACTION_UP) {
            handleSticky = true
            onWheelAngleChange(angle)
        }
        return super.onTouchEvent(event)
    }

    override fun onWheelAngleChange(angle: Float) {
        timer = invalidateTimerAndCreateNew(timer, timerTask)
        timerTask = object : TimerTask() {
            override fun run() {
                goToNearestFixItem()
            }
        }
        timer!!.schedule(timerTask, 100)

        // call listener if is not null
        if (onWheelAngleChangeListener != null) {
            onWheelAngleChangeListener!!.onWheelAngleChange(angle)
        }
    }

    private fun goToNearestFixItem() {
        if (!handleSticky) {
            return
        }
        // getAngle is wide range, we map it too 0 - 360
        val correctAngle = abs(angle) % 360

        // each item angle
        val itemAngle = 360 / wheelItemCount

        // item is currently sticky.
        if (correctAngle % itemAngle == 0f) {
            return
        }
        val power = abs((angle / 360).toInt())
        val nextAngle = ((correctAngle / itemAngle).toInt() + 1) * itemAngle + 360 * power
        val preAngle = (correctAngle / itemAngle).toInt() * itemAngle + 360 * power
        val sign = if (angle < 0) -1 else 1
        if (correctAngle % itemAngle > itemAngle / 2) {
            goToAngleWithAnimation(nextAngle * sign)
        } else {
            goToAngleWithAnimation(preAngle * sign)
        }
    }

    private var temporaryFinalAngel = 0f
    private fun goToAngleWithAnimation(angle: Float) {
        temporaryFinalAngel = angle
        val increase: Boolean = angle <= angle
        uiHandler!!.post(object : Runnable {
            override fun run() {
                if (temporaryFinalAngel == angle) {
                    return
                }

                // user changed angle again, don't set angle again
                if (!handleSticky) {
                    return
                }
                val nextAngle: Float = if (increase) {
                    min(angle + 3, temporaryFinalAngel)
                } else {
                    (angle - 3).coerceAtLeast(temporaryFinalAngel)
                }
                this@StickyWheelView.angle = nextAngle
                uiHandler!!.postDelayed(this, 10)
            }
        })
    }

    private fun invalidateTimerAndCreateNew(timer: Timer?, timerTask: TimerTask?): Timer {
        if (timer != null) {
            timer.cancel()
            timer.purge()
        }
        timerTask?.cancel()
        return Timer()
    }
}
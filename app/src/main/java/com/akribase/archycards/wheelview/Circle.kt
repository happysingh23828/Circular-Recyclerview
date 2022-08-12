package com.akribase.archycards.wheelview

import android.graphics.Rect
import kotlin.math.atan2
import kotlin.math.floor
import kotlin.math.roundToInt

class Circle {
    var centerX = 0f
    var centerY = 0f
    var radius = 0f

    internal constructor()
    internal constructor(centerX: Float, centerY: Float, radius: Float) {
        this.centerX = centerX
        this.centerY = centerY
        this.radius = radius
    }

    fun contains(x: Float, y: Float): Boolean {
        val x1 = centerX - x
        val y1 = centerY - y
        return x1 * x1 + y1 * y1 <= radius * radius
    }

    val boundingRect: Rect
        get() = Rect(
            (centerX - radius).roundToInt(), (centerY - radius).roundToInt(),
            (centerX + radius).roundToInt(), (centerY + radius).roundToInt()
        )

    /**
     * The Angle from this circle's center to the position x, y
     * y is considered to go down (like android view system)
     */
    fun angleTo(x: Float, y: Float): Float {
        return atan2((centerY - y).toDouble(), (x - centerX).toDouble()).toFloat()
    }

    fun angleToDegrees(x: Float, y: Float): Float {
        return Math.toDegrees(angleTo(x, y).toDouble()).toFloat()
    }

    override fun toString(): String {
        return "Radius: $radius X: $centerX Y: $centerY"
    }

    companion object {
        /**
         * Clamps the value to a number between 0 and the upperLimit
         */
        @JvmStatic
        fun clamp(value: Int, upperLimit: Int): Int {
            return if (value < 0) {
                value + -1 * floor((value / upperLimit.toFloat()).toDouble())
                    .toInt() * upperLimit
            } else {
                value % upperLimit
            }
        }

        @JvmStatic
        fun clamp180(value: Float): Float {
            return ((value + 180f) % 360f + 360f) % 360f - 180f
        }

        /**
         * Returns the shortest angle difference when the inputs range between -180 and 180 (such as from Math.atan2)
         */
        @JvmStatic
        fun shortestAngle(angleA: Float, angleB: Float): Float {
            var angle = angleA - angleB
            if (angle > 180f) {
                angle -= 360f
            } else if (angle < -180f) {
                angle += 360f
            }
            return angle
        }
    }
}
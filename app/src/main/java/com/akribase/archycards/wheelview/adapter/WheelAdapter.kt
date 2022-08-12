package com.akribase.archycards.wheelview.adapter

import android.graphics.drawable.Drawable
import  com.akribase.archycards.wheelview.WheelView

/**
 *
 *
 * Provide drawables for the [WheelView] to draw on the wheel.
 *
 *
 *
 *
 * Note that [WheelAdapter] doesn't behave exactly like a typical Adapter from Android source.
 * There are some limitations to using drawables rather than views, but it also means you do not
 * need to worry about recycling drawables as it is not as expensive as view inflation.
 *
 *
 *
 *
 * It may be possible to properly implement an Adapter with recycling Views but for now this will do.
 *
 */
interface WheelAdapter {
    /**
     * @param position the adapter position, between 0 and [.getCount].
     * @return the drawable to be drawn on the wheel at this adapter position.
     */
    fun getDrawable(position: Int): Drawable?

    /**
     * @return the number of items in the adapter.
     */
    val count: Int
}
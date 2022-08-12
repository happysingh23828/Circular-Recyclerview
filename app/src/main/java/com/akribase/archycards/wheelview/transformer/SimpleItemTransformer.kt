package com.akribase.archycards.wheelview.transformer

import android.graphics.Rect
import com.akribase.archycards.wheelview.WheelView.ItemState
import kotlin.math.roundToInt

class SimpleItemTransformer : WheelItemTransformer {
    override fun transform(itemState: ItemState, itemBounds: Rect) {
        val bounds = itemState.bounds
        val radius = bounds.radius
        val x = bounds.centerX
        val y = bounds.centerY
        itemBounds[(x - radius).roundToInt(), (y - radius).roundToInt(), (x + radius).roundToInt()] =
            (y + radius).roundToInt()
    }
}
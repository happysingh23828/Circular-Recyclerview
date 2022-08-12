package com.akribase.archycards.wheelview.transformer

import android.graphics.drawable.Drawable
import com.akribase.archycards.wheelview.WheelView.ItemState
import kotlin.math.abs
import kotlin.math.pow

class FadingSelectionTransformer : WheelSelectionTransformer {
    override fun transform(drawable: Drawable, itemState: ItemState) {
        val relativePosition = abs(itemState.relativePosition)
        var alpha = ((1f - relativePosition.toDouble().pow(2.5)) * 255f).toInt()

        //clamp to between 0 and 255
        if (alpha > 255) alpha = 255 else if (alpha < 0) alpha = 0
        drawable.alpha = alpha
    }
}
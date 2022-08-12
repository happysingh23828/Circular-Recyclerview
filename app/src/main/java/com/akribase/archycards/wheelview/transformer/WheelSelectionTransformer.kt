package com.akribase.archycards.wheelview.transformer

import android.graphics.drawable.Drawable
import com.akribase.archycards.wheelview.WheelView.ItemState

interface WheelSelectionTransformer {
    fun transform(drawable: Drawable, itemState: ItemState)
}
package com.akribase.archycards.wheelview.transformer

import android.graphics.Rect
import com.akribase.archycards.wheelview.WheelView.ItemState

interface WheelItemTransformer {
    /**
     * You have control over the Items draw bounds. By supplying your own WheelItemTransformer
     * you must call set bounds on the itemBounds.
     */
    fun transform(itemState: ItemState, itemBounds: Rect)
}
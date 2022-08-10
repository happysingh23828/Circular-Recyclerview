package com.akribase.archycards.wheelview.transformer;

import android.graphics.drawable.Drawable;

import com.akribase.archycards.wheelview.WheelView;

public interface WheelSelectionTransformer {
    void transform(Drawable drawable, WheelView.ItemState itemState);
}

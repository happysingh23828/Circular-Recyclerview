package com.akribase.archycards.lib;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

import android.util.Log;
import android.view.View;
import android.view.animation.Interpolator;

import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.LayoutManager;


public class ScrollHelper {
    public ScrollHelper() {
    }

    public static void smoothScrollToPosition(RecyclerView recyclerView, ViewPagerLayoutManager viewPagerLayoutManager, int targetPosition) {
        int delta = viewPagerLayoutManager.getOffsetToPosition(targetPosition);
        if (viewPagerLayoutManager.getOrientation() == 1) {
            recyclerView.smoothScrollBy(0, delta);
        } else {
            recyclerView.smoothScrollBy(delta, 0);
        }

    }

    public static void smoothScrollToPosition(RecyclerView recyclerView, ViewPagerLayoutManager viewPagerLayoutManager, int targetPosition, Interpolator interpolator, int duration) {
        int delta = viewPagerLayoutManager.getOffsetToPosition(targetPosition);
        Log.d(ScrollHelper.class.getName(), "smoothScrollToPosition: Offset Delta " + delta + " Position " + targetPosition);
        if (viewPagerLayoutManager.getOrientation() == 1) {
            recyclerView.smoothScrollBy(0, delta, interpolator, duration);
        } else {
            recyclerView.smoothScrollBy(delta, 0, interpolator, duration);
        }

    }

    public static void smoothScrollToTargetView(RecyclerView recyclerView, View targetView) {
        LayoutManager layoutManager = recyclerView.getLayoutManager();
        if (layoutManager instanceof ViewPagerLayoutManager) {
            int targetPosition = ((ViewPagerLayoutManager) layoutManager).getLayoutPositionOfView(targetView);
            smoothScrollToPosition(recyclerView, (ViewPagerLayoutManager) layoutManager, targetPosition);
        }
    }
}


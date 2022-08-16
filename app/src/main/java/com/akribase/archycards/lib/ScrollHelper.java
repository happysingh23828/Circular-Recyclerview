package com.akribase.archycards.lib;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.LayoutManager;


public class ScrollHelper {
    public ScrollHelper() {
    }

    static void smoothScrollToPosition(RecyclerView recyclerView, ViewPagerLayoutManager viewPagerLayoutManager, int targetPosition) {
        int delta = viewPagerLayoutManager.getOffsetToPosition(targetPosition);
        if (viewPagerLayoutManager.getOrientation() == 1) {
            recyclerView.smoothScrollBy(0, delta);
        } else {
            recyclerView.smoothScrollBy(delta, 0);
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


package com.akribase.archycards.lib;

import android.view.animation.DecelerateInterpolator;
import android.widget.Scroller;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.LayoutManager;
import androidx.recyclerview.widget.RecyclerView.OnFlingListener;
import androidx.recyclerview.widget.RecyclerView.OnScrollListener;

public class CenterSnapHelper extends OnFlingListener {
    RecyclerView mRecyclerView;
    Scroller mGravityScroller;
    private boolean snapToCenter = false;
    private final OnScrollListener mScrollListener = new OnScrollListener() {
        boolean mScrolled = false;

        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            ViewPagerLayoutManager layoutManager = (ViewPagerLayoutManager) recyclerView.getLayoutManager();
            ViewPagerLayoutManager.OnPageChangeListener onPageChangeListener = layoutManager.onPageChangeListener;
            if (onPageChangeListener != null) {
                onPageChangeListener.onPageScrollStateChanged(newState);
            }

            if (newState == 0 && this.mScrolled) {
                this.mScrolled = false;
                if (!CenterSnapHelper.this.snapToCenter) {
                    CenterSnapHelper.this.snapToCenter = true;
                    CenterSnapHelper.this.snapToCenterView(layoutManager, onPageChangeListener);
                } else {
                    CenterSnapHelper.this.snapToCenter = false;
                }
            }

        }

        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            if (dx != 0 || dy != 0) {
                this.mScrolled = true;
            }

        }
    };

    public CenterSnapHelper() {
    }

    public boolean onFling(int velocityX, int velocityY) {
        ViewPagerLayoutManager layoutManager = (ViewPagerLayoutManager) this.mRecyclerView.getLayoutManager();
        if (layoutManager == null) {
            return false;
        } else {
            Adapter adapter = this.mRecyclerView.getAdapter();
            if (adapter == null) {
                return false;
            } else if (!layoutManager.getInfinite() && (layoutManager.mOffset == layoutManager.getMaxOffset() || layoutManager.mOffset == layoutManager.getMinOffset())) {
                return false;
            } else {
                int minFlingVelocity = this.mRecyclerView.getMinFlingVelocity();
                this.mGravityScroller.fling(0, 0, velocityX, velocityY, -2147483648, 2147483647, -2147483648, 2147483647);
                int currentPosition;
                int offsetPosition;
                if (layoutManager.mOrientation == 1 && Math.abs(velocityY) > minFlingVelocity) {
                    currentPosition = layoutManager.getCurrentPositionOffset();
                    offsetPosition = (int) ((float) this.mGravityScroller.getFinalY() / layoutManager.mInterval / layoutManager.getDistanceRatio());
                    ScrollHelper.smoothScrollToPosition(this.mRecyclerView, layoutManager, layoutManager.getReverseLayout() ? -currentPosition - offsetPosition : currentPosition + offsetPosition);
                    return true;
                } else if (layoutManager.mOrientation == 0 && Math.abs(velocityX) > minFlingVelocity) {
                    currentPosition = layoutManager.getCurrentPositionOffset();
                    offsetPosition = (int) ((float) this.mGravityScroller.getFinalX() / layoutManager.mInterval / layoutManager.getDistanceRatio());
                    ScrollHelper.smoothScrollToPosition(this.mRecyclerView, layoutManager, layoutManager.getReverseLayout() ? -currentPosition - offsetPosition : currentPosition + offsetPosition);
                    return true;
                } else {
                    return true;
                }
            }
        }
    }

    public void attachToRecyclerView(@Nullable RecyclerView recyclerView) throws IllegalStateException {
        if (this.mRecyclerView != recyclerView) {
            if (this.mRecyclerView != null) {
                this.destroyCallbacks();
            }

            this.mRecyclerView = recyclerView;
            if (this.mRecyclerView != null) {
                LayoutManager layoutManager = this.mRecyclerView.getLayoutManager();
                if (!(layoutManager instanceof ViewPagerLayoutManager)) {
                    return;
                }

                this.setupCallbacks();
                this.mGravityScroller = new Scroller(this.mRecyclerView.getContext(), new DecelerateInterpolator());
                this.snapToCenterView((ViewPagerLayoutManager) layoutManager, ((ViewPagerLayoutManager) layoutManager).onPageChangeListener);
            }

        }
    }

    void snapToCenterView(ViewPagerLayoutManager layoutManager, ViewPagerLayoutManager.OnPageChangeListener listener) {
        int delta = layoutManager.getOffsetToCenter();
        if (delta != 0) {
            if (layoutManager.getOrientation() == 1) {
                this.mRecyclerView.smoothScrollBy(0, delta);
            } else {
                this.mRecyclerView.smoothScrollBy(delta, 0);
            }
        } else {
            this.snapToCenter = false;
        }

        if (listener != null) {
            listener.onPageSelected(layoutManager.getCurrentPosition());
        }

    }

    void setupCallbacks() throws IllegalStateException {
        if (this.mRecyclerView.getOnFlingListener() != null) {
            throw new IllegalStateException("An instance of OnFlingListener already set.");
        } else {
            this.mRecyclerView.addOnScrollListener(this.mScrollListener);
            this.mRecyclerView.setOnFlingListener(this);
        }
    }

    void destroyCallbacks() {
        this.mRecyclerView.removeOnScrollListener(this.mScrollListener);
        this.mRecyclerView.setOnFlingListener((OnFlingListener) null);
    }
}

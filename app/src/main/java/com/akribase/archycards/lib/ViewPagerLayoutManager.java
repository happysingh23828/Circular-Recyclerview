package com.akribase.archycards.lib;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.SparseArray;
import android.view.View;
import android.view.animation.Interpolator;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.LayoutParams;
import androidx.recyclerview.widget.RecyclerView.Recycler;
import androidx.recyclerview.widget.RecyclerView.State;


import java.util.ArrayList;

public abstract class ViewPagerLayoutManager extends LinearLayoutManager {
    public static final int DETERMINE_BY_MAX_AND_MIN = -1;
    public static final int HORIZONTAL = 0;
    public static final int VERTICAL = 1;
    private static final int DIRECTION_NO_WHERE = -1;
    private static final int DIRECTION_FORWARD = 0;
    private static final int DIRECTION_BACKWARD = 1;
    protected static final int INVALID_SIZE = 2147483647;
    private SparseArray<View> positionCache;
    protected int mDecoratedMeasurement;
    protected int mDecoratedMeasurementInOther;
    int mOrientation;
    protected int mSpaceMain;
    protected int mSpaceInOther;
    protected float mOffset;
    protected OrientationHelper mOrientationHelper;
    private boolean mReverseLayout;
    private boolean mShouldReverseLayout;
    private boolean mSmoothScrollbarEnabled;
    private int mPendingScrollPosition;
    private ViewPagerLayoutManager.SavedState mPendingSavedState;
    protected float mInterval;
    ViewPagerLayoutManager.OnPageChangeListener onPageChangeListener;
    private boolean mRecycleChildrenOnDetach;
    private boolean mInfinite;
    private boolean mEnableBringCenterToFront;
    private int mLeftItems;
    private int mRightItems;
    private int mMaxVisibleItemCount;
    private Interpolator mSmoothScrollInterpolator;
    private int mDistanceToBottom;
    private View currentFocusView;
    private boolean mItemRotationEnabled;

    protected abstract float setInterval();

    protected abstract void setItemViewProperty(View var1, float var2);

    protected float setViewElevation(View itemView, float targetOffset) {
        return 0.0F;
    }

    public ViewPagerLayoutManager(Context context) {
        this(context, 0, false);
    }

    public ViewPagerLayoutManager(Context context, int orientation, boolean reverseLayout) {
        super(context);
        this.positionCache = new SparseArray();
        this.mReverseLayout = false;
        this.mShouldReverseLayout = false;
        this.mSmoothScrollbarEnabled = true;
        this.mPendingScrollPosition = -1;
        this.mPendingSavedState = null;
        this.mInfinite = false;
        this.mMaxVisibleItemCount = -1;
        this.mDistanceToBottom = 2147483647;
        this.mItemRotationEnabled = true;
        this.setOrientation(orientation);
        this.setReverseLayout(reverseLayout);
        this.setAutoMeasureEnabled(true);
        this.setItemPrefetchEnabled(false);
    }

    public LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(-2, -2);
    }

    public boolean getRecycleChildrenOnDetach() {
        return this.mRecycleChildrenOnDetach;
    }

    public void setItemRotation(boolean enabled) {
        this.mItemRotationEnabled = enabled;
    }

    public void setRecycleChildrenOnDetach(boolean recycleChildrenOnDetach) {
        this.mRecycleChildrenOnDetach = recycleChildrenOnDetach;
    }

    public void onDetachedFromWindow(RecyclerView view, Recycler recycler) {
        super.onDetachedFromWindow(view, recycler);
        if (this.mRecycleChildrenOnDetach) {
            this.removeAndRecycleAllViews(recycler);
            recycler.clear();
        }

    }

    public Parcelable onSaveInstanceState() {
        if (this.mPendingSavedState != null) {
            return new ViewPagerLayoutManager.SavedState(this.mPendingSavedState);
        } else {
            ViewPagerLayoutManager.SavedState savedState = new ViewPagerLayoutManager.SavedState();
            savedState.position = this.mPendingScrollPosition;
            savedState.offset = this.mOffset;
            savedState.isReverseLayout = this.mShouldReverseLayout;
            return savedState;
        }
    }

    public void onRestoreInstanceState(Parcelable state) {
        if (state instanceof ViewPagerLayoutManager.SavedState) {
            this.mPendingSavedState = new ViewPagerLayoutManager.SavedState((ViewPagerLayoutManager.SavedState) state);
            this.requestLayout();
        }

    }

    public boolean canScrollHorizontally() {
        return this.mOrientation == 0;
    }

    public boolean canScrollVertically() {
        return this.mOrientation == 1;
    }

    public int getOrientation() {
        return this.mOrientation;
    }

    public void setOrientation(int orientation) {
        if (orientation != 0 && orientation != 1) {
            throw new IllegalArgumentException("invalid orientation:" + orientation);
        } else {
            this.assertNotInLayoutOrScroll((String) null);
            if (orientation != this.mOrientation) {
                this.mOrientation = orientation;
                this.mOrientationHelper = null;
                this.mDistanceToBottom = 2147483647;
                this.removeAllViews();
            }
        }
    }

    public int getMaxVisibleItemCount() {
        return this.mMaxVisibleItemCount;
    }

    public void setMaxVisibleItemCount(int mMaxVisibleItemCount) {
        this.assertNotInLayoutOrScroll((String) null);
        if (this.mMaxVisibleItemCount != mMaxVisibleItemCount) {
            this.mMaxVisibleItemCount = mMaxVisibleItemCount;
            this.removeAllViews();
        }
    }

    private void resolveShouldLayoutReverse() {
        if (this.mOrientation != 1 && this.isLayoutRTL()) {
            this.mShouldReverseLayout = !this.mReverseLayout;
        } else {
            this.mShouldReverseLayout = this.mReverseLayout;
        }

    }

    public boolean getReverseLayout() {
        return this.mReverseLayout;
    }

    public void setReverseLayout(boolean reverseLayout) {
        this.assertNotInLayoutOrScroll((String) null);
        if (reverseLayout != this.mReverseLayout) {
            this.mReverseLayout = reverseLayout;
            this.removeAllViews();
        }
    }

    public void setSmoothScrollInterpolator(Interpolator smoothScrollInterpolator) {
        this.mSmoothScrollInterpolator = smoothScrollInterpolator;
    }

    public void smoothScrollToPosition(RecyclerView recyclerView, State state, int position) {
        int offsetPosition;
        if (this.mInfinite) {
            int currentPosition = this.getCurrentPosition();
            int total = this.getItemCount();
            int targetPosition;
            int d1;
            int d2;
            if (position < currentPosition) {
                d1 = currentPosition - position;
                d2 = total - currentPosition + position;
                targetPosition = d1 < d2 ? currentPosition - d1 : currentPosition + d2;
            } else {
                d1 = position - currentPosition;
                d2 = currentPosition + total - position;
                targetPosition = d1 < d2 ? currentPosition + d1 : currentPosition - d2;
            }

            offsetPosition = this.getOffsetToPosition(targetPosition);
        } else {
            offsetPosition = this.getOffsetToPosition(position);
        }

        if (this.mOrientation == 1) {
            recyclerView.smoothScrollBy(0, offsetPosition, this.mSmoothScrollInterpolator);
        } else {
            recyclerView.smoothScrollBy(offsetPosition, 0, this.mSmoothScrollInterpolator);
        }

    }

    public void onLayoutChildren(Recycler recycler, State state) {
        if (state.getItemCount() == 0) {
            this.removeAndRecycleAllViews(recycler);
            this.mOffset = 0.0F;
        } else {
            this.ensureLayoutState();
            this.resolveShouldLayoutReverse();
            View scrap = this.getMeasureView(recycler, state, 0);
            if (scrap == null) {
                this.removeAndRecycleAllViews(recycler);
                this.mOffset = 0.0F;
            } else {
                this.measureChildWithMargins(scrap, 0, 0);
                this.mDecoratedMeasurement = this.mOrientationHelper.getDecoratedMeasurement(scrap);
                this.mDecoratedMeasurementInOther = this.mOrientationHelper.getDecoratedMeasurementInOther(scrap);
                this.mSpaceMain = (this.mOrientationHelper.getTotalSpace() - this.mDecoratedMeasurement) / 2;
                if (this.mDistanceToBottom == 2147483647) {
                    this.mSpaceInOther = (this.mOrientationHelper.getTotalSpaceInOther() - this.mDecoratedMeasurementInOther) / 2;
                } else {
                    this.mSpaceInOther = this.mOrientationHelper.getTotalSpaceInOther() - this.mDecoratedMeasurementInOther - this.mDistanceToBottom;
                }

                this.mInterval = this.setInterval();
                this.setUp();
                if (this.mInterval == 0.0F) {
                    this.mLeftItems = 1;
                    this.mRightItems = 1;
                } else {
                    this.mLeftItems = (int) Math.abs(this.minRemoveOffset() / this.mInterval) + 1;
                    this.mRightItems = (int) Math.abs(this.maxRemoveOffset() / this.mInterval) + 1;
                }

                if (this.mPendingSavedState != null) {
                    this.mShouldReverseLayout = this.mPendingSavedState.isReverseLayout;
                    this.mPendingScrollPosition = this.mPendingSavedState.position;
                    this.mOffset = this.mPendingSavedState.offset;
                }

                if (this.mPendingScrollPosition != -1) {
                    this.mOffset = this.mShouldReverseLayout ? (float) this.mPendingScrollPosition * -this.mInterval : (float) this.mPendingScrollPosition * this.mInterval;
                }

                this.layoutItems(recycler);
            }
        }
    }

    private View getMeasureView(Recycler recycler, State state, int index) {
        if (index < state.getItemCount() && index >= 0) {
            try {
                return recycler.getViewForPosition(index);
            } catch (Exception var5) {
                return this.getMeasureView(recycler, state, index + 1);
            }
        } else {
            return null;
        }
    }

    public void onLayoutCompleted(State state) {
        super.onLayoutCompleted(state);
        this.mPendingSavedState = null;
        this.mPendingScrollPosition = -1;
    }

    public boolean onAddFocusables(RecyclerView recyclerView, ArrayList<View> views, int direction, int focusableMode) {
        int currentPosition = this.getCurrentPosition();
        View currentView = this.findViewByPosition(currentPosition);
        if (currentView == null) {
            return true;
        } else {
            if (recyclerView.hasFocus()) {
                int movement = this.getMovement(direction);
                if (movement != -1) {
                    int targetPosition = movement == 1 ? currentPosition - 1 : currentPosition + 1;
                    ScrollHelper.smoothScrollToPosition(recyclerView, this, targetPosition);
                }
            } else {
                currentView.addFocusables(views, direction, focusableMode);
            }

            return true;
        }
    }

    public View onFocusSearchFailed(View focused, int focusDirection, Recycler recycler, State state) {
        return null;
    }

    private int getMovement(int direction) {
        if (this.mOrientation == 1) {
            if (direction == 33) {
                return this.mShouldReverseLayout ? 0 : 1;
            } else if (direction == 130) {
                return this.mShouldReverseLayout ? 1 : 0;
            } else {
                return -1;
            }
        } else if (direction == 17) {
            return this.mShouldReverseLayout ? 0 : 1;
        } else if (direction == 66) {
            return this.mShouldReverseLayout ? 1 : 0;
        } else {
            return -1;
        }
    }

    void ensureLayoutState() {
        if (this.mOrientationHelper == null) {
            this.mOrientationHelper = OrientationHelper.createOrientationHelper(this, this.mOrientation);
        }

    }

    protected void setUp() {
    }

    private float getProperty(int position) {
        return this.mShouldReverseLayout ? (float) position * -this.mInterval : (float) position * this.mInterval;
    }

    public void onAdapterChanged(Adapter oldAdapter, Adapter newAdapter) {
        this.removeAllViews();
        this.mOffset = 0.0F;
    }

    public void scrollToPosition(int position) {
        if (this.mInfinite || position >= 0 && position < this.getItemCount()) {
            this.mPendingScrollPosition = position;
            this.mOffset = this.mShouldReverseLayout ? (float) position * -this.mInterval : (float) position * this.mInterval;
            this.requestLayout();
        }
    }

    public int computeHorizontalScrollOffset(State state) {
        return this.computeScrollOffset();
    }

    public int computeVerticalScrollOffset(State state) {
        return this.computeScrollOffset();
    }

    public int computeHorizontalScrollExtent(State state) {
        return this.computeScrollExtent();
    }

    public int computeVerticalScrollExtent(State state) {
        return this.computeScrollExtent();
    }

    public int computeHorizontalScrollRange(State state) {
        return this.computeScrollRange();
    }

    public int computeVerticalScrollRange(State state) {
        return this.computeScrollRange();
    }

    private int computeScrollOffset() {
        if (this.getChildCount() == 0) {
            return 0;
        } else if (!this.mSmoothScrollbarEnabled) {
            return !this.mShouldReverseLayout ? this.getCurrentPosition() : this.getItemCount() - this.getCurrentPosition() - 1;
        } else {
            float realOffset = this.getOffsetOfRightAdapterPosition();
            return !this.mShouldReverseLayout ? (int) realOffset : (int) ((float) (this.getItemCount() - 1) * this.mInterval + realOffset);
        }
    }

    private int computeScrollExtent() {
        if (this.getChildCount() == 0) {
            return 0;
        } else {
            return !this.mSmoothScrollbarEnabled ? 1 : (int) this.mInterval;
        }
    }

    private int computeScrollRange() {
        if (this.getChildCount() == 0) {
            return 0;
        } else {
            return !this.mSmoothScrollbarEnabled ? this.getItemCount() : (int) ((float) this.getItemCount() * this.mInterval);
        }
    }

    public int scrollHorizontallyBy(int dx, Recycler recycler, State state) {
        return this.mOrientation == 1 ? 0 : this.scrollBy(dx, recycler, state);
    }

    public int scrollVerticallyBy(int dy, Recycler recycler, State state) {
        return this.mOrientation == 0 ? 0 : this.scrollBy(dy, recycler, state);
    }

    private int scrollBy(int dy, Recycler recycler, State state) {
        if (this.getChildCount() != 0 && dy != 0) {
            this.ensureLayoutState();
            int willScroll = dy;
            float realDx = (float) dy / this.getDistanceRatio();
            if (Math.abs(realDx) < 1.0E-8F) {
                return 0;
            } else {
                float targetOffset = this.mOffset + realDx;
                if (!this.mInfinite && targetOffset < this.getMinOffset()) {
                    willScroll = (int) ((float) dy - (targetOffset - this.getMinOffset()) * this.getDistanceRatio());
                } else if (!this.mInfinite && targetOffset > this.getMaxOffset()) {
                    willScroll = (int) ((this.getMaxOffset() - this.mOffset) * this.getDistanceRatio());
                }

                realDx = (float) willScroll / this.getDistanceRatio();
                this.mOffset += realDx;
                this.layoutItems(recycler);
                return willScroll;
            }
        } else {
            return 0;
        }
    }

    private void layoutItems(Recycler recycler) {
        this.detachAndScrapAttachedViews(recycler);
        this.positionCache.clear();
        int itemCount = this.getItemCount();
        if (itemCount != 0) {
            int currentPos = this.mShouldReverseLayout ? -this.getCurrentPositionOffset() : this.getCurrentPositionOffset();
            int start = currentPos - this.mLeftItems;
            int end = currentPos + this.mRightItems;
            int i;
            if (this.useMaxVisibleCount()) {
                boolean isEven = this.mMaxVisibleItemCount % 2 == 0;
                if (isEven) {
                    i = this.mMaxVisibleItemCount / 2;
                    start = currentPos - i + 1;
                    end = currentPos + i + 1;
                } else {
                    i = (this.mMaxVisibleItemCount - 1) / 2;
                    start = currentPos - i;
                    end = currentPos + i + 1;
                }
            }

            if (!this.mInfinite) {
                if (start < 0) {
                    start = 0;
                    if (this.useMaxVisibleCount()) {
                        end = this.mMaxVisibleItemCount;
                    }
                }

                if (end > itemCount) {
                    end = itemCount;
                }
            }

            float lastOrderWeight = 1.4E-45F;

            for (i = start; i < end; ++i) {
                if (this.useMaxVisibleCount() || !this.removeCondition(this.getProperty(i) - this.mOffset)) {
                    int adapterPosition = i;
                    if (i >= itemCount) {
                        adapterPosition = i % itemCount;
                    } else if (i < 0) {
                        int delta = -i % itemCount;
                        if (delta == 0) {
                            delta = itemCount;
                        }

                        adapterPosition = itemCount - delta;
                    }

                    View scrap = recycler.getViewForPosition(adapterPosition);
                    this.measureChildWithMargins(scrap, 0, 0);
                    this.resetViewProperty(scrap);
                    float targetOffset = this.getProperty(i) - this.mOffset;
                    this.layoutScrap(scrap, targetOffset);
                    float orderWeight = this.mEnableBringCenterToFront ? this.setViewElevation(scrap, targetOffset) : (float) adapterPosition;
                    if (orderWeight > lastOrderWeight) {
                        this.addView(scrap);
                    } else {
                        this.addView(scrap, 0);
                    }

                    if (i == currentPos) {
                        this.currentFocusView = scrap;
                    }

                    lastOrderWeight = orderWeight;
                    this.positionCache.put(i, scrap);
                }
            }

            this.currentFocusView.requestFocus();
        }
    }

    private boolean useMaxVisibleCount() {
        return this.mMaxVisibleItemCount != -1;
    }

    private boolean removeCondition(float targetOffset) {
        return targetOffset > this.maxRemoveOffset() || targetOffset < this.minRemoveOffset();
    }

    private void resetViewProperty(View v) {
        v.setRotation(0.0F);
        v.setRotationY(0.0F);
        v.setRotationX(0.0F);
        v.setScaleX(1.0F);
        v.setScaleY(1.0F);
        v.setAlpha(1.0F);
    }

    float getMaxOffset() {
        return !this.mShouldReverseLayout ? (float) (this.getItemCount() - 1) * this.mInterval : 0.0F;
    }

    float getMinOffset() {
        return !this.mShouldReverseLayout ? 0.0F : (float) (-(this.getItemCount() - 1)) * this.mInterval;
    }

    private void layoutScrap(View scrap, float targetOffset) {
        int left = this.calItemLeft(scrap, targetOffset);
        int top = this.calItemTop(scrap, targetOffset);
        if (this.mOrientation == 1) {
            this.layoutDecorated(scrap, this.mSpaceInOther + left, this.mSpaceMain + top, this.mSpaceInOther + left + this.mDecoratedMeasurementInOther, this.mSpaceMain + top + this.mDecoratedMeasurement);
        } else {
            this.layoutDecorated(scrap, this.mSpaceMain + left, this.mSpaceInOther + top, this.mSpaceMain + left + this.mDecoratedMeasurement, this.mSpaceInOther + top + this.mDecoratedMeasurementInOther);
        }

        if (mItemRotationEnabled) {
            this.setItemViewProperty(scrap, targetOffset);
        }
    }

    protected int calItemLeft(View itemView, float targetOffset) {
        return this.mOrientation == 1 ? 0 : (int) targetOffset;
    }

    protected int calItemTop(View itemView, float targetOffset) {
        return this.mOrientation == 1 ? (int) targetOffset : 0;
    }

    protected float maxRemoveOffset() {
        return (float) (this.mOrientationHelper.getTotalSpace() - this.mSpaceMain);
    }

    protected float minRemoveOffset() {
        return (float) (-this.mDecoratedMeasurement - this.mOrientationHelper.getStartAfterPadding() - this.mSpaceMain);
    }

    protected float getDistanceRatio() {
        return 1.0F;
    }

    public int getCurrentPosition() {
        if (this.getItemCount() == 0) {
            return 0;
        } else {
            int position = this.getCurrentPositionOffset();
            if (!this.mInfinite) {
                return Math.abs(position);
            } else {
                position = !this.mShouldReverseLayout ? (position >= 0 ? position % this.getItemCount() : this.getItemCount() + position % this.getItemCount()) : (position > 0 ? this.getItemCount() - position % this.getItemCount() : -position % this.getItemCount());
                return position == this.getItemCount() ? 0 : position;
            }
        }
    }

    public View findViewByPosition(int position) {
        int itemCount = this.getItemCount();
        if (itemCount == 0) {
            return null;
        } else {
            for (int i = 0; i < this.positionCache.size(); ++i) {
                int key = this.positionCache.keyAt(i);
                if (key >= 0) {
                    if (position == key % itemCount) {
                        return (View) this.positionCache.valueAt(i);
                    }
                } else {
                    int delta = key % itemCount;
                    if (delta == 0) {
                        delta = -itemCount;
                    }

                    if (itemCount + delta == position) {
                        return (View) this.positionCache.valueAt(i);
                    }
                }
            }

            return null;
        }
    }

    public int getLayoutPositionOfView(View v) {
        for (int i = 0; i < this.positionCache.size(); ++i) {
            int key = this.positionCache.keyAt(i);
            View value = (View) this.positionCache.get(key);
            if (value == v) {
                return key;
            }
        }

        return -1;
    }

    int getCurrentPositionOffset() {
        return this.mInterval == 0.0F ? 0 : Math.round(this.mOffset / this.mInterval);
    }

    private float getOffsetOfRightAdapterPosition() {
        if (this.mShouldReverseLayout) {
            return this.mInfinite ? (this.mOffset <= 0.0F ? this.mOffset % (this.mInterval * (float) this.getItemCount()) : (float) this.getItemCount() * -this.mInterval + this.mOffset % (this.mInterval * (float) this.getItemCount())) : this.mOffset;
        } else {
            return this.mInfinite ? (this.mOffset >= 0.0F ? this.mOffset % (this.mInterval * (float) this.getItemCount()) : (float) this.getItemCount() * this.mInterval + this.mOffset % (this.mInterval * (float) this.getItemCount())) : this.mOffset;
        }
    }

    public int getOffsetToCenter() {
        return this.mInfinite ? (int) (((float) this.getCurrentPositionOffset() * this.mInterval - this.mOffset) * this.getDistanceRatio()) : (int) (((float) this.getCurrentPosition() * (!this.mShouldReverseLayout ? this.mInterval : -this.mInterval) - this.mOffset) * this.getDistanceRatio());
    }

    public int getOffsetToPosition(int position) {
        return this.mInfinite ? (int) (((float) (this.getCurrentPositionOffset() + (!this.mShouldReverseLayout ? position - this.getCurrentPositionOffset() : -this.getCurrentPositionOffset() - position)) * this.mInterval - this.mOffset) * this.getDistanceRatio()) : (int) (((float) position * (!this.mShouldReverseLayout ? this.mInterval : -this.mInterval) - this.mOffset) * this.getDistanceRatio());
    }

    public void setOnPageChangeListener(ViewPagerLayoutManager.OnPageChangeListener onPageChangeListener) {
        this.onPageChangeListener = onPageChangeListener;
    }

    public void setInfinite(boolean enable) {
        this.assertNotInLayoutOrScroll((String) null);
        if (enable != this.mInfinite) {
            this.mInfinite = enable;
            this.requestLayout();
        }
    }

    public boolean getInfinite() {
        return this.mInfinite;
    }

    public int getDistanceToBottom() {
        return this.mDistanceToBottom == 2147483647 ? (this.mOrientationHelper.getTotalSpaceInOther() - this.mDecoratedMeasurementInOther) / 2 : this.mDistanceToBottom;
    }

    public void setDistanceToBottom(int mDistanceToBottom) {
        this.assertNotInLayoutOrScroll((String) null);
        if (this.mDistanceToBottom != mDistanceToBottom) {
            this.mDistanceToBottom = mDistanceToBottom;
            this.removeAllViews();
        }
    }

    public void setSmoothScrollbarEnabled(boolean enabled) {
        this.mSmoothScrollbarEnabled = enabled;
    }

    public void setEnableBringCenterToFront(boolean bringCenterToTop) {
        this.assertNotInLayoutOrScroll((String) null);
        if (this.mEnableBringCenterToFront != bringCenterToTop) {
            this.mEnableBringCenterToFront = bringCenterToTop;
            this.requestLayout();
        }
    }

    public boolean getEnableBringCenterToFront() {
        return this.mEnableBringCenterToFront;
    }

    public boolean getSmoothScrollbarEnabled() {
        return this.mSmoothScrollbarEnabled;
    }

    public interface OnPageChangeListener {
        void onPageSelected(int var1);

        void onPageScrollStateChanged(int var1);
    }

    private static class SavedState implements Parcelable {
        int position;
        float offset;
        boolean isReverseLayout;
        public static final Creator<ViewPagerLayoutManager.SavedState> CREATOR = new Creator<ViewPagerLayoutManager.SavedState>() {
            public ViewPagerLayoutManager.SavedState createFromParcel(Parcel in) {
                return new ViewPagerLayoutManager.SavedState(in);
            }

            public ViewPagerLayoutManager.SavedState[] newArray(int size) {
                return new ViewPagerLayoutManager.SavedState[size];
            }
        };

        SavedState() {
        }

        SavedState(Parcel in) {
            this.position = in.readInt();
            this.offset = in.readFloat();
            this.isReverseLayout = in.readInt() == 1;
        }

        public SavedState(ViewPagerLayoutManager.SavedState other) {
            this.position = other.position;
            this.offset = other.offset;
            this.isReverseLayout = other.isReverseLayout;
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.position);
            dest.writeFloat(this.offset);
            dest.writeInt(this.isReverseLayout ? 1 : 0);
        }
    }
}


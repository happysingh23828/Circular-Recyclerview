package com.akribase.archycards.lib;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

import android.content.Context;
import android.view.View;


public class CircleScaleLayoutManager extends ViewPagerLayoutManager {
    public static final int LEFT = 10;
    public static final int RIGHT = 11;
    public static final int TOP = 12;
    public static final int BOTTOM = 13;
    public static final int LEFT_ON_TOP = 4;
    public static final int RIGHT_ON_TOP = 5;
    public static final int CENTER_ON_TOP = 6;
    private int radius;
    private int angleInterval;
    private float moveSpeed;
    private float centerScale;
    private float maxRemoveAngle;
    private float minRemoveAngle;
    private int gravity;
    private boolean flipRotate;
    private int zAlignment;

    public CircleScaleLayoutManager(Context context) {
        this(new CircleScaleLayoutManager.Builder(context));
    }

    public CircleScaleLayoutManager(Context context, int gravity, boolean reverseLayout) {
        this((new CircleScaleLayoutManager.Builder(context)).setGravity(gravity).setReverseLayout(reverseLayout));
    }

    public CircleScaleLayoutManager(Context context, boolean reverseLayout) {
        this((new CircleScaleLayoutManager.Builder(context)).setReverseLayout(reverseLayout));
    }

    public CircleScaleLayoutManager(CircleScaleLayoutManager.Builder builder) {
        this(builder.context, builder.radius, builder.angleInterval, builder.centerScale, builder.moveSpeed, builder.maxRemoveAngle, builder.minRemoveAngle, builder.gravity, builder.zAlignment, builder.flipRotate, builder.maxVisibleItemCount, builder.distanceToBottom, builder.reverseLayout);
    }

    private CircleScaleLayoutManager(Context context, int radius, int angleInterval, float centerScale, float moveSpeed, float max, float min, int gravity, int zAlignment, boolean flipRotate, int maxVisibleItemCount, int distanceToBottom, boolean reverseLayout) {
        super(context, 0, reverseLayout);
        this.setEnableBringCenterToFront(true);
        this.setMaxVisibleItemCount(maxVisibleItemCount);
        this.setDistanceToBottom(distanceToBottom);
        this.radius = radius;
        this.angleInterval = angleInterval;
        this.centerScale = centerScale;
        this.moveSpeed = moveSpeed;
        this.maxRemoveAngle = max;
        this.minRemoveAngle = min;
        this.gravity = gravity;
        this.flipRotate = flipRotate;
        this.zAlignment = zAlignment;
    }

    public int getRadius() {
        return this.radius;
    }

    public int getAngleInterval() {
        return this.angleInterval;
    }

    public float getCenterScale() {
        return this.centerScale;
    }

    public float getMoveSpeed() {
        return this.moveSpeed;
    }

    public float getMaxRemoveAngle() {
        return this.maxRemoveAngle;
    }

    public float getMinRemoveAngle() {
        return this.minRemoveAngle;
    }

    public int getGravity() {
        return this.gravity;
    }

    public boolean getFlipRotate() {
        return this.flipRotate;
    }

    public int getZAlignment() {
        return this.zAlignment;
    }

    public void setRadius(int radius) {
        this.assertNotInLayoutOrScroll((String) null);
        if (this.radius != radius) {
            this.radius = radius;
            this.removeAllViews();
        }
    }

    public void setAngleInterval(int angleInterval) {
        this.assertNotInLayoutOrScroll((String) null);
        if (this.angleInterval != angleInterval) {
            this.angleInterval = angleInterval;
            this.removeAllViews();
        }
    }

    public void setCenterScale(float centerScale) {
        this.assertNotInLayoutOrScroll((String) null);
        if (this.centerScale != centerScale) {
            this.centerScale = centerScale;
            this.requestLayout();
        }
    }

    public void setMoveSpeed(float moveSpeed) {
        this.assertNotInLayoutOrScroll((String) null);
        if (this.moveSpeed != moveSpeed) {
            this.moveSpeed = moveSpeed;
        }
    }

    public void setMaxRemoveAngle(float maxRemoveAngle) {
        this.assertNotInLayoutOrScroll((String) null);
        if (this.maxRemoveAngle != maxRemoveAngle) {
            this.maxRemoveAngle = maxRemoveAngle;
            this.requestLayout();
        }
    }

    public void setMinRemoveAngle(float minRemoveAngle) {
        this.assertNotInLayoutOrScroll((String) null);
        if (this.minRemoveAngle != minRemoveAngle) {
            this.minRemoveAngle = minRemoveAngle;
            this.requestLayout();
        }
    }

    public void setGravity(int gravity) {
        this.assertNotInLayoutOrScroll((String) null);
        assertGravity(gravity);
        if (this.gravity != gravity) {
            this.gravity = gravity;
            if (gravity != 10 && gravity != 11) {
                this.setOrientation(0);
            } else {
                this.setOrientation(1);
            }

            this.requestLayout();
        }
    }

    public void setFlipRotate(boolean flipRotate) {
        this.assertNotInLayoutOrScroll((String) null);
        if (this.flipRotate != flipRotate) {
            this.flipRotate = flipRotate;
            this.requestLayout();
        }
    }

    public void setZAlignment(int zAlignment) {
        this.assertNotInLayoutOrScroll((String) null);
        assertZAlignmentState(zAlignment);
        if (this.zAlignment != zAlignment) {
            this.zAlignment = zAlignment;
            this.requestLayout();
        }
    }

    protected float setInterval() {
        return (float) this.angleInterval;
    }

    protected void setUp() {
        this.radius = this.radius == CircleScaleLayoutManager.Builder.INVALID_VALUE ? this.mDecoratedMeasurementInOther : this.radius;
    }

    protected float maxRemoveOffset() {
        return this.maxRemoveAngle;
    }

    protected float minRemoveOffset() {
        return this.minRemoveAngle;
    }

    protected int calItemLeft(View itemView, float targetOffset) {
        switch (this.gravity) {
            case 10:
                return (int) ((double) this.radius * Math.sin(Math.toRadians((double) (90.0F - targetOffset))) - (double) this.radius);
            case 11:
                return (int) ((double) this.radius - (double) this.radius * Math.sin(Math.toRadians((double) (90.0F - targetOffset))));
            case 12:
            case 13:
            default:
                return (int) ((double) this.radius * Math.cos(Math.toRadians((double) (90.0F - targetOffset))));
        }
    }

    protected int calItemTop(View itemView, float targetOffset) {
        switch (this.gravity) {
            case 10:
            case 11:
                return (int) ((double) this.radius * Math.cos(Math.toRadians((double) (90.0F - targetOffset))));
            case 12:
                return (int) ((double) this.radius * Math.sin(Math.toRadians((double) (90.0F - targetOffset))) - (double) this.radius);
            case 13:
            default:
                return (int) ((double) this.radius - (double) this.radius * Math.sin(Math.toRadians((double) (90.0F - targetOffset))));
        }
    }

    protected void setItemViewProperty(View itemView, float targetOffset) {
        float scale = 1.0F;
        float diff;
        switch (this.gravity) {
            case 10:
            case 13:
            default:
                if (this.flipRotate) {
                    itemView.setRotation(360.0F - targetOffset);
                    if (targetOffset < (float) this.angleInterval && targetOffset > (float) (-this.angleInterval)) {
                        diff = Math.abs(Math.abs(360.0F - itemView.getRotation() - (float) this.angleInterval) - (float) this.angleInterval);
                        scale = (this.centerScale - 1.0F) / (float) (-this.angleInterval) * diff + this.centerScale;
                    }
                } else {
                    itemView.setRotation(targetOffset);
                    if (targetOffset < (float) this.angleInterval && targetOffset > (float) (-this.angleInterval)) {
                        diff = Math.abs(Math.abs(itemView.getRotation() - (float) this.angleInterval) - (float) this.angleInterval);
                        scale = (this.centerScale - 1.0F) / (float) (-this.angleInterval) * diff + this.centerScale;
                    }
                }
                break;
            case 11:
            case 12:
                if (this.flipRotate) {
                    itemView.setRotation(targetOffset);
                    if (targetOffset < (float) this.angleInterval && targetOffset > (float) (-this.angleInterval)) {
                        diff = Math.abs(Math.abs(itemView.getRotation() - (float) this.angleInterval) - (float) this.angleInterval);
                        scale = (this.centerScale - 1.0F) / (float) (-this.angleInterval) * diff + this.centerScale;
                    }
                } else {
                    itemView.setRotation(360.0F - targetOffset);
                    if (targetOffset < (float) this.angleInterval && targetOffset > (float) (-this.angleInterval)) {
                        diff = Math.abs(Math.abs(360.0F - itemView.getRotation() - (float) this.angleInterval) - (float) this.angleInterval);
                        scale = (this.centerScale - 1.0F) / (float) (-this.angleInterval) * diff + this.centerScale;
                    }
                }
        }

        itemView.setScaleX(scale);
        itemView.setScaleY(scale);
    }

    protected float setViewElevation(View itemView, float targetOffset) {
        if (this.zAlignment == 4) {
            return (540.0F - targetOffset) / 72.0F;
        } else {
            return this.zAlignment == 5 ? (targetOffset - 540.0F) / 72.0F : (360.0F - Math.abs(targetOffset)) / 72.0F;
        }
    }

    protected float getDistanceRatio() {
        return this.moveSpeed == 0.0F ? 3.4028235E38F : 1.0F / this.moveSpeed;
    }

    private static void assertGravity(int gravity) {
        if (gravity != 10 && gravity != 11 && gravity != 12 && gravity != 13) {
            throw new IllegalArgumentException("gravity must be one of LEFT RIGHT TOP and BOTTOM");
        }
    }

    private static void assertZAlignmentState(int zAlignment) {
        if (zAlignment != 4 && zAlignment != 5 && zAlignment != 6) {
            throw new IllegalArgumentException("zAlignment must be one of LEFT_ON_TOP RIGHT_ON_TOP and CENTER_ON_TOP");
        }
    }

    public static class Builder {
        private static int INTERVAL_ANGLE = 30;
        private static float DISTANCE_RATIO = 10.0F;
        private static final float SCALE_RATE = 1.2F;
        private static int INVALID_VALUE = -2147483648;
        private int radius;
        private int angleInterval;
        private float centerScale;
        private float moveSpeed;
        private float maxRemoveAngle;
        private float minRemoveAngle;
        private boolean reverseLayout;
        private Context context;
        private int gravity;
        private boolean flipRotate;
        private int zAlignment;
        private int maxVisibleItemCount;
        private int distanceToBottom;

        public Builder(Context context) {
            this.context = context;
            this.radius = INVALID_VALUE;
            this.angleInterval = INTERVAL_ANGLE;
            this.centerScale = 1.2F;
            this.moveSpeed = 1.0F / DISTANCE_RATIO;
            this.maxRemoveAngle = 90.0F;
            this.minRemoveAngle = -90.0F;
            this.reverseLayout = false;
            this.flipRotate = false;
            this.gravity = 13;
            this.zAlignment = 6;
            this.distanceToBottom = 2147483647;
            this.maxVisibleItemCount = -1;
        }

        public CircleScaleLayoutManager.Builder setRadius(int radius) {
            this.radius = radius;
            return this;
        }

        public CircleScaleLayoutManager.Builder setAngleInterval(int angleInterval) {
            this.angleInterval = angleInterval;
            return this;
        }

        public CircleScaleLayoutManager.Builder setCenterScale(float centerScale) {
            this.centerScale = centerScale;
            return this;
        }

        public CircleScaleLayoutManager.Builder setMoveSpeed(int moveSpeed) {
            this.moveSpeed = (float) moveSpeed;
            return this;
        }

        public CircleScaleLayoutManager.Builder setMaxRemoveAngle(float maxRemoveAngle) {
            this.maxRemoveAngle = maxRemoveAngle;
            return this;
        }

        public CircleScaleLayoutManager.Builder setMinRemoveAngle(float minRemoveAngle) {
            this.minRemoveAngle = minRemoveAngle;
            return this;
        }

        public CircleScaleLayoutManager.Builder setReverseLayout(boolean reverseLayout) {
            this.reverseLayout = reverseLayout;
            return this;
        }

        public CircleScaleLayoutManager.Builder setGravity(int gravity) {
            CircleScaleLayoutManager.assertGravity(gravity);
            this.gravity = gravity;
            return this;
        }

        public CircleScaleLayoutManager.Builder setFlipRotate(boolean flipRotate) {
            this.flipRotate = flipRotate;
            return this;
        }

        public CircleScaleLayoutManager.Builder setZAlignment(int zAlignment) {
            CircleScaleLayoutManager.assertZAlignmentState(zAlignment);
            this.zAlignment = zAlignment;
            return this;
        }

        public CircleScaleLayoutManager.Builder setMaxVisibleItemCount(int maxVisibleItemCount) {
            this.maxVisibleItemCount = maxVisibleItemCount;
            return this;
        }

        public CircleScaleLayoutManager.Builder setDistanceToBottom(int distanceToBottom) {
            this.distanceToBottom = distanceToBottom;
            return this;
        }

        public CircleScaleLayoutManager build() {
            return new CircleScaleLayoutManager(this);
        }
    }
}


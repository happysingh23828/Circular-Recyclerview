package com.akribase.archycards.wheelview

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.os.SystemClock
import android.util.AttributeSet
import android.view.*
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.annotation.DrawableRes
import com.akribase.archycards.R
import com.akribase.archycards.wheelview.Circle.Companion.clamp
import com.akribase.archycards.wheelview.Circle.Companion.clamp180
import com.akribase.archycards.wheelview.Circle.Companion.shortestAngle
import com.akribase.archycards.wheelview.adapter.WheelAdapter
import com.akribase.archycards.wheelview.transformer.FadingSelectionTransformer
import com.akribase.archycards.wheelview.transformer.SimpleItemTransformer
import com.akribase.archycards.wheelview.transformer.WheelItemTransformer
import com.akribase.archycards.wheelview.transformer.WheelSelectionTransformer
import kotlin.math.*

open class WheelView : View {
    companion object {
        private val sTempRect = Rect()
        private const val VELOCITY_FRICTION_COEFFICIENT = 0.015f
        private const val CONSTANT_FRICTION_COEFFICIENT = 0.0028f
        private const val ANGULAR_VEL_COEFFICIENT = 22f
        private const val MAX_ANGULAR_VEL = 0.3f
        private const val LEFT_MASK = 0x01
        private const val RIGHT_MASK = 0x02
        private const val TOP_MASK = 0x04
        private const val BOTTOM_MASK = 0x08
        private const val NEVER_USED = 0

        //The touch factors decrease the drag movement towards the center of the wheel. It is there so
        //that dragging the wheel near the center doesn't cause the wheel's angle to change
        //drastically. It is squared to provide a linear function once multiplied by 1/r^2
        private const val TOUCH_FACTOR_SIZE = 20
        private const val TOUCH_DRAG_COEFFICIENT = 0.8f
        private val TOUCH_FACTORS: FloatArray
        private const val CLICK_MAX_DRAGGED_ANGLE = 0.7f
        private val EMPTY_CACHE_ITEM = CacheItem(true)

        //Taken and modified from Android Source for API < 11
        fun resolveSizeAndState(size: Int, measureSpec: Int): Int {
            var result = size
            val specMode = MeasureSpec.getMode(measureSpec)
            val specSize = MeasureSpec.getSize(measureSpec)
            when (specMode) {
                MeasureSpec.UNSPECIFIED -> result = size
                MeasureSpec.AT_MOST -> result = if (specSize < size) {
                    specSize
                } else {
                    size
                }
                MeasureSpec.EXACTLY -> result = specSize
            }
            return result
        }

        init {
            val size = TOUCH_FACTOR_SIZE
            TOUCH_FACTORS = FloatArray(size)
            val maxIndex: Int = size - 1
            val numerator: Float =
                (size * size).toFloat()
            for (i in 0 until size) {
                val factor: Int = maxIndex - i + 1
                TOUCH_FACTORS[i] =
                    (1 - factor * factor / numerator) * TOUCH_DRAG_COEFFICIENT
            }
        }
    }

    private var mVelocityTracker: VelocityTracker? = null
    private val mForceVector = Vector()
    private val mRadiusVector = Vector()
    private var mAngle = 0f
    private var mAngularVelocity = 0f
    private var mLastUpdateTime: Long = 0
    private var mRequiresUpdate = false

    /**
     * The raw selected position (can be negative and isn't cyclic)
     *
     * @see .getAngle
     * @see .getSelectedPosition
     */
    var rawSelectedPosition = 0
        private set
    private var mLastWheelTouchX = 0f
    private var mLastWheelTouchY = 0f
    private var mItemCacheArray: Array<CacheItem?> = arrayOf()

    /**
     * @return the wheel's drawable
     */
    var wheelDrawable: Drawable? = null
        private set

    /**
     * @return the empty item drawable used when rendering positions outside of the adapter range.
     * @see .isEmptyItemPosition
     */
    var emptyItemDrawable: Drawable? = null
        private set

    /**
     * @return The Drawable that is drawn behind the selected item.
     */
    var selectionDrawable: Drawable? = null
        private set
    /**
     * @return `true` if the wheel rotates.
     */
    /**
     *
     *  When true the wheel is rotated.
     *
     *  The default value is true
     */
    var isWheelRotatable = true
    /**
     * @return `true` if the adapter items continuously cycle around the wheel.
     */
    /**
     * Set Repeatable Adapter to true will continuously cycle through the set of adapter items.
     */
    var isRepeatableAdapter = false
    private var mIsWheelDrawableRotatable = true
    //TODO
    /**
     * The item angle is the angle covered per item on the wheel and is in degrees.
     * The [.mItemAnglePadding] is included in the item angle.
     */
    var wheelItemAnglePadding
        get() = mItemAnglePadding
        set(anglePadding) {
            mItemAnglePadding = anglePadding

            //TODO
        }

    /**
     * Angle padding is in degrees and reduces the wheel's items size during layout
     */
    private var mItemAnglePadding = 0f

    /**
     * Selection Angle is the angle at which an item is considered selected.
     * The [.mOnItemSelectListener] is called when the 'most selected' item changes.
     */
    var selectionPadding
        get() = mSelectionPadding.toFloat()
        set(padding) {
            mSelectionPadding = padding.toInt()
        }
    private var mSelectionPadding = 0
    private var mWheelPadding = 0
    private var mWheelToItemDistance = 0
    private var mItemRadius = 0
    private var mWheelRadius = 0
    private var mOffsetX = 0
    private var mOffsetY = 0
    private var mItemCount = 0
    private var mWheelPosition = 0
    private var mLeft = 0
    private var mTop = 0
    private var mWidth = 0
    private var mHeight = 0
    private val mViewBounds = Rect()
    private var mWheelBounds: Circle? = null

    /**
     * Wheel item bounds are always pre-rotation and based on the [.mSelectionAngle]
     */
    private var mWheelItemBounds: MutableList<Circle>? = null

    /**
     * The ItemState contain the rotated position
     */
    private var mItemStates: MutableList<ItemState>? = null
    private var mAdapterItemCount = 0
    private var mIsDraggingWheel = false
    private var mLastTouchAngle = 0f
    private var mClickedItem: ItemState? = null
    private var mDraggedAngle = 0f
    var onWheelItemClickListener: OnWheelItemClickListener? = null
    open var onWheelAngleChangeListener: OnWheelAngleChangeListener? = null
    var onWheelItemSelectListener: OnWheelItemSelectListener? = null
        private set
    var onItemVisibilityChangeListener: OnWheelItemVisibilityChangeListener? = null
        private set
    private var mItemTransformer: WheelItemTransformer? = null
    private var mSelectionTransformer: WheelSelectionTransformer? = null
    private var mAdapter: WheelAdapter? = null

    constructor(context: Context?) : super(context) {
        initWheelView()
    }

    @JvmOverloads
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int = 0) : super(
        context,
        attrs,
        defStyle
    ) {
        initWheelView()

        //TODO possible pattern to follow from android source
        /* final int N = a.getIndexCount();
        for (int i = 0; i < N; i++) {
            int attr = a.getIndex(i);
            switch (attr) {
                case com.android.internal.R.styleable.View_background:
                    background = a.getDrawable(attr);
                    break; */
        val a = context.obtainStyledAttributes(attrs, R.styleable.WheelView, defStyle, 0)
        var d = a.getDrawable(R.styleable.WheelView_emptyItemDrawable)
        if (d != null) {
            setEmptyItemDrawable(d)
        } else if (a.hasValue(R.styleable.WheelView_emptyItemColor)) {
            val color = a.getColor(R.styleable.WheelView_emptyItemColor, NEVER_USED)
            setEmptyItemColor(color)
        }
        d = a.getDrawable(R.styleable.WheelView_wheelDrawable)
        if (d != null) {
            setWheelDrawable(d)
        } else if (a.hasValue(R.styleable.WheelView_wheelColor)) {
            val color = a.getColor(R.styleable.WheelView_wheelColor, NEVER_USED)
            setWheelColor(color)
        }
        d = a.getDrawable(R.styleable.WheelView_selectionDrawable)
        if (d != null) {
            setSelectionDrawable(d)
        } else if (a.hasValue(R.styleable.WheelView_selectionColor)) {
            val color = a.getColor(R.styleable.WheelView_selectionColor, NEVER_USED)
            setSelectionColor(color)
        }
        isWheelRotatable = a.getBoolean(R.styleable.WheelView_rotatable, true)
        mSelectionPadding = a.getDimensionPixelSize(R.styleable.WheelView_selectionPadding, 0)
        isRepeatableAdapter = a.getBoolean(R.styleable.WheelView_repeatItems, false)
        mIsWheelDrawableRotatable = a.getBoolean(R.styleable.WheelView_rotatableWheelDrawable, true)
        selectionPadding = a.getFloat(R.styleable.WheelView_selectionAngle, 0f)
        setWheelRadius(
            a.getLayoutDimension(
                R.styleable.WheelView_wheelRadius,
                0 /* TODO Wrap_content */
            )
        )
        mOffsetX = a.getDimensionPixelSize(R.styleable.WheelView_wheelOffsetX, 0)
        mOffsetY = a.getDimensionPixelSize(R.styleable.WheelView_wheelOffsetY, 0)
        mWheelToItemDistance = a.getLayoutDimension(
            R.styleable.WheelView_wheelToItemDistance,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        val itemCount = a.getInteger(R.styleable.WheelView_wheelItemCount, 0)

        //TODO maybe just remove angle padding?
        mItemAnglePadding = a.getFloat(
            R.styleable.WheelView_wheelItemAnglePadding,
            0f
        ) //TODO angle works with the ItemRadius
        if (itemCount != 0) {
            setWheelItemCount(itemCount)
        } else {
            val itemAngle = a.getFloat(R.styleable.WheelView_wheelItemAngle, 0f)
            if (itemAngle != 0f) {
                wheelItemAngle = itemAngle
            }
        }
        mItemRadius = a.getDimensionPixelSize(R.styleable.WheelView_wheelItemRadius, 0)
        if (mItemCount == 0 && mWheelToItemDistance > 0 && mWheelRadius > 0) {
            wheelItemAnglePadding = calculateAngle(
                mWheelRadius.toFloat(),
                mWheelToItemDistance.toFloat()
            ) + mItemAnglePadding
            wheelItemAngle = wheelItemAnglePadding
        }
        val itemTransformerStr = a.getString(R.styleable.WheelView_wheelItemTransformer)
        if (itemTransformerStr != null) {
            mItemTransformer =
                validateAndInstantiate(itemTransformerStr, WheelItemTransformer::class.java)
        }
        val selectionTransformerStr = a.getString(R.styleable.WheelView_selectionTransformer)
        if (selectionTransformerStr != null) {
            mSelectionTransformer = validateAndInstantiate(
                selectionTransformerStr,
                WheelSelectionTransformer::class.java
            )
        }
        mWheelPadding = a.getDimensionPixelSize(R.styleable.WheelView_wheelPadding, 0)
        mWheelPosition = a.getInt(R.styleable.WheelView_wheelPosition, 0)
        a.recycle()
    }

    private fun <T> validateAndInstantiate(clazzName: String, clazz: Class<out T>): T? {
        var errorMessage: String?
        var instance: T?
        try {
            val xmlClazz = Class.forName(clazzName)
            if (clazz.isAssignableFrom(xmlClazz)) {
                try {
                    errorMessage = null
                    instance = xmlClazz.newInstance() as T
                } catch (e: InstantiationException) {
                    errorMessage = "No argument less constructor for " + xmlClazz.simpleName
                    instance = null
                } catch (e: IllegalAccessException) {
                    errorMessage =
                        "The argument less constructor is not public for " + xmlClazz.simpleName
                    instance = null
                }
            } else {
                errorMessage =
                    "Class inflated from xml (" + xmlClazz.simpleName + ") does not implement " + clazz.simpleName
                instance = null
            }
        } catch (e: ClassNotFoundException) {
            errorMessage = "$clazzName class was not found when inflating from xml"
            instance = null
        }
        return if (errorMessage != null) {
            throw InflateException(errorMessage)
        } else {
            instance
        }
    }

    private fun hasMask(value: Int, mask: Int): Boolean {
        return value and mask == mask
    }

    val isPositionLeft: Boolean
        get() = hasMask(mWheelPosition, LEFT_MASK)
    val isPositionRight: Boolean
        get() = hasMask(mWheelPosition, RIGHT_MASK)
    val isPositionTop: Boolean
        get() = hasMask(mWheelPosition, TOP_MASK)
    val isPositionBottom: Boolean
        get() = hasMask(mWheelPosition, BOTTOM_MASK)

    fun initWheelView() {
        mItemTransformer = SimpleItemTransformer()
        mSelectionTransformer = FadingSelectionTransformer()
    }

    interface OnWheelItemClickListener {
        fun onWheelItemClick(parent: WheelView?, position: Int, isSelected: Boolean)
    }

    /**
     * A listener for when a wheel item is selected.
     */
    interface OnWheelItemSelectListener {
        /**
         * @param parent       WheelView that calls this listener
         * @param itemDrawable - The Drawable of the wheel item that is closest to the selection angle
         * (or closest to the selection angle)
         * @param position     of the adapter that is closest to the selection angle
         */
        fun onWheelItemSelected(
            parent: WheelView?,
            itemDrawable: Drawable?,
            position: Int
        ) //TODO onWheelItemSettled?
    }

    fun setOnWheelItemSelectedListener(listener: OnWheelItemSelectListener?) {
        onWheelItemSelectListener = listener
    }

    interface OnWheelItemVisibilityChangeListener {
        fun onItemVisibilityChange(adapter: WheelAdapter?, position: Int, isVisible: Boolean)
    }

    fun setOnWheelItemVisibilityChangeListener(
        listener: OnWheelItemVisibilityChangeListener?
    ) {
        onItemVisibilityChangeListener = listener
    }

    /**
     * A listener for when the wheel's angle has changed.
     */
    interface OnWheelAngleChangeListener {
        /**
         * Receive a callback when the wheel's angle has changed.
         */
        fun onWheelAngleChange(angle: Float)
    }

    var adapter: WheelAdapter?
        get() = mAdapter
        set(wheelAdapter) {
            mAdapter = wheelAdapter
            val count = mAdapter!!.count
            mItemCacheArray = arrayOfNulls(count)
            mAdapterItemCount = count
            invalidate()
        }

    fun setWheelItemTransformer(itemTransformer: WheelItemTransformer?) {
        requireNotNull(itemTransformer) { "WheelItemTransformer cannot be null" }
        mItemTransformer = itemTransformer
    }

    fun setWheelSelectionTransformer(transformer: WheelSelectionTransformer?) {
        mSelectionTransformer = transformer
    }
    /**
     * @return `true` if the wheel drawable rotates.
     */
    /**
     *
     *  When true the wheel drawable is rotated as well as the wheel items.
     * For performance it is better to not rotate the wheel drawable if possible.
     *
     *  The default value is true
     */
    var isWheelDrawableRotatable: Boolean
        get() = mIsWheelDrawableRotatable
        set(isWheelDrawableRotatable) {
            mIsWheelDrawableRotatable = isWheelDrawableRotatable
            invalidate()
        }
    //TODO mItemRadius = calculateWheelItemRadius(mItemAngle);

    //TODO
    var wheelItemAngle: Float
        get() = wheelItemAnglePadding
        set(angle) {
            wheelItemAnglePadding = angle + mItemAnglePadding
            mItemCount = calculateItemCount(wheelItemAnglePadding)
            //TODO mItemRadius = calculateWheelItemRadius(mItemAngle);
            if (mWheelBounds != null) {
                invalidate()
            }

            //TODO
        }

    private fun calculateItemAngle(itemCount: Int): Float {
        return 360f / itemCount
    }

    private fun calculateItemCount(angle: Float): Int {
        return (360f / angle).toInt()
    }

    var selectionAngle: Float
        get() = selectionPadding
        set(angle) {
            selectionPadding = clamp180(angle)
            if (mWheelBounds != null) {
                layoutWheelItems()
            }
        }

    fun setWheelToItemDistance(distance: Int) {
        mWheelToItemDistance = distance
    }

    val wheelToItemDistance: Float
        get() = mWheelToItemDistance.toFloat()

    fun setWheelItemRadius(radius: Int) {
        mItemRadius = radius
    }

    /* TODO
    public void setWheelItemRadius(float radius, int itemCount) {
        mItemRadius = radius;
        mItemAngle = calculateItemAngle(itemCount);
        mItemCount = itemCount;
    } */
    val wheelItemRadius: Float
        get() = mItemRadius.toFloat()

    /**
     * Sets the wheel radius in pixels.
     */
    fun setWheelRadius(radius: Int) {
        require(radius >= -1) { "Invalid Wheel Radius: $radius" }
        mWheelRadius = radius
    }

    /**
     * Gets the radius of the wheel in pixels
     */
    val wheelRadius: Float
        get() = mWheelRadius.toFloat()

    /**
     * Sets the number of items to be displayed on the wheel.
     */
    fun setWheelItemCount(count: Int) {
        mItemCount = count
        wheelItemAnglePadding = calculateItemAngle(count)
        if (mWheelBounds != null) {
            invalidate()
            //TODO ?
        }
    }

    /**
     * @return the count of wheel items that are displayed on the wheel.
     */
    val wheelItemCount: Float
        get() = mItemCount.toFloat()

    fun setWheelOffsetX(offsetX: Int) {
        mOffsetX = offsetX
        //TODO
    }

    val wheelOffsetX: Float
        get() = mOffsetX.toFloat()

    fun setWheelOffsetY(offsetY: Int) {
        mOffsetY = offsetY
        //TODO
    }

    val wheelOffsetY: Float
        get() = mOffsetY.toFloat()
    /*
    public void setWheelPosition(int position) {
        //TODO possible solution to animate or instantly?
    }*/
    /**
     * Find the largest circle to fit within the item angle.
     * The point of intersection occurs at a tangent to the wheel item.
     */
    private fun calculateWheelItemRadius(angle: Float): Float {
        return (mWheelToItemDistance * sin(Math.toRadians(((angle - mItemAnglePadding) / 2f).toDouble()))).toFloat()
    }

    private fun calculateAngle(innerRadius: Float, outerRadius: Float): Float {
        return 2f * Math.toDegrees(asin((innerRadius / outerRadius).toDouble())).toFloat()
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        val width = right - left
        val height = bottom - top
        if (mWidth != width || mHeight != height || mLeft != left || mTop != top) {
            layoutWheel(0, 0, width, height)
        }
        super.onLayout(changed, left, top, right, bottom)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        //if we are not to measure exactly then check what size we would like to be
        var desiredWidth: Int = if (widthMode != MeasureSpec.EXACTLY) {
            if (mWheelRadius >= 0) {
                mWheelRadius * 2 + paddingLeft + paddingRight
            } else {
                widthSize
            }
        } else {
            -1
        }
        var desiredHeight: Int = if (heightMode != MeasureSpec.EXACTLY) {
            if (mWheelRadius >= 0) {
                mWheelRadius * 2 + paddingTop + paddingBottom
            } else {
                heightSize
            }
        } else {
            -1
        }
        desiredWidth = max(desiredWidth, suggestedMinimumWidth)
        desiredHeight = max(desiredHeight, suggestedMinimumHeight)
        val width = resolveSizeAndState(desiredWidth, widthMeasureSpec)
        val height = resolveSizeAndState(desiredHeight, heightMeasureSpec)
        setMeasuredDimension(width, height)
    }

    private fun layoutWheel(left: Int, top: Int, width: Int, height: Int) {
        if (width == 0 || height == 0) return
        mLeft = left
        mTop = top
        mWidth = width
        mHeight = height
        mViewBounds[left, top, left + width] = top + height
        setWheelBounds(width, height)
        layoutWheelItems()
    }

    private fun setWheelBounds(width: Int, height: Int) {
        var relativeVertical = 0.5f
        var relativeHorizontal = 0.5f
        if (isPositionLeft) relativeHorizontal -= 0.5f
        if (isPositionRight) relativeHorizontal += 0.5f
        if (isPositionTop) relativeVertical -= 0.5f
        if (isPositionBottom) relativeVertical += 0.5f
        val centerX = (mOffsetX + width * relativeHorizontal).toInt()
        val centerY = (mOffsetY + height * relativeVertical).toInt()
        val wheelRadius = measureWheelRadius(mWheelRadius, width, height)
        mWheelBounds = Circle(centerX.toFloat(), centerY.toFloat(), wheelRadius.toFloat())
        if (wheelDrawable != null) {
            wheelDrawable!!.bounds = mWheelBounds!!.boundingRect
        }
    }

    private fun measureWheelRadius(radius: Int, width: Int, height: Int): Int {
        return if (radius == ViewGroup.LayoutParams.MATCH_PARENT) {
            min(
                width - paddingLeft - paddingRight,
                height - paddingTop - paddingBottom
            ) / 2
        } else {
            radius
        }
    }

    private fun layoutWheelItems() {
        mItemStates = ArrayList(mItemCount)
        for (i in 0 until mItemCount) {
            mItemStates?.add(ItemState())
        }
        if (mWheelItemBounds == null) {
            mWheelItemBounds = ArrayList(mItemCount)
        } else if (mWheelItemBounds!!.isNotEmpty()) {
            mWheelItemBounds!!.clear()
        }
        if (mWheelToItemDistance == ViewGroup.LayoutParams.MATCH_PARENT) {
            mWheelToItemDistance = (mWheelBounds!!.radius - mItemRadius - mWheelPadding).toInt()
        }
        val itemAngleRadians = Math.toRadians(wheelItemAnglePadding.toDouble()).toFloat()
        val offsetRadians = Math.toRadians(-selectionPadding.toDouble()).toFloat()
        for (i in 0 until mItemCount) {
            val angle = itemAngleRadians * i + offsetRadians
            val x =
                mWheelBounds!!.centerX + mWheelToItemDistance * cos(angle.toDouble()).toFloat()
            val y =
                mWheelBounds!!.centerY + mWheelToItemDistance * sin(angle.toDouble()).toFloat()
            mWheelItemBounds!!.add(Circle(x, y, mItemRadius.toFloat()))
        }
        invalidate()
    }

    /**
     * You should set the wheel drawable not to rotate for a performance benefit.
     * See the method [.setWheelDrawableRotatable]
     */
    fun setWheelColor(color: Int) {
        setWheelDrawable(createOvalDrawable(color))
    }

    /**
     * Sets the wheel's drawable that can also rotate with the items.
     *
     * @see .setWheelDrawableRotatable
     * @see .setWheelDrawable
     */
    fun setWheelDrawable(@DrawableRes resId: Int) {
        setWheelDrawable(resources.getDrawable(resId))
    }

    /**
     *
     *
     * Sets the wheel's drawable that can also rotate with the items.
     *
     *
     *
     * Note if the drawable has infinite lines of symmetry then you should set the wheel drawable to
     * not rotate, see [.setWheelDrawableRotatable]. In other words, if the drawable
     * doesn't look any different whilst it is rotating, you should improve the performance by
     * disabling the drawable from rotating.
     *
     *
     * @see .setWheelDrawableRotatable
     */
    fun setWheelDrawable(drawable: Drawable?) {
        wheelDrawable = drawable
        if (mWheelBounds != null) {
            wheelDrawable!!.bounds = mWheelBounds!!.boundingRect
            invalidate()
        }
    }

    /**
     * Sets the empty item drawable that is drawn when outside of the adapter range.
     *
     * @see .isEmptyItemPosition
     */
    fun setEmptyItemColor(color: Int) {
        setEmptyItemDrawable(createOvalDrawable(color))
    }

    /**
     * Sets the empty item drawable that is drawn when outside of the adapter range.
     *
     * @see .isEmptyItemPosition
     */
    fun setEmptyItemDrawable(@DrawableRes resId: Int) {
        setEmptyItemDrawable(resources.getDrawable(resId))
    }

    /**
     * Sets the empty item drawable that is drawn when outside of the adapter range.
     *
     * @see .isEmptyItemPosition
     */
    fun setEmptyItemDrawable(drawable: Drawable?) {
        emptyItemDrawable = drawable
        EMPTY_CACHE_ITEM.mDrawable = drawable
        if (mWheelBounds != null) {
            invalidate()
        }
    }

    /**
     * Sets the selection drawable to be a circular color
     *
     * @see .setSelectionDrawable
     * @see .setSelectionDrawable
     */
    fun setSelectionColor(color: Int) {
        setSelectionDrawable(createOvalDrawable(color))
    }

    /**
     * Sets the selection drawable from a Drawable Resource.
     *
     * @see .setSelectionColor
     * @see .setSelectionDrawable
     */
    fun setSelectionDrawable(@DrawableRes resId: Int) {
        setSelectionDrawable(resources.getDrawable(resId))
    }

    /**
     * Set the selection drawable that is drawn behind the selected item.
     *
     * @see .setSelectionDrawable
     * @see .setSelectionColor
     */
    fun setSelectionDrawable(drawable: Drawable?) {
        selectionDrawable = drawable
        invalidate()
    }

    /**
     * @return the absolute angle for the item at the given position
     */
    fun getAngleForPosition(rawPosition: Int): Float {
        return rawPosition * wheelItemAnglePadding
    }

    /**
     *
     *
     * Changes the wheel angle so that the item at the provided position becomes selected.
     *
     *
     *
     * Note that this does not change the selection angle, instead it will rotate the wheel
     * to the angle where the provided position becomes selected.
     *
     *
     * @param rawPosition the raw position (can take negative numbers)
     */
    fun setSelected(rawPosition: Int, isAnimate: Boolean) {
        if (isAnimate) {
            //must rotate the wheel in the opposite direction so that the given position becomes selected
            val valueAnimator = ValueAnimator.ofFloat(
                -1f * getAngleForPosition(rawSelectedPosition),
                -1f * getAngleForPosition(rawPosition)
            )
            valueAnimator.addUpdateListener { valueAnimator ->
                angle = valueAnimator.animatedValue as Float
            }
            valueAnimator.duration = 10000 //animation duration
            valueAnimator.interpolator = AccelerateDecelerateInterpolator() //animation duration
            valueAnimator.start()
        } else {
            angle = -1f * getAngleForPosition(rawPosition)
        }
    }
    /**
     * Changes the wheel angle so that the item in the middle of the adapter becomes selected.
     *
     * @see .setSelected
     */
    /**
     * Checks to see if the selectedPosition has changed.
     */
    private fun updateSelectedPosition() {
        val position =
            ((-mAngle + -0.5 * sign(mAngle) * wheelItemAnglePadding) / wheelItemAnglePadding).toInt()
        selectedPosition = position
    }

    /**
     * @return `true` if this adapter position is empty.
     *
     *
     * This is only possible with non-repeatable items.
     */
    fun isEmptyItemPosition(position: Int): Boolean {
        return !isRepeatableAdapter && (position < 0 || position >= mAdapterItemCount)
    }

    /**
     * @param position of the item in the Adapter
     * @return The Drawable at the specific position in the Adapter
     */
    fun getWheelItemDrawable(position: Int): Drawable? {
        if (mAdapter == null || mAdapterItemCount == 0) return null
        val cacheItem = getCacheItem(position)
        return if (!cacheItem.mDirty) cacheItem.mDrawable else mAdapter!!.getDrawable(position)
            .also { cacheItem.mDrawable = it }
    }

    /**
     * Invalidate the drawable at the specific position so that the next Draw call
     * will refresh the Drawable at this given position in the adapter.
     */
    fun invalidateWheelItemDrawable(position: Int) {
        val adapterPos = rawPositionToAdapterPosition(position)
        if (isEmptyItemPosition(adapterPos)) return
        val cacheItem = mItemCacheArray[adapterPos]
        if (cacheItem != null) cacheItem.mDirty = true
        invalidate()
    }

    /**
     * Invalidate all wheel items. Note - If you need to change the number of items
     * in the adapter then you will need to use [.setAdapter]
     *
     * @see .invalidateWheelItemDrawable
     */
    fun invalidateWheelItemDrawables() {
        for (i in 0 until mAdapterItemCount) {
            invalidateWheelItemDrawable(i)
        }
    }

    private fun createOvalDrawable(color: Int): Drawable {
        val shapeDrawable = ShapeDrawable(OvalShape())
        shapeDrawable.paint.color = color
        return shapeDrawable
    }

    /**
     * @return the adapter position that is closest to the selection
     * @see .getRawSelectedPosition
     * @see .getAngle
     */
    var selectedPosition: Int
        get() = rawPositionToAdapterPosition(rawSelectedPosition)
        private set(position) {
            if (rawSelectedPosition == position) return
            rawSelectedPosition = position
            if (onWheelItemSelectListener != null && !isEmptyItemPosition(position)) {
                val adapterPos = selectedPosition
                onWheelItemSelectListener!!.onWheelItemSelected(
                    this,
                    getWheelItemDrawable(adapterPos),
                    adapterPos
                )
            }
        }
    /**
     * @return the wheel angle in degrees.
     * @see .getRawSelectedPosition
     * @see .getSelectedPosition
     */
    /**
     * Set the angle of the wheel instantaneously.
     * Note this does not animate to the provided angle.
     *
     * @param angle given in degrees and can be any value (not only between 0 and 360)
     */
    var angle: Float
        get() = mAngle
        set(angle) {
            mAngle = angle
            updateSelectedPosition()
            if (onWheelAngleChangeListener != null) {
                onWheelAngleChangeListener!!.onWheelAngleChange(mAngle)
            }
            invalidate()
        }

    private fun addAngle(degrees: Float) {
        angle = mAngle + degrees
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isWheelRotatable) return true
        val x = event.x
        val y = event.y
        if (!mWheelBounds!!.contains(x, y)) {
            if (mIsDraggingWheel) {
                flingWheel()
            }
            return true
        }
        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                if (!mIsDraggingWheel) {
                    startWheelDrag(event, x, y)
                }
                mClickedItem = getClickedItem(x, y)
            }
            MotionEvent.ACTION_UP -> {
                if (onWheelItemClickListener != null && mClickedItem != null && mClickedItem === getClickedItem(
                        x,
                        y
                    ) && mDraggedAngle < CLICK_MAX_DRAGGED_ANGLE
                ) {
                    val isSelected = abs(mClickedItem!!.relativePosition) < 1f
                    onWheelItemClickListener!!.onWheelItemClick(
                        this,
                        mClickedItem!!.mAdapterPosition, isSelected
                    )
                }
                if (mIsDraggingWheel) {
                    flingWheel()
                }
                if (mVelocityTracker != null) {
                    mVelocityTracker!!.recycle()
                    mVelocityTracker = null
                }
            }
            MotionEvent.ACTION_CANCEL -> {
                if (mIsDraggingWheel) {
                    flingWheel()
                }
                if (mVelocityTracker != null) {
                    mVelocityTracker!!.recycle()
                    mVelocityTracker = null
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (!mIsDraggingWheel) {
                    startWheelDrag(event, x, y)
                    return true
                }
                mVelocityTracker!!.addMovement(event)
                mLastWheelTouchX = x
                mLastWheelTouchY = y
                setRadiusVector(x, y)
                val wheelRadiusSquared = mWheelBounds!!.radius * mWheelBounds!!.radius
                val touchRadiusSquared =
                    mRadiusVector.x * mRadiusVector.x + mRadiusVector.y * mRadiusVector.y
                val touchFactor =
                    TOUCH_FACTORS[(touchRadiusSquared / wheelRadiusSquared * TOUCH_FACTORS.size).toInt()]
                val touchAngle = mWheelBounds!!.angleToDegrees(x, y)
                val draggedAngle = -1f * shortestAngle(touchAngle, mLastTouchAngle) * touchFactor
                addAngle(draggedAngle)
                mLastTouchAngle = touchAngle
                mDraggedAngle += draggedAngle
                if (mRequiresUpdate) {
                    mRequiresUpdate = false
                }
            }
        }
        return true
    }

    private fun startWheelDrag(event: MotionEvent, x: Float, y: Float) {
        mIsDraggingWheel = true
        mDraggedAngle = 0f
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain()
        } else {
            mVelocityTracker!!.clear()
        }
        mVelocityTracker!!.addMovement(event)
        mAngularVelocity = 0f
        mLastTouchAngle = mWheelBounds!!.angleToDegrees(x, y)
    }

    private fun flingWheel() {
        mIsDraggingWheel = false
        mVelocityTracker!!.computeCurrentVelocity(1)

        //torque = r X F
        mForceVector[mVelocityTracker!!.xVelocity] = mVelocityTracker!!.yVelocity
        setRadiusVector(mLastWheelTouchX, mLastWheelTouchY)
        val torque = mForceVector.crossProduct(mRadiusVector)

        //dw/dt = torque / I = torque / mr^2
        val wheelRadiusSquared = mWheelBounds!!.radius * mWheelBounds!!.radius
        val angularAccel = torque / wheelRadiusSquared

        //estimate an angular velocity based on the strength of the angular acceleration
        var angularVel = angularAccel * ANGULAR_VEL_COEFFICIENT

        //clamp the angular velocity
        if (angularVel > MAX_ANGULAR_VEL) angularVel =
            MAX_ANGULAR_VEL else if (angularVel < -MAX_ANGULAR_VEL) angularVel = -MAX_ANGULAR_VEL
        mAngularVelocity = angularVel
        mLastUpdateTime = SystemClock.uptimeMillis()
        mRequiresUpdate = true
        invalidate()
    }

    private fun setRadiusVector(x: Float, y: Float) {
        val rVectorX = mWheelBounds!!.centerX - x
        val rVectorY = mWheelBounds!!.centerY - y
        mRadiusVector[rVectorX] = rVectorY
    }

    /**
     * Converts the raw position to a position within the adapter bounds.
     *
     * @see .rawPositionToWheelPosition
     * @see .rawPositionToWheelPosition
     */
    fun rawPositionToAdapterPosition(position: Int): Int {
        return if (isRepeatableAdapter) clamp(position, mAdapterItemCount) else position
    }
    /**
     * Converts the raw position to a position within the wheel item bounds.
     *
     * @see .rawPositionToAdapterPosition
     * @see .rawPositionToWheelPosition
     */
    /**
     * Converts the raw position to a position within the wheel item bounds.
     *
     * @see .rawPositionToAdapterPosition
     * @see .rawPositionToWheelPosition
     */
    @JvmOverloads
    fun rawPositionToWheelPosition(
        position: Int,
        adapterPosition: Int = rawPositionToAdapterPosition(position)
    ): Int {
        val circularOffset =
            if (isRepeatableAdapter) {
                floor((position / mAdapterItemCount.toFloat()).toDouble())
                    .toInt() * (mAdapterItemCount - mItemCount)
            } else {
                0
            }
        return clamp(adapterPosition + circularOffset, mItemCount)
    }

    /**
     * Estimates the wheel's new angle and angular velocity
     */
    private fun update(deltaTime: Float) {
        val vel = mAngularVelocity
        val velSqr = vel * vel
        if (vel > 0f) {
            //TODO the damping is not based on time
            mAngularVelocity -= velSqr * VELOCITY_FRICTION_COEFFICIENT + CONSTANT_FRICTION_COEFFICIENT
            if (mAngularVelocity < 0f) mAngularVelocity = 0f
        } else if (vel < 0f) {
            mAngularVelocity -= velSqr * -VELOCITY_FRICTION_COEFFICIENT - CONSTANT_FRICTION_COEFFICIENT
            if (mAngularVelocity > 0f) mAngularVelocity = 0f
        }
        if (mAngularVelocity != 0f) {
            addAngle(mAngularVelocity * deltaTime)
        } else {
            mRequiresUpdate = false
        }
    }

    private fun updateWheelStateIfReq() {
        if (!mRequiresUpdate) return
        val currentTime = SystemClock.uptimeMillis()
        val timeDiff = currentTime - mLastUpdateTime
        mLastUpdateTime = currentTime
        update(timeDiff.toFloat())
    }

    override fun onDraw(canvas: Canvas) {
        updateWheelStateIfReq()
        if (mWheelBounds == null) return  //issue with layoutWheel not being called before draw call
        if (wheelDrawable != null) {
            drawWheel(canvas)
        }
        if (mAdapter != null && mAdapterItemCount > 0) {
            drawWheelItems(canvas)
        }
    }

    private fun drawWheel(canvas: Canvas) {
        if (mIsWheelDrawableRotatable) {
            canvas.save()
            canvas.rotate(mAngle, mWheelBounds!!.centerX, mWheelBounds!!.centerY)
            wheelDrawable!!.draw(canvas)
            canvas.restore()
        } else {
            wheelDrawable!!.draw(canvas)
        }
    }

    private fun drawWheelItems(canvas: Canvas) {
        val angleInRadians = Math.toRadians(mAngle.toDouble())
        val cosAngle = cos(angleInRadians)
        val sinAngle = sin(angleInRadians)
        val centerX = mWheelBounds!!.centerX
        val centerY = mWheelBounds!!.centerY
        val wheelItemOffset = mItemCount / 2
        val offset = rawSelectedPosition - wheelItemOffset
        val length = mItemCount + offset
        for (i in offset until length) {
            val adapterPosition = rawPositionToAdapterPosition(i)
            val wheelItemPosition = rawPositionToWheelPosition(i, adapterPosition)
            val itemBounds = mWheelItemBounds!![wheelItemPosition]
            val radius = itemBounds.radius

            //translate before rotating so that origin is at the wheel's center
            val x = itemBounds.centerX - centerX
            val y = itemBounds.centerY - centerY

            //rotate
            var x1 = (x * cosAngle - y * sinAngle).toFloat()
            var y1 = (x * sinAngle + y * cosAngle).toFloat()

            //translate back after rotation
            x1 += centerX
            y1 += centerY
            val itemState = mItemStates!![wheelItemPosition]
            updateItemState(itemState, adapterPosition, x1, y1, radius)
            mItemTransformer!!.transform(itemState, sTempRect)

            //Empty positions can only occur from having "non repeatable" items
            val cacheItem = getCacheItem(adapterPosition)

            //don't draw if outside of the view bounds
            if (Rect.intersects(sTempRect, mViewBounds)) {
                if (cacheItem.mDirty && !cacheItem.mIsEmpty) {
                    cacheItem.mDrawable = mAdapter!!.getDrawable(adapterPosition)
                    cacheItem.mDirty = false
                }
                if (!cacheItem.mIsVisible) {
                    cacheItem.mIsVisible = true
                    if (onItemVisibilityChangeListener != null) {
                        onItemVisibilityChangeListener!!.onItemVisibilityChange(
                            mAdapter,
                            adapterPosition,
                            true
                        )
                    }
                }
                if (i == rawSelectedPosition && selectionDrawable != null && !isEmptyItemPosition(i)) {
                    selectionDrawable!!.setBounds(
                        sTempRect.left - mSelectionPadding,
                        sTempRect.top - mSelectionPadding,
                        sTempRect.right + mSelectionPadding,
                        sTempRect.bottom + mSelectionPadding
                    )
                    mSelectionTransformer!!.transform(selectionDrawable!!, itemState)
                    selectionDrawable!!.draw(canvas)
                }
                val drawable = cacheItem.mDrawable
                if (drawable != null) {
                    drawable.bounds = sTempRect
                    drawable.draw(canvas)
                }
            } else {
                if (cacheItem != null && cacheItem.mIsVisible) {
                    cacheItem.mIsVisible = false
                    if (onItemVisibilityChangeListener != null) {
                        onItemVisibilityChangeListener!!.onItemVisibilityChange(
                            mAdapter,
                            adapterPosition,
                            false
                        )
                    }
                }
            }
        }
    }

    /**
     * The ItemState is used to provide extra information when transforming the selection drawable
     * or item bounds.
     */
    class ItemState {
        var wheelView: WheelView? = null
        var bounds: Circle = Circle()
        var angleFromSelection = 0f
        var relativePosition = 0f
        var mAdapterPosition = 0

    }

    private fun updateItemState(
        itemState: ItemState, adapterPosition: Int,
        x: Float, y: Float, radius: Float
    ) {
        val itemAngle = mWheelBounds!!.angleToDegrees(x, y)
        val angleFromSelection = shortestAngle(itemAngle, selectionPadding)
        val relativePos = angleFromSelection / wheelItemAnglePadding * 2f
        itemState.angleFromSelection = angleFromSelection
        itemState.relativePosition = relativePos
        itemState.bounds.centerX = x
        itemState.bounds.centerY = y
        itemState.mAdapterPosition = adapterPosition

        //TODO The radius is always known - doesn't really need this?
        itemState.bounds.radius = radius
    }

    private fun getClickedItem(touchX: Float, touchY: Float): ItemState? {
        for (state in mItemStates!!) {
            val itemBounds = state.bounds
            if (itemBounds.contains(touchX, touchY)) return state
        }
        return null
    }

    internal class CacheItem() {
        var mDirty = true
        var mIsVisible = false
        var mIsEmpty = false
        var mDrawable: Drawable? = null

        constructor(isEmpty: Boolean) : this() {
            mIsEmpty = isEmpty
        }
    }

    private fun getCacheItem(position: Int): CacheItem {
        if (isEmptyItemPosition(position)) return EMPTY_CACHE_ITEM
        var cacheItem = mItemCacheArray[position]
        if (cacheItem == null) {
            cacheItem = CacheItem()
            mItemCacheArray[position] = cacheItem
        }
        return cacheItem
    }

    /**
     * A simple class to represent a vector with an add and cross product method. Used only to
     * calculate the Wheel's angular velocity in [.flingWheel]
     */
    internal class Vector {
        var x = 0f
        var y = 0f
        operator fun set(x: Float, y: Float) {
            this.x = x
            this.y = y
        }

        fun crossProduct(vector: Vector): Float {
            return x * vector.y - y * vector.x
        }

        override fun toString(): String {
            return "Vector: ($x, $y)"
        }
    }
}
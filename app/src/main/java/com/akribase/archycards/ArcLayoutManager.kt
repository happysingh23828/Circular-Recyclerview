package com.akribase.archycards

import android.content.res.Resources
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.core.math.MathUtils
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.acos
import kotlin.math.floor
import kotlin.math.sin


class ArcLayoutManager(
    resources: Resources,
    private val screenWidth: Int,
    private val viewWidth: Int,
    private val viewHeight: Int,
) : RecyclerView.LayoutManager() {

    private var horizontalScrollOffset = viewWidth
    var scrollEnabled = true

    private val recyclerViewHeight =
        (resources.getDimensionPixelSize(R.dimen.recyclerview_height)).toDouble()

    override fun generateDefaultLayoutParams(): RecyclerView.LayoutParams =
        RecyclerView.LayoutParams(MATCH_PARENT, MATCH_PARENT)

    override fun onLayoutChildren(recycler: RecyclerView.Recycler, state: RecyclerView.State) {
        fill(recycler, state)
    }

    private fun fill(recycler: RecyclerView.Recycler, state: RecyclerView.State) {
        detachAndScrapAttachedViews(recycler)

        // Looping
        val firstVisiblePosition =
            floor(horizontalScrollOffset.toDouble() / viewWidth.toDouble()).toInt()
        val lastVisiblePosition = (horizontalScrollOffset + screenWidth) / viewWidth

        for (index in firstVisiblePosition..lastVisiblePosition) {
            var recyclerIndex = index % itemCount
            if (recyclerIndex < 0) {
                recyclerIndex += itemCount
            }
            val view = recycler.getViewForPosition(recyclerIndex)
            addView(view)

            layoutChildView(index, viewWidth, view)

        }

        // Remove scrap views
        val scrapListCopy = recycler.scrapList.toList()
        scrapListCopy.forEach {
            recycler.recycleView(it.itemView)
        }
    }

    private fun layoutChildView(i: Int, viewWidthWithSpacing: Int, view: View) {
        val left = i * viewWidthWithSpacing - horizontalScrollOffset
        val right = left + viewWidth

        val viewCentreX = left + viewWidth / 2
        val s: Double = screenWidth.toDouble() / 2
        val h: Double = recyclerViewHeight
        val radius: Double = (h * h + s * s) / (h * 2)
        val cosAlpha = (s - viewCentreX) / radius
        val alpha = acos(MathUtils.clamp(cosAlpha, -1.0, 1.0))
        val yComponent = radius - (radius * sin(alpha))

        val top = (h + yComponent - recyclerViewHeight).toInt()
        val bottom = viewHeight + recyclerViewHeight

        // Measure
        measureChild(view, viewWidth, viewHeight + 200)
        // Layout
        layoutDecorated(view, left, top, right, bottom.toInt())
    }

    override fun canScrollHorizontally() = scrollEnabled

    override fun scrollHorizontallyBy(
        dx: Int,
        recycler: RecyclerView.Recycler,
        state: RecyclerView.State
    ): Int {
        horizontalScrollOffset += dx
        fill(recycler, state)
        return dx
    }
}
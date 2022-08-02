package com.akribase.archycards

import android.content.res.Resources
import android.util.Log
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
    private val viewWidth: Double,
    private val viewHeight: Double,
) : RecyclerView.LayoutManager() {

    init {
        Log.d("UNUN", "ScreenWidth : $screenWidth viewWIdth: $viewWidth ViewHeight : $viewHeight")
        Log.d("UNUN", "======>>======")
    }

    private var horizontalScrollOffset = viewWidth / 2
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
            floor(horizontalScrollOffset / viewWidth).toInt()
        val lastVisiblePosition = ((horizontalScrollOffset + screenWidth) / viewWidth).toInt()

        Log.d("UNUN", "FVP : $firstVisiblePosition LVP: $lastVisiblePosition")
        Log.d("UNUN", "======>>======")

        for (index in firstVisiblePosition..lastVisiblePosition) {
            var recyclerIndex = index % itemCount
            if (recyclerIndex < 0) {
                recyclerIndex += itemCount
            }
            val view = recycler.getViewForPosition(recyclerIndex)
            addView(view)

            layoutChildView(index, viewWidth, view)
            Log.d("UNUN", "layoutChildView Index : $recyclerIndex")
            Log.d("UNUN", "======>>======")
        }

        // Remove scrap views
        val scrapListCopy = recycler.scrapList.toList()
        scrapListCopy.forEach {
            recycler.recycleView(it.itemView)
        }
    }

    private fun layoutChildView(i: Int, viewWidthWithSpacing: Double, view: View) {
        val left = i * viewWidthWithSpacing - horizontalScrollOffset
        val right = left + viewWidth

        val viewCentreX = left + viewWidth / 2
        val s: Double = screenWidth.toDouble() / 2
        val h: Double = recyclerViewHeight
        val radius: Double = (h * h + s * s) / (h * 2)
        val cosAlpha = (s - viewCentreX) / radius
        val alpha = acos(MathUtils.clamp(cosAlpha, -1.0, 1.0))
        val yComponent = radius - (radius * sin(alpha))

        Log.d(
            "UNUN",
            "Raidus $radius, cosAlpha : $cosAlpha, alpha $alpha, yComponent $yComponent  "
        )
        Log.d("UNUN", "======>>======")
        val top = (h + yComponent - recyclerViewHeight).toInt()
        val bottom = viewHeight + recyclerViewHeight

        // Measure
        measureChild(view, viewWidth.toInt(), (viewHeight + 200).toInt())
        // Layout
        layoutDecorated(view, left.toInt(), top, right.toInt(), bottom.toInt())

        Log.d(
            "UNUN",
            "Top: $top Bottom : $bottom Right : $right Left : $left, s  ${s}, h $h, viewCenterX : $viewCentreX"
        )
        Log.d("UNUN", "======>>======")
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
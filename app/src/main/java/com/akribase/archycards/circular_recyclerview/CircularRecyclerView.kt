package com.akribase.archycards.circular_recyclerview

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import android.view.animation.CycleInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearSnapHelper
import com.akribase.archycards.databinding.LayoutCircularViewBinding
import com.akribase.archycards.lib.CenterSnapHelper
import com.akribase.archycards.lib.CircleScaleLayoutManager
import com.akribase.archycards.lib.ScrollHelper
import com.akribase.archycards.lib.ViewPagerLayoutManager
import kotlinx.coroutines.*

class CircularRecyclerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr), DefaultLifecycleObserver {

    private var binding: LayoutCircularViewBinding
    private var adapter = CircularRecyclerAdapter()
    private lateinit var snapHelper: CenterSnapHelper
    private lateinit var layoutManager: ViewPagerLayoutManager

    init {
        binding = LayoutCircularViewBinding.inflate(LayoutInflater.from(context), this)
        initRecyclerview()
    }

    private fun initRecyclerview() {
        binding.rv.post {
            setLayoutManager()
            snapHelper = CenterSnapHelper()
            snapHelper.attachToRecyclerView(binding.rv)
            binding.rv.adapter = adapter
        }
    }

    private fun setLayoutManager() {
        val screenWidth = binding.root.width
        layoutManager = CircleScaleLayoutManager
            .Builder(context)
            .setRadius((screenWidth / 1.50).toInt())
            .setReverseLayout(true)
            .setMaxVisibleItemCount(MAX_VISIBLE_ITEMS_COUNT)
            .build()
        layoutManager.setItemRotation(false)
        layoutManager.infinite = true
        binding.rv.layoutManager = layoutManager
    }

    fun registerLifecycleOwner(lifecycle: Lifecycle) {
        lifecycle.addObserver(this)
    }

    fun createItems(listOfItems: List<Item>) {
        adapter.updateList(listOfItems.toMutableList())
    }

    fun appendItems(listOfItems: List<Item>) {
        adapter.appendList(listOfItems.toMutableList())
    }

    /**
     * Scrolling methods
     * [startAutoScroll] for starting infinite scrolling
     * [stopAutoScroll] for stopping infinite scrolling
     * [animateAndSelectItem] for selecting particular index and scroll to it.
     */
    private var isAutoScrolling = false

    private var autoScrollJob: Job? = null

    fun startAutoScroll() {
        if (isAutoScrolling.not()) {

            // reset auto scroll
            isAutoScrolling = true
            autoScrollJob?.cancel()


            val screenWidth = binding.root.width
            autoScrollJob = CoroutineScope(Dispatchers.IO).launchPeriodicAsync(AUTO_SCROLL_MILLIS) {
                if (isAutoScrolling) {
                    binding.rv.smoothScrollBy(
                        -screenWidth,
                        0,
                        DecelerateInterpolator(),
                        AUTO_SCROLL_MILLIS.toInt() + 1300 // delta
                    )
                }
            }
        }
    }

    fun stopAutoScroll() {
        isAutoScrolling = false
        autoScrollJob?.cancel()
    }

    fun animateAndSelectItem(position: Int) {
        stopAutoScroll()

        var targetPosition = layoutManager.currentPosition + INDEX_OFFSET_FOR_FINDING


        if (targetPosition >= layoutManager.itemCount) {
            targetPosition = INDEX_OFFSET_FOR_FINDING
        }

        adapter.updateItemOnList(position, targetPosition)

        ScrollHelper.smoothScrollToPosition(
            binding.rv,
            layoutManager,
            targetPosition,
            DecelerateInterpolator(),
            getScrollDurationForIndexOffsetForFinding()
        )
    }


    override fun onDestroy(owner: LifecycleOwner) {
        stopAutoScroll()
        super.onDestroy(owner)
    }

    /**
     * Utils
     */
    private fun getScrollXDurationForFullScreenWidth(): Int {
        return AUTO_SCROLL_MILLIS.toInt() + 1300
    }

    private fun getScrollDurationForIndexOffsetForFinding(): Int {
        return getScrollXDurationForFullScreenWidth() * MAX_VISIBLE_ITEMS_COUNT - 1
    }

    data class Item(
        val id: String,
        val borderColor: Int,
        val imageUrl: String
    )

    companion object {
        private const val AUTO_SCROLL_MILLIS = 100L // scroll x offset in n mills
        private const val MAX_VISIBLE_ITEMS_COUNT = 6 // scroll x offset in n mills
        private const val INDEX_OFFSET_FOR_FINDING = 20 // scroll x offset in n mills
    }
}

fun CoroutineScope.launchPeriodicAsync(
    repeatMillis: Long,
    action: () -> Unit
) = this.async {
    if (repeatMillis > 0) {
        while (isActive) {
            action()
            delay(repeatMillis)
        }
    } else {
        action()
    }
}


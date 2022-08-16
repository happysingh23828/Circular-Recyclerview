package com.akribase.archycards.circular_recyclerview

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
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

    var binding: LayoutCircularViewBinding
    private var adapter = CircularRecyclerAdapter()
    private lateinit var snapHelper: CenterSnapHelper
    private lateinit var layoutManager: ViewPagerLayoutManager

    init {
        binding = LayoutCircularViewBinding.inflate(LayoutInflater.from(context), this)
        initRecyclerview()
    }

    fun registerLifecycleOwner(lifecycle: Lifecycle) {
        lifecycle.addObserver(this)
    }

    private fun initRecyclerview() {
        binding.rv.post {
            setLayoutManager()
            snapHelper = CenterSnapHelper()
            setPageChangeListener()
            snapHelper.attachToRecyclerView(binding.rv)
            binding.rv.adapter = adapter
        }
    }

    private fun setLayoutManager() {
        val screenWidth = binding.root.width
        layoutManager = CircleScaleLayoutManager
            .Builder(context)
            .setRadius((screenWidth / 1.50).toInt())
            .setMaxVisibleItemCount(6)
            .build()
        layoutManager.setItemRotation(false)
        layoutManager.infinite = true
        binding.rv.layoutManager = layoutManager
    }

    fun createItems(listOfItems: List<Item>) {
        adapter.updateList(listOfItems.toMutableList())
    }

    fun appendItems(listOfItems: List<Item>) {
        adapter.appendList(listOfItems.toMutableList())
    }

    fun animateAndSelectItem(position: Int, duration: Int) {
        ScrollHelper.smoothScrollToPosition(
            binding.rv,
            layoutManager,
            position,
            AccelerateDecelerateInterpolator(),
            duration
        )
    }

    data class Item(
        val id: String,
        val borderColor: Int,
        val imageUrl: String
    )

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
        // reset auto scroll
        isAutoScrolling = false
        autoScrollJob?.cancel()
    }

    private fun setPageChangeListener() {
        layoutManager.setOnPageChangeListener(object : ViewPagerLayoutManager.OnPageChangeListener {
            override fun onPageSelected(var1: Int) {
                Log.d(CircularRecyclerView::class.java.name, "onPageSelected: $var1")
            }

            override fun onPageScrollStateChanged(var1: Int) {
                Log.d(CircularRecyclerView::class.java.name, "onPageScrollStateChanged: $var1")
            }
        })
    }

    override fun onDestroy(owner: LifecycleOwner) {
        stopAutoScroll()
        super.onDestroy(owner)
    }

    companion object {
        private const val AUTO_SCROLL_MILLIS = 100L // scroll x offset in n mills
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


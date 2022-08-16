package com.akribase.archycards.circular_recyclerview

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearSnapHelper
import com.akribase.archycards.databinding.LayoutCircularViewBinding
import com.akribase.archycards.lib.CircleScaleLayoutManager
import com.akribase.archycards.lib.ScrollHelper
import com.akribase.archycards.lib.ViewPagerLayoutManager

class CircularRecyclerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr), DefaultLifecycleObserver {

    var binding: LayoutCircularViewBinding
    private var adapter = CircularRecyclerAdapter()
    private lateinit var snapHelper: LinearSnapHelper
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
            snapHelper = LinearSnapHelper()
            snapHelper.attachToRecyclerView(binding.rv)
            val snapOnScrollListener = SnapOnScrollListener(snapHelper)
            binding.rv.addOnScrollListener(snapOnScrollListener)
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

    private var isAutoScrolling = false

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
    }

    override fun onPause(owner: LifecycleOwner) {
        super.onPause(owner)
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
    }

    data class Item(
        val id: String,
        val borderColor: Int,
        val imageUrl: String
    )
}


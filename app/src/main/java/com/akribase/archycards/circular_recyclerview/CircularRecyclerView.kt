package com.akribase.archycards.circular_recyclerview

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.annotation.ColorRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearSnapHelper
import com.akribase.archycards.databinding.LayoutCircularViewBinding

class CircularRecyclerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    var binding: LayoutCircularViewBinding
    private var adapter = CircularRecyclerAdapter()
    private lateinit var snapHelper: LinearSnapHelper

    init {
        binding = LayoutCircularViewBinding.inflate(LayoutInflater.from(context), this)
        initRecyclerview()
    }

    private fun initRecyclerview() {
        binding.rv.post {
            val screenWidth = binding.root.width
            binding.rv.layoutManager = ArcLayoutManager(resources, screenWidth)
            snapHelper = LinearSnapHelper()
            snapHelper.attachToRecyclerView(binding.rv)
            val snapOnScrollListener = SnapOnScrollListener(snapHelper)
            binding.rv.addOnScrollListener(snapOnScrollListener)
            binding.rv.adapter = adapter
        }
    }

    fun createItems(listOfItems: List<Item>) {
        adapter.updateList(listOfItems.toMutableList())
    }

    fun appendItems(listOfItems: List<Item>) {
        adapter.appendList(listOfItems.toMutableList())
    }

    fun animateAndSelectItem(position: Int, duration: Int) {
        // TODO calculate dX for position to add smooth Scroll
        binding.rv.smoothScrollBy(2000, 0, AccelerateDecelerateInterpolator(), duration)
    }


    data class Item(
        val id: String,
        @ColorRes val borderColor: Int,
        val imageUrl: String
    )
}



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

    init {
        binding = LayoutCircularViewBinding.inflate(LayoutInflater.from(context), this)
        initRecyclerview()
    }

    private fun initRecyclerview() {

    }

    fun createItems(listOfItems: List<Item>) {
        binding.wheelView.adapter = CircularWheelAdapter(listOfItems, context)
    }

    fun appendItems(listOfItems: List<Item>) {
        binding.wheelView.adapter = CircularWheelAdapter(listOfItems, context)
    }

    fun animateAndSelectItem(position: Int, duration: Int) {
        binding.wheelView.setSelected(position, true)
    }

    data class Item(
        val id: String,
        val borderColor: Int,
        val imageUrl: String
    )
}



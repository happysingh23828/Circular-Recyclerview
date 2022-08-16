package com.akribase.archycards.circular_recyclerview

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SnapHelper
import com.akribase.archycards.databinding.ItemCircularViewBinding
import com.bumptech.glide.Glide

class CircularRecyclerAdapter : RecyclerView.Adapter<CircularRecyclerAdapter.CircularViewHolder>() {

    private val listOfItems: MutableList<CircularRecyclerView.Item> = mutableListOf()

    fun appendList(listOfItems: MutableList<CircularRecyclerView.Item>) {
        this.listOfItems.addAll(listOfItems)
        notifyDataSetChanged()
    }

    fun updateList(listOfItems: MutableList<CircularRecyclerView.Item>) {
        this.listOfItems.clear()
        this.listOfItems.addAll(listOfItems)
        notifyDataSetChanged()
    }

    fun updateItemOnList(selectedPosition: Int, targetPosition: Int) {
        val itemToReplace = listOfItems[selectedPosition]
        this.listOfItems[targetPosition] = itemToReplace
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CircularViewHolder {
        return CircularViewHolder(
            ItemCircularViewBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: CircularViewHolder, position: Int) {
        holder.bind(listOfItems[position])
    }

    override fun getItemCount(): Int {
        return listOfItems.size
    }

    inner class CircularViewHolder(private val binding: ItemCircularViewBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(circularItem: CircularRecyclerView.Item) {
            Glide.with(binding.root.context).load(circularItem.imageUrl).into(binding.imageView)
            binding.ivPosition.text = adapterPosition.toString()
        }
    }

}

class SnapOnScrollListener(
    private val snapHelper: SnapHelper
) : RecyclerView.OnScrollListener() {
    private val rvSnapPos = MutableLiveData(RecyclerView.NO_POSITION)

    val snapPosition: LiveData<Int>
        get() = rvSnapPos

    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
        if (newState == RecyclerView.SCROLL_STATE_IDLE) {
            maybeNotifySnapPositionChange(recyclerView)
        }
    }

    private fun maybeNotifySnapPositionChange(recyclerView: RecyclerView) {
        val snapPosition = snapHelper.getSnapPosition(recyclerView)
        val snapPositionChanged = rvSnapPos.value != snapPosition
        if (snapPositionChanged) {
            rvSnapPos.value = snapPosition
        }
    }
}

fun SnapHelper.getSnapPosition(recyclerView: RecyclerView): Int {
    val layoutManager = recyclerView.layoutManager ?: return RecyclerView.NO_POSITION
    val snapView = findSnapView(layoutManager) ?: return RecyclerView.NO_POSITION
    return layoutManager.getPosition(snapView)
}
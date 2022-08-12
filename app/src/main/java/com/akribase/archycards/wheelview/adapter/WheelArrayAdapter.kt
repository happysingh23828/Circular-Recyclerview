package com.akribase.archycards.wheelview.adapter

abstract class WheelArrayAdapter<T>(private val mItems: List<T>) : WheelAdapter {
    fun getItem(position: Int): T {
        return mItems[position]
    }

    override val count: Int
        get() = mItems.size
}
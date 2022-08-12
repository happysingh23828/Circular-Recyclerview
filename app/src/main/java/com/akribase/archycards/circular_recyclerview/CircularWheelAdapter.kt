package com.akribase.archycards.circular_recyclerview

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import com.akribase.archycards.R
import com.akribase.archycards.wheelview.adapter.WheelArrayAdapter
import com.bumptech.glide.Glide
import de.hdodenhof.circleimageview.CircleImageView


class CircularWheelAdapter(list: List<CircularRecyclerView.Item?>, private val context: Context) :
    WheelArrayAdapter<CircularRecyclerView.Item?>(list) {
    private var holder: ViewHolder? = null
    private val myInflater: LayoutInflater = LayoutInflater.from(context)
    override fun getDrawable(position: Int): Drawable {
        var convertView: View? = null
        if (convertView == null) {
            convertView = myInflater.inflate(R.layout.item_circular_view, null)
            holder = ViewHolder()
            holder!!.avatar =
                convertView.findViewById<View>(R.id.imageView) as CircleImageView
            convertView.tag = holder
        } else {
            holder = convertView.tag as ViewHolder
        }

        val data = getItem(position) as CircularRecyclerView.Item

        /*try {
            Glide.with(context).load(data.imageUrl).into(holder!!.avatar!!)
        } catch (ex : Exception) {
            holder!!.avatar!!.setImageDrawable(context.getDrawable(R.drawable.p0))
        }*/
        holder!!.avatar!!.setImageDrawable(context.getDrawable(R.drawable.p0))
        holder!!.avatar!!.borderColor = context.resources.getColor(data.borderColor)
        convertView!!.isDrawingCacheEnabled = true
        convertView.measure(
            View.MeasureSpec.makeMeasureSpec(
                0,
                View.MeasureSpec.UNSPECIFIED
            ),
            View.MeasureSpec.makeMeasureSpec(
                0,
                View.MeasureSpec.UNSPECIFIED
            )
        )
        convertView.layout(0, 0, convertView.measuredWidth, convertView.measuredHeight)
        convertView.buildDrawingCache(true)
        val bitmap = Bitmap.createBitmap(convertView.drawingCache)
        convertView.isDrawingCacheEnabled = false
        return BitmapDrawable(context.resources, bitmap)
    }

    inner class ViewHolder {
        var avatar: CircleImageView? = null
    }

}
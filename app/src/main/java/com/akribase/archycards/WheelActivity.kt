package com.akribase.archycards

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.akribase.archycards.databinding.ActivityWheelActivityBinding
import com.akribase.archycards.wheelview.adapter.WheelArrayAdapter
import com.bumptech.glide.Glide
import de.hdodenhof.circleimageview.CircleImageView

class WheelActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWheelActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWheelActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        showWheelCircle()
    }

    private fun showWheelCircle() {
        //create data for the adapter

        //create data for the adapter
        val entries: MutableList<WheelData> = ArrayList(150)
        for (i in 0 until 100) {
            val data = getDataForPosition(i)
            entries.add(
                WheelData(
                    borderHex = data.first,
                    imageUrl = data.second
                )
            )
        }

        binding.wheelview.adapter = WheelListAdapter(entries, this)

        binding.btnMatch.setOnClickListener {
            binding.wheelview.setSelected(50, true)
        }
    }


    private fun getDataForPosition(position: Int): Pair<String, Int> {
        return when (position % 4) {
            1 -> Pair("#0CB1B1", R.drawable.p0)
            2 -> Pair("#FFD953", R.drawable.p1)
            3 -> Pair("#34A4FF", R.drawable.p2)
            else -> Pair("#FFD953", R.drawable.p3)
        }
    }
}

class WheelListAdapter(list: List<WheelData?>, private val context: Context) :
    WheelArrayAdapter<WheelData?>(list) {
    var holder: ViewHolder? = null
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

        val data = getItem(position) as WheelData

//        Glide.with(context).load(data.imageUrl).into(holder!!.avatar!!)
        holder!!.avatar!!.setImageDrawable(context.getDrawable(data.imageUrl))
        holder!!.avatar!!.borderColor = Color.parseColor(data.borderHex)
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

data class WheelData(
    val borderHex: String,
    val imageUrl: Int
)

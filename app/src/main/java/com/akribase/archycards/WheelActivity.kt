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
        val entries: MutableList<WheelData> = ArrayList(90)
        for (i in 0 until 100) {
            entries.add(
                WheelData(
                    borderHex = "#FFD953",
                    imageUrl = "https://picsum.photos/200/200"
                )
            )
        }

        binding.wheelview.adapter = WheelListAdapter(entries, this)

        binding.btnMatch.setOnClickListener {
//            binding.wheelview.adapter = WheelListAdapter(entries, this)
            binding.wheelview.setSelected(30)
        }
    }
}

class WheelListAdapter(list: List<WheelData?>?, private val context: Context) :
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

        Glide.with(context).load(data.imageUrl).into(holder!!.avatar!!)
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
    val imageUrl: String
)

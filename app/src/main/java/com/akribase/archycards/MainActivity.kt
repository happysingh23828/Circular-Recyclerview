package com.akribase.archycards

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SnapHelper
import com.akribase.archycards.circular_recyclerview.CircularRecyclerView
import com.akribase.archycards.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initRv(binding.rv)
    }

    private fun initRv(rv: CircularRecyclerView) {

        val arrayOfPhotos = arrayListOf(
            237,
            433,
            577,
            582,
            593,
            659,
            718,
            783,
            790,
            837,
            1024,
            1025,
            1074,
            1084
        );

        val listOfItems = mutableListOf<CircularRecyclerView.Item>()
        for (i in 0..100) {
            listOfItems.add(
                CircularRecyclerView.Item(
                    borderColor = R.color.teal_200,
                    imageUrl = "https://picsum.photos/id/${arrayOfPhotos[i.rem(10)]}/300/300",
                    id = i.toString()
                )
            )
        }
        binding.rv.createItems(listOfItems)
        rv.createItems(listOfItems)
        binding.btnScroll.setOnClickListener {
            binding.rv.animateAndSelectItem(60, 4000)
        }
    }

}

fun SnapHelper.getSnapPosition(recyclerView: RecyclerView): Int {
    val layoutManager = recyclerView.layoutManager ?: return RecyclerView.NO_POSITION
    val snapView = findSnapView(layoutManager) ?: return RecyclerView.NO_POSITION
    return layoutManager.getPosition(snapView)
}
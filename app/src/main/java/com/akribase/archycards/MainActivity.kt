package com.akribase.archycards

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SnapHelper
import com.akribase.archycards.circular_recyclerview.ArcLayoutManager
import com.akribase.archycards.circular_recyclerview.CircularRecyclerView
import com.akribase.archycards.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    private lateinit var snapHelper: LinearSnapHelper
    private lateinit var layoutManager: ArcLayoutManager
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initRv(binding.rv)
    }

    private fun initRv(rv: CircularRecyclerView) {
        val rewards = listOf(
            R.drawable.p0,
            R.drawable.p1,
            R.drawable.p2,
            R.drawable.p3,
            R.drawable.p0,
            R.drawable.p1,
            R.drawable.p2,
            R.drawable.p3,
            R.drawable.p0,
            R.drawable.p1,
            R.drawable.p2,
            R.drawable.p3,
            R.drawable.p0,
            R.drawable.p1,
            R.drawable.p2,
            R.drawable.p3,
        )

        val listOfItems = rewards.mapIndexed { index, i ->
            CircularRecyclerView.Item(
                id = index.toString(),
                imageUrl = "sdsd",
                borderColor = R.color.p1
            )
        }

        rv.createItems(listOfItems)

        binding.btnScroll.setOnClickListener {
            binding.rv.animateAndSelectItem(15, 4000)
        }
    }

}

fun SnapHelper.getSnapPosition(recyclerView: RecyclerView): Int {
    val layoutManager = recyclerView.layoutManager ?: return RecyclerView.NO_POSITION
    val snapView = findSnapView(layoutManager) ?: return RecyclerView.NO_POSITION
    return layoutManager.getPosition(snapView)
}
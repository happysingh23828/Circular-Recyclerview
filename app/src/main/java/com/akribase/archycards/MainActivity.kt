package com.akribase.archycards

import android.content.res.Resources
import android.os.Bundle
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.akribase.archycards.arc.ArcControl
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

    private fun initRv(rv: RecyclerView) {
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

        setArcLayoutManager()
//        setArcLayoutManager2()
        rv.adapter = RewardsAdapter(rewards)

        snapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(rv)

        val snapOnScrollListener = SnapOnScrollListener(snapHelper)
        rv.addOnScrollListener(snapOnScrollListener)


        binding.btnScroll.setOnClickListener {
            rv.smoothScrollBy(10000, 0, AccelerateDecelerateInterpolator(), 7000)
        }
    }

    fun setArcLayoutManager() {
        val screenWidth = Resources.getSystem().displayMetrics.widthPixels
        binding.rv.layoutManager =
            ArcLayoutManager(resources, screenWidth, screenWidth / 3.4, screenWidth / 3.4).apply {
                layoutManager = this
            }
    }

    fun setArcLayoutManager2() {
        val screenWidth = Resources.getSystem().displayMetrics.widthPixels
        val arcControl = ArcControl(screenWidth - 500, 30f, 0f, 30f, -360, 360)
        val arcLayoutManager = com.akribase.archycards.arc.ArcLayoutManager(arcControl)
        binding.rv.layoutManager = arcLayoutManager
    }

    override fun onResume() {
        super.onResume()
        binding.rv.smoothScrollBy(1, 0)
    }

}
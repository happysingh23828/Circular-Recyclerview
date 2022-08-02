package com.akribase.archycards

import android.animation.ObjectAnimator
import android.content.res.Resources
import android.graphics.drawable.GradientDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import android.view.animation.BounceInterpolator
import android.view.animation.LinearInterpolator
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.akribase.archycards.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var snapHelper: LinearSnapHelper
    private lateinit var layoutManager: ArcLayoutManager
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        initRv(binding.rv)
    }

    private fun initRv(rv: RecyclerView) {
        val rewards = listOf(R.drawable.reward1, R.drawable.reward2, R.drawable.reward3)
        val screenWidth = Resources.getSystem().displayMetrics.widthPixels
        val viewWidth = screenWidth / 4
        val viewHeight = viewWidth

        rv.adapter = RewardsAdapter(rewards, viewWidth, viewHeight)
        rv.layoutManager = ArcLayoutManager(resources, screenWidth, viewWidth, viewHeight).apply {
            layoutManager = this
        }


        snapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(rv)

        val snapOnScrollListener = SnapOnScrollListener(snapHelper)
        rv.addOnScrollListener(snapOnScrollListener)
    }

    override fun onResume() {
        super.onResume()
        binding.rv.smoothScrollBy(1, 0)
    }

}
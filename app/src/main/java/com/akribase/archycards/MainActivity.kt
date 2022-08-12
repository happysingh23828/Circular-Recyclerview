package com.akribase.archycards

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.akribase.archycards.circular_recyclerview.CircularRecyclerView
import com.akribase.archycards.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initRv()
    }

    private fun initRv() {

        val listOfItems = mutableListOf<CircularRecyclerView.Item>()

        for (i in 0..100) {
            listOfItems.add(
                CircularRecyclerView.Item(
                    borderColor = R.color.teal_200,
                    imageUrl = "",
                    id = i.toString()
                )
            )
        }

        binding.circularRecyclerView.createItems(listOfItems)

        binding.btnScroll.setOnClickListener {
            binding.circularRecyclerView.animateAndSelectItem(50,4000)
        }
    }

}
package com.akribase.archycards

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.view.*
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.akribase.archycards.databinding.ItemViewBinding

class RewardsAdapter(
    private val rewards: List<Int>
) : RecyclerView.Adapter<RewardsAdapter.RewardsHolder>() {

    @SuppressLint("ClickableViewAccessibility")
    inner class RewardsHolder(private val binding: ItemViewBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(id: Int) {
            binding.imageView.setImageDrawable(ContextCompat.getDrawable(itemView.context, id))
            binding.imageView.setCircleBackgroundColorResource(getCircleBorderColor())
            binding.position.backgroundTintList = ColorStateList.valueOf(getCircleBorderColor())
            binding.position.text = adapterPosition.toString()
            binding.root.setOnClickListener {
                Toast.makeText(binding.root.context, "$adapterPosition", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        private fun getCircleBorderColor(): Int {
            return when (adapterPosition % 6) {
                1 -> R.color.p1
                2 -> R.color.p2
                3 -> R.color.p3
                4 -> R.color.p4
                5 -> R.color.p5
                else -> R.color.p6
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RewardsHolder {
        return RewardsHolder(
            ItemViewBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: RewardsHolder, position: Int) {
        holder.bind(rewards[position])
    }

    override fun getItemCount(): Int {
        return rewards.size
    }
}

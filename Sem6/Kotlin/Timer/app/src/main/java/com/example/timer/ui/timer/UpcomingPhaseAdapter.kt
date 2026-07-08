package com.example.timerapp.ui.timer

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.timerapp.data.model.Phase
import com.example.timerapp.databinding.ItemUpcomingPhaseBinding

class UpcomingPhaseAdapter :
    ListAdapter<Phase, UpcomingPhaseAdapter.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(val binding: ItemUpcomingPhaseBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemUpcomingPhaseBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val phase = getItem(position)
        with(holder.binding) {
            tvPhaseName.text = if (phase.label.isNotEmpty())
                "${phase.type.name}: ${phase.label}"
            else phase.type.name
            val m = phase.durationSeconds / 60
            val s = phase.durationSeconds % 60
            tvPhaseDuration.text = "%02d:%02d".format(m, s)
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Phase>() {
        override fun areItemsTheSame(old: Phase, new: Phase) =
            old.type == new.type && old.durationSeconds == new.durationSeconds
        override fun areContentsTheSame(old: Phase, new: Phase) = old == new
    }
}

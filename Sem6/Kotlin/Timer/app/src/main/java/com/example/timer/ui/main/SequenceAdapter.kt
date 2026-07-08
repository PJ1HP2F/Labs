package com.example.timerapp.ui.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.timerapp.data.model.TimerSequence
import com.example.timerapp.databinding.ItemSequenceBinding

class SequenceAdapter(
    private val onEdit: (TimerSequence) -> Unit,
    private val onDelete: (TimerSequence) -> Unit,
    private val onStart: (TimerSequence) -> Unit
) : ListAdapter<TimerSequence, SequenceAdapter.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(val binding: ItemSequenceBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSequenceBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val sequence = getItem(position)
        with(holder.binding) {
            tvName.text = sequence.name
            colorIndicator.setBackgroundColor(sequence.color)
            val phases = sequence.buildPhases()
            tvPhaseCount.text = root.context.getString(
                com.example.timerapp.R.string.phases_count, phases.size
            )
            btnEdit.setOnClickListener { onEdit(sequence) }
            btnDelete.setOnClickListener { onDelete(sequence) }
            btnStart.setOnClickListener { onStart(sequence) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<TimerSequence>() {
        override fun areItemsTheSame(old: TimerSequence, new: TimerSequence) = old.id == new.id
        override fun areContentsTheSame(old: TimerSequence, new: TimerSequence) = old == new
    }
}

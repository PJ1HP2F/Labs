package com.example.timerapp.data.model

import com.example.timerapp.R

enum class PhaseType(val labelResId: Int) {
    WARMUP(R.string.phase_warmup),
    WORK(R.string.phase_work),
    REST(R.string.phase_rest),
    COOLDOWN(R.string.phase_cooldown)
}

data class Phase(
    val type: PhaseType,
    val durationSeconds: Int,
    val label: String = ""
)

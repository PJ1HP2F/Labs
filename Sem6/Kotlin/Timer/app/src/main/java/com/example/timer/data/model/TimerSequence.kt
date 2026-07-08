package com.example.timerapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sequences")
data class TimerSequence(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val color: Int,               // ARGB color int
    val warmupSeconds: Int = 10,
    val workSeconds: Int = 30,
    val restSeconds: Int = 10,
    val cooldownSeconds: Int = 10,
    val workRepetitions: Int = 3, // Number of work phases
    val restBetweenSets: Int = 30 // Rest between sets in seconds
) {
    /**
     * Expand the sequence into a flat list of Phase objects.
     * Pattern: Warmup → [Work → Rest] × N → Cooldown
     */
    fun buildPhases(): List<Phase> {
        val phases = mutableListOf<Phase>()
        phases.add(Phase(PhaseType.WARMUP, warmupSeconds))
        repeat(workRepetitions) { index ->
            phases.add(Phase(PhaseType.WORK, workSeconds, "Set ${index + 1}"))
            if (index < workRepetitions - 1) {
                phases.add(Phase(PhaseType.REST, restSeconds))
            }
        }
        phases.add(Phase(PhaseType.REST, restBetweenSets, "Long Rest"))
        phases.add(Phase(PhaseType.COOLDOWN, cooldownSeconds))
        return phases
    }
}

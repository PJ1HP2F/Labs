package com.example.timerapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.timerapp.data.model.Phase
import com.example.timerapp.data.model.TimerSequence

class TimerViewModel : ViewModel() {

    private val _currentPhaseIndex = MutableLiveData(0)
    val currentPhaseIndex: LiveData<Int> = _currentPhaseIndex

    private val _remainingSeconds = MutableLiveData(0)
    val remainingSeconds: LiveData<Int> = _remainingSeconds

    private val _isRunning = MutableLiveData(false)
    val isRunning: LiveData<Boolean> = _isRunning

    private val _phases = MutableLiveData<List<Phase>>(emptyList())
    val phases: LiveData<List<Phase>> = _phases

    private val _isFinished = MutableLiveData(false)
    val isFinished: LiveData<Boolean> = _isFinished

    fun initialize(sequence: TimerSequence) {
        val phaseList = sequence.buildPhases()
        _phases.value = phaseList
        _currentPhaseIndex.value = 0
        _remainingSeconds.value = phaseList.firstOrNull()?.durationSeconds ?: 0
        _isFinished.value = false
    }

    fun updateFromService(phaseIndex: Int, remainingSeconds: Int, isRunning: Boolean) {
        _currentPhaseIndex.value = phaseIndex
        _remainingSeconds.value = remainingSeconds
        _isRunning.value = isRunning
    }

    fun setFinished() {
        _isFinished.value = true
        _isRunning.value = false
    }
}
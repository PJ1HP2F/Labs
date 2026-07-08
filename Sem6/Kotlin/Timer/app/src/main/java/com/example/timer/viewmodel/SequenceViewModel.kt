package com.example.timerapp.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.example.timerapp.App
import com.example.timerapp.data.model.TimerSequence
import com.example.timerapp.data.repository.SequenceRepository
import kotlinx.coroutines.launch

class SequenceViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: SequenceRepository = (application as App).repository

    val sequences: LiveData<List<TimerSequence>> = repository.allSequences.asLiveData()

    fun insert(sequence: TimerSequence) = viewModelScope.launch {
        repository.insert(sequence)
    }

    fun update(sequence: TimerSequence) = viewModelScope.launch {
        repository.update(sequence)
    }

    fun delete(sequence: TimerSequence) = viewModelScope.launch {
        repository.delete(sequence)
    }

    fun deleteAll() = viewModelScope.launch {
        repository.deleteAll()
    }

    suspend fun getById(id: Long): TimerSequence? = repository.getById(id)
}

class SequenceViewModelFactory(private val application: Application) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SequenceViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SequenceViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

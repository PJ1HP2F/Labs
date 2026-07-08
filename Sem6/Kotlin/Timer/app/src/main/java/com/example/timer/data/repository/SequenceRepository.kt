package com.example.timerapp.data.repository

import com.example.timerapp.data.db.TimerSequenceDao
import com.example.timerapp.data.model.TimerSequence
import kotlinx.coroutines.flow.Flow

class SequenceRepository(private val dao: TimerSequenceDao) {

    val allSequences: Flow<List<TimerSequence>> = dao.getAllSequences()

    suspend fun getById(id: Long): TimerSequence? = dao.getSequenceById(id)

    suspend fun insert(sequence: TimerSequence): Long = dao.insert(sequence)

    suspend fun update(sequence: TimerSequence) = dao.update(sequence)

    suspend fun delete(sequence: TimerSequence) = dao.delete(sequence)

    suspend fun deleteAll() = dao.deleteAll()
}

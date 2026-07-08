package com.example.timerapp.data.db

import androidx.room.*
import com.example.timerapp.data.model.TimerSequence
import kotlinx.coroutines.flow.Flow

@Dao
interface TimerSequenceDao {

    @Query("SELECT * FROM sequences ORDER BY id DESC")
    fun getAllSequences(): Flow<List<TimerSequence>>

    @Query("SELECT * FROM sequences WHERE id = :id")
    suspend fun getSequenceById(id: Long): TimerSequence?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(sequence: TimerSequence): Long

    @Update
    suspend fun update(sequence: TimerSequence)

    @Delete
    suspend fun delete(sequence: TimerSequence)

    @Query("DELETE FROM sequences")
    suspend fun deleteAll()
}

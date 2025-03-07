package com.example.saahas.Models.Room

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.saahas.Models.VoiceRecordingHistory

@Dao
interface VoiceRecordingDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecording(recording: VoiceRecordingHistory)

    @Query("SELECT * FROM Voice_Recordings ORDER BY timestamp DESC")
    fun getAllRecordings(): LiveData<List<VoiceRecordingHistory>>

    @Query("DELETE FROM Voice_Recordings WHERE id = :recordingId")
    suspend fun deleteRecording(recordingId: Long)
}
package com.example.saahas.Models.Room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.saahas.Models.Contact
import com.example.saahas.Models.VoiceRecordingHistory

@Database(entities = [VoiceRecordingHistory::class, Contact::class], version = 2, exportSchema = false)
abstract class VoiceRecordingDatabase : RoomDatabase() {
    abstract fun voiceRecordingDao(): VoiceRecordingDao
    abstract fun ContactDao(): ContactDao

    companion object {
        @Volatile
        private var Instance: VoiceRecordingDatabase? = null

        fun getDatabase(context: Context): VoiceRecordingDatabase {
            return Instance ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    VoiceRecordingDatabase::class.java,
                    "voice_recording_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                Instance = instance
                instance
            }
        }

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `contacts` (
                        `phoneNumber` TEXT NOT NULL PRIMARY KEY,
                        `name` TEXT NOT NULL
                    )
                """.trimIndent())
            }
        }
    }
}
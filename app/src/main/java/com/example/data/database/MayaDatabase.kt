package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        MemoryEntity::class,
        ChatMessageEntity::class,
        TodoEntity::class,
        WorkflowEntity::class,
        ScheduledTaskEntity::class,
        CalendarEventEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class MayaDatabase : RoomDatabase() {
    abstract fun memoryDao(): MemoryDao
    abstract fun chatDao(): ChatDao
    abstract fun todoDao(): TodoDao
    abstract fun workflowDao(): WorkflowDao
    abstract fun scheduledTaskDao(): ScheduledTaskDao
    abstract fun calendarEventDao(): CalendarEventDao

    companion object {
        @Volatile
        private var INSTANCE: MayaDatabase? = null

        fun getDatabase(context: Context): MayaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MayaDatabase::class.java,
                    "maya_ai_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

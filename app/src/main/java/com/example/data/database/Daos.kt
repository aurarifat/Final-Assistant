package com.example.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MemoryDao {
    @Query("SELECT * FROM memories ORDER BY isPinned DESC, timestamp DESC")
    fun getAllMemories(): Flow<List<MemoryEntity>>

    @Query("SELECT * FROM memories WHERE content LIKE '%' || :query || '%' OR title LIKE '%' || :query || '%'")
    suspend fun searchMemories(query: String): List<MemoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemory(memory: MemoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(memories: List<MemoryEntity>)

    @Update
    suspend fun updateMemory(memory: MemoryEntity)

    @Delete
    suspend fun deleteMemory(memory: MemoryEntity)

    @Query("DELETE FROM memories")
    suspend fun clearAll()

    @Query("SELECT * FROM memories")
    suspend fun getAllList(): List<MemoryEntity>
}

@Dao
interface ChatDao {
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity): Long

    @Query("DELETE FROM chat_messages")
    suspend fun clearChat()

    @Query("SELECT * FROM chat_messages")
    suspend fun getAllList(): List<ChatMessageEntity>
}

@Dao
interface TodoDao {
    @Query("SELECT * FROM todos ORDER BY isCompleted ASC, timestamp DESC")
    fun getAllTodos(): Flow<List<TodoEntity>>

    @Query("SELECT * FROM todos WHERE title LIKE '%' || :query || '%'")
    suspend fun searchTodos(query: String): List<TodoEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTodo(todo: TodoEntity): Long

    @Update
    suspend fun updateTodo(todo: TodoEntity)

    @Delete
    suspend fun deleteTodo(todo: TodoEntity)

    @Query("SELECT * FROM todos")
    suspend fun getAllList(): List<TodoEntity>
}

@Dao
interface WorkflowDao {
    @Query("SELECT * FROM workflows ORDER BY id DESC")
    fun getAllWorkflows(): Flow<List<WorkflowEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkflow(workflow: WorkflowEntity): Long

    @Delete
    suspend fun deleteWorkflow(workflow: WorkflowEntity)

    @Query("SELECT * FROM workflows")
    suspend fun getAllList(): List<WorkflowEntity>
}

@Dao
interface ScheduledTaskDao {
    @Query("SELECT * FROM scheduled_tasks ORDER BY id DESC")
    fun getAllTasks(): Flow<List<ScheduledTaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: ScheduledTaskEntity): Long

    @Update
    suspend fun updateTask(task: ScheduledTaskEntity)

    @Delete
    suspend fun deleteTask(task: ScheduledTaskEntity)

    @Query("SELECT * FROM scheduled_tasks")
    suspend fun getAllList(): List<ScheduledTaskEntity>
}

@Dao
interface CalendarEventDao {
    @Query("SELECT * FROM calendar_events ORDER BY startTime ASC")
    fun getAllEvents(): Flow<List<CalendarEventEntity>>

    @Query("SELECT * FROM calendar_events WHERE title LIKE '%' || :query || '%'")
    suspend fun searchEvents(query: String): List<CalendarEventEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: CalendarEventEntity): Long

    @Update
    suspend fun updateEvent(event: CalendarEventEntity)

    @Delete
    suspend fun deleteEvent(event: CalendarEventEntity)

    @Query("SELECT * FROM calendar_events")
    suspend fun getAllList(): List<CalendarEventEntity>
}

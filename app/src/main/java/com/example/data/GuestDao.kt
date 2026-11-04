package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface GuestDao {
    @Query("SELECT * FROM guest_entries ORDER BY date DESC, createdAt DESC")
    fun getAllEntries(): Flow<List<GuestEntry>>

    @Query("SELECT * FROM guest_entries WHERE date = :date ORDER BY createdAt ASC")
    suspend fun getEntriesForDateSync(date: String): List<GuestEntry>

    @Query("SELECT * FROM guest_entries WHERE date = :date ORDER BY createdAt ASC")
    fun getEntriesForDate(date: String): Flow<List<GuestEntry>>

    @Query("SELECT * FROM guest_entries WHERE date LIKE :yearMonth || '%' ORDER BY date DESC, createdAt DESC")
    fun getEntriesForMonth(yearMonth: String): Flow<List<GuestEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: GuestEntry): Long

    @Update
    suspend fun updateEntry(entry: GuestEntry)

    @Delete
    suspend fun deleteEntry(entry: GuestEntry)
}

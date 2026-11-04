package com.example.data

import kotlinx.coroutines.flow.Flow

class GuestRepository(private val guestDao: GuestDao) {
    val allEntries: Flow<List<GuestEntry>> = guestDao.getAllEntries()

    fun getEntriesForMonth(yearMonth: String): Flow<List<GuestEntry>> {
        return guestDao.getEntriesForMonth(yearMonth)
    }

    suspend fun getEntriesForDateSync(date: String): List<GuestEntry> {
        return guestDao.getEntriesForDateSync(date)
    }

    suspend fun insertEntry(entry: GuestEntry): Long {
        return guestDao.insertEntry(entry)
    }

    suspend fun updateEntry(entry: GuestEntry) {
        guestDao.updateEntry(entry)
    }

    suspend fun deleteEntry(entry: GuestEntry) {
        guestDao.deleteEntry(entry)
    }
}

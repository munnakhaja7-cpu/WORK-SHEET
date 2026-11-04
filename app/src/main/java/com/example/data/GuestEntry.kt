package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "guest_entries")
data class GuestEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val date: String, // format "YYYY-MM-DD"
    val name: String,
    val phoneNumber: String,
    val model: String,
    val rsNo: String,
    val ac: Double,
    val food: Double,
    val extra: Double,
    val total: Double,
    val createdAt: Long = System.currentTimeMillis()
)

package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.GuestEntry
import com.example.data.GuestRepository
import com.example.utils.PreferencesManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LedgerViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: GuestRepository
    val prefs: PreferencesManager = PreferencesManager(application)

    val allEntries: StateFlow<List<GuestEntry>>

    init {
        val guestDao = AppDatabase.getDatabase(application).guestDao()
        repository = GuestRepository(guestDao)
        allEntries = repository.allEntries.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    suspend fun getEntryCountAndTotalsForDate(date: String): Pair<Int, Double> {
        val entries = repository.getEntriesForDateSync(date)
        val count = entries.size
        val total = entries.sumOf { it.total }
        return Pair(count, total)
    }

    suspend fun calculateDefaultPricingForDate(date: String): Triple<Double, Double, Double> {
        val entries = repository.getEntriesForDateSync(date)
        val count = entries.size
        return when {
            count == 0 || count == 1 -> Triple(10.0, 0.0, 0.0) // 1st & 2nd entry: A/C = 10
            count == 2 -> Triple(0.0, 32.0, 25.0) // 3rd entry: Food = 32, Extra = 25, A/C = 0
            else -> Triple(0.0, 32.0, 25.0) // 4th+ entry onwards
        }
    }

    fun addEntry(
        date: String,
        name: String,
        phoneNumber: String,
        model: String,
        rsNo: String,
        ac: Double,
        food: Double,
        extra: Double
    ) {
        viewModelScope.launch {
            val total = ac + food + extra
            val entry = GuestEntry(
                date = date,
                name = name,
                phoneNumber = phoneNumber,
                model = model,
                rsNo = rsNo,
                ac = ac,
                food = food,
                extra = extra,
                total = total
            )
            repository.insertEntry(entry)
        }
    }

    fun updateEntry(
        id: Long,
        date: String,
        name: String,
        phoneNumber: String,
        model: String,
        rsNo: String,
        ac: Double,
        food: Double,
        extra: Double,
        createdAt: Long
    ) {
        viewModelScope.launch {
            val total = ac + food + extra
            val entry = GuestEntry(
                id = id,
                date = date,
                name = name,
                phoneNumber = phoneNumber,
                model = model,
                rsNo = rsNo,
                ac = ac,
                food = food,
                extra = extra,
                total = total,
                createdAt = createdAt
            )
            repository.updateEntry(entry)
        }
    }

    fun deleteEntry(entry: GuestEntry) {
        viewModelScope.launch {
            repository.deleteEntry(entry)
        }
    }

    fun getTodayDateString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    fun getCurrentYearMonthString(): String {
        return SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())
    }
}

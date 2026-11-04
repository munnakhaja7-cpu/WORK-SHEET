package com.example.utils

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("guest_ledger_prefs", Context.MODE_PRIVATE)

    var businessName: String
        get() = prefs.getString("business_name", "Grand Hotel & Rest House") ?: "Grand Hotel & Rest House"
        set(value) = prefs.edit().putString("business_name", value).apply()

    var location: String
        get() = prefs.getString("location", "Main Bazaar, City Center") ?: "Main Bazaar, City Center"
        set(value) = prefs.edit().putString("location", value).apply()
}

package com.notebook.android.data.preferences

import android.content.Context
import android.content.SharedPreferences

class RefferalPreferance(context: Context) {

    private val appContext = context.applicationContext
    private val refferalPrefs: SharedPreferences
    private val refferalEditor: SharedPreferences.Editor

    companion object {
        private const val prefs_name = "refferalPrefs"
        private const val mode = 0
    }

    init {
        refferalPrefs = appContext.getSharedPreferences(prefs_name, mode)
        refferalEditor = refferalPrefs.edit()
        refferalEditor.apply()
    }

    fun clearPreference() {
        refferalEditor.clear()
        refferalEditor.commit()
    }

    var refferCode: String?
        get() = refferalPrefs.getString("shareRefferalCode", "")
        set(refferCode) {
            refferalEditor.putString("shareRefferalCode", refferCode)
            refferalEditor.commit()
        }
}
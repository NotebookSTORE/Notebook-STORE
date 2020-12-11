package com.notebook.android.data.db.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import org.jetbrains.annotations.NotNull

@Entity(tableName = "Country", indices = [Index(value = ["country_code"], unique = true)])
data class Country(
    @NotNull
    @PrimaryKey(autoGenerate = false)
    var country_code: String,
    var country_name: String? = null
) {
}
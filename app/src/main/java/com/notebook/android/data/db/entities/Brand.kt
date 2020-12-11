package com.notebook.android.data.db.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import org.jetbrains.annotations.NotNull

@Entity(tableName = "Brand", indices = [Index(value = ["id"], unique = true)])
data class Brand(
    @NotNull
    @PrimaryKey(autoGenerate = false)
    var id: String,
    var title: String? = null,
    var image: String? = null) {
}
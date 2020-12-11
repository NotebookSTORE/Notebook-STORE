package com.notebook.android.data.db.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "FaqExpandable", indices = [Index(value = ["id"], unique = true)])
data class FaqExpandable(
    @PrimaryKey(autoGenerate = false)
    var id:Int,
    var title: String? = null,
    var description: String? = null,
    var status: Int? = null,
    var isExpandable:Boolean = false
) {
}
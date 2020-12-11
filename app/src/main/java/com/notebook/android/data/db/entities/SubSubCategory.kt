package com.notebook.android.data.db.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "SubSubCategory", indices = [Index(value = ["id"], unique = true)])
data class SubSubCategory(
    @PrimaryKey(autoGenerate = false)
    var id: Int? = null,
    var category_id: Int? = null,
    var subcategory_id: Int? = null,
    var title: String? = null,
    var alias: String? = null
){
}
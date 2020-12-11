package com.notebook.android.data.db.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "SubCategory", indices = [Index(value = ["id"], unique = true)])
data class SubCategory(
    @PrimaryKey(autoGenerate = false)
    var id: Int? = null,
    var category_id: Int? = null,
    var title: String? = null,
    var description: String? = null,
    var alias: String? = null,
    var image: String? = null,
    var homeimage_mobile:String ?= null,
    var mobile_popular:String ?= null
){
}
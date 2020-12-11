package com.notebook.android.data.db.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "Category", indices = [Index(value = ["id"], unique = true)])
data class Category(
    @PrimaryKey(autoGenerate = false)
    var id:Int ?= null,
    var title: String? = null,
    var alias: String? = null,
    var image: String? = null,
    var homeimage_mobile: String? = null,
    var home_image: String? = null
){
}
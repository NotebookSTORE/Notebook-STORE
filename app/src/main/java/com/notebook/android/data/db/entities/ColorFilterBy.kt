package com.notebook.android.data.db.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "ColorFilterBy", indices = [Index(value = ["id"], unique = true)])
data class ColorFilterBy(
    @PrimaryKey(autoGenerate = false)
    var id:Int,
    var title:String ?= null,
    var isColorSelected:Boolean = false
) {
}
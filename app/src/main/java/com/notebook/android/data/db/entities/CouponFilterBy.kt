package com.notebook.android.data.db.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "CouponFilterBy", indices = [Index(value = ["id"], unique = true)])
data class CouponFilterBy(
    @PrimaryKey(autoGenerate = false)
    var id:Int,
    var code:String ?= null,
    var isCouponSelected:Boolean = false
) {
}
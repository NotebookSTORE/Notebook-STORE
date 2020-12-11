package com.notebook.android.data.db.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "Address",
    indices = [Index(value = ["id"], unique = true)])
data class Address(
    @PrimaryKey(autoGenerate = false)
    var id:Int,
    var street:String ?= null,
    var locality:String ?= null,
    var city:String ?= null,
    var state:String ?= null,
    var pincode:String ?= null,
    var country:String ?= null,
    var defaultaddress:Int ?= null
):Serializable
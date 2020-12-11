package com.notebook.android.data.db.entities

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "DrawerSubSubCategory", indices = [Index(value = ["id"], unique = true)])
data class DrawerSubSubCategory(
    @PrimaryKey(autoGenerate = false)
    var id: Int,
    var category_id: Int,
    var subcategory_id: Int,
    var title: String? = null,
    var status: Int? = null,
    var alias: String? = null,
    var created_at: String? = null,
    var deleted_at:String ?= null,
    var updated_at:String ?= null
):Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readValue(Int::class.java.classLoader) as Int,
        parcel.readValue(Int::class.java.classLoader) as Int,
        parcel.readValue(Int::class.java.classLoader) as Int,
        parcel.readString(),
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(id)
        parcel.writeValue(category_id)
        parcel.writeValue(subcategory_id)
        parcel.writeString(title)
        parcel.writeValue(status)
        parcel.writeString(alias)
        parcel.writeString(created_at)
        parcel.writeString(deleted_at)
        parcel.writeString(updated_at)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<DrawerSubSubCategory> {
        override fun createFromParcel(parcel: Parcel): DrawerSubSubCategory {
            return DrawerSubSubCategory(parcel)
        }

        override fun newArray(size: Int): Array<DrawerSubSubCategory?> {
            return arrayOfNulls(size)
        }
    }
}
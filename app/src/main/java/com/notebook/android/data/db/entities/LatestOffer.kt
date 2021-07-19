package com.notebook.android.data.db.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import org.jetbrains.annotations.NotNull

@Entity(tableName = "LatestOffer", indices = [Index(value = ["id"], unique = true)])
data class LatestOffer(
    @NotNull
    @PrimaryKey(autoGenerate = false)
    var id: String,
    var url: String? = null,
    var offer:Int,
    var image: String? = null,
    var category_id: Int? = null,
    var brand_id: Int? = null
) {
}
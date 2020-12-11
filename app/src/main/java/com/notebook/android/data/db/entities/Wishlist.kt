package com.notebook.android.data.db.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

const val FAVOURITE_IDS = 0
@Entity(tableName = "Wishlist", indices = [Index(value = ["id"], unique = true)])
data class Wishlist(
    var id: Int? = null,
    var keyfeature:List<String> ?= null,
    var material: String? = null,
    var title: String? = null,
    var alias: String? = null,
    var image: String? = null,
    var status: Int? = null,
    var short_description: String? = null,
    var description: String? = null,
    var data_sheet: String? = null,
    var quantity: Int,
    var price: Float,
    var offer_price: Int,
    var product_code: String? = null,
    var product_condition: String? = null,
    var discount: Int,
    var latest: Int? = null,
    var best: Int? = null,
    var brandtitle: String? = null,
    var colortitle: String? = null,
    var favQuantity:Int = 1
) {

    /*var category_id: Int? = null,
    var subcategory_id: Int? = null,*/

    @PrimaryKey(autoGenerate = true)
    var favId:Int = FAVOURITE_IDS
}
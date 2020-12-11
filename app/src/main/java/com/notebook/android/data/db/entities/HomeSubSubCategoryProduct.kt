package com.notebook.android.data.db.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "HomeSubSubCategoryProduct", indices = [Index(value = ["id"], unique = true)])
data class HomeSubSubCategoryProduct(
    @PrimaryKey(autoGenerate = false)
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
    var customerRating: Float ?= null,
    var reviewCount:Int ?= null
)
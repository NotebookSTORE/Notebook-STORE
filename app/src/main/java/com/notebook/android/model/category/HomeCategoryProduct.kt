package com.notebook.android.model.category

import com.notebook.android.data.db.entities.CategoryProduct

data class HomeCategoryProduct(
    var status: Int? = null,
    var error: Boolean,
    var msg: String? = null,
    var product: List<CategoryProduct>? = null,
    var banner:List<CategoryBanner>?= null
){
    data class CategoryBanner(
        var id: Int? = null,
        var title: Boolean,
        var image: String? = null
    )
}
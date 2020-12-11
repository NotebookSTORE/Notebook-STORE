package com.notebook.android.model.category

import com.notebook.android.data.db.entities.HomeSubSubCategoryProduct

data class DrawerSubSubCategoryProduct(
    var status: Int? = null,
    var error: Boolean,
    var msg: String? = null,
    var product: List<HomeSubSubCategoryProduct>? = null
)
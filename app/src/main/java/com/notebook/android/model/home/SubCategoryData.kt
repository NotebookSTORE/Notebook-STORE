package com.notebook.android.model.home

import com.notebook.android.data.db.entities.SubCategory

data class SubCategoryData(
    var status: Int? = null,
    var error: Boolean,
    var msg: String? = null,
    var subcategory: List<SubCategory>? = null
)
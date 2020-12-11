package com.notebook.android.model.drawer

import com.notebook.android.data.db.entities.SubSubCategory

data class SubSubCategoryData(
    var status: Int? = null,
    var error: Boolean,
    var msg: String? = null,
    var subsubcategory: List<SubSubCategory>? = null
)
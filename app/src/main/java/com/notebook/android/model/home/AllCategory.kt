package com.notebook.android.model.home

import com.notebook.android.data.db.entities.Category

data class AllCategory (
    var status: Int? = null,
    var error: Boolean,
    var msg: String? = null,
    var category: List<Category>? = null
    )
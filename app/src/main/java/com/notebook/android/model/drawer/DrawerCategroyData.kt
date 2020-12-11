package com.notebook.android.model.drawer

import com.notebook.android.data.db.entities.DrawerCategory
import com.notebook.android.data.db.entities.DrawerSubCategory
import com.notebook.android.data.db.entities.PolicyData
import com.notebook.android.data.db.entities.SocialData

data class DrawerCategroyData(
    var status: Int? = null,
    var error: Boolean,
    var msg: String? = null,
    var catsub: List<DrawerCategory>? = null,
    var social: List<SocialData> ?= null,
    var policy: List<PolicyData> ?= null,
    var faq: String ?= null,
    var aboutUs:String ?= null
)
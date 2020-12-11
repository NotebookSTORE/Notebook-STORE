package com.notebook.android.model.drawerParts

import com.notebook.android.data.db.entities.FaqExpandable

data class Faqs(
    var error:Boolean,
    var msg:String ?= null,
    var status:Int,
    var faqdata:List<FaqExpandable> ?= null
) {
}
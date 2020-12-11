package com.notebook.android.ui.category.productParts.responseListener

import com.notebook.android.data.db.entities.HomeSubSubCategoryProduct

interface SSCategoryProductListener {
    fun onApiCallStarted()
    fun onSuccess(ssCategoryProd:List<HomeSubSubCategoryProduct>?)
    fun onFailure(msg:String)
}
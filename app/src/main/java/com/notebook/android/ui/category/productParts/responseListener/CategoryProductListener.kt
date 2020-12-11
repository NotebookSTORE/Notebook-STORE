package com.notebook.android.ui.category.productParts.responseListener

import com.notebook.android.data.db.entities.CategoryProduct
import com.notebook.android.model.category.HomeCategoryProduct
import com.notebook.android.model.home.SubCategoryProductData

interface CategoryProductListener {
    fun onApiCallStarted()
    fun onSuccess(categProd:List<CategoryProduct>?)
    fun onFailure(msg:String)
    fun onGetCategoryBannerData(categData:List<HomeCategoryProduct.CategoryBanner>)
}
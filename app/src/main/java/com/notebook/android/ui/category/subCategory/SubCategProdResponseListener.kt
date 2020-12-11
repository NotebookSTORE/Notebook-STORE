package com.notebook.android.ui.dashboard.frag.fragHome.subCa

import com.notebook.android.data.db.entities.CategoryProduct
import com.notebook.android.data.db.entities.SubCategoryProduct
import com.notebook.android.model.category.HomeCategoryProduct
import com.notebook.android.model.home.SubCategoryProductData

interface SubCategProdResponseListener {
    fun onApiCallStarted()
    fun onSuccess(subCategoryProdList: List<SubCategoryProduct>?)
    fun onGetSubCategoryBannerData(subCategData:List<SubCategoryProductData.BannerData>)
    fun onFailure(msg:String)
    fun onNoInternetAvailable(msg:String)
}
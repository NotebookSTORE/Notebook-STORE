package com.notebook.android.ui.splash

import com.notebook.android.data.db.database.NotebookDatabase
import com.notebook.android.data.db.entities.*
import com.notebook.android.data.network.NotebookApi
import com.notebook.android.data.network.SafeApiRequest
import com.notebook.android.model.drawer.DrawerCategroyData
import com.notebook.android.model.home.AllCategory
import com.notebook.android.model.home.BannerData
import com.notebook.android.model.home.SubCategoryData

class SplashRepository(
    val db: NotebookDatabase,
    val notebookApi: NotebookApi
) : SafeApiRequest(){

    suspend fun getBannerData(): BannerData {
        return apiRequest { notebookApi.bannerSliderData(1) }
    }

    suspend fun getAllCategory(): AllCategory {
        return apiRequest { notebookApi.allCategoryData() }
    }

    suspend fun getAllSubCategory(): SubCategoryData {
        return apiRequest { notebookApi.subCategoryData() }
    }

    // Drawer operation function here....
    suspend fun getDrawerCategoryData(): DrawerCategroyData {
        return apiRequest { notebookApi.subCategoryDisplayDrawer()}
    }

    suspend fun insertAllDrawerSubCategData(list:List<DrawerCategory>) = db.getDrawerDao().insertAllDrawerCategoryData(list)
    fun getAllDataFromDrawerDB() = db.getDrawerDao().getDrawerCategoryData()

    //insert all banner or category here...
    suspend fun insertAllCategoryIntoDB(categList:List<Category>) = db.getCategoryDao().insertAllCategory(categList)
    suspend fun insertAllSubCategoryIntoDB(subCategList:List<SubCategory>) = db.getCategoryDao().insertAllSubCategory(subCategList)
    suspend fun insertSubSubCategoryIntoDB(subSubCategList:List<SubSubCategory>) = db.getCategoryDao().insertAllSubSubCategory(subSubCategList)

    suspend fun insertAllBannerIntoDB(bannerList:List<Banner>) = db.getHomeDao().insertAllBanner(bannerList)
}
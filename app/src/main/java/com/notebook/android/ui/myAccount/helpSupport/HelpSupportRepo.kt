package com.notebook.android.ui.myAccount.helpSupport

import com.notebook.android.data.db.database.NotebookDatabase
import com.notebook.android.data.db.entities.Address
import com.notebook.android.data.db.entities.Country
import com.notebook.android.data.db.entities.FaqExpandable
import com.notebook.android.data.network.NotebookApi
import com.notebook.android.data.network.SafeApiRequest
import com.notebook.android.model.address.CountryData
import com.notebook.android.model.address.FetchAddresses
import com.notebook.android.model.drawerParts.Faqs
import com.notebook.android.model.helpSupport.FeedbackData
import com.notebook.android.model.helpSupport.HelpSupportData
import com.notebook.android.model.helpSupport.ReportProblem
import okhttp3.MultipartBody
import okhttp3.RequestBody

class HelpSupportRepo(
    val db:NotebookDatabase,
    val apiService:NotebookApi
) : SafeApiRequest() {

    suspend fun getFaqData() : Faqs{
        return apiRequest { apiService.drawerFaqData() }
    }

    suspend fun reportProblemRegisterOnServer(userID: RequestBody, token: RequestBody, email: RequestBody, name: RequestBody, reportImg: MultipartBody.Part, msg: RequestBody) : ReportProblem {
        return apiRequest { apiService.reportProblemWithImageUpload(userID, token, email, name, reportImg, msg) }
    }

    suspend fun appFeedbackRegisterOnServer(userID:Int, token:String, email:String, name:String, rating:Float, msg:String) : FeedbackData {
        return apiRequest { apiService.appRatingORFeedback(userID, token, email, name, rating, msg) }
    }

    suspend fun helpSupportData() : HelpSupportData{
        return apiRequest { apiService.getHelpSupportData() }
    }

    suspend fun insertFaqDataToDB(faqList:List<FaqExpandable>) = db.getDrawerDao().insertAllFaqData(faqList)
    fun getFaqDataFromDB() = db.getDrawerDao().getAllFaqData()

    //get user detail...
    fun getUser() = db.getUserDao().getUser()
    suspend fun deleteUser() = db.getUserDao().deleteUser()
    suspend fun clearCartTable() = db.getDetailProdDao().clearCartTable()
    suspend fun clearAddressTable() = db.getAddressDao().clearAddressTable()
    suspend fun clearOrderTable() = db.getOrderHistoryDao().clearOrderHistoryTable()
    suspend fun clearFavouriteTable() = db.getWishlistDao().clearWishlistTable()
}
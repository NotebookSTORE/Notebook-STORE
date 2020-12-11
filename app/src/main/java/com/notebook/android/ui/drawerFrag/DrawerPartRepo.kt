package com.notebook.android.ui.drawerFrag

import com.notebook.android.data.db.database.NotebookDatabase
import com.notebook.android.data.db.entities.FaqExpandable
import com.notebook.android.data.network.NotebookApi
import com.notebook.android.data.network.SafeApiRequest
import com.notebook.android.model.drawerParts.ContactUs
import com.notebook.android.model.drawerParts.Faqs
import com.notebook.android.model.helpSupport.HelpSupportData

class DrawerPartRepo(
    val db:NotebookDatabase,
    val notebookApi: NotebookApi
):SafeApiRequest() {

    suspend fun callContactUsApi(fullname:String, phone:String, email:String, msg:String) : ContactUs{
        return apiRequest { notebookApi.contactUsPostToServer(fullname, phone, email, msg) }
    }

    suspend fun getFaqDataFromServer() : Faqs{
        return apiRequest { notebookApi.drawerFaqData() }
    }

    suspend fun helpSupportData() : HelpSupportData {
        return apiRequest { notebookApi.getHelpSupportData() }
    }

    suspend fun insertFaqDataIntoDB(faqList:List<FaqExpandable>) = db.getDrawerDao().insertAllFaqData(faqList)
    fun getAllFaqDataFromDB() = db.getDrawerDao().getAllFaqData()
}
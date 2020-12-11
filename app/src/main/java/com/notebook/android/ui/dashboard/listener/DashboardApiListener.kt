package com.notebook.android.ui.dashboard.listener

import com.notebook.android.data.db.entities.Banner
import com.notebook.android.data.db.entities.PolicyData
import com.notebook.android.data.db.entities.SocialData
import com.notebook.android.data.db.entities.User

interface DashboardApiListener {
    fun onApiCallStarted()
    fun onApiCartAddCallStarted()
    fun onSuccess(apiResponse:String)
    fun onSuccessPolicy(apiResponse:String)
    fun onSocialDrawerData(social:List<SocialData>)
    fun onPolicyDrawerData(policy:List<PolicyData>)
    fun onDrawerFaqAboutUsData(faqLink:String, aboutUsLink:String)
    fun onCartItemAdded(isAdded:Boolean)
    fun onSuccessBulkOrderData(bannerResponse: List<Banner>)
    fun onGettingUpgradeCheck(isUpgradeAvail:Int?)
    fun onFailure(msg:String)
    fun onApiFailure(msg:String)
    fun onInternetNotAvailable(msg:String)
    fun onSuccessLogout()
    fun onInvalidCredential()

    fun onFailureCart(msg:String, isAddCart:Boolean)
    fun onApiFailureCart(msg:String)
    fun onInternetNotAvailableCart(msg:String)
}
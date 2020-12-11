package com.notebook.android.data.preferences

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.notebook.android.utility.Constant.WITHOUT_SOCIAL_LOGIN

private const val PRIME_MEMBERsHIP_TYPE = "primeSubscriptionCharge"
private const val LOGIN_TYPE = "loginType"
private const val USER_TOKEN = "userID"
private const val USER_ID = "userToken"
private const val USER_VERIFIED = "userVerified"
private const val LOGIN_TYPE_IMAGE_UPDATED = "loginTypeImageUpdated"
private const val BRAND_SELECTED_POSITION = "brandPos"
private const val DISCOUNT_SELECTED_POSITION = "discountPos"
private const val RATING_SELECTED_POSITION = "ratingPos"
private const val COLOR_SELECTED_POSITION = "colorPos"
private const val FIREBASE_INSTANCE_ID = "device_id"
private const val SORT_PRODUCT_VALUE = "sortingValue"
private const val DEFAULT_ADDRESS_MODAL = "defaultAddrModal"
private const val DEFAULT_ADDRESS = "defaultAddr"
private const val REFFERAL_PLAY_STORE_KEY = "referKey"
private const val WALLET_AMOUNT_KEY = "walletKey"
private const val FILTER_COMMON_IMAGE_URL = "filterImageUrl"
private const val COD_PAYMENT_OPTION = "codOption"
private const val SIMILAR_DISCOUNTED_PRODUCT_ITEM_CLICKED = "similarDiscountedProdClicked"
private const val ORDER_SUMMARY_COUPON_CHECK = "orderSummaryCouponCheck"

class NotebookPrefs(context: Context) {

    private val appContext = context.applicationContext
    private val notebookStorePrefs: SharedPreferences
    private val notebookEditor: SharedPreferences.Editor

    companion object {
        private const val prefs_name = "notebookPrefs"
        private const val mode = 0
    }

    init {
        notebookStorePrefs = appContext.getSharedPreferences(prefs_name, mode)
        notebookEditor = notebookStorePrefs.edit()
        notebookEditor.apply()
    }

    fun clearPreference() {
        notebookEditor.clear()
        notebookEditor.commit()
    }

    var userContactEmail: String?
        get() = notebookStorePrefs.getString("ContactEmail", "")
        set(contactEmail) {
            notebookEditor.putString("ContactEmail", contactEmail)
            notebookEditor.commit()
        }

    var userContactPhone: String?
        get() = notebookStorePrefs.getString("ContactPhone", "")
        set(contactPhone) {
            notebookEditor.putString("ContactPhone", contactPhone)
            notebookEditor.commit()
        }

    var TermsConditionLink: String?
        get() = notebookStorePrefs.getString("TermConditionLink", "")
        set(termLink) {
            notebookEditor.putString("TermConditionLink", termLink)
            notebookEditor.commit()
        }

    var FaqsDynamicLink: String?
        get() = notebookStorePrefs.getString("FaqDynamicLink", "")
        set(faqDynamicLink) {
            notebookEditor.putString("FaqDynamicLink", faqDynamicLink)
            notebookEditor.commit()
        }

    var primeSubscriptionCharge: String?
        get() = notebookStorePrefs.getString(PRIME_MEMBERsHIP_TYPE, "")
        set(primeCharge) {
            notebookEditor.putString(PRIME_MEMBERsHIP_TYPE, primeCharge)
            notebookEditor.commit()
        }

    var loginType: String?
        get() = notebookStorePrefs.getString(LOGIN_TYPE, WITHOUT_SOCIAL_LOGIN)
        set(catValue) {
            notebookEditor.putString(LOGIN_TYPE, catValue)
            notebookEditor.commit()
        }

    var userID: Int
        get() = notebookStorePrefs.getInt(USER_ID, 0)
        set(userVerifiedID) {
            notebookEditor.putInt(USER_ID, userVerifiedID)
            notebookEditor.commit()
        }

    var primeUserUpgradeAvail: Int
        get() = notebookStorePrefs.getInt("upgradePrime", 0)
        set(primeUpgrade) {
            notebookEditor.putInt("upgradePrime", primeUpgrade)
            notebookEditor.commit()
        }

    var userToken: String?
        get() = notebookStorePrefs.getString(USER_TOKEN, "")
        set(token) {
            notebookEditor.putString(USER_TOKEN, token)
            notebookEditor.commit()
        }

    var isVerified: Int
        get() = notebookStorePrefs.getInt(USER_VERIFIED, 0)
        set(isVerified) {
            notebookEditor.putInt(USER_VERIFIED, isVerified)
            notebookEditor.commit()
        }

    var similarDiscountedProductClicked: Boolean
        get() = notebookStorePrefs.getBoolean(SIMILAR_DISCOUNTED_PRODUCT_ITEM_CLICKED, false)
        set(isSimilarClicked) {
            notebookEditor.putBoolean(SIMILAR_DISCOUNTED_PRODUCT_ITEM_CLICKED, isSimilarClicked)
            notebookEditor.commit()
        }

    var orderSummaryCoupon: Boolean
        get() = notebookStorePrefs.getBoolean(ORDER_SUMMARY_COUPON_CHECK, false)
        set(isCheckOrderSummaryClicked) {
            notebookEditor.putBoolean(ORDER_SUMMARY_COUPON_CHECK, isCheckOrderSummaryClicked)
            notebookEditor.commit()
        }

    var codOptionPaymentAvail: Boolean
        get() = notebookStorePrefs.getBoolean(COD_PAYMENT_OPTION, false)
        set(isCodAvailable) {
            notebookEditor.putBoolean(COD_PAYMENT_OPTION, isCodAvailable)
            notebookEditor.commit()
        }

    var loginTypeOnImageUpdated: String?
        get() = notebookStorePrefs.getString(LOGIN_TYPE_IMAGE_UPDATED, WITHOUT_SOCIAL_LOGIN)
        set(catValueImageUpdated) {
            notebookEditor.putString(LOGIN_TYPE_IMAGE_UPDATED, catValueImageUpdated)
            notebookEditor.commit()
        }


    var brandPos: Int
        get() = notebookStorePrefs.getInt(BRAND_SELECTED_POSITION, -1)
        set(brandPos) {
            notebookEditor.putInt(BRAND_SELECTED_POSITION, brandPos)
            notebookEditor.commit()
        }

    var discountPos: Int
        get() = notebookStorePrefs.getInt(DISCOUNT_SELECTED_POSITION, -1)
        set(discPos) {
            notebookEditor.putInt(DISCOUNT_SELECTED_POSITION, discPos)
            notebookEditor.commit()
        }

    var colorPos: Int
        get() = notebookStorePrefs.getInt(COLOR_SELECTED_POSITION, -1)
        set(colorPos) {
            notebookEditor.putInt(COLOR_SELECTED_POSITION, colorPos)
            notebookEditor.commit()
        }

    var ratingPos: Int
        get() = notebookStorePrefs.getInt(RATING_SELECTED_POSITION, -1)
        set(ratePos) {
            notebookEditor.putInt(RATING_SELECTED_POSITION, ratePos)
            notebookEditor.commit()
        }

    var firebaseDeviceID: String?
        get() = notebookStorePrefs.getString(FIREBASE_INSTANCE_ID, "")
        set(fbToken) {
            notebookEditor.putString(FIREBASE_INSTANCE_ID, fbToken)
            notebookEditor.commit()
        }

    var sortedValue: Int
        get() = notebookStorePrefs.getInt(SORT_PRODUCT_VALUE, 2)
        set(sortValue) {
            notebookEditor.putInt(SORT_PRODUCT_VALUE, sortValue)
            notebookEditor.commit()
        }

    var defaultAddr: String?
        get() = notebookStorePrefs.getString(DEFAULT_ADDRESS, "")
        set(defaultAddress) {
            notebookEditor.putString(DEFAULT_ADDRESS, defaultAddress)
            notebookEditor.commit()
        }

    var defaultAddrModal: String?
        get() = notebookStorePrefs.getString(DEFAULT_ADDRESS_MODAL, "")
        set(defaultAddressModal) {
            notebookEditor.putString(DEFAULT_ADDRESS_MODAL, defaultAddressModal)
            notebookEditor.commit()
        }

    /*var RefferalCode: String?
        get() = notebookStorePrefs.getString(REFFERAL_PLAY_STORE_KEY, "")
        set(referPlayStoreKey) {
            notebookEditor.putString(REFFERAL_PLAY_STORE_KEY, referPlayStoreKey)
            notebookEditor.commit()
        }*/

    var walletAmount: String?
        get() = notebookStorePrefs.getString(WALLET_AMOUNT_KEY, "")
        set(walletAmountKey) {
            notebookEditor.putString(WALLET_AMOUNT_KEY, walletAmountKey)
            notebookEditor.commit()
        }

    var FilterCommonImageUrl: String?
        get() = notebookStorePrefs.getString(FILTER_COMMON_IMAGE_URL, "")
        set(FilterProdImageKey) {
            notebookEditor.putString(FILTER_COMMON_IMAGE_URL, FilterProdImageKey)
            notebookEditor.commit()
        }





    // Regular/Prime merchant field here....
    var merchantName: String?
        get() = notebookStorePrefs.getString("MerchantName", "")
        set(merchName) {
            notebookEditor.putString("MerchantName", merchName)
            notebookEditor.commit()
        }

    var merchantEmail: String?
        get() = notebookStorePrefs.getString("MerchantEmail", "")
        set(merchEmail) {
            notebookEditor.putString("MerchantEmail", merchEmail)
            notebookEditor.commit()
        }

    var merchantPhone: String?
        get() = notebookStorePrefs.getString("MerchantPhone", "")
        set(merchPhone) {
            notebookEditor.putString("MerchantPhone", merchPhone)
            notebookEditor.commit()
        }

    var merchantDOB: String?
        get() = notebookStorePrefs.getString("MerchantDOB", "")
        set(merchDOB) {
            notebookEditor.putString("MerchantDOB", merchDOB)
            notebookEditor.commit()
        }

    var merchantAadharNumber: String?
        get() = notebookStorePrefs.getString("MerchantAadharNumber", "")
        set(merchAadhar) {
            notebookEditor.putString("MerchantAadharNumber", merchAadhar)
            notebookEditor.commit()
        }

    var merchantPanNumber: String?
        get() = notebookStorePrefs.getString("MerchantPanNumber", "")
        set(merchPan) {
            notebookEditor.putString("MerchantPanNumber", merchPan)
            notebookEditor.commit()
        }

    var merchantAddressBuilding: String?
        get() = notebookStorePrefs.getString("MerchantAddressBuilding", "")
        set(merchBuilding) {
            notebookEditor.putString("MerchantAddressBuilding", merchBuilding)
            notebookEditor.commit()
        }

    var merchantAddressLocality: String?
        get() = notebookStorePrefs.getString("MerchantAddressLocality", "")
        set(merchLocality) {
            notebookEditor.putString("MerchantAddressLocality", merchLocality)
            notebookEditor.commit()
        }

    var merchantAddressCity: String?
        get() = notebookStorePrefs.getString("MerchantAddressCity", "")
        set(merchCity) {
            notebookEditor.putString("MerchantAddressCity", merchCity)
            notebookEditor.commit()
        }

    var merchantAddressState: String?
        get() = notebookStorePrefs.getString("MerchantAddressState", "")
        set(merchState) {
            notebookEditor.putString("MerchantAddressState", merchState)
            notebookEditor.commit()
        }

    var merchantAddressPincode: String?
        get() = notebookStorePrefs.getString("MerchantAddressPincode", "")
        set(merchPincode) {
            notebookEditor.putString("MerchantAddressPincode", merchPincode)
            notebookEditor.commit()
        }

    var merchantAccountNumber: String?
        get() = notebookStorePrefs.getString("MerchantAccountNumber", "")
        set(merchAccount) {
            notebookEditor.putString("MerchantAccountNumber", merchAccount)
            notebookEditor.commit()
        }

    var merchantBankName: String?
        get() = notebookStorePrefs.getString("MerchantBankName", "")
        set(merchBankName) {
            notebookEditor.putString("MerchantBankName", merchBankName)
            notebookEditor.commit()
        }

    var merchantBankLocation: String?
        get() = notebookStorePrefs.getString("MerchantBankLoc", "")
        set(merchBankLoc) {
            notebookEditor.putString("MerchantBankLoc", merchBankLoc)
            notebookEditor.commit()
        }

    var merchantIfscCode: String?
        get() = notebookStorePrefs.getString("MerchantIfscCode", "")
        set(merchIfsc) {
            notebookEditor.putString("MerchantIfscCode", merchIfsc)
            notebookEditor.commit()
        }

    var merchantUpiID: String?
        get() = notebookStorePrefs.getString("MerchantUpiID", "")
        set(merchUpi) {
            notebookEditor.putString("MerchantUpiID", merchUpi)
            notebookEditor.commit()
        }

    var merchantRefferalID: String?
        get() = notebookStorePrefs.getString("MerchantRefferalID", "")
        set(merchRefferal) {
            notebookEditor.putString("MerchantRefferalID", merchRefferal)
            notebookEditor.commit()
        }

    var merchantInstituteName: String?
        get() = notebookStorePrefs.getString("MerchantInstituteName", "")
        set(merchInstitute) {
            notebookEditor.putString("MerchantInstituteName", merchInstitute)
            notebookEditor.commit()
        }

    var cancelledChequeImage: String?
        get() = notebookStorePrefs.getString("MerchantChequeImage", "")
        set(merchCheque) {
            notebookEditor.putString("MerchantChequeImage", merchCheque)
            notebookEditor.commit()
        }

    var merchantRegisterFor: Int?
        get() = notebookStorePrefs.getInt("MerchantRegisterFor", 0)
        set(merchRegister) {
            notebookEditor.putInt("MerchantRegisterFor", merchRegister?:0)
            notebookEditor.commit()
        }

    var merchantKycStatus: Int?
        get() = notebookStorePrefs.getInt("MerchantKycStatus", 1)
        set(merchKyc) {
            notebookEditor.putInt("MerchantKycStatus", merchKyc?:1)
            notebookEditor.commit()
        }

    var aadharFrontImage: String?
        get() = notebookStorePrefs.getString("MerchantAadharFrontImage", "")
        set(merchAadharFront) {
            notebookEditor.putString("MerchantAadharFrontImage", merchAadharFront)
            notebookEditor.commit()
        }

    var aadharBackImage: String?
        get() = notebookStorePrefs.getString("MerchantAadharBackImage", "")
        set(merchAadharBack) {
            notebookEditor.putString("MerchantAadharBackImage", merchAadharBack)
            notebookEditor.commit()
        }

    var pancardImage: String?
        get() = notebookStorePrefs.getString("MerchantPancardImage", "")
        set(merchPanImage) {
            notebookEditor.putString("MerchantPancardImage", merchPanImage)
            notebookEditor.commit()
        }
}
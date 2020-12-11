package com.notebook.android.ui.merchant

import com.notebook.android.data.db.database.NotebookDatabase
import com.notebook.android.data.db.entities.Address
import com.notebook.android.data.db.entities.Banner
import com.notebook.android.data.db.entities.MerchantBenefit
import com.notebook.android.data.db.entities.User
import com.notebook.android.data.network.NotebookApi
import com.notebook.android.data.network.SafeApiRequest
import com.notebook.android.model.address.FetchAddresses
import com.notebook.android.model.auth.UserData
import com.notebook.android.model.home.BannerData
import com.notebook.android.model.merchant.MerchantBenefitData
import com.notebook.android.model.merchant.PrimeMerchantResponse
import com.notebook.android.model.merchant.RegularMerchantResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.Response

class MerchantRepo(
    val db:NotebookDatabase,
    val notebookApi:NotebookApi
) : SafeApiRequest(){

    suspend fun registerPrimeMerchant(
        fullName: RequestBody, email: RequestBody, dob: RequestBody, phone: RequestBody,
        address: RequestBody, locality: RequestBody, city: RequestBody,
        state: RequestBody, pincode: RequestBody, country: RequestBody, identityDetail: RequestBody, panCardNo: RequestBody,
        identityImg: MultipartBody.Part, panCardImg: MultipartBody.Part,
        identityImg2: MultipartBody.Part, cancelChequeImg: MultipartBody.Part, accountNum: RequestBody,
        bankName: RequestBody, ifscCode: RequestBody, bankLocation: RequestBody,
        upi: RequestBody, refferalCode: RequestBody,
        deviceID: RequestBody, registerForPart:RequestBody, instituteValue:RequestBody
    ): PrimeMerchantResponse {
        return apiRequest {
            notebookApi.registerPrimeMerchant(
                fullName, email, dob, phone, address, locality, city, state, pincode, country,
                identityDetail, panCardNo, identityImg, panCardImg, identityImg2, cancelChequeImg,
                accountNum, bankName, ifscCode, bankLocation, upi, refferalCode,
                deviceID, registerForPart, instituteValue)
        }
    }

    suspend fun registerPrimeMerchantUpload(
        fullName: RequestBody, email: RequestBody, dob: RequestBody, phone: RequestBody,
        address: RequestBody, locality: RequestBody, city: RequestBody,
        state: RequestBody, pincode: RequestBody, country: RequestBody, identityDetail: RequestBody, panCardNo: RequestBody,
        identityImg: RequestBody, panCardImg: RequestBody,
        identityImg2: RequestBody, cancelChequeImg:RequestBody, accountNum: RequestBody,
        bankName: RequestBody, ifscCode: RequestBody, bankLocation: RequestBody,
        upi: RequestBody, refferalCode: RequestBody,
        deviceID: RequestBody, registerForPart:RequestBody, instituteValue:RequestBody
    ): PrimeMerchantResponse {
        return apiRequest {
            notebookApi.registerPrimeMerchantUpdate(
                fullName, email, dob, phone, address, locality, city, state, pincode, country,
                identityDetail, panCardNo, identityImg, panCardImg, identityImg2, cancelChequeImg,
                accountNum, bankName, ifscCode, bankLocation, upi, refferalCode,
                deviceID, registerForPart, instituteValue)
        }
    }

    suspend fun registerPrimeMerchantUploadOnlyCheque(
        fullName: RequestBody, email: RequestBody, dob: RequestBody, phone: RequestBody,
        address: RequestBody, locality: RequestBody, city: RequestBody,
        state: RequestBody, pincode: RequestBody, country: RequestBody, identityDetail: RequestBody, panCardNo: RequestBody,
        identityImg: RequestBody, panCardImg: RequestBody,
        identityImg2: RequestBody, cancelChequeImg: MultipartBody.Part, accountNum: RequestBody,
        bankName: RequestBody, ifscCode: RequestBody, bankLocation: RequestBody,
        upi: RequestBody, refferalCode: RequestBody,
        deviceID: RequestBody, registerForPart:RequestBody, instituteValue:RequestBody
    ): PrimeMerchantResponse {
        return apiRequest {
            notebookApi.registerPrimeMerchantUpdateOnlyCheque(
                fullName, email, dob, phone, address, locality, city, state, pincode, country,
                identityDetail, panCardNo, identityImg, panCardImg, identityImg2, cancelChequeImg,
                accountNum, bankName, ifscCode, bankLocation, upi, refferalCode,
                deviceID, registerForPart, instituteValue)
        }
    }

    suspend fun getUserDataFromServer(userID:Int, token: String) : UserData {
        return apiRequest { notebookApi.userDetailFromServer(userID, token) }
    }

    suspend fun registerRegularMerchant(
        fullName: RequestBody, email: RequestBody, dob: RequestBody, phone: RequestBody,
        address: RequestBody, locality: RequestBody, city: RequestBody,
        state: RequestBody, pincode: RequestBody, country: RequestBody, identityDetail: RequestBody, panCardNo: RequestBody,
        identityImg: MultipartBody.Part, panCardImg: MultipartBody.Part,
        identityImg2: MultipartBody.Part, refferalCode: RequestBody,
        deviceID: RequestBody, registerForPart:RequestBody, instituteValue:RequestBody): RegularMerchantResponse {

        return apiRequest {
            notebookApi.registerRegularMerchant(
                fullName, email, dob, phone, address, locality, city, state, pincode, country,
                identityDetail, panCardNo, identityImg, panCardImg, identityImg2,
                refferalCode, deviceID, registerForPart, instituteValue)
        }
    }

    suspend fun registerRegularMerchantUpdate(
        fullName: RequestBody, email: RequestBody, dob: RequestBody, phone: RequestBody,
        address: RequestBody, locality: RequestBody, city: RequestBody,
        state: RequestBody, pincode: RequestBody, country: RequestBody, identityDetail: RequestBody, panCardNo: RequestBody,
        identityImg: RequestBody, panCardImg: RequestBody,
        identityImg2: RequestBody, refferalCode: RequestBody,
        deviceID: RequestBody, registerForPart:RequestBody, instituteValue:RequestBody): RegularMerchantResponse {

        return apiRequest {
            notebookApi.registerRegularMerchantUpdate(
                fullName, email, dob, phone, address, locality, city, state, pincode, country,
                identityDetail, panCardNo, identityImg, panCardImg, identityImg2,
                refferalCode, deviceID, registerForPart, instituteValue)
        }
    }

    suspend fun getBannerData(bannerType:Int): BannerData {
        return apiRequest { notebookApi.bannerSliderData(bannerType) }
    }

    suspend fun getMerchantBenefitDataFromServer() : MerchantBenefitData{
        return apiRequest { notebookApi.merchantBenifitData() }
    }

    suspend fun otpVerifyApi(mobNumber:String, otp:String): UserData {
        return apiRequest { notebookApi.otpVerification(mobNumber, otp) }
    }

    suspend fun fetchAddressesFromServer(userID:Int, token:String) : FetchAddresses {
        return apiRequest { notebookApi.fetchAddressFromServer(userID, token) }
    }

    suspend fun insertAllAddressToDB(addrList:List<Address>) = db.getAddressDao().insertAllAddressData(addrList)
    suspend fun update(user: User) = db.getUserDao().updateUser(user)
    suspend fun insertUser(user:User) = db.getUserDao().insertUser(user)
    fun getUser() = db.getUserDao().getUser()

    //merchant accessing function here...
    suspend fun insertMerchantBenefitData(merchBenefitData:List<MerchantBenefit>) =
        db.getMerchantDao().insertAllMerchantBenefit(merchBenefitData)
    suspend fun clearMerchantTable() = db.getMerchantDao().clearMerchantBenefitTable()
    fun getMerchantBenefitDataAccToType(merchantType:String) = db.getMerchantDao().getAllMerchantBenefitAccToType(merchantType)
}
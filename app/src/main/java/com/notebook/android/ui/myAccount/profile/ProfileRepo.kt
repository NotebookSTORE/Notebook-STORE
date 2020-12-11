package com.notebook.android.ui.myAccount.profile

import com.notebook.android.data.db.database.NotebookDatabase
import com.notebook.android.data.db.entities.User
import com.notebook.android.data.network.NotebookApi
import com.notebook.android.data.network.SafeApiRequest
import com.notebook.android.model.autdh.ProfileUpdate
import com.notebook.android.model.auth.ChangePass
import com.notebook.android.model.auth.LogoutUserData
import com.notebook.android.model.auth.UserData
import com.notebook.android.model.orderSummary.AfterPaymentRawData
import com.notebook.android.model.orderSummary.PaymentSuccesData
import com.notebook.android.model.orderSummary.WalletSuccess
import com.notebook.android.model.wallet.AddWallet
import com.notebook.android.model.wallet.AddWalletResponse
import com.notebook.android.model.wallet.WalletAmountRaw
import com.notebook.android.model.wallet.WalletAmountResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody

class ProfileRepo(val db: NotebookDatabase,
                  val apiService: NotebookApi
) : SafeApiRequest() {

    //User Profile update function here..
    suspend fun userProfileUpdate(userID: RequestBody, email: RequestBody, fullname: RequestBody,
                                  profileImg: MultipartBody.Part,
                                  dob: RequestBody, gender: RequestBody, mobile:RequestBody
    ): ProfileUpdate {
        return apiRequest { apiService.userProfileUpdate(userID, email, fullname, profileImg, dob, gender, mobile) }
    }

    suspend fun otpVerifyApi(mobNumber:String, otp:String): UserData {
        return apiRequest { apiService.otpVerification(mobNumber, otp) }
    }

    suspend fun userProfileStringUpdate(userID: Int, email: String, fullname: String,
                                        profileImg: String, dob: String, gender: String, mobile:String): ProfileUpdate {
        return apiRequest { apiService.userProfileStringUpdate(userID, email, fullname, profileImg, dob, gender, mobile) }
    }

    suspend fun paymentSaveToDB(paymentRawData: AfterPaymentRawData) : PaymentSuccesData {
        return apiRequest { apiService.paymentSaveToDBAfterPayment(paymentRawData) }
    }

    suspend fun userProfileImageRemove(email: String): ChangePass {
        return apiRequest { apiService.profileUpdateRemoveImage(email) }
    }

    suspend fun addWalletAmountFromGateway(addWalletData: AddWallet) : AddWalletResponse {
        return apiRequest { apiService.addWalletAmount(addWalletData) }
    }

    suspend fun getWalletAmountFromServer(walletAmountRaw: WalletAmountRaw) : WalletAmountResponse {
        return apiRequest { apiService.walletAmountGet(walletAmountRaw) }
    }

    suspend fun afterAddWalletSuccessFromServer(walletSuccessRaw: WalletSuccess) : PaymentSuccesData {
        return apiRequest { apiService.afterPaymentWalletSuccess(walletSuccessRaw) }
    }

    suspend fun logoutUserFromServer(userID:Int, token: String) : LogoutUserData {
        return apiRequest { apiService.logoutUser(userID, token) }
    }

    suspend fun clearCartTable() = db.getDetailProdDao().clearCartTable()
    suspend fun clearAddressTable() = db.getAddressDao().clearAddressTable()
    suspend fun clearOrderTable() = db.getOrderHistoryDao().clearOrderHistoryTable()
    suspend fun clearFavouriteTable() = db.getWishlistDao().clearWishlistTable()

    fun getUser() = db.getUserDao().getUser()
    suspend fun deleteUser() = db.getUserDao().deleteUser()
    suspend fun updateUser(user: User) = db.getUserDao().updateUser(user)
}
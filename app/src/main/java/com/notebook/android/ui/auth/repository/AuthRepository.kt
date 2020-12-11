package com.notebook.android.ui.auth.repository

import com.notebook.android.data.db.database.NotebookDatabase
import com.notebook.android.data.db.entities.User
import com.notebook.android.data.network.NotebookApi
import com.notebook.android.data.network.SafeApiRequest
import com.notebook.android.model.auth.*

class AuthRepository(
    val notebookApi: NotebookApi,
    val notebookDB:NotebookDatabase
) : SafeApiRequest() {

    suspend fun userSignupApi(name:String, email:String, mobNumber:String, password:String,
                              deviceID: String, typeUser:Int, refferalCode:String):RegistrationResponse{
        return apiRequest { notebookApi.userRegistration(name, email, mobNumber,
            password, deviceID, typeUser, refferalCode) }
    }

    suspend fun otpVerifyApi(mobNumber:String, otp:String): UserData {
        return apiRequest { notebookApi.otpVerification(mobNumber, otp) }
    }

    suspend fun otpVerifyWithEmail(email:String, otp:String): UserData {
        return apiRequest { notebookApi.otpVerificationWithEmail(email, otp) }
    }

    suspend fun login(email:String, password:String): UserData {
        return apiRequest { notebookApi.postLogin(email, password) }
    }

    suspend fun socialMobileLogin(username: String, email: String, mobNumber: String,
                                  profileImg: String, deviceID:String, userType:Int): RegistrationResponse {
        return apiRequest { notebookApi.mobileSocialLogin(username, email,
            deviceID, userType, mobNumber, profileImg) }
    }

    suspend fun socialMobileLoginServer(username: String, email: String,
                                        deviceID:String, profileImg: String, userType:Int, socialID:String): UserData {
        return apiRequest { notebookApi.socialMobileLogin(username, email,
            deviceID, profileImg, userType, socialID) }
    }

    suspend fun mobileLoginExist(email:String, userType:Int): UserData {
        return apiRequest { notebookApi.mobileloginexist(email, userType) }
    }

    suspend fun forgotPassword(inputValue:String): ForgotPass {
        return apiRequest { notebookApi.forgotPasswordWithOptions(inputValue) }
    }

    suspend fun changePassword(mobNumber:String, password:String): ChangePass {
        return apiRequest { notebookApi.changePassword(mobNumber, password) }
    }

    suspend fun changePasswordWithEmail(email:String, password:String): ChangePass {
        return apiRequest { notebookApi.changePasswordWithEmail(email, password) }
    }

    suspend fun insertUser(user:User) = notebookDB.getUserDao().insertUser(user)
}

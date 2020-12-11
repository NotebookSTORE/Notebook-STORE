package com.notebook.android.ui.orderSummary

import com.notebook.android.data.db.database.NotebookDatabase
import com.notebook.android.data.db.entities.CouponApply
import com.notebook.android.data.network.NotebookApi
import com.notebook.android.data.network.SafeApiRequest
import com.notebook.android.model.cashfree.CFTokenResponse
import com.notebook.android.model.coupon.CouponData
import com.notebook.android.model.orderSummary.AfterPaymentRawData
import com.notebook.android.model.orderSummary.OrderPaymentDetail
import com.notebook.android.model.orderSummary.PaymentSuccesData
import com.notebook.android.model.orderSummary.WalletSuccess
import com.notebook.android.model.wallet.AddWallet
import com.notebook.android.model.wallet.AddWalletResponse
import com.notebook.android.model.wallet.WalletAmountRaw
import com.notebook.android.model.wallet.WalletAmountResponse

class OrderSummaryRepo(
    val db : NotebookDatabase,
    val notebookApi: NotebookApi
) : SafeApiRequest() {

    suspend fun orderPlacedWithCOD(orderDetails: OrderPaymentDetail) : CFTokenResponse {
        return apiRequest { notebookApi.orderPlacedWithCOD(orderDetails) }
    }

    suspend fun paymentSaveToDB(paymentRawData: AfterPaymentRawData) : PaymentSuccesData {
        return apiRequest { notebookApi.paymentSaveToDBAfterPayment(paymentRawData) }
    }

    /*suspend fun getApplyCouponData(userID:Int) : CouponData {
        return apiRequest { notebookApi.couponDataFromServer(userID) }
    }*/

    suspend fun addWalletAmountFromGateway(addWalletData: AddWallet) : AddWalletResponse {
        return apiRequest { notebookApi.addWalletAmount(addWalletData) }
    }

    suspend fun getWalletAmountFromServer(walletAmountRaw: WalletAmountRaw) : WalletAmountResponse {
        return apiRequest { notebookApi.walletAmountGet(walletAmountRaw) }
    }

    suspend fun afterAddWalletSuccessFromServer(walletSuccessRaw: WalletSuccess) : PaymentSuccesData {
        return apiRequest { notebookApi.afterPaymentWalletSuccess(walletSuccessRaw) }
    }

    suspend fun insertCouponDat(couponList:List<CouponApply>) = db.getDetailProdDao().insertAllCouponData(couponList)
    fun getAllCouponDataFromDB() = db.getDetailProdDao().getAllCouponData()
}
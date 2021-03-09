package com.notebook.android.ui.myAccount.wallet.redeem

import com.notebook.android.data.db.database.NotebookDatabase
import com.notebook.android.data.network.NotebookApi
import com.notebook.android.data.network.SafeApiRequest
import com.notebook.android.model.wallet.redeem.WalletRedeemHistoryResponse
import com.notebook.android.model.wallet.redeem.WalletRedeemResponse

class WalletRedeemRepo(private val apiService: NotebookApi) : SafeApiRequest() {
    suspend fun fetchRedeemHistory(userId: Int): WalletRedeemHistoryResponse? {
        return apiRequest { apiService.fetchRedeemHistory(userId) }
    }

    suspend fun redeemWalletPoints(userId: Int): WalletRedeemResponse? {
        return apiRequest { apiService.redeemWalletPoints(userId) }
    }
}
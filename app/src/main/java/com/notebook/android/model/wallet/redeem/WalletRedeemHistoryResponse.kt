package com.notebook.android.model.wallet.redeem

data class WalletRedeemHistoryResponse(
    val error: Boolean,
    val status: Int,
    val msg: String,
    val history: List<WalletRedeemHistory>
)
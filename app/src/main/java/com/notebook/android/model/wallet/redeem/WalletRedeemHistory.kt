package com.notebook.android.model.wallet.redeem

import java.text.SimpleDateFormat
import java.util.*

data class WalletRedeemHistory(
    val transaction_date: String?,
    val transaction_amount: String?,
    val transaction_id: String?
) {
    fun getTransactionDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val displayDateFormat = SimpleDateFormat("dd MMM, yyyy HH:mm:ss", Locale.getDefault())

        if (transaction_date.isNullOrEmpty()) {
            return ""
        }
        val date = dateFormat.parse(transaction_date)
        return if (date == null) {
            ""
        } else {
            displayDateFormat.format(date)
        }
    }
}
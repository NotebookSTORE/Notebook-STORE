package com.notebook.android.ui.myAccount.wallet.redeem

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.notebook.android.R
import com.notebook.android.model.wallet.redeem.WalletRedeemHistory
import kotlinx.android.synthetic.main.item_view_wallet_redeem.view.*

class WalletRedeemHistoryItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun bind(walletRedeemHistory: WalletRedeemHistory) {
        itemView.redeemAmount.text = String.format(
            "%1\$s%2\$s",
            itemView.context.getString(R.string.rupeeIcon),
            walletRedeemHistory.transaction_amount
        )

        itemView.redeemId.text =
            String.format("%1\$s%2\$s", "Id: ", walletRedeemHistory.transaction_id)

        itemView.redeemDate.text = String.format(
            "%1\$s%2\$s",
            "Date: ",
            walletRedeemHistory.getTransactionDate()
        )
    }
}
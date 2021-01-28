package com.notebook.android.ui.myAccount.wallet.redeem

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.notebook.android.R
import com.notebook.android.model.wallet.redeem.WalletRedeemHistory

class WalletRedeemHistoryAdapter : RecyclerView.Adapter<WalletRedeemHistoryItemViewHolder>() {

    private val redeemHistoryItems: MutableList<WalletRedeemHistory> = mutableListOf()

    fun clear() {
        redeemHistoryItems.clear()
        notifyDataSetChanged()
    }

    fun addItems(redeemHistoryItems: List<WalletRedeemHistory>) {
        this.redeemHistoryItems.addAll(redeemHistoryItems)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WalletRedeemHistoryItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_view_wallet_redeem, parent, false)
        return WalletRedeemHistoryItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: WalletRedeemHistoryItemViewHolder, position: Int) {
        holder.bind(redeemHistoryItems[position])
    }

    override fun getItemCount(): Int {
        return redeemHistoryItems.size
    }
}
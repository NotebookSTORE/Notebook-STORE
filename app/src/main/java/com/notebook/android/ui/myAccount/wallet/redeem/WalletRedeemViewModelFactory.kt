package com.notebook.android.ui.myAccount.wallet.redeem

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.notebook.android.ui.myAccount.wallet.redeem.WalletRedeemRepo

class WalletRedeemViewModelFactory(
    private val walletRedeemRepo: WalletRedeemRepo
): ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return WalletRedeemViewModel(walletRedeemRepo) as T
    }
}
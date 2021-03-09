package com.notebook.android.ui.myAccount.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.notebook.android.ui.myAccount.wallet.redeem.WalletRedeemRepo

class ProfileVMFactory(
    val profileRepo: ProfileRepo,
    val walletRedeemRepo: WalletRedeemRepo
): ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return ProfileVM(profileRepo, walletRedeemRepo) as T
    }
}
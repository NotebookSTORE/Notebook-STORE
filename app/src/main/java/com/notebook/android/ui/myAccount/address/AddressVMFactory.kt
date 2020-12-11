package com.notebook.android.ui.myAccount.address

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class AddressVMFactory(
    val addrRepo: AddressRepo
): ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return AddressVM(addrRepo) as T
    }
}
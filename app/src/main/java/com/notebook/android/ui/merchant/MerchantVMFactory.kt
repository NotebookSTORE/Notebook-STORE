package com.notebook.android.ui.merchant

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class MerchantVMFactory(
    val merchantRepo:MerchantRepo
): ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return MerchantViewModel(merchantRepo) as T
    }
}
package com.notebook.android.ui.myAccount.helpSupport

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class HelpSupportVMFactory(
    val helpSupportRepo: HelpSupportRepo
): ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return HelpSupportVM(helpSupportRepo) as T
    }
}
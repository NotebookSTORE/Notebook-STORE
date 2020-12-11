package com.notebook.android.ui.myAccount.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ProfileVMFactory(
    val profileRepo: ProfileRepo
): ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return ProfileVM(profileRepo) as T
    }
}
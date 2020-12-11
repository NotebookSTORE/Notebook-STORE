package com.notebook.android.ui.myOrder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.notebook.android.ui.auth.viewmodel.AuthViewModel
import com.notebook.android.ui.dashboard.repository.DashboardRepo
import com.notebook.android.ui.dashboard.viewmodel.DashboardViewModel

class MyOrderVMFactory(
    val repo:MyOrderRepo
) :ViewModelProvider.NewInstanceFactory(){

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return MyOrderVM(repo) as T
    }
}
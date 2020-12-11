package com.notebook.android.ui.dashboard.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.notebook.android.ui.auth.viewmodel.AuthViewModel
import com.notebook.android.ui.dashboard.repository.DashboardRepo
import com.notebook.android.ui.dashboard.viewmodel.DashboardViewModel

class DashboardViewModelFactory(
    val repo:DashboardRepo
) :ViewModelProvider.NewInstanceFactory(){

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return DashboardViewModel(repo) as T
    }
}
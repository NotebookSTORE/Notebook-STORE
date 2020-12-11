package com.notebook.android.ui.drawerFrag

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.notebook.android.ui.dashboard.repository.DashboardRepo

class AboutUsVmFactory(
    val dashboardRepo: DashboardRepo
): ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return AboutUsViewModel(dashboardRepo) as T
    }
}
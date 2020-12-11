package com.notebook.android.ui.filter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.notebook.android.ui.auth.repository.AuthRepository
import com.notebook.android.ui.auth.viewmodel.AuthViewModel

class FilterVMFactory(
    val filterRepo: FilterRepo
): ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return FilterByVM(filterRepo) as T
    }
}
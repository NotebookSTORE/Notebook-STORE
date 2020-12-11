package com.notebook.android.ui.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class FilterCommonProductVMFactory(
    val filterProdRepo:FilterCommonProductRepository
): ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return FilterCommonProductVM(filterProdRepo) as T
    }
}
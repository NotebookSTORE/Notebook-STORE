package com.notebook.android.ui.dashboard.searchProduct

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class SearchProductVMFactory(
    val searchProdRepo: SearchProdRepo
): ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return SearchProductVM(searchProdRepo) as T
    }
}
package com.notebook.android.ui.productDetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class DetailProductVMFactory(
    val prodDetailRepo: ProdDetailRepo
): ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return DetailProductVM(prodDetailRepo) as T
    }
}
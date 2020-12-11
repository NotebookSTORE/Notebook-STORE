package com.notebook.android.ui.category.productParts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ProductVMFactory(
    val productRepo: BSLProductRepo
): ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return BSLProductVM(productRepo) as T
    }
}
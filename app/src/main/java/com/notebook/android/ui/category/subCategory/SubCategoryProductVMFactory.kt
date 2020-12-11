package com.notebook.android.ui.category.subCategory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class SubCategoryProductVMFactory(
    val subCategRepo: SubCategProdRepo
): ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return SubCategoryProductVM(subCategRepo) as T
    }
}
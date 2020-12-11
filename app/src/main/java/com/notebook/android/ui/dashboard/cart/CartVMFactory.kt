package com.notebook.android.ui.dashboard.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class CartVMFactory(
    val cartRepo: CartRepo
): ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return CartVM(cartRepo) as T
    }
}
package com.notebook.android.ui.orderSummary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class OrderSummaryVMFactory(
    val repo:OrderSummaryRepo
) :ViewModelProvider.NewInstanceFactory(){

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return OrderSummaryVM(repo) as T
    }
}
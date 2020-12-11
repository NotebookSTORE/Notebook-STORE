package com.notebook.android.ui.productDetail.base

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedViewModel : ViewModel() {
    val sharedData = MutableLiveData<Any>()

    fun share(obj: Any) {
        sharedData.value = obj
    }
}
package com.notebook.android.ui.productDetail

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.notebook.android.data.db.entities.OrderSummaryProduct

class SharedVM : ViewModel() {

    var productOrderList: MutableLiveData<ArrayList<OrderSummaryProduct>> = MutableLiveData()
    var productDeliveryCharge : MutableLiveData<Float> = MutableLiveData()
//    var prodCouponLiveData : MutableLiveData<ProductCoupon.ProdCoupon> = MutableLiveData()
    var codOptionLiveData : MutableLiveData<Int> = MutableLiveData()
    var freeDeliveryAmountLiveData : MutableLiveData<Int> = MutableLiveData()

    fun setProductOrderSummaryList(prodList:ArrayList<OrderSummaryProduct>){
        productOrderList.value?.forEach {ab->
            Log.d("testing vm b:", "${ab.cartTotalAmount}")
        }
        productOrderList.value = prodList
        productOrderList.value?.forEach {ab->
            Log.d("testing vm a:", "${ab.cartTotalAmount}")
        }
    }

    fun setDeliveryCharge(delCharges: Float) {
        productDeliveryCharge.value = delCharges
    }

    /*fun setCouponData(couponData:ProductCoupon.ProdCoupon){
        prodCouponLiveData.value = couponData
    }*/

    fun setCodOptionForPayment(cod:Int){
        codOptionLiveData.value = cod
    }

    fun setFreeDeliveryData(freeDelAmount:Int){
        freeDeliveryAmountLiveData.value = freeDelAmount
    }
}
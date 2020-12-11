package com.notebook.android.utility

import android.graphics.Paint
import android.util.Log
import android.widget.TextView
import androidx.databinding.BindingAdapter
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

@BindingAdapter("orderQuantityTimes")
fun setOrderQuantityMultiple(textview: TextView, quantity:Int){
    textview.text = "x${quantity}"
}

@BindingAdapter(value = ["price", "discount", "cartQty"])
fun setOrderSummaryAmount(textview: TextView, price:Float, discount:Int,  cartQty:Int){
    val result = (price * discount) / 100f
    Log.e("priceCalculation", " :: ${price} :: ${discount} :: ${result} :: ${price-result}")
    textview.text = "₹ ${(price-result).times(cartQty)}"
}

@BindingAdapter(value = ["priceOrder", "discountOrder", "cartQtyOrder", "shipingChargeOrder"])
fun setOrderTotalAmount(textview: TextView, price:Float, discount:Int,  cartQty:Int, shipCharge:Float){
    val result = (price * discount) / 100f
    textview.text = "₹ ${((price-result).times(cartQty)).plus(shipCharge)}"
}

@BindingAdapter("orderQuantityCustom")
fun setOrderQuantity(textview: TextView, qty:Int){
    textview.text = "Qty : ${qty}"
}

@BindingAdapter("orderDeliveryCharges")
fun setDeliveryCharges(textview: TextView, delCharge:Int){
    textview.text = "₹ ${delCharge}"
}

@BindingAdapter("orderPriceCustom")
fun setOrderPriceCustomr(textview: TextView, price:Float){
    textview.text = "Price : Rs.${price}"
}

@BindingAdapter("couponCode")
fun setOrderTotalAmount(textview: TextView, code:String){
    textview.text = "# ${code}"
}

@BindingAdapter("orderApprovedDate")
fun orderApprovedDate(textview:TextView, date:String){
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    val dateParse = dateFormat.parse(date)//You will get date object relative to server/client timezone wherever it is parsed
    val formatter = SimpleDateFormat("dd MMM, yyyy", Locale.getDefault()); //If you need time just put specific format for time like 'HH:mm:ss'
    val convertedDate = formatter.format(dateParse!!)
    textview.text = "${convertedDate}"
}

@BindingAdapter("orderExpectedDate")
fun orderExpectedDate(textview:TextView, date:String?){
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val formatter = SimpleDateFormat("dd MMM, yyyy", Locale.getDefault()); //If you need time just put specific format for time like 'HH:mm:ss'

    if(!date.isNullOrEmpty()){
        val dateParse = dateFormat.parse(date)//You will get date object relative to server/client timezone wherever it is parsed
        val convertedDate = formatter.format(dateParse!!)
        textview.text = "${convertedDate}"
    }else{
        textview.text = ""
    }
}

@BindingAdapter(value = ["address", "city",  "state", "country", "zipcode"])
fun setOrderAddress(textview: TextView, address:String, city:String, state:String, country:String, zipcode:String){
    textview.text = "${address}, ${city}, ${state}, ${country} - ${zipcode}"
}

@BindingAdapter("returnPolicyDate")
fun setReturnPolicyDate(textview: TextView, date:String?){
    if(date.isNullOrEmpty()){
        textview.text = ""/*No return policy available*/
    }else{
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val dateParse = dateFormat.parse(date)//You will get date object relative to server/client timezone wherever it is parsed
        val formatter = SimpleDateFormat("dd MMM, yyyy", Locale.getDefault()); //If you need time just put specific format for time like 'HH:mm:ss'
        val convertedDate = formatter.format(dateParse!!)
        textview.text = "Return Policy ended on ${convertedDate}"
    }
}
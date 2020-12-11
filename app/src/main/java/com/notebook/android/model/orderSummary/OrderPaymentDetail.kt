package com.notebook.android.model.orderSummary

data class OrderPaymentDetail(
    var userID:Int,
    var token:String,
    var address:String,//street, locality dena hai...
    var name:String?,
    var phone:String,
    var email:String,
    var state:String,
    var city:String,
    var country:String,
    var zipcode:String,
    var amountPayable:Float,
    var paymentmethod:String,
    var amountafterdiscount:Float,
    var coupon_code:String,
    var discount:String,
    var productData:ArrayList<ProductData>,
    var expected_date:String,
    var paymentType:Int,
    var delivery_charges:Float,
    var primeUpdated:Int
) {
    data class ProductData(
        var cartQuantity:Int,
        var original_price:Float,
        var price:Float,
        var id:String,
        var totalamount:Float)
}
package com.notebook.android.adapter.order

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.notebook.android.BR
import com.notebook.android.R
import com.notebook.android.adapter.DetailProduct.DetailProductSpinnerAdpater
import com.notebook.android.data.db.entities.CouponApply
import com.notebook.android.data.db.entities.OrderSummaryProduct
import com.notebook.android.databinding.OrderSummaryProductLayoutBinding
import com.notebook.android.ui.orderSummary.OrderSummary.Companion.applyCoupon
import com.notebook.android.ui.orderSummary.OrderSummary.Companion.productID
import com.notebook.android.ui.orderSummary.OrderSummary.Companion.totalAmountPayableCouponCheck
import com.notebook.android.ui.orderSummary.OrderSummary.Companion.userData
import com.notebook.android.utility.Constant
import kotlin.math.roundToInt

class OrderSummaryAdapter(val mCtx: Context, val prodListData: ArrayList<OrderSummaryProduct>,
                          private val orderPriceListener: OrderPriceListener)
: RecyclerView.Adapter<OrderSummaryAdapter.OrderSummaryVH>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): OrderSummaryVH {
        val orderSummaryBinding: OrderSummaryProductLayoutBinding = DataBindingUtil.inflate(
            LayoutInflater.from(mCtx), R.layout.order_summary_product_layout, parent, false)
        return OrderSummaryVH(orderSummaryBinding)
    }

    inner class OrderSummaryVH(val orderSummaryBinding: OrderSummaryProductLayoutBinding)
        : RecyclerView.ViewHolder(orderSummaryBinding.root) {

        private var prodQty = 0

        fun bind(prodData: OrderSummaryProduct){
            orderSummaryBinding.setVariable(BR.productOrderModel, prodData)
            orderSummaryBinding.executePendingBindings()

            Log.e("orderSummaryAdapter", " :: pos->${adapterPosition} :: qty->${prodData.cartQuantity}" +
                    " :: totalAmount->${prodData.cartTotalAmount} :: ${prodData.discount} :: ${prodData.price}")
            orderSummaryBinding.tvProdQuantity.text = "${prodData.cartQuantity} Pack"
            val result = (prodData.price.times(prodData.discount)).div(100f)
            val finalResult = prodData.price.minus(result)
            orderSummaryBinding.tvProdAmount.text = "₹ ${prodData.cartQuantity.times(finalResult)}"
            Log.e("result", " :: result -> ${result} :: finalResult -> ${finalResult} :: ${prodQty.times(finalResult)}")
//            orderSummaryBinding.tvProdDeliveryBy.text = "Delivery by Tomorrow,Fri | ₹ ${prodData.delivery_charges}"

            if(prodData.quantity != 0){
                val qtyIntList = setQuantityIntegerList(prodData.quantity)
                val qtyList = setQuantityList(prodData.quantity)
                val qtyAdapter = DetailProductSpinnerAdpater(mCtx, qtyList)
                orderSummaryBinding.spProductQuantity.adapter = qtyAdapter

                var cartQtyPosition = 0
                for(pos in 0 until qtyList.size){
                    Log.e("itemPosAdapter", " :: ${prodData.cartQuantity} :: ${qtyIntList[pos]}")
                    if(prodData.cartQuantity == qtyIntList[pos]){
                        cartQtyPosition = pos
                        Log.e("loopPos", " :: $pos")
                    }
                }
                orderSummaryBinding.spProductQuantity.setSelection(cartQtyPosition, true)

                orderSummaryBinding.spProductQuantity.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
                    override fun onNothingSelected(p0: AdapterView<*>?) {}

                    override fun onItemSelected(parent: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                        prodQty = qtyIntList[p2]

                        orderSummaryBinding.tvProdQuantity.text = "${prodQty} Pack"
                        val result = (prodData.price.times(prodData.discount)).div(100f)
                        val finalResult = prodData.price.minus(result)
                        orderSummaryBinding.tvProdAmount.text = "₹ ${prodQty.times(finalResult)}"

                        Log.e("totalAmountCheckSpinner", " :: ${prodQty.times(finalResult)}")
                        prodData.cartQuantity = prodQty
                        prodData.cartTotalAmount = prodQty.times(prodData.price)
                        orderPriceListener.onChangeProdQuantity(adapterPosition, prodData.id.toInt(), prodQty, prodQty.times(finalResult))
                        checkApplyCoupon()
                    }
                }
            }
        }

        private fun checkApplyCoupon() {
            Log.e("totalAmountCheck", " :: $totalAmountPayableCouponCheck :: " +
                    "${applyCoupon?.coupon_type}")
            if(applyCoupon?.coupon_type?.equals(Constant.COUPON_USER_TYPE_PRODUCT_ONLY, true) == true){

                if(!productID.isNullOrEmpty()){
                    if (applyCoupon?.product_id.equals(productID)){
                        if(applyCoupon?.coupon_user_type?.toInt() == Constant.REGULAR_MERCHANT_TYPE
                            || applyCoupon?.coupon_user_type?.toInt() == Constant.NORMAL_MERCHANT_TYPE){
                            if(totalAmountPayableCouponCheck >= applyCoupon?.max_amount?.toFloat()?:0f){
                                orderPriceListener.onPriceCheckOnCoupon(true)
                            }else{
                                orderPriceListener.onPriceCheckOnCoupon(false)
                            }
                        }else if(applyCoupon?.coupon_user_type?.toInt() == Constant.PRIME_MERCHANT_TYPE){
                            if(totalAmountPayableCouponCheck >= applyCoupon?.max_amount?.toFloat()?:0f){
                                orderPriceListener.onPriceCheckOnCoupon(true)
                            }else{
                                orderPriceListener.onPriceCheckOnCoupon(false)
                            }
                        }else if(applyCoupon?.coupon_user_type?.toInt() == Constant.COUPON_USER_TYPE_SPECIAL){
                            if(applyCoupon?.email_can_avail.isNullOrEmpty()){
                                if(totalAmountPayableCouponCheck >= applyCoupon?.max_amount?.toFloat()?:0f){
                                    orderPriceListener.onPriceCheckOnCoupon(true)
                                }else{
                                    orderPriceListener.onPriceCheckOnCoupon(false)
                                }
                            }else{
                                if(applyCoupon?.email_can_avail.equals(userData?.email, true)){
                                    if(totalAmountPayableCouponCheck >= applyCoupon?.max_amount?.toFloat()?:0f){
                                    orderPriceListener.onPriceCheckOnCoupon(true)
                                }else{
                                    orderPriceListener.onPriceCheckOnCoupon(false)
                                }
                                }else{
                                    orderPriceListener.errorMessage("Your email id not match")
                                }
                            }

                        }else if(applyCoupon?.coupon_user_type?.toInt() == Constant.COUPON_USER_TYPE_BULK){
                            if(userData?.registerfor == 2){
                                if(totalAmountPayableCouponCheck >= applyCoupon?.max_amount?.toFloat()?:0f){
                                    orderPriceListener.onPriceCheckOnCoupon(true)
                                }else{
                                    orderPriceListener.onPriceCheckOnCoupon(false)
                                }
                            }else{
                                orderPriceListener.errorMessage("You are not registered as Institution")
                            }
                        }
                    }else{
                        orderPriceListener.errorMessage("This coupon is not applicable for this product due to id mismatch")
                    }
                }

            } else{
                if (applyCoupon?.coupon_type?.toInt() == Constant.COUPON_USER_TYPE_NORMAL_GENERIC){
                    if(totalAmountPayableCouponCheck >= applyCoupon?.max_amount?.toFloat()?:0f){
                        orderPriceListener.onPriceCheckOnCoupon(true)
                    }else{
                        orderPriceListener.onPriceCheckOnCoupon(false)
                    }
                }else if (applyCoupon?.coupon_type?.toInt() == Constant.COUPON_USER_TYPE_GENERIC_INSTITUTE){
                    if(userData?.registerfor == 2){
                        if(totalAmountPayableCouponCheck >= applyCoupon?.max_amount?.toFloat()?:0f){
                            orderPriceListener.onPriceCheckOnCoupon(true)
                        }else{
                            orderPriceListener.onPriceCheckOnCoupon(false)
                        }
                    }else{
                        orderPriceListener.errorMessage("You are not registered as Institution")
                    }
                }
            }
        }
        private fun setQuantityList(qty:Int) : ArrayList<String> {
            val qtyArray = ArrayList<String>()
            for(i in 0 until qty){
                qtyArray.add("${i+1} Pcs")
            }
            return qtyArray
        }

        private fun setQuantityIntegerList(qty:Int) : ArrayList<Int> {
            val qtyArray = ArrayList<Int>()
            for(i in 0 until qty){
                qtyArray.add(i+1)
            }
            return qtyArray
        }
    }


    override fun getItemCount(): Int {
        return prodListData.size
    }

    override fun onBindViewHolder(holder: OrderSummaryVH, position: Int) {
        holder.bind(prodListData[position])
    }

    interface OrderPriceListener{
        fun onChangeProdQuantity(orderItemPosition:Int, prodID:Int, orderQty:Int, orderAmount:Float)
        fun onPriceCheckOnCoupon(isGreater:Boolean)
        fun errorMessage(msg:String)
    }
}
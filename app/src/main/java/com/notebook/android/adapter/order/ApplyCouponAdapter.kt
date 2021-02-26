package com.notebook.android.adapter.order

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.notebook.android.BR
import com.notebook.android.R
import com.notebook.android.data.db.entities.CouponApply
import com.notebook.android.databinding.ApplyCouponLayoutBinding
import com.notebook.android.ui.orderSummary.OrderSummary

class ApplyCouponAdapter(val mCtx: Context, var prodID:String, var emailID:String, var registerFor:Int,
                         var userType:Int, var totalAmount:Float, val couponListData: List<CouponApply>,
val applyCouponListener:ApplyCouponListener)
: RecyclerView.Adapter<ApplyCouponAdapter.OrderSummaryVH>() {

    init {
        Log.e("couponApplicableData", " :: $prodID :: $emailID :: $registerFor :: $userType :: $totalAmount")
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): OrderSummaryVH {
        val couponItemBindig: ApplyCouponLayoutBinding = DataBindingUtil.inflate(
            LayoutInflater.from(mCtx), R.layout.apply_coupon_layout, parent, false)
        return OrderSummaryVH(couponItemBindig)
    }

    inner class OrderSummaryVH(val couponItemBindig: ApplyCouponLayoutBinding)
        : RecyclerView.ViewHolder(couponItemBindig.root) {

        var isProductAvailable = false

        fun bind(couponData: CouponApply) {
            couponItemBindig.setVariable(BR.couponModel, couponData)
            couponItemBindig.executePendingBindings()

            couponItemBindig.tvApplyClick.setOnClickListener {

                if (prodID.isEmpty()) {
                    loop@ for (couponCanApply in OrderSummary.couponCanApplyListData) {
                        if (couponData.code == couponCanApply.code) {
                            applyCouponListener.onApplyCoupon(couponData)
                            isProductAvailable = true
                            break@loop
                        } else {
                            isProductAvailable = false
                        }
                    }
                } else {
                    isProductAvailable = if (totalAmount > (couponData.max_amount?.toFloat() ?: 0f)) {
                        applyCouponListener.onApplyCoupon(couponData)
                        true
                    } else {
                        false
                    }
                }

                if (!isProductAvailable) {
                    applyCouponListener.errorMessage("This coupon is not applicable for this order")
                }
            }

            /*if(couponData.coupon_type?.equals(Constant.COUPON_USER_TYPE_PRODUCT_ONLY, true) == true){

                     loop@ for (applyProd in prodList) {
                            if (couponData.product_id?.equals(applyProd.id) == true) {

                                if (couponData.coupon_user_type?.toInt() == userType
                                    || couponData.coupon_user_type?.toInt() == userType
                                ) {
                                    if (totalAmount >= couponData.max_amount?.toFloat() ?: 0f) {
                                        applyCouponListener.onApplyCoupon(couponData)
                                    } else {
                                        applyCouponListener.errorMessage("Your payable amount is less than coupon amount. You need to shop more")
                                    }
                                } else if (couponData.coupon_user_type?.toInt() == userType) {
                                    if (totalAmount >= couponData.max_amount?.toFloat() ?: 0f) {
                                        applyCouponListener.onApplyCoupon(couponData)
                                    } else {
                                        applyCouponListener.errorMessage("Your payable amount is less than coupon amount. You need to shop more")
                                    }
                                } else if (couponData.coupon_user_type?.toInt() == Constant.COUPON_USER_TYPE_SPECIAL) {
                                    if (couponData.email_can_avail.isNullOrEmpty()) {
                                        if (totalAmount >= couponData.max_amount?.toFloat() ?: 0f) {
                                            applyCouponListener.onApplyCoupon(couponData)
                                        } else {
                                            applyCouponListener.errorMessage("Your payable amount is less than coupon amount. You need to shop more")
                                        }
                                    } else {
                                        if (couponData.email_can_avail.equals(emailID, true)) {
                                            if (totalAmount >= couponData.max_amount?.toFloat() ?: 0f) {
                                                applyCouponListener.onApplyCoupon(couponData)
                                            } else {
                                                applyCouponListener.errorMessage("Your payable amount is less than coupon amount. You need to shop more")
                                            }
                                        } else {
                                            applyCouponListener.errorMessage("You are not valid for this special coupon")
                                        }
                                    }

                                } else if (couponData.coupon_user_type?.toInt() == Constant.COUPON_USER_TYPE_BULK) {
                                    if (registerFor == 2) {
                                        if (totalAmount >= couponData.max_amount?.toFloat() ?: 0f) {
                                            applyCouponListener.onApplyCoupon(couponData)
                                        } else {
                                            applyCouponListener.errorMessage("Your payable amount is less than coupon amount. You need to shop more")
                                        }
                                    } else {
                                        applyCouponListener.errorMessage("You are not registered as Institution type")
                                    }
                                } else {
                                    if (userType == Constant.PRIME_MERCHANT_TYPE) {
                                        if (couponData.coupon_user_type?.toInt() != userType) {
                                            if (couponData.coupon_user_type?.toInt() == Constant.REGULAR_MERCHANT_TYPE) {
                                                applyCouponListener.errorMessage("This Coupon for Regular Merchant")
                                            }
                                        }
                                    } else if (userType == Constant.REGULAR_MERCHANT_TYPE) {
                                        if (couponData.coupon_user_type?.toInt() != userType) {
                                            if (couponData.coupon_user_type?.toInt() == Constant.PRIME_MERCHANT_TYPE) {
                                                applyCouponListener.errorMessage("To avail this coupon become a Prime Merchant")
                                            }
                                        }
                                    } else if (userType == Constant.NORMAL_MERCHANT_TYPE) {
                                        if (couponData.coupon_user_type?.toInt() != userType) {
                                            if (couponData.coupon_user_type?.toInt() == Constant.PRIME_MERCHANT_TYPE) {
                                                applyCouponListener.errorMessage("To avail this coupon become a Prime Merchant")
                                            } else {
                                                applyCouponListener.errorMessage("This Coupon for Regular Merchant")
                                            }
                                        }
                                    } else {
                                        applyCouponListener.errorMessage("This coupon is not applicable for this merchant")
                                    }
                                }
                                isProductAvailable = true
                                break@loop
                            } else {
                                isProductAvailable = false
                            }
                        }

                     if (!isProductAvailable) {
                         applyCouponListener.errorMessage("This coupon is not applicable for this product because id is different")
                     }

                    } else{
                        if (couponData.coupon_type?.toInt() == Constant.COUPON_USER_TYPE_NORMAL_GENERIC){
                            if(totalAmount >= couponData.max_amount?.toFloat()?:0f){
                                applyCouponListener.onApplyCoupon(couponData)
                            }else{
                                applyCouponListener.errorMessage("Your payable amount is less than coupon amount. You need to shop more")
                            }
                        }else if (couponData.coupon_type?.toInt() == Constant.COUPON_USER_TYPE_GENERIC_INSTITUTE){
                            if(registerFor == 2){
                                if(totalAmount >= couponData.max_amount?.toFloat()?:0f){
                                    applyCouponListener.onApplyCoupon(couponData)
                                }else{
                                    applyCouponListener.errorMessage("Your payable amount is less than coupon amount. You need to shop more")
                                }
                            }else{
                                applyCouponListener.errorMessage("You are not registered as Institution type")
                            }
                        }
                }*/
        }
    }

    override fun getItemCount(): Int {
        return couponListData.size
    }

    override fun onBindViewHolder(holder: OrderSummaryVH, position: Int) {
        holder.bind(couponListData[position])
    }

    interface ApplyCouponListener{
        fun onApplyCoupon(couponData: CouponApply)
        fun errorMessage(msg:String)
    }
}
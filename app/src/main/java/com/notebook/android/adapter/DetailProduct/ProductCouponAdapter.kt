package com.notebook.android.adapter.DetailProduct

import android.content.Context
import android.graphics.Paint
import android.opengl.Visibility
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.notebook.android.BR
import com.notebook.android.R
import com.notebook.android.data.db.entities.DiscountedProduct
import com.notebook.android.databinding.DiscountedProductsLayoutBinding
import com.notebook.android.databinding.ProductCouponLayoutBinding
import com.notebook.android.model.home.ProductCoupon
import com.notebook.android.ui.dashboard.frag.fragHome.DetailViewProductFrag
import com.notebook.android.ui.dashboard.frag.fragHome.DetailViewProductFrag.Companion.productPrice
import com.notebook.android.utility.Constant
import java.util.*

class ProductCouponAdapter(val mCtx: Context, val productList: ArrayList<ProductCoupon.ProdCoupon>,
                          private var discProductListener: ProductCouponListener)
    : RecyclerView.Adapter<ProductCouponAdapter.ProductVH>() {

    private var selectedPos:Int = -1
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ProductVH {
        val couponItemBinding: ProductCouponLayoutBinding = DataBindingUtil.inflate(
            LayoutInflater.from(mCtx), R.layout.product_coupon_layout, parent, false)
        return ProductVH(couponItemBinding)
    }

    inner class ProductVH(val latestProdBinding: ProductCouponLayoutBinding)
        : RecyclerView.ViewHolder(latestProdBinding.root) {

        fun bind(discountedProd: ProductCoupon.ProdCoupon){
            latestProdBinding.setVariable(BR.couponData, discountedProd)
            latestProdBinding.executePendingBindings()
        }
    }

    override fun getItemCount(): Int {
        return productList.size
    }

    override fun onBindViewHolder(holder: ProductVH, position: Int) {
        holder.bind(productList[position])

//        val couponData = productList[position]

        if(selectedPos==position)
            holder.latestProdBinding.imgCouponSelected.visibility = View.VISIBLE
        else
            holder.latestProdBinding.imgCouponSelected.visibility = View.GONE

        /*holder.latestProdBinding.clCouponItemView.setOnClickListener {
            if(DetailViewProductFrag.userData != null){
                if(couponData.usertype == DetailViewProductFrag.userData!!.usertype){
                    if (productPrice!!.compareTo(couponData.totalamount.toFloat())>=0){
                        selectedPos= position
                        discProductListener.prodCouponObj(productList[selectedPos])
                        notifyDataSetChanged()
                    }else{
                        discProductListener.upgradeToPrimeMerchant("Your price not match with selected coupon.")
                    }
                }else if(couponData.usertype == DetailViewProductFrag.userData!!.usertype){
                    if (productPrice!!.compareTo(couponData.totalamount.toFloat())>=0){
                        selectedPos= position
                        discProductListener.prodCouponObj(productList[selectedPos])
                        notifyDataSetChanged()
                    }else{
                        discProductListener.upgradeToPrimeMerchant("Your price not match with selected coupon.")
                    }
                }else{
                    discProductListener.upgradeToPrimeMerchant("Please upgrade your account for use this coupon.")
                }
            }else{
                discProductListener.couponForLogin("Please login first to use coupon")
            }
        }*/
    }

    interface ProductCouponListener{
        fun prodCouponObj(coupon: ProductCoupon.ProdCoupon)
        fun upgradeToPrimeMerchant(msg:String)
        fun couponForLogin(msg:String)
    }
}
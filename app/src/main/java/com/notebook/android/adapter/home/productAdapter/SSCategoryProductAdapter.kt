package com.notebook.android.adapter.home.productAdapter

import android.content.Context
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.notebook.android.BR
import com.notebook.android.R
import com.notebook.android.data.db.entities.HomeSubSubCategoryProduct
import com.notebook.android.databinding.SubSubCategoryProductLayoutBinding

class SSCategoryProductAdapter (val mCtx: Context, val ssCategProductList:ArrayList<HomeSubSubCategoryProduct>,
                                val ssCategProductListen: SSCategoryProductListener)
    : RecyclerView.Adapter<SSCategoryProductAdapter.LatestViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): LatestViewHolder {
        val ssCategItemBinding: SubSubCategoryProductLayoutBinding = DataBindingUtil.inflate(
            LayoutInflater.from(mCtx), R.layout.sub_sub_category_product_layout, parent, false)
        return LatestViewHolder(ssCategItemBinding)
    }

    inner class LatestViewHolder(val ssCategProdBinding: SubSubCategoryProductLayoutBinding)
        : RecyclerView.ViewHolder(ssCategProdBinding.root) {

        fun bind(Prod: HomeSubSubCategoryProduct){
            ssCategProdBinding.tvAfterAddingPriceInDiscount.paintFlags = ssCategProdBinding
                .tvAfterAddingPriceInDiscount.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            ssCategProdBinding.rbProductRating.rating = Prod.customerRating?:4f

            ssCategProdBinding.setVariable(BR.sscategoryProduct, Prod)
            ssCategProdBinding.executePendingBindings()

            ssCategProdBinding.clTopProductLayout.setOnClickListener {
                ssCategProductListen.ssCategProductCallback(Prod)
            }

            ssCategProdBinding.imgAddToCart.setOnClickListener {
                ssCategProductListen.ssCategAddToCart(Prod.id!!, 1)
            }

        }
    }

    override fun getItemCount(): Int {
        return ssCategProductList.size
    }

    override fun onBindViewHolder(holder: LatestViewHolder, position: Int) {
        holder.bind(ssCategProductList[position])
    }

    interface SSCategoryProductListener{
        fun ssCategProductCallback(ssCategProd: HomeSubSubCategoryProduct)
        fun ssCategAddToCart(prodID:Int, cartQty:Int)
    }
}
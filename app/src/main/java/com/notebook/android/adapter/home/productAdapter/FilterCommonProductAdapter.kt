package com.notebook.android.adapter.home.productAdapter

import android.content.Context
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.notebook.android.BR
import com.notebook.android.R
import com.notebook.android.data.db.entities.BestSeller
import com.notebook.android.data.db.entities.FilterProduct
import com.notebook.android.databinding.BestSellerProductLayoutBinding
import com.notebook.android.databinding.FilterCommonProductItemLayoutBinding

class FilterCommonProductAdapter(val mCtx: Context, val fcProductList:ArrayList<FilterProduct>,
                                 val fcProductListener: FilterCommonProductListener)
    : RecyclerView.Adapter<FilterCommonProductAdapter.FCViewHolder>() {

    override fun onViewRecycled(holder: FCViewHolder) {
        super.onViewRecycled(holder)
        Glide.with(mCtx).clear(holder.itemView.rootView)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): FCViewHolder {
        val filterProdItemBinding: FilterCommonProductItemLayoutBinding = DataBindingUtil.inflate(
            LayoutInflater.from(mCtx), R.layout.filter_common_product_item_layout, parent, false)
        return FCViewHolder(filterProdItemBinding)
    }

    inner class FCViewHolder(val filterProdItemBinding: FilterCommonProductItemLayoutBinding)
        : RecyclerView.ViewHolder(filterProdItemBinding.root) {

        fun bind(filterProd: FilterProduct){
            filterProdItemBinding.rbProductRating.rating = filterProd.customerRating?:4f
            filterProdItemBinding.setVariable(BR.filterCommonProduct, filterProd)
            filterProdItemBinding.executePendingBindings()

            if(filterProd.quantity <= 0){
                filterProdItemBinding.imgAddToCart.visibility = View.GONE
                filterProdItemBinding.clOutStockLayout.visibility = View.GONE
//                filterProdItemBinding.clTopProductLayout.isEnabled = false
                filterProdItemBinding.imgAddToCart.isEnabled = false
            }else{
                filterProdItemBinding.imgAddToCart.visibility = View.VISIBLE
                filterProdItemBinding.clOutStockLayout.visibility = View.GONE
//                filterProdItemBinding.clTopProductLayout.isEnabled = true
                filterProdItemBinding.imgAddToCart.isEnabled = true
            }

            filterProdItemBinding.clTopProductLayout.setOnClickListener {
                if(filterProd.quantity <= 0){
                    fcProductListener.fcCartEmptyError("Product is Out of Stock")
                }else{
                    fcProductListener.fcProductCallback(filterProd, filterProdItemBinding.imgProductImage)
                }
            }

            filterProdItemBinding.imgAddToCart.setOnClickListener {
                fcProductListener.fcAddToCart(filterProd.id, 1)
            }
        }
    }

    override fun getItemCount(): Int {
        return fcProductList.size
    }

    override fun onBindViewHolder(holder: FCViewHolder, position: Int) {
        holder.bind(fcProductList[position])
    }

    interface FilterCommonProductListener{
        fun fcProductCallback(fcProd: FilterProduct, imgProduct:ImageView)
        fun fcAddToCart(prodID:Int, cartQty:Int)
        fun fcCartEmptyError(msg:String)
    }
}
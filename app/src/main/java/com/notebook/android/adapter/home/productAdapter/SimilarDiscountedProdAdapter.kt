package com.notebook.android.adapter.home.productAdapter

import android.content.Context
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.notebook.android.BR
import com.notebook.android.R
import com.notebook.android.data.db.entities.Product
import com.notebook.android.databinding.SimilarDiscountedProductLayoutBinding

class SimilarDiscountedProdAdapter (val mCtx: Context, val similarProductList:List<Product>,
                                    val similarProductListen: SimilarProductListener)
    : RecyclerView.Adapter<SimilarDiscountedProdAdapter.LatestViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): LatestViewHolder {
        val similarProdItemBinding: SimilarDiscountedProductLayoutBinding = DataBindingUtil.inflate(
            LayoutInflater.from(mCtx), R.layout.similar_discounted_product_layout, parent, false)
        return LatestViewHolder(similarProdItemBinding)
    }

    inner class LatestViewHolder(val similarProdItemBinding: SimilarDiscountedProductLayoutBinding)
        : RecyclerView.ViewHolder(similarProdItemBinding.root) {

        fun bind(Prod: Product){
            similarProdItemBinding.tvAfterAddingPriceInDiscount.paintFlags = similarProdItemBinding
                .tvAfterAddingPriceInDiscount.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG

            similarProdItemBinding.setVariable(BR.similarProduct, Prod)
            similarProdItemBinding.executePendingBindings()

            itemView.rootView.setOnClickListener {
                similarProductListen.similarProductCallback(Prod)
            }

            similarProdItemBinding.rbProductRating.rating = Prod.customerRating?:4f
            similarProdItemBinding.clTopProductLayout.setOnClickListener {
                similarProductListen.similarProductCallback(Prod)
            }

            similarProdItemBinding.imgAddToCart.setOnClickListener {
                similarProductListen.similarProdAddToCart(Prod.id!!.toInt(), 1)
            }

        }
    }

    override fun getItemCount(): Int {
        return similarProductList.size
    }

    override fun onBindViewHolder(holder: LatestViewHolder, position: Int) {
        holder.bind(similarProductList[position])
    }

    interface SimilarProductListener{
        fun similarProductCallback(similarProd: Product)
        fun similarProdAddToCart(prodID:Int, cartQty:Int)
    }
}
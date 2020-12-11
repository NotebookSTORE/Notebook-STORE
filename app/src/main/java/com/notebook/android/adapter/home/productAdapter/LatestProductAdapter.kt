package com.notebook.android.adapter.home.productAdapter

import android.content.Context
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.notebook.android.BR
import com.notebook.android.R
import com.notebook.android.data.db.entities.LatestProduct
import com.notebook.android.databinding.LatestProductLayoutBinding

class LatestProductAdapter (val mCtx: Context, val latestProductList:ArrayList<LatestProduct>,
                            val latestProductListen: LatestProductListener)
    : RecyclerView.Adapter<LatestProductAdapter.LatestViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): LatestViewHolder {
        val latestItemBinding: LatestProductLayoutBinding = DataBindingUtil.inflate(
            LayoutInflater.from(mCtx), R.layout.latest_product_layout, parent, false)
        return LatestViewHolder(latestItemBinding)
    }

    inner class LatestViewHolder(val latestProdBinding: LatestProductLayoutBinding)
        : RecyclerView.ViewHolder(latestProdBinding.root) {

        fun bind(Prod: LatestProduct){
            latestProdBinding.tvAfterAddingPriceInDiscount.paintFlags = latestProdBinding
                .tvAfterAddingPriceInDiscount.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            latestProdBinding.rbProductRating.rating = Prod.customerRating?:4f

            latestProdBinding.setVariable(BR.latestProduct, Prod)
            latestProdBinding.executePendingBindings()

            latestProdBinding.clTopProductLayout.setOnClickListener {
                latestProductListen.latestProductCallback(Prod)
            }

            latestProdBinding.imgAddToCart.setOnClickListener {
                latestProductListen.latestAddToCart(Prod.id!!, 1)
            }

        }
    }

    override fun getItemCount(): Int {
        return latestProductList.size
    }

    override fun onBindViewHolder(holder: LatestViewHolder, position: Int) {
        holder.bind(latestProductList[position])
    }

    interface LatestProductListener{
        fun latestProductCallback(latestProd: LatestProduct)
        fun latestAddToCart(prodID:Int, cartQty:Int)
    }
}
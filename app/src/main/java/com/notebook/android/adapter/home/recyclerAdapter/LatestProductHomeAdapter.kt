package com.notebook.android.adapter.home

import android.content.Context
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.notebook.android.BR
import com.notebook.android.R
import com.notebook.android.data.db.entities.LatestProductHome
import com.notebook.android.databinding.LatestProductHomeLayoutBinding
import java.util.*

class LatestProductHomeAdapter(val mCtx: Context,
                               val latestProductList:ArrayList<LatestProductHome>,
                               val latestProductListen: latestProductListener)
    : RecyclerView.Adapter<LatestProductHomeAdapter.SubCategoryViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SubCategoryViewHolder {
        val subCategItemBinding:LatestProductHomeLayoutBinding = DataBindingUtil.inflate(
            LayoutInflater.from(mCtx), R.layout.latest_product_home_layout, parent, false)
        return SubCategoryViewHolder(subCategItemBinding)
    }

    inner class SubCategoryViewHolder(val latestProdBinding: LatestProductHomeLayoutBinding)
        :RecyclerView.ViewHolder(latestProdBinding.root) {

        fun bind(latestProd:LatestProductHome){
            latestProdBinding.tvAfterAddingPriceInDiscount.paintFlags =
                latestProdBinding.tvAfterAddingPriceInDiscount.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            latestProdBinding.rbProductRating.rating = latestProd.customerRating?:4f

            latestProdBinding.setVariable(BR.latestProductHome, latestProd)
            latestProdBinding.executePendingBindings()

            if(latestProd.quantity <= 0){
                latestProdBinding.imgAddToCart.visibility = View.GONE
            }else{
                latestProdBinding.imgAddToCart.visibility = View.VISIBLE
            }

            latestProdBinding.clTopProductLayout.setOnClickListener {
                if(latestProd.quantity <= 0){
                    latestProductListen.fcCartEmptyError("Product is Out of Stock")
                }else{
                    latestProductListen.latestProductObj(latestProd)
                }
            }

            latestProdBinding.imgAddToCart.setOnClickListener {
                latestProductListen.latestAddToCart(latestProd.id!!, 1)
            }

        }
    }

    override fun getItemCount(): Int {
        return latestProductList.size
    }

    override fun onBindViewHolder(holder: SubCategoryViewHolder, position: Int) {
        holder.bind(latestProductList[position])
    }

    interface latestProductListener{
        fun latestProductObj(latestProd: LatestProductHome)
        fun latestAddToCart(prodID:Int, cartQty:Int)
        fun fcCartEmptyError(msg:String)
    }
}
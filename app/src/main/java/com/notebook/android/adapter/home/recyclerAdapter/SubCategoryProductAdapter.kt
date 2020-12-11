package com.notebook.android.adapter.home.recyclerAdapter

import android.content.Context
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.notebook.android.BR
import com.notebook.android.R
import com.notebook.android.data.db.entities.SubCategoryProduct
import com.notebook.android.databinding.SubCategoryProductLayoutBinding

class SubCategoryProductAdapter(val mCtx: Context, val subCategProductList:ArrayList<SubCategoryProduct>,
                                val subCategProductListen: SubCategoryProductListener)
    : RecyclerView.Adapter<SubCategoryProductAdapter.SubCategoryViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SubCategoryViewHolder {
        val subCategItemBinding:SubCategoryProductLayoutBinding = DataBindingUtil.inflate(
            LayoutInflater.from(mCtx), R.layout.sub_category_product_layout, parent, false)
        return SubCategoryViewHolder(subCategItemBinding)
    }

    inner class SubCategoryViewHolder(val subCategProdBinding: SubCategoryProductLayoutBinding)
        :RecyclerView.ViewHolder(subCategProdBinding.root) {

        fun bind(Prod:SubCategoryProduct){
            subCategProdBinding.tvAfterAddingPriceInDiscount.paintFlags =
                subCategProdBinding.tvAfterAddingPriceInDiscount.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            subCategProdBinding.rbProductRating.rating = Prod.customerRating?:4f

            subCategProdBinding.setVariable(BR.subCategoryProduct, Prod)
            subCategProdBinding.executePendingBindings()

            subCategProdBinding.clTopProductLayout.setOnClickListener {
                subCategProductListen.subCategProductObj(Prod)
            }

            subCategProdBinding.imgAddToCart.setOnClickListener {
                subCategProductListen.subCategAddToCart(Prod.id!!, 1)
            }


        }
    }

    override fun getItemCount(): Int {
        return subCategProductList.size
    }

    override fun onBindViewHolder(holder: SubCategoryViewHolder, position: Int) {
        holder.bind(subCategProductList[position])
    }

    interface SubCategoryProductListener{
        fun subCategProductObj(subCategProd: SubCategoryProduct)
        fun subCategAddToCart(prodID:Int, cartQty:Int)
    }
}
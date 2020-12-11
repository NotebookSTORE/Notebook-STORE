package com.notebook.android.adapter.home.productAdapter

import android.content.Context
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.notebook.android.BR
import com.notebook.android.R
import com.notebook.android.data.db.entities.CategoryProduct
import com.notebook.android.databinding.CategoryProductLayoutBinding

class CategoryProductAdapter (val mCtx: Context, val categoryProductList:ArrayList<CategoryProduct>,
                              val categProductListen: CategoryProductListener)
    : RecyclerView.Adapter<CategoryProductAdapter.LatestViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): LatestViewHolder {
        val categItemBinding: CategoryProductLayoutBinding = DataBindingUtil.inflate(
            LayoutInflater.from(mCtx), R.layout.category_product_layout, parent, false)
        return LatestViewHolder(categItemBinding)
    }

    inner class LatestViewHolder(val categProdBinding: CategoryProductLayoutBinding)
        : RecyclerView.ViewHolder(categProdBinding.root) {

        fun bind(Prod: CategoryProduct){
            categProdBinding.tvAfterAddingPriceInDiscount.paintFlags = categProdBinding
                .tvAfterAddingPriceInDiscount.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            categProdBinding.rbProductRating.rating = Prod.customerRating?:4f

            categProdBinding.setVariable(BR.categoryProduct, Prod)
            categProdBinding.executePendingBindings()

            categProdBinding.clTopProductLayout.setOnClickListener {
                categProductListen.categProductCallback(Prod)
            }

            categProdBinding.imgAddToCart.setOnClickListener {
                categProductListen.categAddToCart(Prod.id!!, 1)
            }

        }
    }

    override fun getItemCount(): Int {
        return categoryProductList.size
    }

    override fun onBindViewHolder(holder: LatestViewHolder, position: Int) {
        holder.bind(categoryProductList[position])
    }

    interface CategoryProductListener{
        fun categProductCallback(categProd: CategoryProduct)
        fun categAddToCart(prodID:Int, cartQty:Int)
    }
}
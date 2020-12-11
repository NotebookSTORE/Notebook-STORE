package com.notebook.android.adapter.home.recyclerAdapter

import android.content.Context
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.notebook.android.BR
import com.notebook.android.R
import com.notebook.android.data.db.entities.SearchProduct
import com.notebook.android.databinding.SearchProductLayoutBinding

class SearchProductAdapter(val mCtx: Context, var searchProductList:ArrayList<SearchProduct>,
                           val searchProductListen: SearchProductListener)
    : RecyclerView.Adapter<SearchProductAdapter.SearchViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SearchViewHolder {
        val searchItemBinding: SearchProductLayoutBinding = DataBindingUtil.inflate(
            LayoutInflater.from(mCtx), R.layout.search_product_layout, parent, false)
        return SearchViewHolder(searchItemBinding)
    }

    inner class SearchViewHolder(val searchProdBinding: SearchProductLayoutBinding)
        : RecyclerView.ViewHolder(searchProdBinding.root) {

        fun bind(Prod: SearchProduct){
            searchProdBinding.tvAfterAddingPriceInDiscount.paintFlags = searchProdBinding
                .tvAfterAddingPriceInDiscount.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            searchProdBinding.setVariable(BR.searchProduct, Prod)
            searchProdBinding.executePendingBindings()

            if(Prod.quantity <= 0){
                searchProdBinding.imgAddToCart.visibility = View.GONE
            }else{
                searchProdBinding.imgAddToCart.visibility = View.VISIBLE
            }

            searchProdBinding.clTopProductLayout.setOnClickListener {
                searchProductListen.searchProductObj(Prod)
            }

            searchProdBinding.imgAddToCart.setOnClickListener {
                searchProductListen.searchAddToCart(Prod.id!!, 1)
            }

        }
    }

    fun setListToAdpater(searchProductList:ArrayList<SearchProduct>){
        this.searchProductList = searchProductList
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return searchProductList.size
    }

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        holder.bind(searchProductList[position])
    }

    interface SearchProductListener{
        fun searchProductObj(searcgProd: SearchProduct)
        fun searchAddToCart(prodID:Int, cartQty:Int)
    }
}
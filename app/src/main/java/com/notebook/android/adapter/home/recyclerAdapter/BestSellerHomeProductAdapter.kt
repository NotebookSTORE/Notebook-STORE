package com.notebook.android.adapter.home

import android.content.Context
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.recyclerview.widget.RecyclerView
import com.max.ecomaxgo.maxpe.view.flight.utility.loadAllTypeImage
import com.max.ecomaxgo.maxpe.view.flight.utility.loadAllTypeImageWithSize
import com.notebook.android.BR
import com.notebook.android.R
import com.notebook.android.data.db.entities.BestSellerHome
import com.notebook.android.databinding.BestSellerProductHomeLayoutBinding
import java.util.*

class BestSellerHomeProductAdapter(val mCtx: Context, val bestSellerList:ArrayList<BestSellerHome>,
                                   val bestSellerProductListener: BestSellerProductListener)
    : RecyclerView.Adapter<BestSellerHomeProductAdapter.SubCategoryViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SubCategoryViewHolder {
        val subCategItemBinding:BestSellerProductHomeLayoutBinding = DataBindingUtil.inflate(
            LayoutInflater.from(mCtx), R.layout.best_seller_product_home_layout, parent, false)
        return SubCategoryViewHolder(subCategItemBinding)
    }

    inner class SubCategoryViewHolder(val bestSellerBinding: BestSellerProductHomeLayoutBinding)
        :RecyclerView.ViewHolder(bestSellerBinding.root) {

        fun bind(bestSeller:BestSellerHome){
            bestSellerBinding.tvAfterAddingPriceInDiscount.paintFlags=
                bestSellerBinding.tvAfterAddingPriceInDiscount.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG

            bestSellerBinding.rbProductRating.rating = bestSeller.customerRating?:4f
            bestSellerBinding.setVariable(BR.bestSellerProduct, bestSeller)
            bestSellerBinding.executePendingBindings()

            loadAllTypeImageWithSize(bestSellerBinding.imgProductImage,"https://notebookstore.in/public/uploads/product/",bestSeller.image,200,200)

            if(bestSeller.quantity <= 0){
                bestSellerBinding.imgAddToCart.visibility = View.GONE
            }else{
                bestSellerBinding.imgAddToCart.visibility = View.VISIBLE
            }

            bestSellerBinding.clTopProductLayout.setOnClickListener {
                if(bestSeller.quantity <= 0){
                    bestSellerProductListener.fcCartEmptyError("Product is Out of Stock")
                }else{
                    bestSellerProductListener.bestProductObj(bestSeller)
                }
            }

            bestSellerBinding.imgAddToCart.setOnClickListener {
                bestSellerProductListener.bestAddToCart(bestSeller.id!!, 1)
            }
        }

    }

    override fun getItemCount(): Int {
        return bestSellerList.size
    }

    override fun onBindViewHolder(holder: SubCategoryViewHolder, position: Int) {
        holder.bind(bestSellerList[position])
    }

    interface BestSellerProductListener{
        fun bestProductObj(bestSellerProd: BestSellerHome)
        fun bestAddToCart(prodID:Int, cartQty:Int)
        fun fcCartEmptyError(msg:String)
    }
}
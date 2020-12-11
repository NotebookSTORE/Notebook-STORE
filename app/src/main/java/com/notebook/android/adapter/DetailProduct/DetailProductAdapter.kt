package com.notebook.android.adapter.DetailProduct

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.notebook.android.BR
import com.notebook.android.R
import com.notebook.android.data.db.entities.DiscountedProduct
import com.notebook.android.databinding.DiscountedProductsLayoutBinding
import java.util.*

class DetailProductAdapter(val mCtx: Context, val productList: ArrayList<DiscountedProduct>,
                           val discProductListener: discountProductListener)
    : RecyclerView.Adapter<DetailProductAdapter.ProductVH>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ProductVH {
        val discProdItemBinding: DiscountedProductsLayoutBinding = DataBindingUtil.inflate(
            LayoutInflater.from(mCtx), R.layout.discounted_products_layout, parent, false)
        return ProductVH(discProdItemBinding)
    }

    inner class ProductVH(val discountedProdBinding: DiscountedProductsLayoutBinding)
        : RecyclerView.ViewHolder(discountedProdBinding.root) {

        var prodQty:Int = 0
        var prodPrice:Float = 0f

        fun bind(discountedProd: DiscountedProduct){
            discountedProdBinding.tvActualPrice.paintFlags =
                discountedProdBinding.tvActualPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG

            discountedProdBinding.rbProductRating.rating = discountedProd.customerRating ?: 4f
            if(discountedProd.reviewCount == 0){
                discountedProdBinding.tvProdReviews.text  = "1 Reviews"
            }else{
                discountedProdBinding.tvProdReviews.text  = "${discountedProd.reviewCount} Reviews"
            }

            if(discountedProd.quantity <= 0){
                discountedProdBinding.btnBuyNowDiscont.isEnabled = false
                discountedProdBinding.btnAddCartDiscont.isEnabled = false
                discountedProdBinding.spProductQuantity.isEnabled = false
            }else{
                discountedProdBinding.btnBuyNowDiscont.isEnabled = true
                discountedProdBinding.btnAddCartDiscont.isEnabled = true
                discountedProdBinding.spProductQuantity.isEnabled = true
            }

            discountedProdBinding.setVariable(BR.discountedProd, discountedProd)
            discountedProdBinding.executePendingBindings()

            discountedProdBinding.btnBuyNowDiscont.setOnClickListener {
                discProductListener.buyDiscountedProducts(discountedProd, prodQty, prodPrice)
            }

            discountedProdBinding.btnAddCartDiscont.setOnClickListener {
                discProductListener.addToCartItem(discountedProd.id, prodQty)
            }

            discountedProdBinding.clTopDiscountedProdLayout.setOnClickListener{
                discProductListener.showProdDetailOnClickDiscountedProd(discountedProd)
            }

            if(discountedProd.quantity != 0){
                val qtyIntList = setQuantityIntegerList(discountedProd.quantity)
                val qtyList = setQuantityList(discountedProd.quantity)
                val qtyAdapter = DetailProductSpinnerAdpater(mCtx, qtyList)
                discountedProdBinding.spProductQuantity.adapter = qtyAdapter

                discountedProdBinding.spProductQuantity.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
                    override fun onNothingSelected(p0: AdapterView<*>?) {
                    }

                    override fun onItemSelected(parent: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
//                        Log.e("selected qty", " :: ${parent?.getItemAtPosition(position)} :: ${qtyList[p2]}")
                        prodQty = qtyIntList[p2]
                        prodPrice = prodQty.times(discountedProd.price.minus(Math.round((discountedProd.price * discountedProd.discount) / 100.0)))
                    }

                }
            }

        }

        private fun setQuantityList(qty:Int) : ArrayList<String>{
            val qtyArray = ArrayList<String>()
            for(i in 0 until qty){
                qtyArray.add("${i+1} Pcs")
            }
            return qtyArray
        }

        private fun setQuantityIntegerList(qty:Int) : ArrayList<Int> {
            val qtyArray = ArrayList<Int>()
            for(i in 0 until qty){
                qtyArray.add(i+1)
            }
            return qtyArray
        }
    }


    override fun getItemCount(): Int {
        return productList.size
    }

    override fun onBindViewHolder(holder: ProductVH, position: Int) {
        holder.bind(productList[position])
    }

    interface discountProductListener{
        fun buyDiscountedProducts(discProd: DiscountedProduct, prodQty: Int, prodPrice:Float)
        fun addToCartItem(prodID:String, prodQty:Int)
        fun showProdDetailOnClickDiscountedProd(discProd: DiscountedProduct)
    }
}
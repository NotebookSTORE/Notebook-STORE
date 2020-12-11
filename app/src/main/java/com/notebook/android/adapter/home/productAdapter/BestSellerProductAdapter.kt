package com.notebook.android.adapter.home.productAdapter

import android.content.Context
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.notebook.android.BR
import com.notebook.android.R
import com.notebook.android.data.db.entities.BestSeller
import com.notebook.android.databinding.BestSellerProductLayoutBinding

class BestSellerProductAdapter(val mCtx: Context, val bsProductList:ArrayList<BestSeller>,
                               val bsProductListen: BSProductListener)
    : RecyclerView.Adapter<BestSellerProductAdapter.BSViewHolder>() {

    override fun onViewRecycled(holder: BSViewHolder) {
        super.onViewRecycled(holder)
        Glide.with(mCtx).clear(holder.itemView.rootView)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BSViewHolder {
        val bsItemBinding: BestSellerProductLayoutBinding = DataBindingUtil.inflate(
            LayoutInflater.from(mCtx), R.layout.best_seller_product_layout, parent, false)
        return BSViewHolder(bsItemBinding)
    }

    inner class BSViewHolder(val bsProdBinding: BestSellerProductLayoutBinding)
        : RecyclerView.ViewHolder(bsProdBinding.root) {

        fun bind(bsProd: BestSeller){
            bsProdBinding.tvAfterAddingPriceInDiscount.paintFlags =
                bsProdBinding.tvAfterAddingPriceInDiscount.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            bsProdBinding.rbProductRating.rating = bsProd.customerRating?:4f

            bsProdBinding.setVariable(BR.bslProduct, bsProd)
            bsProdBinding.executePendingBindings()

            bsProdBinding.clTopProductLayout.setOnClickListener {
                bsProductListen.bsProductCallback(bsProd, bsProdBinding.imgProductImage)
            }

            bsProdBinding.imgAddToCart.setOnClickListener {
                bsProductListen.bsAddToCart(bsProd.id!!, 1)
            }
        }

        /*Glide.with(context)
                .setDefaultRequestOptions(requestOptions)
                .load(images.get(position))
                .error(R.drawable.no_internet_con)
                .thumbnail(.1f)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.image)
                .into(holder.pictureView)*/
    }

    /*It'll optimize your memory for loading images.

    RequestOptions reqOpt = RequestOptions
    .fitCenterTransform()
    .transform(new RoundedCorners(5))
    .diskCacheStrategy(DiskCacheStrategy.ALL) // It will cache your image after loaded for first time
    .override(holder.ivThumb.getWidth(),holder.ivThumb.getHeight()) // Overrides size of downloaded image and converts it's bitmaps to your desired image size*/

    override fun getItemCount(): Int {
        return bsProductList.size
    }

    override fun onBindViewHolder(holder: BSViewHolder, position: Int) {
        holder.bind(bsProductList[position])
    }

    interface BSProductListener{
        fun bsProductCallback(bsProd: BestSeller, impProduct:ImageView)
        fun bsAddToCart(prodID:Int, cartQty:Int)
    }
}
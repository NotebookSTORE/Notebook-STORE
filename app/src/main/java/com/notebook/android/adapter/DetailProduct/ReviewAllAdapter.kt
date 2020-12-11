package com.notebook.android.adapter.DetailProduct

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.notebook.android.BR
import com.notebook.android.R
import com.notebook.android.data.db.entities.RatingReviews
import com.notebook.android.databinding.ReviewDataLayoutBinding
import com.notebook.android.utility.Constant.RATING_PRODUCT_IMAGE_PATH

class ReviewAllAdapter(val mCtx: Context, val ratingList:List<RatingReviews>)
    : RecyclerView.Adapter<ReviewAllAdapter.ReviewVH>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewAllAdapter.ReviewVH {
        val ratingItemBinding:ReviewDataLayoutBinding = DataBindingUtil.inflate(LayoutInflater.from(mCtx),
            R.layout.review_data_layout,
        parent, false)

        return ReviewVH(ratingItemBinding)
    }

    override fun getItemCount(): Int {
        return ratingList.size
    }

    override fun onBindViewHolder(holder: ReviewAllAdapter.ReviewVH, position: Int) {
        holder.bind(ratingList[position])
    }

    inner class ReviewVH(private val ratingItemBinding:ReviewDataLayoutBinding) : RecyclerView.ViewHolder(ratingItemBinding.root){

        fun bind(ratingItem:RatingReviews){
            ratingItemBinding.setVariable(BR.ratingData, ratingItem)
            ratingItemBinding.executePendingBindings()

            if(ratingItem.image.isNullOrEmpty()){
                ratingItemBinding.imgProdRating.visibility = View.GONE
            }else{
                ratingItemBinding.imgProdRating.visibility = View.VISIBLE
                Glide.with(mCtx).load("${RATING_PRODUCT_IMAGE_PATH}${ratingItem.image}").into(ratingItemBinding.imgProdRating)
            }
            ratingItemBinding.rbProductRating.rating = ratingItem.rating
        }
    }
}
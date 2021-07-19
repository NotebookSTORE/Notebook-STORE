package com.notebook.android.ui.productDetail.frag

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.notebook.android.BR
import com.notebook.android.R
import com.notebook.android.databinding.ProductBenefitInfoLayoutBinding
import com.notebook.android.databinding.ZoomImageViewBinding
import com.notebook.android.model.home.BenefitProductData
import com.notebook.android.model.home.FreeDeliveryData
import java.util.*

class ZoomImageAdapter(val mCtx: Context, val benefitList: Array<String>)
    : RecyclerView.Adapter<ZoomImageAdapter.ZoomImageVH>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ZoomImageVH {
        val zoomImgBinding: ZoomImageViewBinding = DataBindingUtil.inflate(
            LayoutInflater.from(mCtx), R.layout.zoom_image_view, parent, false)
        return ZoomImageVH(zoomImgBinding)
    }

    inner class ZoomImageVH(val zoomImgBinding: ZoomImageViewBinding)
        : RecyclerView.ViewHolder(zoomImgBinding.root) {

        fun bind(imgUrl:String){
            Glide.with(mCtx).load(imgUrl).into(zoomImgBinding.imgPhotoView)
        }
    }


    override fun getItemCount(): Int {
        return benefitList.size
    }

    override fun onBindViewHolder(holder: ZoomImageVH, position: Int) {
        holder.bind(benefitList[position])
    }

}
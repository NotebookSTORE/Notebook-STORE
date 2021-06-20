package com.notebook.android.adapter.home

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.max.ecomaxgo.maxpe.view.flight.utility.loadAllTypeImage
import com.max.ecomaxgo.maxpe.view.flight.utility.loadAllTypeImageWithSize
import com.notebook.android.BR
import com.notebook.android.R
import com.notebook.android.data.db.entities.Brand
import com.notebook.android.databinding.BrandItemLayoutBinding
import com.notebook.android.utility.Constant
import com.notebook.android.utility.Constant.BRAND_IMAGE_PATH

class BrandDataAdapter (val mCtx: Context, val brandList:ArrayList<Brand>, val brandListener: BrandListener)
    : RecyclerView.Adapter<BrandDataAdapter.BrandViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BrandViewHolder {
        val subCategItemBinding:BrandItemLayoutBinding = DataBindingUtil.inflate(
            LayoutInflater.from(mCtx), R.layout.brand_item_layout, parent, false)
        return BrandViewHolder(subCategItemBinding)
    }

    inner class BrandViewHolder(itemView: BrandItemLayoutBinding)
        :RecyclerView.ViewHolder(itemView.root) {

        private var brandBinding:BrandItemLayoutBinding ?= null
        init {
            brandBinding = itemView
        }

        fun bind(brand:Brand){
            brandBinding?.setVariable(BR.brandData, brand)
            brandBinding?.executePendingBindings()
            loadAllTypeImageWithSize(brandBinding!!.imgBrandImage, brandBinding!!.root.context.getString(R.string.brandImgPath), brand.image,200,200)
            brandBinding?.cvBrand!!.setOnClickListener {
                brandListener.BrandId(brand.id.toInt(), brand.title!!)
            }
        }
    }

    override fun getItemCount(): Int {
        return brandList.size
    }

    override fun onBindViewHolder(holder: BrandViewHolder, position: Int) {
        holder.bind(brandList[position])
        Log.e("brand img", " :: ${BRAND_IMAGE_PATH}${brandList[position].image}")
    }

    interface BrandListener{
        fun BrandId(brandID:Int, title:String)
    }
}
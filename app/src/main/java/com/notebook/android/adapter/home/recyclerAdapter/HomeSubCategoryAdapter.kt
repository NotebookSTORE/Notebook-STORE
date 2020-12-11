package com.notebook.android.adapter.home

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.notebook.android.BR
import com.notebook.android.R
import com.notebook.android.data.db.entities.SubCategory
import com.notebook.android.databinding.SubCategoryHomeLayoutBinding
import com.notebook.android.utility.Constant.SUB_CATEGORY_IMAGE_PATH
import kotlinx.android.synthetic.main.sub_category_home_layout.view.*
import java.util.*

class HomeSubCategoryAdapter(val mCtx: Context, val subCategList:ArrayList<SubCategory>,
                             val subCategoryListener: SubCategoryListener)
    : RecyclerView.Adapter<HomeSubCategoryAdapter.SubCategoryViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SubCategoryViewHolder {
        val subCategItemBinding:SubCategoryHomeLayoutBinding = DataBindingUtil.inflate(
            LayoutInflater.from(mCtx), R.layout.sub_category_home_layout, parent, false)
        return SubCategoryViewHolder(subCategItemBinding)
    }

    inner class SubCategoryViewHolder(val subCategoryBinding: SubCategoryHomeLayoutBinding)
        :RecyclerView.ViewHolder(subCategoryBinding.root) {

        fun bind(subCategory:SubCategory){
            subCategoryBinding.setVariable(BR.SubCategory, subCategory)
            subCategoryBinding.executePendingBindings()
            subCategoryBinding.clSubCategoryView.setBackgroundColor(getRandomColorCode())

            subCategoryBinding.clCategoryLayout.setOnClickListener {
                subCategoryListener.subCategoryId(subCategory.id!!, subCategory.title!!)
            }
        }
    }

    fun getRandomColorCode(): Int {
        val random = Random()
        return Color.argb(255, random.nextInt(256), random.nextInt(256), random.nextInt(256))
    }

    override fun getItemCount(): Int {
        return subCategList.size
    }

    override fun onBindViewHolder(holder: SubCategoryViewHolder, position: Int) {
        holder.bind(subCategList[position])
    }

    interface SubCategoryListener{
        fun subCategoryId(subCategID:Int, title:String)
    }
}
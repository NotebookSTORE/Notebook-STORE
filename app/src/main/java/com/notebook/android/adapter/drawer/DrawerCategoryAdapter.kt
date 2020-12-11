package com.notebook.android.adapter.drawer

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.notebook.android.R
import com.notebook.android.data.db.entities.DrawerCategory
import com.notebook.android.databinding.DrawerCategoryLayoutBinding
import com.notebook.android.utility.Constant

class DrawerCategoryAdapter(val mCtx: Context, val categList:List<DrawerCategory>,
                            val categListener:CategoryDataListener)
    : RecyclerView.Adapter<DrawerCategoryAdapter.DrawerCategoryViewHolder>(){

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DrawerCategoryViewHolder {
        val drawerCategBinding: DrawerCategoryLayoutBinding = DataBindingUtil.inflate(
            LayoutInflater.from(mCtx), R.layout.drawer_category_layout, parent, false)
        return DrawerCategoryViewHolder(drawerCategBinding)
    }

    override fun getItemCount(): Int {
        return categList.size
    }

    override fun onBindViewHolder(
        holder: DrawerCategoryViewHolder,
        position: Int
    ) {
        holder.bind(categList[position])

    }

    inner class DrawerCategoryViewHolder(val drawerCategBinding: DrawerCategoryLayoutBinding)
        : RecyclerView.ViewHolder(drawerCategBinding.root){

        fun bind(drawerCategory: DrawerCategory){
            val currentPos = adapterPosition
            val drawerCategItem = categList[currentPos]
            val isExpanded = drawerCategItem.isDrawerCategoryOpen
            val isArrowExpanded = drawerCategItem.isDrawerCategArrowOpen
            drawerCategBinding.clSubSubCategLayout.visibility = if(isExpanded) View.VISIBLE else View.GONE
            if(isArrowExpanded){
                drawerCategBinding.imgCategArrow.setImageResource(R.drawable.ic_category_down_arrow)
            } else{
                drawerCategBinding.imgCategArrow.setImageResource( R.drawable.ic_categoy_right_arrow)
            }


            drawerCategBinding.tvSubCategTitle.text = drawerCategory.title
            if(!drawerCategory.homeimage_mobile.isNullOrEmpty()){
                Glide.with(mCtx)
                    .load("${Constant.CATEGORY_IMAGE_PATH}${drawerCategory.homeimage_mobile}")
                .into(drawerCategBinding.imgCategory)
            }

            Log.e("sub categ size", " :: ${drawerCategory.subsubcategorys?.size}")
            val subCategLayoutManager = LinearLayoutManager(mCtx)
            drawerCategBinding.recViewSubCategory.apply {
                layoutManager = subCategLayoutManager
                itemAnimator = DefaultItemAnimator()
                hasFixedSize()
            }

            drawerCategory.subsubcategorys?.let {
                val subSubCategAdapter = DrawerSubCategoryAdapter(mCtx,
                    drawerCategory.subsubcategorys?: ArrayList(),
                    object : DrawerSubCategoryAdapter.SubCategoryDataListener{
                        override fun getSubCategoryData(ssCategID: Int, title:String) {
                            categListener.getSubSubCategoryData(ssCategID, title)
                        }

                    })
                drawerCategBinding.recViewSubCategory.adapter = subSubCategAdapter
            }
            if(drawerCategory.subsubcategorys?.isNullOrEmpty() == true){
                drawerCategBinding.clSubSubCategLayout.visibility = View.GONE
            }

            drawerCategBinding.clCategoryView.setOnClickListener{

                drawerCategItem.subsubcategorys?.let {
                    if(isExpanded){
                        drawerCategBinding.clSubSubCategLayout.visibility = View.GONE
                        drawerCategItem.isDrawerCategoryOpen = false
                        drawerCategItem.isDrawerCategArrowOpen = false
                    }else{
                        drawerCategBinding.clSubSubCategLayout.visibility = View.VISIBLE
                        drawerCategItem.isDrawerCategoryOpen = true
                        drawerCategItem.isDrawerCategArrowOpen = true
                    }
                }
                notifyItemChanged(currentPos)
            }
        }
    }

    interface CategoryDataListener{
        fun getSubSubCategoryData(subCategID:Int, subCategTitle:String)
    }
}
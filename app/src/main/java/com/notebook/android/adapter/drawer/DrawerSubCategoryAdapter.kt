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
import com.notebook.android.data.db.entities.DrawerSubCategory
import com.notebook.android.databinding.DrawerCategoryLayoutBinding
import com.notebook.android.databinding.DrawerSubCategoryLayoutBinding
import com.notebook.android.utility.Constant

class DrawerSubCategoryAdapter(val mCtx: Context, val subCategList:List<DrawerSubCategory>,
                               val subCategListener:SubCategoryDataListener)
    : RecyclerView.Adapter<DrawerSubCategoryAdapter.DrawerSubCategoryViewHolder>(){

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DrawerSubCategoryViewHolder {
        val drawerSubCategBinding: DrawerSubCategoryLayoutBinding = DataBindingUtil.inflate(
            LayoutInflater.from(mCtx), R.layout.drawer_sub_category_layout, parent, false)
        return DrawerSubCategoryViewHolder(drawerSubCategBinding)
    }

    override fun getItemCount(): Int {
        return subCategList.size
    }

    override fun onBindViewHolder(
        holder: DrawerSubCategoryViewHolder,
        position: Int
    ) {
        holder.bind(subCategList[position])

    }

    inner class DrawerSubCategoryViewHolder(val drawerSubCategBinding: DrawerSubCategoryLayoutBinding )
        : RecyclerView.ViewHolder(drawerSubCategBinding.root){

        fun bind(drawerCategory: DrawerSubCategory){
            val currentPos = adapterPosition
            val drawerCategItem = subCategList[currentPos]
            val isExpanded = drawerCategItem.isDrawerSubCategoryOpen
            val isArrowExpanded = drawerCategItem.isDrawerSubCategArrowOpen
            drawerSubCategBinding.clSubSubCategLayout.visibility = if(isExpanded) View.VISIBLE else View.GONE
            drawerSubCategBinding.imgSubCategArrow.setImageResource(
                if(isArrowExpanded){
                    R.drawable.ic_category_down_arrow
                } else{
                    R.drawable.ic_categoy_right_arrow
                }
            )

            drawerSubCategBinding.tvSubCategTitle.text = drawerCategory.title
            if(!drawerCategory.homeimage_mobile.isNullOrEmpty()){
                Glide.with(mCtx)
                    .load("${Constant.SUB_CATEGORY_IMAGE_PATH}${drawerCategory.homeimage_mobile}")
                .into(drawerSubCategBinding.imgSubCateg)
            }

            Log.e("sub categ size", " :: ${drawerCategory.subsubcategory?.size}")
            val subCategLayoutManager = LinearLayoutManager(mCtx)
            drawerSubCategBinding.recViewSubSubCategory.apply {
                layoutManager = subCategLayoutManager
                itemAnimator = DefaultItemAnimator()
                hasFixedSize()
            }

            drawerCategory.subsubcategory?.let {
                val subSubCategAdapter = SubSubCategoryAdapter(mCtx,
                    drawerCategory.subsubcategory?: ArrayList(),
                    object : SubSubCategoryAdapter.SubSubCategListener{
                        override fun sendSubSubCategID(ssCategID: Int, title:String) {
                            subCategListener.getSubCategoryData(ssCategID, title)
                        }

                    })
                drawerSubCategBinding.recViewSubSubCategory.adapter = subSubCategAdapter
            }
            if(drawerCategory.subsubcategory?.isNullOrEmpty() == true){
                drawerSubCategBinding.clSubSubCategLayout.visibility = View.GONE
            }

            drawerSubCategBinding.clSubCategoryView.setOnClickListener{

                drawerCategItem.subsubcategory?.let {
                    if(isExpanded){
                        drawerSubCategBinding.clSubSubCategLayout.visibility = View.GONE
//                        drawerBinding!!.recViewSubSubCategory.visibility = View.GONE
//                        drawerCategBinding.imgSubCategArrow.setImageResource(R.drawable.ic_categoy_right_arrow)
                        drawerCategItem.isDrawerSubCategoryOpen = false
                        drawerCategItem.isDrawerSubCategArrowOpen = false
                    }else{
                        drawerSubCategBinding.clSubSubCategLayout.visibility = View.VISIBLE
//                        drawerBinding!!.recViewSubSubCategory.visibility = View.VISIBLE
//                        drawerCategBinding.imgSubCategArrow.setImageResource(R.drawable.ic_category_down_arrow)
                        drawerCategItem.isDrawerSubCategoryOpen = true
                        drawerCategItem.isDrawerSubCategArrowOpen = true
                    }
                }
                notifyItemChanged(currentPos)
            }
        }
    }

    interface SubCategoryDataListener{
        fun getSubCategoryData(subCategID:Int, subCategTitle:String)
    }
}
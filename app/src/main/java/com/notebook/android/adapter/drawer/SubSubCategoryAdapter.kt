package com.notebook.android.adapter.drawer

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.notebook.android.BR
import com.notebook.android.R
import com.notebook.android.data.db.entities.DrawerSubCategory
import com.notebook.android.data.db.entities.DrawerSubSubCategory
import com.notebook.android.data.db.entities.SubSubCategory
import com.notebook.android.databinding.SubSubCategoryLayoutBinding

class SubSubCategoryAdapter(val mCtx: Context, val subSubCategList:List<DrawerSubSubCategory>,
                            subSubCategListeners:SubSubCategListener)
    : RecyclerView.Adapter<SubSubCategoryAdapter.SubSubCategoryViewHolder>(){

    private var subSubCategListen:SubSubCategListener ?= null
    init{
        this.subSubCategListen = subSubCategListeners
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SubSubCategoryViewHolder {
        val subSubCategBinding: SubSubCategoryLayoutBinding = DataBindingUtil.inflate(
            LayoutInflater.from(mCtx), R.layout.sub_sub_category_layout, parent, false)
        return SubSubCategoryViewHolder(subSubCategBinding)
    }

    override fun getItemCount(): Int {
        return subSubCategList.size
    }

    override fun onBindViewHolder(
        holder: SubSubCategoryViewHolder,
        position: Int
    ) {
        holder.bind(subSubCategList[position])

    }

    inner class SubSubCategoryViewHolder(val subSubCategBinding: SubSubCategoryLayoutBinding)
        : RecyclerView.ViewHolder(subSubCategBinding.root){

        fun bind(subSubCategory: DrawerSubSubCategory){
            subSubCategBinding.setVariable(BR.subSubCategModel, subSubCategory)
            subSubCategBinding.executePendingBindings()

            subSubCategBinding.root.setOnClickListener {
                subSubCategListen?.sendSubSubCategID(subSubCategory.id, subSubCategory.title!!)
            }
        }
    }

    interface SubSubCategListener{
        fun sendSubSubCategID(ssCategID:Int, title:String)
    }
}
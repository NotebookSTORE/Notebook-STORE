package com.notebook.android.adapter.drawer

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.notebook.android.BR
import com.notebook.android.R
import com.notebook.android.data.db.entities.DrawerSubSubCategory
import com.notebook.android.data.db.entities.FaqExpandable
import com.notebook.android.data.db.entities.SubSubCategory
import com.notebook.android.databinding.FaqItemLayoutBinding
import com.notebook.android.databinding.SubSubCategoryLayoutBinding

class FaqDataAdapter(val mCtx: Context, val faqDataList:List<FaqExpandable>)
    : RecyclerView.Adapter<FaqDataAdapter.FaqDataVH>(){

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): FaqDataVH {
        val faqDataBinding: FaqItemLayoutBinding = DataBindingUtil.inflate(
            LayoutInflater.from(mCtx), R.layout.faq_item_layout, parent, false)
        return FaqDataVH(faqDataBinding)
    }

    override fun getItemCount(): Int {
        return faqDataList.size
    }

    override fun onBindViewHolder(
        holder: FaqDataVH,
        position: Int
    ) {
        holder.bind(faqDataList[position])
    }

    inner class FaqDataVH(val faqDataBinding: FaqItemLayoutBinding)
        : RecyclerView.ViewHolder(faqDataBinding.root){

        fun bind(faqDataItem: FaqExpandable){
            faqDataBinding.setVariable(BR.faqData, faqDataItem)
            faqDataBinding.executePendingBindings()

            val currentPos = adapterPosition
            val faqData = faqDataList[currentPos]
            val isExpanded = faqData.isExpandable
            faqDataBinding.tvFaqDesc.visibility = if(isExpanded) View.VISIBLE else View.GONE

            faqDataBinding.tvFaqTitle.setOnClickListener {
                if(isExpanded){
                    faqDataBinding.tvFaqDesc.visibility = View.GONE
//                        drawerBinding!!.recViewSubSubCategory.visibility = View.GONE
//                    drawerBinding!!.imgSubCategArrow.setImageResource(R.drawable.ic_menu_right_arrow)
                    faqData.isExpandable = false
                }else{
                    faqDataBinding.tvFaqDesc.visibility = View.VISIBLE
//                        drawerBinding!!.recViewSubSubCategory.visibility = View.VISIBLE
//                    drawerBinding!!.imgSubCategArrow.setImageResource(R.drawable.ic_arrow_down)
                    faqData.isExpandable = true
                }
                notifyItemChanged(currentPos)
            }
        }
    }
}
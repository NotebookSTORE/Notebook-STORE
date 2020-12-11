package com.notebook.android.adapter.filterBy

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.notebook.android.BR
import com.notebook.android.R
import com.notebook.android.data.db.entities.DiscountFilterBy
import com.notebook.android.data.preferences.NotebookPrefs
import com.notebook.android.databinding.DiscountFilterbyLayoutBinding

class DiscountFilterAdapter(
    val mCtx: Context, val discountList:List<DiscountFilterBy>, private var discountFilterListner: DiscountFilterDataListener
) : RecyclerView.Adapter<DiscountFilterAdapter.DiscountVH>() {

    private var notebookPrefs: NotebookPrefs = NotebookPrefs(mCtx)
    private var itemSelectPos = notebookPrefs.discountPos

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiscountVH {
        val discountFilterBinding: DiscountFilterbyLayoutBinding  = DataBindingUtil.inflate(
            LayoutInflater.from(mCtx),
            R.layout.discount_filterby_layout, parent, false)
        return DiscountVH(discountFilterBinding)
    }

    override fun getItemCount(): Int {
        return discountList.size
    }

    override fun onBindViewHolder(holder: DiscountVH, position: Int) {
        holder.bind(discountList[position])
    }

    inner class DiscountVH(val discFilterBinding: DiscountFilterbyLayoutBinding)
        : RecyclerView.ViewHolder(discFilterBinding.root){

        fun bind(discFilterBy: DiscountFilterBy) {
            discFilterBinding.setVariable(BR.discountFilterBy, discFilterBy)
            discFilterBinding.executePendingBindings()

            if(discFilterBy.isDiscountSelected){
                discFilterBinding.tvDiscount10Percent.setBackgroundResource(R.drawable.filter_select_bg)
                discFilterBinding.tvDiscount10Percent.setTextColor(mCtx.resources.getColor(R.color.colorWhite))
            }else{
                discFilterBinding.tvDiscount10Percent.setBackgroundResource(R.drawable.filter_unselect_bg)
                discFilterBinding.tvDiscount10Percent.setTextColor(mCtx.resources.getColor(R.color.colorLightGrey))
            }

            discFilterBinding.root.setOnClickListener {
                val colorIDJsonArray = ArrayList<Int>()
                if(discFilterBy.isDiscountSelected){
                    discFilterBy.isDiscountSelected = !discFilterBy.isDiscountSelected
                }else{
                    discFilterBy.isDiscountSelected = !discFilterBy.isDiscountSelected
                }
                notifyItemChanged(position)

                for(color in discountList){
                    if(color.isDiscountSelected){
                        colorIDJsonArray.add(color.discount!!)
                    }
                }

                discountFilterListner.getDiscountData(discountList[position].id, discountList[position].discount!!, colorIDJsonArray)
            }
        }
    }

    interface DiscountFilterDataListener{
        fun getDiscountData(discountID:Int, discountTitle:Int, discountValueArray:ArrayList<Int>)
    }
}
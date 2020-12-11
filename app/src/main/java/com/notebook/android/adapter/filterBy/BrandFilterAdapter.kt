package com.notebook.android.adapter.filterBy

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.notebook.android.BR
import com.notebook.android.R
import com.notebook.android.data.db.entities.BrandFilterBy
import com.notebook.android.data.preferences.NotebookPrefs
import com.notebook.android.databinding.BrandFilterbyLayoutBinding

class BrandFilterAdapter(
    val mCtx:Context, val brandList:List<BrandFilterBy>,
    private var brandFilterListner: BrandFilterDataListener
) : RecyclerView.Adapter<BrandFilterAdapter.BrandVH>() {

    private var notebookPrefs:NotebookPrefs = NotebookPrefs(mCtx)
    private var itemSelectPos = notebookPrefs.brandPos

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BrandVH {
        val brandFilterBinding:BrandFilterbyLayoutBinding = DataBindingUtil.inflate(LayoutInflater.from(mCtx),
            R.layout.brand_filterby_layout, parent, false)
        return BrandVH(brandFilterBinding)
    }

    override fun getItemCount(): Int {
        Log.e("brandss size", " :: ${brandList.size}")
       return brandList.size
    }

    override fun onBindViewHolder(holder: BrandVH, position: Int) {
        holder.bind(brandList[position], position)
    }

    inner class BrandVH(val brandFilterBinding: BrandFilterbyLayoutBinding)
        : RecyclerView.ViewHolder(brandFilterBinding.root){

        fun bind(brandFilterBy: BrandFilterBy, pos:Int) {
            brandFilterBinding.setVariable(BR.brandFilterBy, brandFilterBy)
            brandFilterBinding.executePendingBindings()

            if(brandFilterBy.isBrandSelected){
                brandFilterBinding.tvDiscount10Percent.setBackgroundResource(R.drawable.filter_select_bg)
                brandFilterBinding.tvDiscount10Percent.setTextColor(mCtx.resources.getColor(R.color.colorWhite))
            }else{
                brandFilterBinding.tvDiscount10Percent.setBackgroundResource(R.drawable.filter_unselect_bg)
                brandFilterBinding.tvDiscount10Percent.setTextColor(mCtx.resources.getColor(R.color.colorLightGrey))
            }

            brandFilterBinding.root.setOnClickListener {
                val brandIDJsonArray = ArrayList<Int>()
                if(brandFilterBy.isBrandSelected){
                    brandFilterBy.isBrandSelected = !brandFilterBy.isBrandSelected
                }else{
                    brandFilterBy.isBrandSelected = !brandFilterBy.isBrandSelected
                }
                notifyItemChanged(pos)

                for(brand in brandList){
                    if(brand.isBrandSelected){
                        brandIDJsonArray.add(brand.id)
                    }
                }

                brandFilterListner.getBrandData(brandList[position].id, brandList[position].title!!, brandIDJsonArray)
            }
        }
    }

    interface BrandFilterDataListener{
        fun getBrandData(brandID:Int, brandTitle:String, brandIDArray:ArrayList<Int>)
    }
}
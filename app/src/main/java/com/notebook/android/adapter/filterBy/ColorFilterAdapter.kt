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
import com.notebook.android.data.db.entities.ColorFilterBy
import com.notebook.android.data.preferences.NotebookPrefs
import com.notebook.android.databinding.BrandFilterbyLayoutBinding
import com.notebook.android.databinding.ColorFilterbyLayoutBinding

class ColorFilterAdapter(
    val mCtx: Context, val colorList:List<ColorFilterBy>, private var colorFilterListner: ColorFilterDataListener
) : RecyclerView.Adapter<ColorFilterAdapter.ColorVH>() {

    private var notebookPrefs: NotebookPrefs = NotebookPrefs(mCtx)
    private var itemSelectPos = notebookPrefs.colorPos

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColorVH {
        val colorFilterBinding: ColorFilterbyLayoutBinding = DataBindingUtil.inflate(
            LayoutInflater.from(mCtx),
            R.layout.color_filterby_layout, parent, false)
        return ColorVH(colorFilterBinding)
    }

    override fun getItemCount(): Int {
        Log.e("brandss size", " :: ${colorList.size}")
        return colorList.size
    }

    override fun onBindViewHolder(holder: ColorVH, position: Int) {
        holder.bind(colorList[position])
    }

    inner class ColorVH(val colorFilterBinding: ColorFilterbyLayoutBinding)
        : RecyclerView.ViewHolder(colorFilterBinding.root){

        fun bind(colorFilterBy: ColorFilterBy) {
            colorFilterBinding.setVariable(BR.colorFilterBy, colorFilterBy)
            colorFilterBinding.executePendingBindings()

            if(colorFilterBy.isColorSelected){
                colorFilterBinding.tvDiscount10Percent.setBackgroundResource(R.drawable.filter_select_bg)
                colorFilterBinding.tvDiscount10Percent.setTextColor(mCtx.resources.getColor(R.color.colorWhite))
            }else{
                colorFilterBinding.tvDiscount10Percent.setBackgroundResource(R.drawable.filter_unselect_bg)
                colorFilterBinding.tvDiscount10Percent.setTextColor(mCtx.resources.getColor(R.color.colorLightGrey))
            }

            colorFilterBinding.root.setOnClickListener {
                val colorIDJsonArray = ArrayList<Int>()
                if(colorFilterBy.isColorSelected){
                    colorFilterBy.isColorSelected = !colorFilterBy.isColorSelected
                }else{
                    colorFilterBy.isColorSelected = !colorFilterBy.isColorSelected
                }
                notifyItemChanged(position)

                for(color in colorList){
                    if(color.isColorSelected){
                        colorIDJsonArray.add(color.id)
                    }
                }

                colorFilterListner.getColorData(colorList[position].id, colorList[position].title!!, colorIDJsonArray)
            }
        }

    }

    interface ColorFilterDataListener{
        fun getColorData(colorID:Int, colorTitle:String, colorIDArray:ArrayList<Int>)
    }
}
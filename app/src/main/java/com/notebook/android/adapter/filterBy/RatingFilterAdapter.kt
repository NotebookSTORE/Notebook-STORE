package com.notebook.android.adapter.filterBy

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.common.base.Splitter
import com.notebook.android.BR
import com.notebook.android.R
import com.notebook.android.data.db.entities.RatingFilterBy
import com.notebook.android.data.preferences.NotebookPrefs
import com.notebook.android.databinding.RatingFilterbyLayoutBinding

class RatingFilterAdapter(
    val mCtx: Context, val ratingList:List<RatingFilterBy>, private var rateFilterListner: RatingFilterDataListener
) : RecyclerView.Adapter<RatingFilterAdapter.RatingVH>() {

    private var notebookPrefs: NotebookPrefs = NotebookPrefs(mCtx)
    private var itemSelectPos = notebookPrefs.ratingPos

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RatingVH {
        val ratingFilterBinding: RatingFilterbyLayoutBinding = DataBindingUtil.inflate(
            LayoutInflater.from(mCtx),
            R.layout.rating_filterby_layout, parent, false)
        return RatingVH(ratingFilterBinding)
    }

    override fun getItemCount(): Int {
        Log.e("brandss size", " :: ${ratingList.size}")
        return ratingList.size
    }

    override fun onBindViewHolder(holder: RatingVH, position: Int) {
        holder.bind(ratingList[position])
    }

    inner class RatingVH(val ratingFilterBinding: RatingFilterbyLayoutBinding)
        : RecyclerView.ViewHolder(ratingFilterBinding.root){

        fun bind(rateFilterBy: RatingFilterBy) {
            ratingFilterBinding.setVariable(BR.ratingFilterBy, rateFilterBy)
            ratingFilterBinding.executePendingBindings()

            if(rateFilterBy.isRatingSelected){
                ratingFilterBinding.tvDiscount10Percent.setBackgroundResource(R.drawable.filter_select_bg)
                ratingFilterBinding.tvDiscount10Percent.setTextColor(mCtx.resources.getColor(R.color.colorWhite))
            }else{
                ratingFilterBinding.tvDiscount10Percent.setBackgroundResource(R.drawable.filter_unselect_bg)
                ratingFilterBinding.tvDiscount10Percent.setTextColor(mCtx.resources.getColor(R.color.colorLightGrey))
            }

            ratingFilterBinding.root.setOnClickListener {
                val brandIDJsonArray = ArrayList<Int>()
                if(rateFilterBy.isRatingSelected){
                    rateFilterBy.isRatingSelected = !rateFilterBy.isRatingSelected
                }else{
                    rateFilterBy.isRatingSelected = !rateFilterBy.isRatingSelected
                }
                notifyItemChanged(position)

                for(brand in ratingList){
                    if(brand.isRatingSelected){

                        for (s in Splitter.on(',').trimResults().omitEmptyStrings()
                            .split(brand.ratingvalue)) {
                            brandIDJsonArray.add(s.toInt())
                        }
                    }
                }

                rateFilterListner.getRatingData(ratingList[position].id, ratingList[position].title!!, brandIDJsonArray)
            }
        }
    }

    interface RatingFilterDataListener{
        fun getRatingData(rateID:Int, rateTitle:String, ratingArray:List<Int>)
    }
}
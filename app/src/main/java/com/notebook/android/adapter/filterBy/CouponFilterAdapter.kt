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
import com.notebook.android.data.db.entities.CouponFilterBy
import com.notebook.android.data.preferences.NotebookPrefs
import com.notebook.android.databinding.BrandFilterbyLayoutBinding
import com.notebook.android.databinding.ColorFilterbyLayoutBinding
import com.notebook.android.databinding.CouponFilterbyLayoutBinding

class CouponFilterAdapter(
    val mCtx: Context, val couponList:List<CouponFilterBy>,
    private var couponFilterListner: CouponFilterDataListener
) : RecyclerView.Adapter<CouponFilterAdapter.CouponVH>() {

    private var notebookPrefs: NotebookPrefs = NotebookPrefs(mCtx)
    private var itemSelectPos = notebookPrefs.colorPos

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CouponVH {
        val couponFilterBinding: CouponFilterbyLayoutBinding = DataBindingUtil.inflate(
            LayoutInflater.from(mCtx),
            R.layout.coupon_filterby_layout, parent, false)
        return CouponVH(couponFilterBinding)
    }

    override fun getItemCount(): Int {
        Log.e("brandss size", " :: ${couponList.size}")
        return couponList.size
    }

    override fun onBindViewHolder(holder: CouponVH, position: Int) {
        holder.bind(couponList[position])
    }

    inner class CouponVH(val couponFilterBinding: CouponFilterbyLayoutBinding)
        : RecyclerView.ViewHolder(couponFilterBinding.root){

        fun bind(couponFilterBy: CouponFilterBy) {
            couponFilterBinding.setVariable(BR.couponFilterBy, couponFilterBy)
            couponFilterBinding.executePendingBindings()

            if(couponFilterBy.isCouponSelected){
                couponFilterBinding.tvCouponCode.setBackgroundResource(R.drawable.filter_select_bg)
                couponFilterBinding.tvCouponCode.setTextColor(mCtx.resources.getColor(R.color.colorWhite))
            }else{
                couponFilterBinding.tvCouponCode.setBackgroundResource(R.drawable.filter_unselect_bg)
                couponFilterBinding.tvCouponCode.setTextColor(mCtx.resources.getColor(R.color.colorLightGrey))
            }

            couponFilterBinding.root.setOnClickListener {
                val couponIDJsonArray = ArrayList<Int>()
                if(couponFilterBy.isCouponSelected){
                    couponFilterBy.isCouponSelected = !couponFilterBy.isCouponSelected
                }else{
                    couponFilterBy.isCouponSelected = !couponFilterBy.isCouponSelected
                }
                notifyItemChanged(position)

                for(coupon in couponList){
                    if(coupon.isCouponSelected){
                        couponIDJsonArray.add(coupon.id)
                    }
                }

                couponFilterListner.getCouponData(couponList[position].id, couponList[position].code!!, couponIDJsonArray)
            }
        }

    }

    interface CouponFilterDataListener{
        fun getCouponData(couponID:Int, couponTitle:String, couponIDArray:ArrayList<Int>)
    }
}
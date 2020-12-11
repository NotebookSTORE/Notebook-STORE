package com.notebook.android.adapter.DetailProduct

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.notebook.android.R

class CustomSpinnerAdpater(val mCtx: Context, val qtyList:ArrayList<Int>) : BaseAdapter() {

    class SpinnerVH(){
        lateinit var tvQuantity:TextView
    }

    override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View {
        val spinnerVH:SpinnerVH?
        val spinnerCustomView: View?

        if(p1 == null){
            spinnerVH = SpinnerVH()
            val inflater:LayoutInflater = mCtx.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            spinnerCustomView = inflater.inflate(R.layout.custom_spinner_layout, p2, false)

            spinnerCustomView.tag = spinnerVH
        }else{
            spinnerVH = p1.tag as SpinnerVH
            spinnerCustomView = p1
        }

        spinnerVH.tvQuantity = spinnerCustomView?.findViewById(R.id.tvProductQty) as TextView
        spinnerVH.tvQuantity.text = "${qtyList[p0]}"
        return spinnerCustomView
    }

    override fun getItem(p0: Int): Any? {
        return qtyList[p0]
    }

    override fun getItemId(p0: Int): Long {
        return p0.toLong()
    }

    override fun getCount(): Int {
        return qtyList.size
    }
}
package com.notebook.android.adapter.DetailProduct

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.notebook.android.R

class DetailProductSpinnerAdpater(val mCtx: Context, val qtyList:ArrayList<String>) : BaseAdapter() {

    internal var inflter: LayoutInflater = LayoutInflater.from(mCtx)

    inner class SpinnerVH(){
        protected lateinit var tvQuantity:TextView
    }

    override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View {
        val view = inflter.inflate(R.layout.detail_product_spinner_layout, p2, false)
        val tvQuantity = view.findViewById<TextView>(R.id.tvProductQty)
        tvQuantity.text = qtyList[p0]
        return view
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
package com.notebook.android.adapter.address

import android.content.Context
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.notebook.android.BR
import com.notebook.android.R
import com.notebook.android.data.db.entities.Address
import com.notebook.android.data.db.entities.Cart
import com.notebook.android.data.db.entities.Country
import com.notebook.android.databinding.AddressItemLayoutBinding
import com.notebook.android.databinding.CartItemLayoutBinding
import com.notebook.android.databinding.CountrySelectionItemLayoutBinding
import java.util.*

class CountrySelectionAdapter(val mCtx: Context,
                              var countryList: List<Country>,
                              val countrySelectionListener: CountrySelectionListener)
    : RecyclerView.Adapter<CountrySelectionAdapter.CountryVH>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CountryVH {
        val countrySelectionItemBinding: CountrySelectionItemLayoutBinding = DataBindingUtil.inflate(
            LayoutInflater.from(mCtx), R.layout.country_selection_item_layout, parent, false)
        return CountryVH(countrySelectionItemBinding)
    }

    inner class CountryVH(val countrySelectionItemBinding: CountrySelectionItemLayoutBinding)
        : RecyclerView.ViewHolder(countrySelectionItemBinding.root) {

        fun bind(countryData: Country){
            countrySelectionItemBinding.setVariable(BR.countryModel, countryData)
            countrySelectionItemBinding.executePendingBindings()

            countrySelectionItemBinding.root.setOnClickListener {
                countrySelectionListener.countrySelectID(countryData.country_name!!)
            }
        }
    }

    override fun getItemCount(): Int {
        return countryList.size
    }

    override fun onBindViewHolder(holder: CountryVH, position: Int) {
        holder.bind(countryList[position])
    }

    fun filterCountryData(countryList: List<Country>){
        this.countryList = countryList
        notifyDataSetChanged()
    }

    interface CountrySelectionListener{
        fun countrySelectID(countryName:String)
    }
}
package com.notebook.android.ui.popupDialogFrag

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.notebook.android.R
import com.notebook.android.adapter.address.CountrySelectionAdapter
import com.notebook.android.data.db.entities.Country
import com.notebook.android.databinding.CountrySelectionDialogLayoutBinding

class CountrySelectionDialog : DialogFragment() {

    private lateinit var countrySelectBinding:CountrySelectionDialogLayoutBinding
    private lateinit var countryList:List<Country>
    private lateinit var countrySelectedValueListener:CountrySelectedValueListener
    private lateinit var countrySelectionAdapter:CountrySelectionAdapter

    fun setCountrySelectionListener(countrySelectedValueListener:CountrySelectedValueListener){
        this.countrySelectedValueListener = countrySelectedValueListener
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        countrySelectBinding = DataBindingUtil.inflate(inflater, R.layout.country_selection_dialog_layout,
            container, false)

        if(arguments != null){
            val list = requireArguments().getString("countryList")
            countryList = getArrayList(list!!)
            setupRecyclerView()
        }

        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        countrySelectBinding.edtSearchCountryHere.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {}

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                filterCountry(p0.toString())
            }

        })
        return countrySelectBinding.root
    }

    fun filterCountry(countryName:String){
        val countryLists = ArrayList<Country>()
        for (country in countryList){
            if(country.country_name!!.toLowerCase().contains(countryName, true)){
                countryLists.add(country)
            }
        }

        countrySelectionAdapter.filterCountryData(countryLists)
    }

    private fun getArrayList(key:String) : ArrayList<Country>{
        val gson = Gson()
        val type = object : TypeToken<ArrayList<Country>>() {}.type

        return gson.fromJson(key, type)
    }

    private fun setupRecyclerView(){
        val countrySelectionLayoutManager = LinearLayoutManager(requireContext())
        countrySelectionAdapter = CountrySelectionAdapter(requireContext(), countryList, object : CountrySelectionAdapter.CountrySelectionListener{
            override fun countrySelectID(countryName: String) {
                countrySelectedValueListener.onSelectedValue(countryName)
                dismissAllowingStateLoss()
            }
        })
        countrySelectBinding.recViewCountryData.apply {
            layoutManager = countrySelectionLayoutManager
            itemAnimator = DefaultItemAnimator()
            hasFixedSize()
            adapter = countrySelectionAdapter
        }
    }

    interface CountrySelectedValueListener{
        fun onSelectedValue(selectValue:String)
    }
}
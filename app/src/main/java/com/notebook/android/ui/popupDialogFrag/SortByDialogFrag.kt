package com.notebook.android.ui.popupDialogFrag

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.notebook.android.R
import com.notebook.android.data.preferences.NotebookPrefs
import com.notebook.android.databinding.ProductSortingLayoutBinding

class SortByDialogFrag : DialogFragment() {

    private lateinit var productSortingBinding:ProductSortingLayoutBinding
    private var sortSelectedValueListener:SortSelectedValueListener ?= null

    fun setSortingListener(sortSelectedValueListener:SortSelectedValueListener){
        this.sortSelectedValueListener = sortSelectedValueListener
    }

    private val notebookPrefs: NotebookPrefs by lazy {
        NotebookPrefs(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {

        productSortingBinding = DataBindingUtil.inflate(inflater,
            R.layout.product_sorting_layout, container, false)

        if(notebookPrefs.sortedValue == 1){
            productSortingBinding.rbPriceHighToLow.isChecked = true
        }else if(notebookPrefs.sortedValue == 2){
            productSortingBinding.rbPriceLowToHigh.isChecked = true
        }else if(notebookPrefs.sortedValue == 3){
            productSortingBinding.rbRelevance.isChecked = true
        }else if(notebookPrefs.sortedValue == 4){
            productSortingBinding.rbNewestFirst.isChecked = true
        }else{
            productSortingBinding.rbPopularity.isChecked = true
        }

        return productSortingBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        productSortingBinding.tvApply.setOnClickListener{
            sortSelectedValueListener?.sortSelectedValue(notebookPrefs.sortedValue)
            dismissAllowingStateLoss()
        }

        productSortingBinding.rgSortByProducts.setOnCheckedChangeListener { _, checkedId ->
            when(checkedId){

                R.id.rbPriceHighToLow -> {
                    notebookPrefs.sortedValue  = 1
                }

                R.id.rbPriceLowToHigh -> {
                    notebookPrefs.sortedValue  = 2
                }

                R.id.rbRelevance -> {
                    notebookPrefs.sortedValue  = 3
                }

                R.id.rbNewestFirst -> {
                    notebookPrefs.sortedValue  = 4
                }

                R.id.rbPopularity -> {
                    notebookPrefs.sortedValue  = 5
                }
            }
        }
    }

    interface SortSelectedValueListener{
        fun sortSelectedValue(value:Int)
    }
}
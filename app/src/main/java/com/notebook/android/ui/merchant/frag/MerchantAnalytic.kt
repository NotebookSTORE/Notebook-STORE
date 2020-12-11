package com.notebook.android.ui.merchant.frag

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.notebook.android.R
import com.notebook.android.databinding.FragmentMerchantAnalyticBinding

class MerchantAnalytic : Fragment() {

    private lateinit var fragMerchantAnalyticBinding:FragmentMerchantAnalyticBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragMerchantAnalyticBinding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_merchant_analytic, container, false)
        return fragMerchantAnalyticBinding.root
    }
}
package com.notebook.android.ui.productDetail.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController

abstract class BaseFragment : Fragment() {

    protected var rootView: View? = null
    private var sharedViewModel: SharedViewModel? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (rootView == null) {
            rootView = inflater.inflate(getLayoutId(), container, false)
            initSharedViewModel()
        }
        return rootView
    }

    abstract fun getLayoutId():Int

    open fun onFragmentResult(result: Any){}

    protected fun startFragmentForResult(actionId: Int){
        findNavController().navigate(actionId)
    }

    protected fun popBackStackWithResult(result: Any){
        findNavController().popBackStack()
        sharedViewModel?.share(result)
    }

    private fun initSharedViewModel() {
        sharedViewModel = ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)

        sharedViewModel?.sharedData?.observe(requireActivity(),
            Observer<Any> { params -> onFragmentResult(params) })
    }
}
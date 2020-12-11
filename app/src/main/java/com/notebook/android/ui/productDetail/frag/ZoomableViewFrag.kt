package com.notebook.android.ui.productDetail.frag

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentActivity
import com.bumptech.glide.Glide
import com.notebook.android.R
import com.notebook.android.databinding.FragmentReviewProductBinding
import com.notebook.android.databinding.FragmentZoomableViewBinding
import com.notebook.android.ui.dashboard.frag.fragHome.DetailViewProductFragArgs
import com.notebook.android.utility.Constant

class ZoomableViewFrag : Fragment() {

    private lateinit var fragmentZoomableViewBinding: FragmentZoomableViewBinding

    private lateinit var mContext: Context
    private lateinit var mActivity: FragmentActivity
    private var imgUrl:String ?= null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if(this::mContext.isInitialized){

        }else{

        }
        mContext = context
        mActivity = requireActivity()

        val args = ZoomableViewFragArgs.fromBundle(requireArguments())
        imgUrl = args.imgUrl
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        fragmentZoomableViewBinding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_zoomable_view, container, false)

        fragmentZoomableViewBinding.lifecycleOwner = this
        return fragmentZoomableViewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Glide.with(mContext).load(imgUrl).into(fragmentZoomableViewBinding.imgPhotoView)
    }
}
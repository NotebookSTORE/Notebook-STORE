package com.notebook.android.ui.productDetail.frag

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.SnapHelper
import com.notebook.android.R
import com.notebook.android.databinding.FragmentZoomableViewBinding

class ZoomableViewFrag : Fragment() {

    private var position: Int = 0
    private lateinit var fragmentZoomableViewBinding: FragmentZoomableViewBinding

    private lateinit var mContext: Context
    private lateinit var mActivity: FragmentActivity
    private var imgUrl: Array<String>? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (this::mContext.isInitialized) {

        } else {

        }
        mContext = context
        mActivity = requireActivity()

        val args = ZoomableViewFragArgs.fromBundle(requireArguments())
        imgUrl = args.imgUrl
        position = args.imgPosition
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        fragmentZoomableViewBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_zoomable_view, container, false
        )

        fragmentZoomableViewBinding.lifecycleOwner = this
        return fragmentZoomableViewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        imgUrl?.let {
            fragmentZoomableViewBinding.rvPhotos.adapter = ZoomImageAdapter(requireContext(), it)
            LinearSnapHelper().attachToRecyclerView(fragmentZoomableViewBinding.rvPhotos)
            fragmentZoomableViewBinding.rvPhotos.scrollToPosition(position)
        }
    }
}
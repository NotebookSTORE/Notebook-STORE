package com.notebook.android.ui.popupDialogFrag

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.notebook.android.R
import com.notebook.android.databinding.RequestReturnReasonLayoutBinding

class RequestReturnDialog: DialogFragment(), View.OnClickListener {

    internal lateinit var returnLayoutBinding : RequestReturnReasonLayoutBinding
    private lateinit var returnListener: RequestReturnListener

    fun setRequestReturnListener(returnListener: RequestReturnListener){
        this.returnListener = returnListener
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {

        val window: Window = dialog?.window!!
        window.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        window.setGravity(Gravity.CENTER)
        returnLayoutBinding = DataBindingUtil.inflate(inflater,
            R.layout.request_return_reason_layout, container, false)

        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        returnLayoutBinding.tvReason1.setOnClickListener(this)
        returnLayoutBinding.tvReason2.setOnClickListener(this)
        returnLayoutBinding.tvReason3.setOnClickListener(this)
        returnLayoutBinding.tvReason4.setOnClickListener(this)
        returnLayoutBinding.tvReason5.setOnClickListener(this)
        returnLayoutBinding.imgClosePopup.setOnClickListener(this)

        return returnLayoutBinding.root
    }

    interface RequestReturnListener{
        fun onGetReason(reason:String)
    }

    override fun onClick(p0: View?) {
        when(p0){

            returnLayoutBinding.imgClosePopup -> {
                dismissAllowingStateLoss()
            }

            returnLayoutBinding.tvReason1 -> {
                returnListener.onGetReason(returnLayoutBinding.tvReason1.text.toString())
                dismissAllowingStateLoss()
            }

            returnLayoutBinding.tvReason2 -> {
                returnListener.onGetReason(returnLayoutBinding.tvReason2.text.toString())
                dismissAllowingStateLoss()
            }

            returnLayoutBinding.tvReason3 -> {
                returnListener.onGetReason(returnLayoutBinding.tvReason3.text.toString())
                dismissAllowingStateLoss()
            }

            returnLayoutBinding.tvReason4 -> {
                returnListener.onGetReason(returnLayoutBinding.tvReason4.text.toString())
                dismissAllowingStateLoss()
            }

            returnLayoutBinding.tvReason5 -> {
                returnListener.onGetReason(returnLayoutBinding.tvReason5.text.toString())
                dismissAllowingStateLoss()
            }
        }
    }
}
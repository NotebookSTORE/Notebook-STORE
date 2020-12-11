package com.notebook.android.ui.popupDialogFrag

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatEditText
import androidx.fragment.app.DialogFragment
import com.notebook.android.R

class CancelOrderDialog : DialogFragment(), RequestReturnDialog.RequestReturnListener {

    internal lateinit var view : View
    private var cancelOrderListener:CancelOrderReasonListener ?= null
    fun setCancelOrderListener(cancelOrderListener:CancelOrderReasonListener){
        this.cancelOrderListener = cancelOrderListener
    }

    private lateinit var edtReasonForCancel:AppCompatEditText

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        view = inflater.inflate(R.layout.cancel_order_dialog_layout, container, false)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val imgClosePopup:ImageView = view.findViewById(R.id.imgClosePopup)
        val tvCancelYes:TextView = view.findViewById(R.id.tvCancelYes)
        val tvCancelNo: TextView = view.findViewById(R.id.tvSubmit)
        edtReasonForCancel = view.findViewById(R.id.edtReasonForCancel)
        val tvErrorMsg: TextView = view.findViewById(R.id.tvErrorMsg)

        edtReasonForCancel.setOnClickListener {
            val returnDialog = RequestReturnDialog()
            returnDialog.setRequestReturnListener(this)
            returnDialog.show(requireActivity().supportFragmentManager, "Return Dialog")
        }

        imgClosePopup.setOnClickListener {
            dismissAllowingStateLoss()
        }

        tvCancelYes.setOnClickListener {
            dismissAllowingStateLoss()
        }

        tvCancelNo.setOnClickListener {
           val value = edtReasonForCancel.text.toString()
            if(TextUtils.isEmpty(value)){
                tvErrorMsg.visibility = View.VISIBLE
                tvErrorMsg.text = "* Please enter reason for cancel order"
            }else{
                cancelOrderListener?.onGetReason(value)
                dismissAllowingStateLoss()
            }
        }
        return view
    }

    interface CancelOrderReasonListener{
        fun onGetReason(reason:String)
    }

    override fun onGetReason(reason: String) {
        edtReasonForCancel.setText(reason)
    }
}
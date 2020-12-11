package com.notebook.android.ui.popupDialogFrag

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.notebook.android.R
import com.notebook.android.databinding.AddressRequiredDialogBinding

class RequiredAddressDialog : DialogFragment() {

    internal lateinit var addressRequiredBinding : AddressRequiredDialogBinding
    private lateinit var requiredListener: AddressRequiredListener

    fun setAddresListener(requiredListener: AddressRequiredListener){
        this.requiredListener = requiredListener
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
        addressRequiredBinding = DataBindingUtil.inflate(inflater,
            R.layout.address_required_dialog, container, false)

        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        addressRequiredBinding.tvCancelNo.setOnClickListener {
            dismissAllowingStateLoss()
        }

        val arguments = requireArguments().getString("defaultAddr")
        addressRequiredBinding.tvCancelRequest.text = arguments

        addressRequiredBinding.tvCancelYes.setOnClickListener {
            requiredListener.onRequiredAddress(true)
            dismissAllowingStateLoss()
        }
        return addressRequiredBinding.root
    }

    interface AddressRequiredListener{
        fun onRequiredAddress(isAccept:Boolean)
    }
}
package com.notebook.android.ui.popupDialogFrag

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.notebook.android.R
import com.notebook.android.databinding.FragmentVerificationSuccessDialogBinding

class ConfirmationDialog : DialogFragment() {

    private lateinit var verifyBinding : FragmentVerificationSuccessDialogBinding
    private var confirmDialogDismiss: ConfirmDialogDismiss ?= null

    fun setDialogListener(confirmDialogDismiss: ConfirmDialogDismiss){
        this.confirmDialogDismiss = confirmDialogDismiss
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        verifyBinding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_verification_success_dialog, container, false)

        activity?.let {
            Glide.with(it).load(R.raw.succes_animation).into(verifyBinding.imgVerificatinSuccessGif)
        }

        if(arguments != null){
            val confirmMsg = requireArguments().getString("toastMsg")
            verifyBinding.tvSuccessMessage.text = confirmMsg
        }
        handleScreenForHome()
        return verifyBinding.root
    }

    private fun handleScreenForHome(){
        Handler().postDelayed({
            dismissAllowingStateLoss()
            confirmDialogDismiss?.ondismissed()
        }, 2000)
    }

    interface ConfirmDialogDismiss{
        fun ondismissed()
    }
}
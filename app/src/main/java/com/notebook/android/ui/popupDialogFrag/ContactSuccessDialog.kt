package com.notebook.android.ui.popupDialogFrag

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.notebook.android.R
import com.notebook.android.databinding.ContactSuccessDialogLayoutBinding
import com.notebook.android.databinding.FragmentVerificationSuccessDialogBinding
import com.notebook.android.ui.auth.responseListener.SuccessVerificationListener

class ContactSuccessDialog : DialogFragment() {

    private lateinit var verifyBinding : ContactSuccessDialogLayoutBinding
    private lateinit var successVerificationListener: SuccessVerificationListener

    fun setSuccessListener(successVerificationListener: SuccessVerificationListener){
        this.successVerificationListener = successVerificationListener
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        verifyBinding = DataBindingUtil.inflate(inflater, R.layout.contact_success_dialog_layout, container, false)

        if(arguments != null){
            val msg = requireArguments().getString("msg")
            verifyBinding.tvSuccessMessage.text = msg
        }
        activity?.let {
            Glide.with(it).load(R.raw.succes_animation).into(verifyBinding.imgVerificatinSuccessGif)
        }

        handleScreenForHome()
        return verifyBinding.root
    }

    private fun handleScreenForHome(){
        Handler().postDelayed({
           successVerificationListener.userRegisteredSuccessfully(true)
            dismissAllowingStateLoss()
        }, 2500)
    }
}
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
import com.notebook.android.databinding.FragmentVerificationSuccessDialogBinding
import com.notebook.android.ui.auth.responseListener.SuccessVerificationListener

class VerificationSuccesDialog : DialogFragment() {

    private lateinit var verifyBinding : FragmentVerificationSuccessDialogBinding
    private lateinit var successVerificationListener: SuccessVerificationListener

    fun setSuccessListener(successVerificationListener: SuccessVerificationListener){
        this.successVerificationListener = successVerificationListener
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
            val title = requireArguments().getString("successText")
            verifyBinding.tvSuccessMessage.text = title
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
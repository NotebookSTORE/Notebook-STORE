package com.notebook.android.ui.popupDialogFrag

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.tasks.Task
import com.max.ecomaxgo.maxpe.view.flight.utility.toastShow
import com.notebook.android.R
import com.notebook.android.application.AppSignatureHelper
import com.notebook.android.databinding.VerifyResetPasswordPopupLayoutBinding
import com.notebook.android.ui.auth.responseListener.OtpVerificationListener

class OtpVerificationOfResetPassDialog : DialogFragment() {

    private var mobNumber:String ?= null
    private var otpValue:String ?= null
    private var otpEnterByUser:String ?= null

    private lateinit var  mActivity: FragmentActivity
    private lateinit var verifyPopupBinding:VerifyResetPasswordPopupLayoutBinding
    private lateinit var otpVerificationListener: OtpVerificationListener

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mActivity = requireActivity()
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    private val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action.equals("otp", true)) {
                val message:String = intent?.getStringExtra("otpValue")?:""
                Log.e("sms data", " :: ${message}")
                verifyPopupBinding.otpView.setText(message)
            }
        }
    }

    private fun startSMSRetriever() {

        val appSignatureHelper = AppSignatureHelper(requireContext())
        Log.e("appSignature", " :: ${appSignatureHelper.getAppSignatures()}")
        // Get an instance of SmsRetrieverClient, used to start listening for a matching SMS message.
        val client = SmsRetriever.getClient(requireContext() /* context */);

        // Starts SmsRetriever, which waits for ONE matching SMS message until timeout
        // (5 minutes). The matching SMS message will be sent via a Broadcast Intent with
        // action SmsRetriever#SMS_RETRIEVED_ACTION.
        val task: Task<Void> = client.startSmsRetriever()

        // Listen for success/failure of the start Task. If in a background thread, this
        // can be made blocking using Tasks.await(task, [timeout]);
        task.addOnSuccessListener {
            Log.d("SmsRetriever", "SmsRetriever Start Success :: ${task.result}")
        }

        task.addOnFailureListener {
            Log.d("SmsRetriever", "SmsRetriever Start Failed")
        }
    }


    fun setVerificationListener(otpVerificationListener: OtpVerificationListener){
        this.otpVerificationListener = otpVerificationListener
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        verifyPopupBinding = DataBindingUtil.inflate(inflater,
            R.layout.verify_reset_password_popup_layout, container, false)
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(receiver, IntentFilter("otp"))

        if(arguments != null){
            mobNumber = requireArguments().getString("mobile")
            otpValue = requireArguments().getString("otp")
            Log.e("data", " :: $mobNumber :: $otpValue")
        }
        startSMSRetriever()
        verifyPopupBinding.tvResendOptText.text = "OTP sent to $mobNumber"
        verifyPopupBinding.otpView.setAnimationEnable(true)
        verifyPopupBinding.otpView.setOtpCompletionListener {
            Log.e("otp value", it)
            otpEnterByUser = it
//            otpVerificationListener.otpVerifyData(otpEnterByUser!!)
//            dismissAllowingStateLoss()
        }

        verifyPopupBinding.tvResendOtp.setOnClickListener{
            otpVerificationListener.resendOtpCall(true)
            dismissAllowingStateLoss()
        }

        verifyPopupBinding.tvChangeNumber.setOnClickListener{
            dismissAllowingStateLoss()
        }

        verifyPopupBinding.imgCloseDialog.setOnClickListener {
            dismissAllowingStateLoss()
        }

        verifyPopupBinding.btnVerifyOtp.setOnClickListener {
            if(otpEnterByUser.isNullOrEmpty()){
                context?.toastShow("Please enter otp")
            }else if(!otpEnterByUser.equals(otpValue)){
                context?.toastShow("You have to enter wrong OTP")
            }else{
                otpVerificationListener.otpVerifyData(otpEnterByUser!!)
                dismissAllowingStateLoss()
            }
        }
        return verifyPopupBinding.root
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(receiver)
    }
}
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
import com.notebook.android.databinding.VerificationPopupDialogLayoutBinding
import com.notebook.android.ui.auth.responseListener.OtpVerificationListener
import com.notebook.android.ui.dashboard.MainDashboardPage
import java.util.*

class VerificationPopupDialog : DialogFragment() {

    private lateinit var  mActivity: FragmentActivity
    private lateinit var verificationBinding: VerificationPopupDialogLayoutBinding
    private lateinit var otpVerificationListener: OtpVerificationListener

    private var mobNumber:String ?= null
    private var otpValue:String ?= null
    private var otpEnterByUser:String ?= null

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
                verificationBinding.otpView.setText(message)
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
        verificationBinding = DataBindingUtil.inflate(inflater,
            R.layout.verification_popup_dialog_layout, container, false)
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(receiver, IntentFilter("otp"))

        if(arguments != null){
            mobNumber = requireArguments().getString("mobile")
            otpValue = requireArguments().getString("otp")
            Log.e("data", " :: $mobNumber :: $otpValue")
        }

        startSMSRetriever()
        setTimer()
        verificationBinding.otpView.setAnimationEnable(true)
        verificationBinding.otpView.setOtpCompletionListener {
            Log.e("otp value", it)
            otpEnterByUser = it
//            otpVerificationListener.otpVerifyData(otpEnterByUser!!)
//            dismissAllowingStateLoss()
        }

        verificationBinding.imgCloseDialog.setOnClickListener {
            dismissAllowingStateLoss()
        }

        verificationBinding.btnVerifyOtp.setOnClickListener {
            if(otpEnterByUser.isNullOrEmpty()){
                context?.toastShow("Please enter otp")
            }else if(!otpEnterByUser.equals(otpValue)){
                context?.toastShow("You have to enter wrong OTP")
            }else{
                otpVerificationListener.otpVerifyData(otpEnterByUser!!)
                dismissAllowingStateLoss()
            }
        }

        verificationBinding.tvResendOTP.setOnClickListener {
            otpVerificationListener.resendOtpCall(true)
            verificationBinding.llTimerResendOTP.visibility = View.VISIBLE
            verificationBinding.tvResendOTP.visibility = View.GONE
            dismissAllowingStateLoss()
        }
        return verificationBinding.root
    }

    private fun setTimer(){

        var seconds = 60
        val timerOtp = Timer()
        timerOtp.scheduleAtFixedRate(object : TimerTask() {

            override fun run() {
                mActivity.runOnUiThread {
                    verificationBinding.tvOtpTimer.text = "$seconds Sec"
                    seconds -= 1

                    if(seconds == 0) {
                        verificationBinding.llTimerResendOTP.visibility = View.GONE
                        verificationBinding.tvResendOTP.visibility = View.VISIBLE
                        timerOtp.purge()
                    }
                }
            }
        }, 0, 1000)
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(receiver)
    }
}
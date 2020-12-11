package com.notebook.android.ui.auth.frag

import android.os.Bundle
import android.os.Handler
import android.text.InputType
import android.text.TextUtils
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.RadioButton
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.max.ecomaxgo.maxpe.view.flight.utility.showSnackBar
import com.max.ecomaxgo.maxpe.view.flight.utility.toastShow
import com.max.ecomaxgo.maxpe.view.flight.utility.validateEmail

import com.notebook.android.R
import com.notebook.android.data.db.entities.User
import com.notebook.android.databinding.FragmentResetPasswordByOptionBinding
import com.notebook.android.model.auth.ForgotPass
import com.notebook.android.ui.auth.factory.AuthViewModelFactory
import com.notebook.android.ui.auth.responseListener.ForgotResponseListener
import com.notebook.android.ui.auth.responseListener.OtpVerificationListener
import com.notebook.android.ui.auth.viewmodel.AuthViewModel
import com.notebook.android.ui.popupDialogFrag.OtpVerificationOfResetPassDialog
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance

class ResetPasswordByOptionFrag : Fragment(), KodeinAware, View.OnClickListener,
    ForgotResponseListener, OtpVerificationListener {

    private lateinit var resetPassBinding:FragmentResetPasswordByOptionBinding
    private lateinit var authViewModel: AuthViewModel

    override val kodein by kodein()
    private val viewModelFactory : AuthViewModelFactory by instance<AuthViewModelFactory>()
    private var fieldValue:String ?= null
    private lateinit var rdSelectOption:RadioButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        resetPassBinding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_reset_password_by_option, container, false)
        resetPassBinding.lifecycleOwner = this
        activity?.let {
            authViewModel = ViewModelProvider(it, viewModelFactory).get(AuthViewModel::class.java)
        }
        authViewModel.forgotResponseListener = this

        rdSelectOption = resetPassBinding.root
            .findViewById(resetPassBinding.rgPasswordResetSelection.checkedRadioButtonId)
        fieldValue = rdSelectOption.text.toString()
        resetPassBinding.edtInputValue.inputType = InputType.TYPE_CLASS_PHONE
        Log.e("Field Value", " :: $fieldValue")

        resetPassBinding.rgPasswordResetSelection.setOnCheckedChangeListener { _, checkedId ->
            rdSelectOption = resetPassBinding.root.findViewById(checkedId)

            if(rdSelectOption.text.toString().equals("email", true)){
                fieldValue = rdSelectOption.text.toString()
                resetPassBinding.edtInputValue.setHint("Email")
                resetPassBinding.edtInputValue.inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
                resetPassBinding.edtInputValue.setText("")
            }else{
                fieldValue = rdSelectOption.text.toString()
                resetPassBinding.edtInputValue.setHint("Mobile Number")
                resetPassBinding.edtInputValue.inputType = InputType.TYPE_CLASS_PHONE
                resetPassBinding.edtInputValue.setText("")
            }
        }
        resetPassBinding.btnSentOtpForResetPass.setOnClickListener(this)

        return resetPassBinding.root
    }

    private lateinit var navController: NavController
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
    }

    override fun onClick(v: View?) {
        when(v){
            resetPassBinding.btnSentOtpForResetPass -> {
                val value = resetPassBinding.edtInputValue.text.toString()
                if(TextUtils.isEmpty(value)){
                    resetPassBinding.root.showSnackBar("Please enter input value")
                }else{
                    if(fieldValue.equals("email", true)){
                        if(!validateEmail(value)){
                            showErrorView("Please enter valid email")
                        }else{
                            authViewModel.forgotPass(value)
                            resetPassBinding.btnSentOtpForResetPass.visibility = View.GONE
                            resetPassBinding.pbForgotLoading.visibility = View.VISIBLE
                        }
                    }else{
                        if(value.length < 10){
                            showErrorView("Please enter valid mobile number")
                        }else{
                            authViewModel.forgotPass(value)
                            resetPassBinding.btnSentOtpForResetPass.visibility = View.GONE
                            resetPassBinding.pbForgotLoading.visibility = View.VISIBLE
                        }
                    }
                }
            }
        }
    }

    override fun onSuccess(response: ForgotPass) {
        resetPassBinding.btnSentOtpForResetPass.visibility = View.VISIBLE
        resetPassBinding.pbForgotLoading.visibility = View.GONE

        val verificationPopupDialog = OtpVerificationOfResetPassDialog()
        verificationPopupDialog.isCancelable = false
        val bundle = Bundle()
        bundle.putString("mobile", resetPassBinding.edtInputValue.text.toString())
        bundle.putString("otp", response.otp)
        verificationPopupDialog.arguments = bundle
        verificationPopupDialog.setVerificationListener(this)
        verificationPopupDialog.show(requireActivity().supportFragmentManager, "Show Verification Popup !!")
    }

    override fun onFailure(msg: String) {
        showErrorView(msg)
        resetPassBinding.btnSentOtpForResetPass.visibility = View.VISIBLE
        resetPassBinding.pbForgotLoading.visibility = View.GONE
    }

    private fun showErrorView(msg:String){
        resetPassBinding.clErrorView.visibility = View.VISIBLE
        resetPassBinding.tvErrorText.text = msg
        resetPassBinding.clErrorView.startAnimation(AnimationUtils.loadAnimation(requireContext(),R.anim.slide_down))

        Handler().postDelayed({
            resetPassBinding.clErrorView.visibility = View.GONE
            resetPassBinding.clErrorView.startAnimation(AnimationUtils.loadAnimation(requireContext(),R.anim.slide_up))
        }, 1200)
        }

    override fun onOtpSuccess(resp: User) {
        val optionFragmentAction = ResetPasswordByOptionFragDirections
            .actionResetPasswordByOptionFragToResetPasswordWithNewPasswordFrag()
        optionFragmentAction.fieldValue = resetPassBinding.edtInputValue.text.toString()
        optionFragmentAction.fieldType = fieldValue!!
        navController.navigate(optionFragmentAction)
    }

    override fun otpVerifyData(otpValue: String) {
        if (fieldValue.equals("email", true)){
            authViewModel.verifyOtpWithForgotPassEmail(resetPassBinding.edtInputValue.text.toString(), otpValue)
        }else{
            authViewModel.verifyOtpWithForgotPass(resetPassBinding.edtInputValue.text.toString(), otpValue)
        }
    }

    override fun resendOtpCall(resend: Boolean) {
        val value = resetPassBinding.edtInputValue.text.toString()
        if (fieldValue.equals("email", true)){
            authViewModel.forgotPass(value)
        }else{
            authViewModel.forgotPass(value)
        }
    }
}
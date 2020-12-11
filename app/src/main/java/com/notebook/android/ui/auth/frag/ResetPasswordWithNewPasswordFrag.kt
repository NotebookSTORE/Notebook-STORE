package com.notebook.android.ui.auth.frag

import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.Navigation
import com.google.android.gms.auth.api.Auth
import com.max.ecomaxgo.maxpe.view.flight.utility.showSnackBar
import com.max.ecomaxgo.maxpe.view.flight.utility.toastShow

import com.notebook.android.R
import com.notebook.android.databinding.FragmentResetPasswordWithNewPasswordBinding
import com.notebook.android.ui.auth.factory.AuthViewModelFactory
import com.notebook.android.ui.auth.responseListener.ChangePassListener
import com.notebook.android.ui.auth.responseListener.SuccessVerificationListener
import com.notebook.android.ui.auth.viewmodel.AuthViewModel
import com.notebook.android.ui.popupDialogFrag.VerificationSuccesDialog
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance

class ResetPasswordWithNewPasswordFrag : Fragment(), KodeinAware, View.OnClickListener, ChangePassListener,
    SuccessVerificationListener {

    private lateinit var resetPassBinding:FragmentResetPasswordWithNewPasswordBinding
    private lateinit var authViewModel: AuthViewModel

    override val kodein by kodein()
    private val viewModelFactory : AuthViewModelFactory by instance<AuthViewModelFactory>()
    private lateinit var fieldValue:String
    private lateinit var fieldType:String
    private lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        resetPassBinding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_reset_password_with_new_password, container, false)
        resetPassBinding.lifecycleOwner = this
        activity?.let {
            authViewModel = ViewModelProvider(it, viewModelFactory).get(AuthViewModel::class.java)
        }
        authViewModel.changePassListener = this

        resetPassBinding.btnSetNewPassword.setOnClickListener(this)
        return resetPassBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = Navigation.findNavController(view)
        if(arguments != null){
            val args = ResetPasswordWithNewPasswordFragArgs.fromBundle(requireArguments())
            fieldValue = args.fieldValue
            fieldType = args.fieldType
        }
    }

    override fun onClick(v: View?) {
        val newPass = resetPassBinding.edtNewPass.text.toString()
        val confPass = resetPassBinding.edtConfPass.text.toString()

        if(TextUtils.isEmpty(newPass)){
            showErrorView("Please enter New Password")
        }else if(TextUtils.isEmpty(confPass)){
            showErrorView("Please enter Confirm Password")
        }else if(newPass.length < 8){
           showErrorView("Please enter atleast 8 character of password")
        }else if(!newPass.equals(confPass, true)){
            showErrorView("Your password doesn't match, please check it again")
        }else{
            if(fieldType.equals("email", true)){
                authViewModel.changePassWithEmail(fieldValue, newPass)
                resetPassBinding.btnSetNewPassword.visibility = View.GONE
                resetPassBinding.pbForgotLoading.visibility = View.VISIBLE
            }else{
                authViewModel.changePass(fieldValue, newPass)
                resetPassBinding.btnSetNewPassword.visibility = View.GONE
                resetPassBinding.pbForgotLoading.visibility = View.VISIBLE
            }
        }
    }

    override fun onSuccess(status: Boolean) {
        resetPassBinding.btnSetNewPassword.visibility = View.VISIBLE
        resetPassBinding.pbForgotLoading.visibility = View.GONE

        val verificationSuccessDialog = VerificationSuccesDialog()
        verificationSuccessDialog.isCancelable = false
        val bundle = Bundle()
        bundle.putString("successText", "Password reset successfully")
        verificationSuccessDialog.setSuccessListener(this)
        verificationSuccessDialog.arguments = bundle
        verificationSuccessDialog.show(requireActivity().supportFragmentManager, "Verification Successful !!")
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

    override fun onFailure(msg: String) {
       showErrorView(msg)
        resetPassBinding.btnSetNewPassword.visibility = View.VISIBLE
        resetPassBinding.pbForgotLoading.visibility = View.GONE
    }

    override fun userRegisteredSuccessfully(isSuccess: Boolean) {
        val navOptions = NavOptions.Builder().setPopUpTo(R.id.loginFrag, true).build()
        navController.navigate(R.id.action_resetPasswordWithNewPasswordFrag_to_loginFrag, null, navOptions)
    }
}

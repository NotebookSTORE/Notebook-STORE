package com.notebook.android.ui.popupDialogFrag

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.notebook.android.R
import com.notebook.android.databinding.UserLoginRequestLayoutBinding

class UserLogoutDialog : DialogFragment() {

    internal lateinit var userLoginPopupBinding : UserLoginRequestLayoutBinding
    private lateinit var loginPopupListener: UserLoginPopupListener

    fun setUserLoginRequestListener(loginPopupListener: UserLoginPopupListener){
        this.loginPopupListener = loginPopupListener
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog?.window?.setGravity(Gravity.CENTER)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        userLoginPopupBinding = DataBindingUtil.inflate(inflater,
            R.layout.user_login_request_layout, container, false)

        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        if((arguments != null)){
            val displayTitle = requireArguments().getString("displayTitle")
            userLoginPopupBinding.tvCancelRequest.text = displayTitle
        }else{
            userLoginPopupBinding.tvCancelRequest.text = "Please login first, if you want to use this feature !!"
        }

        userLoginPopupBinding.tvCancelNo.setOnClickListener {
            dismissAllowingStateLoss()
        }

        userLoginPopupBinding.imgCloseDialog.setOnClickListener {
            dismissAllowingStateLoss()
        }

        userLoginPopupBinding.tvCancelYes.setOnClickListener {
            loginPopupListener.onUserAccepted(true)
            dismissAllowingStateLoss()
        }
        return userLoginPopupBinding.root
    }

    interface UserLoginPopupListener{
        fun onUserAccepted(isAccept:Boolean)
    }
}
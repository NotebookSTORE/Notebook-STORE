package com.notebook.android.ui.popupDialogFrag

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.notebook.android.R
import com.notebook.android.databinding.CouponAlertDialogLayoutBinding
import com.notebook.android.databinding.UserLoginRequestLayoutBinding

class CouponAlertDialog : DialogFragment() {

    internal lateinit var userLoginPopupBinding : CouponAlertDialogLayoutBinding
  /*  private lateinit var loginPopupListener: UserLoginPopupListener

    fun setUserLoginRequestListener(loginPopupListener: UserLoginPopupListener){
        this.loginPopupListener = loginPopupListener
    }*/

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog?.window?.setGravity(Gravity.CENTER)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        userLoginPopupBinding = DataBindingUtil.inflate(inflater,
            R.layout.coupon_alert_dialog_layout, container, false)

        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        if((arguments != null)) {
            val displayTitle = requireArguments().getString("displayTitle")
            userLoginPopupBinding.tvCancelRequest.text = displayTitle
        }

        userLoginPopupBinding.tvCancelNo.setOnClickListener {
            dismissAllowingStateLoss()
        }

        userLoginPopupBinding.tvCancelYes.setOnClickListener {
//            loginPopupListener.onUserAccepted(true)
            dismissAllowingStateLoss()
        }
        return userLoginPopupBinding.root
    }

    /*interface UserLoginPopupListener{
        fun onUserAccepted(isAccept:Boolean)
    }*/
}
package com.notebook.android.ui.popupDialogFrag

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.notebook.android.R
import com.notebook.android.ui.dashboard.listener.LogoutListener

class LogoutDialogFrag : DialogFragment() {

    internal lateinit var view : View
    private lateinit var logoutListener: LogoutListener

    fun setLogoutListener(logoutListener: LogoutListener){
        this.logoutListener = logoutListener
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        view = inflater.inflate(R.layout.fragment_logout_dialog, container, false)

        val tvCancelNo: TextView = view.findViewById(R.id.tvCancelNo)
        val tvCancelYes: TextView = view.findViewById(R.id.tvCancelYes)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        tvCancelNo.setOnClickListener {
            dismiss()
        }

        tvCancelYes.setOnClickListener {
            logoutListener.logoutListener()
            dismissAllowingStateLoss()
        }
        return view
    }
}
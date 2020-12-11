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

class LoadingDialog : DialogFragment() {

    internal lateinit var view : View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        view = inflater.inflate(R.layout.loading_dialog_layout, container, false)

        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return view
    }

    override fun isCancelable(): Boolean {
        return false
    }
}
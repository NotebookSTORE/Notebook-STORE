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

class RegisterForDialog : DialogFragment() {

    internal lateinit var view : View
    private lateinit var registerListener: RegisterForListener

    fun setRegisterForListener(registerListener: RegisterForListener){
        this.registerListener = registerListener
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        view = inflater.inflate(R.layout.register_for_type_layout, container, false)

        val tvRegisterIndividual: TextView = view.findViewById(R.id.tvRegisterIndividual)
        val tvRegisterInstitute: TextView = view.findViewById(R.id.tvRegisterInstitute)
//        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        tvRegisterIndividual.setOnClickListener {
            registerListener.getRegisterType("Individual", 1)
            dismissAllowingStateLoss()
        }

        tvRegisterInstitute.setOnClickListener {
            registerListener.getRegisterType("Institute", 2)
            dismissAllowingStateLoss()
        }
        return view
    }

    interface RegisterForListener{
        fun getRegisterType(data:String, type:Int)
    }
}
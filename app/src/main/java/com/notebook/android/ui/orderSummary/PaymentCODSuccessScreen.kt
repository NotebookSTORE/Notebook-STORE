package com.notebook.android.ui.orderSummary

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.notebook.android.R
import com.notebook.android.databinding.FragmentPaymentCODSuccessScreenBinding

class PaymentCODSuccessScreen : Fragment() {

    private lateinit var fragPaymentCODBinding:FragmentPaymentCODSuccessScreenBinding
    private lateinit var navController:NavController

    private lateinit var mContext: Context
    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext= context
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        fragPaymentCODBinding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_payment_c_o_d_success_screen, container, false)

        if(arguments != null){
            val paymentArgs = PaymentCODSuccessScreenArgs.fromBundle(requireArguments())
            Log.e("paymentStatus", " :: ${paymentArgs.status}")

            if(paymentArgs.status == 1){
                fragPaymentCODBinding.imgVerificatinSuccessGif.apply {
                    setAnimation(R.raw.payment_success)
                    playAnimation()
                    loop(true)
                }
            }else{
                fragPaymentCODBinding.imgVerificatinSuccessGif.apply {
                    setAnimation(R.raw.transaction_failed_animation)
                    playAnimation()
                    loop(true)
                }
            }
            fragPaymentCODBinding.tvOrderID.text = paymentArgs.orderID
            fragPaymentCODBinding.tvOrderAmount.text = "Rs. ${paymentArgs.orderAmount}"
            fragPaymentCODBinding.tvOrderStatus.text = paymentArgs.msg
        }
        return fragPaymentCODBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)

        fragPaymentCODBinding.tvGoToHomePaymentSuccess.setOnClickListener{
            navController.popBackStack(R.id.homeFrag, false)
        }
        handleScreenForHome()
    }

    private fun handleScreenForHome(){
        Handler().postDelayed({
            navController.popBackStack(R.id.homeFrag, false)
        }, 4000)
    }

}

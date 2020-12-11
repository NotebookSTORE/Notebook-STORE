package com.notebook.android.ui.myAccount.wallet

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.Navigation

import com.notebook.android.R
import com.notebook.android.databinding.FragmentPaymentInfoCardBinding

class PaymentInfoCardFrag : Fragment() {

    private lateinit var fragmentPaymentInfoCardBinding: FragmentPaymentInfoCardBinding
    private lateinit var navController: NavController
    private var addAmountValue:Int ?= null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        fragmentPaymentInfoCardBinding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_payment_info_card, container, false)

        val paymentInfoArgs = PaymentInfoCardFragArgs.fromBundle(requireArguments())
         addAmountValue = paymentInfoArgs.addWalletAmount
        fragmentPaymentInfoCardBinding.edtAmountToAdd.setText("₹ $addAmountValue")
        return fragmentPaymentInfoCardBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = Navigation.findNavController(view)
        fragmentPaymentInfoCardBinding.btnPaymentProceedToPay.setOnClickListener {
            val paymentInfoDirections:PaymentInfoCardFragDirections.ActionPaymentInfoCardFragToPaymentFinishFrag =
                PaymentInfoCardFragDirections.actionPaymentInfoCardFragToPaymentFinishFrag(addAmountValue!!)
            navController.navigate(paymentInfoDirections)
        }
    }

}

package com.notebook.android.ui.myAccount.wallet

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.max.ecomaxgo.maxpe.view.flight.utility.showSnackBar

import com.notebook.android.R
import com.notebook.android.databinding.FragmentPaymentFinishBinding

class PaymentFinishFrag : Fragment() {

    private lateinit var paymentFinishBinding:FragmentPaymentFinishBinding
    private lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        paymentFinishBinding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_payment_finish, container, false)

        val paymentFinishArgs = PaymentFinishFragArgs.fromBundle(requireArguments())
        paymentFinishBinding.edtAmountToAdd.setText("₹ ${paymentFinishArgs.addWalletAmount}")
        return paymentFinishBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = Navigation.findNavController(view)
        paymentFinishBinding.btnPaymentFinish.setOnClickListener {
            paymentFinishBinding.root.showSnackBar("Task is Pending")
        }
    }

}

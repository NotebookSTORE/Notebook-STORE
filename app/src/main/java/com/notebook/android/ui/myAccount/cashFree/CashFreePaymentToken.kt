package com.notebook.android.ui.myAccount.cashFree

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.gocashfree.cashfreesdk.CFPaymentService
import com.notebook.android.R
import com.notebook.android.databinding.FragmentCashFreePaymentTokenBinding

class CashFreePaymentToken : Fragment() {

    private lateinit var fragCFPaymentWebViewBinding:FragmentCashFreePaymentTokenBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        fragCFPaymentWebViewBinding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_cash_free_payment_token, container, false)
//        doPayment()

        if(arguments != null){
            val cfArgs = CashFreePaymentTokenArgs.fromBundle(requireArguments())
            doPayment(cfArgs.cfTokenObject.cfToken, cfArgs.cfTokenObject.orderId, cfArgs.cfTokenObject.orderAmount,
                cfArgs.cfTokenObject.customerPhone, cfArgs.cfTokenObject.customerEmail)
        }
        return fragCFPaymentWebViewBinding.root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?){
        super.onActivityResult(requestCode, resultCode, data)
        //Same request code for all payment APIs.
        Log.d("CFToken", "ReqCode : " + CFPaymentService.REQ_CODE)
        Log.d("CFToken", "API Response : ")
        //Prints all extras. Replace with app logic.
        if (data != null) {
            if(CFPaymentService.REQ_CODE == Activity.RESULT_OK){
                val bundle = data.getExtras()
                if (bundle != null)
                    for (key in bundle.keySet()) {
                        if (bundle.getString(key) != null) {
                            Log.e("CFToken", key + " : " + bundle.getString(key))
                        }
                    }
            }

        }
    }

    private fun doPayment(cfToken:String, orderId:String, orderAmount:String, custPhone:String, custEmail:String){
        val dataSendMap = HashMap<String, String>()
        dataSendMap.put("appId", "140310079578525432ef90edc13041")
        dataSendMap.put("orderId", orderId)
        dataSendMap.put("orderAmount", orderAmount)
        dataSendMap.put("orderCurrency", "INR")
        dataSendMap.put("customerPhone", custPhone)
        dataSendMap.put("customerEmail", custEmail)
        CFPaymentService.getCFPaymentServiceInstance().doPayment(requireActivity(), dataSendMap, cfToken, "TEST",
            "#784BD2", "#FFFFFF", false)
    }
}
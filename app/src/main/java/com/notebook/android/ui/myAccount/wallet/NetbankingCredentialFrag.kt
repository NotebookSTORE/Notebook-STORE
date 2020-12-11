package com.notebook.android.ui.myAccount.wallet

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.max.ecomaxgo.maxpe.view.flight.utility.showSnackBar

import com.notebook.android.R
import com.notebook.android.adapter.wallet.NetBankingBankAdapter
import com.notebook.android.databinding.FragmentNetbankingCredentialBinding
import com.notebook.android.model.payment.NetbankingData

class NetbankingCredentialFrag : Fragment() {

    private lateinit var fragNetBankingBinding:FragmentNetbankingCredentialBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragNetBankingBinding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_netbanking_credential, container, false)
        setupRecyclerView()
        createStaticList()

        return fragNetBankingBinding.root
    }

    private lateinit var netBankList:ArrayList<NetbankingData>
    private fun createStaticList(){
        netBankList = ArrayList()
        netBankList.add(NetbankingData("HDFC Bank", R.drawable.hdfc_bank, false))
        netBankList.add(NetbankingData("ICICI Bank", R.drawable.icici_bank, false))
        netBankList.add(NetbankingData("State bank of India", R.drawable.sbi_bank, false))
        netBankList.add(NetbankingData("Axis Bank", R.drawable.axis_bank, false))
        netBankList.add(NetbankingData("Kotak Bank", R.drawable.kotak_bank, false))

        val netBankingAdapter = NetBankingBankAdapter(requireContext(), netBankList)
        fragNetBankingBinding.recViewNetBankingList.adapter = netBankingAdapter
    }

    private fun setupRecyclerView(){
        val layoutManager = LinearLayoutManager(requireContext())
        fragNetBankingBinding.recViewNetBankingList.layoutManager = layoutManager
        fragNetBankingBinding.recViewNetBankingList.itemAnimator = DefaultItemAnimator()
        fragNetBankingBinding.recViewNetBankingList.hasFixedSize()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fragNetBankingBinding.btnNetBankingProceedToPay.setOnClickListener {
            fragNetBankingBinding.root.showSnackBar("Task is Pending !!")
        }
    }

}

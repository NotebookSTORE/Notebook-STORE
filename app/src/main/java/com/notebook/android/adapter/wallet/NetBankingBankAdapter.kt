package com.notebook.android.adapter.wallet

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.notebook.android.BR
import com.notebook.android.R
import com.notebook.android.databinding.NetBankingItemLayoutBinding
import com.notebook.android.model.payment.NetbankingData

class NetBankingBankAdapter(val mCtx: Context, val netBankingList:List<NetbankingData>) : RecyclerView.Adapter<NetBankingBankAdapter.NetBankingBankVH>() {

    private var lastSelectedItem = -1
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): NetBankingBankVH {
        val netBankingBinding:NetBankingItemLayoutBinding = DataBindingUtil.inflate(LayoutInflater.from(mCtx),
        R.layout.net_banking_item_layout, parent, false)
        return NetBankingBankVH(netBankingBinding)
    }

    override fun getItemCount(): Int {
return netBankingList.size
    }

    override fun onBindViewHolder(holder: NetBankingBankVH, position: Int) {
        holder.bind(netBankingList[position])

        holder.netBankingBinding!!.rdNetBankingBankName.isChecked = (lastSelectedItem == position)
    }

    inner class NetBankingBankVH(netBankingBinding:NetBankingItemLayoutBinding)
        : RecyclerView.ViewHolder(netBankingBinding.root) {

        var netBankingBinding:NetBankingItemLayoutBinding ?= null
        init {
            this.netBankingBinding = netBankingBinding
        }

        fun bind(netBank:NetbankingData){
            netBankingBinding?.setVariable(BR.netBankingModel, netBank)
            netBankingBinding?.executePendingBindings()

            Glide.with(mCtx).load(netBank.imgDrawable).into(netBankingBinding!!.imgBankLogo)

            val radioButton:RadioButton = netBankingBinding!!.rdNetBankingBankName
            radioButton.isChecked = netBank.isChecked

            netBankingBinding!!.rdNetBankingBankName.setOnClickListener {
                lastSelectedItem = adapterPosition
                notifyDataSetChanged()
            }

            netBankingBinding!!.root.setOnClickListener {
                lastSelectedItem = adapterPosition
                notifyDataSetChanged()
            }
        }
    }
}
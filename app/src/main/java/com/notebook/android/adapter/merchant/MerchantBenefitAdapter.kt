package com.notebook.android.adapter.merchant

import android.content.Context
import android.text.Html
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.notebook.android.BR
import com.notebook.android.R
import com.notebook.android.data.db.entities.MerchantBenefit
import com.notebook.android.databinding.MerchantBenefitPrimeItemLayoutBinding

class MerchantBenefitAdapter(val mCtx: Context, val merchBenefitList:List<MerchantBenefit>
) : RecyclerView.Adapter<MerchantBenefitAdapter.MerchantBenefitVH>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MerchantBenefitVH {
        val merchBenefitBinding: MerchantBenefitPrimeItemLayoutBinding = DataBindingUtil.inflate(
            LayoutInflater.from(mCtx), R.layout.merchant_benefit_prime_item_layout, parent, false
        )
        return MerchantBenefitVH(merchBenefitBinding)
    }

    override fun getItemCount(): Int {
        return merchBenefitList.size
    }

    override fun onBindViewHolder(holder: MerchantBenefitVH, position: Int) {
        val benefitData = merchBenefitList[position]
        holder.bind(benefitData)
    }

    inner class MerchantBenefitVH(val merchBenefitBinding: MerchantBenefitPrimeItemLayoutBinding) :
        RecyclerView.ViewHolder(merchBenefitBinding.root) {

        fun bind(merchBenefitData: MerchantBenefit) {
            merchBenefitBinding.setVariable(BR.merchantBenefitPrimeModal, merchBenefitData)
            merchBenefitBinding.executePendingBindings()

            merchBenefitBinding.tvSecurePaymentDesc.text = Html.fromHtml(merchBenefitData.description).toString()
        }
    }
}
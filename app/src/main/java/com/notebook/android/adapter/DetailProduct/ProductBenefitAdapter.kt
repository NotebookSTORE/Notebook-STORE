package com.notebook.android.adapter.DetailProduct

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.notebook.android.BR
import com.notebook.android.R
import com.notebook.android.databinding.ProductBenefitInfoLayoutBinding
import com.notebook.android.model.home.BenefitProductData
import com.notebook.android.model.home.FreeDeliveryData
import java.util.*

class ProductBenefitAdapter(val mCtx: Context, val benefitList: ArrayList<FreeDeliveryData.FreeDelivery>)
    : RecyclerView.Adapter<ProductBenefitAdapter.ProductBenefitVH>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ProductBenefitVH {
        val benefitProdItemBinding: ProductBenefitInfoLayoutBinding = DataBindingUtil.inflate(
            LayoutInflater.from(mCtx), R.layout.product_benefit_info_layout, parent, false)
        return ProductBenefitVH(benefitProdItemBinding)
    }

    inner class ProductBenefitVH(val benefitProdItemBinding: ProductBenefitInfoLayoutBinding)
        : RecyclerView.ViewHolder(benefitProdItemBinding.root) {

        fun bind(benefitProd: FreeDeliveryData.FreeDelivery){
            benefitProdItemBinding.setVariable(BR.prodBenefitModal, benefitProd)
            benefitProdItemBinding.executePendingBindings()

            benefitProdItemBinding.tvFreeDeliverySubtitle.text = "Rs.${benefitProd.price}"
        }
    }


    override fun getItemCount(): Int {
        return benefitList.size
    }

    override fun onBindViewHolder(holder: ProductBenefitVH, position: Int) {
        holder.bind(benefitList[position])
    }

}
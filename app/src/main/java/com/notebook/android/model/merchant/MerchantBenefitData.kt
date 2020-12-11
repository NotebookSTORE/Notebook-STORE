package com.notebook.android.model.merchant

import com.notebook.android.data.db.entities.MerchantBenefit

data class MerchantBenefitData(
    var status: Int,
    var error: Boolean,
    var msg: String,
    var merchantbenefit:List<MerchantBenefit> ?= null,
    var membership:List<PrimeMemberShipCharge>
){
    data class PrimeMemberShipCharge(
        var primemember_charge:String)
}
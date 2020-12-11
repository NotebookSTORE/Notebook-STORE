package com.notebook.android.model.helpSupport

data class HelpSupportData(
    var status: Int,
    var error: Boolean,
    var msg: String? = null,
    var helpSupport:ArrayList<HelpSupportMain>
) {
    data class HelpSupportMain(
        var email:String,
        var phone:String
    )
}
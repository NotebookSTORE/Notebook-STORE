package com.notebook.android.model

data class AboutUsResponse (
    val error:Boolean,
    val msg:String,
    val status: Int,
    val aboutus: ArrayList<AboutUsDetail>
)

data class AboutUsDetail(
    val id: Int,
    val title:String,
    val description:String
)
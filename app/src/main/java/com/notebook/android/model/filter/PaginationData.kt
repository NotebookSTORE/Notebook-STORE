package com.notebook.android.model.filter

import com.notebook.android.model.home.DiscountedPageDetail
import com.notebook.android.model.home.PageDetail

data class PaginationData(
    var first_page_url: String? = null,
    val from: Int? = 1,
    val last_page: Int? = 3,
    val last_page_url: String? = null,
    val next_page_url: String? = null,
    val path: String? = null,
    val per_page: Int? = 15,
    val prev_page_url: String? = null,
    val to: Int? = null,
    val total: Int? = null
){
    companion object{
        fun create(product: PageDetail?=null): PaginationData {
            return PaginationData(
                 product?.first_page_url,
                 product?.from,
                 product?.last_page,
                product?.last_page_url,
                product?.next_page_url,
                 product?.path,
                 product?.per_page,
                 product?.prev_page_url,
                 product?.to,
                 product?.total
            )
        }

        fun create(product: DiscountedPageDetail? = null): PaginationData {
            return PaginationData(
                product?.first_page_url,
                product?.from,
                product?.last_page,
                product?.last_page_url,
                product?.next_page_url,
                product?.path,
                product?.per_page,
                product?.prev_page_url,
                product?.to,
                product?.total
            )
        }
    }
}
package com.notebook.android.utility

object Constant {
    const val MERCHANT_IMAGE_PATH = "https://demo.mbrcables.com/notebookstore/public/uploads/Merchant/"
    const val CATEGORY_IMAGE_PATH = "https://demo.mbrcables.com/notebookstore/public/uploads/category/"
    const val BANNER_IMAGE_PATH = "https://demo.mbrcables.com/notebookstore/public/uploads/Banner/"
    const val SUB_CATEGORY_IMAGE_PATH = "https://demo.mbrcables.com/notebookstore/public/uploads/subcategory/"

    const val BENEFIETS_IMAGE_PATH = "https://demo.mbrcables.com/notebookstore/public/uploads/Benefit/"
    const val LATEST_OFFER_IMAGE_PATH = "https://demo.mbrcables.com/notebookstore/public/uploads/latestoffer/"
    const val LOGO_IMAGE_PATH = "https://demo.mbrcables.com/notebookstore/public/uploads/Logo/"
    const val POLICY_IMAGE_PATH = "https://demo.mbrcables.com/notebookstore/public/uploads/Policy/"
    const val PRODUCT_IMAGE_PATH = "https://demo.mbrcables.com/notebookstore/public/uploads/product/"
    const val PRODUCTIMAGE_IMAGE_PATH = "https://demo.mbrcables.com/notebookstore/public/uploads/productimage/"
    const val  BRAND_IMAGE_PATH= "https://demo.mbrcables.com/notebookstore/public/uploads/brands/"
    const val  MERCHANT_BANNER_IMAGE_PATH= "https://demo.mbrcables.com/notebookstore/public/uploads/Merchantbanner/"
    const val RATING_PRODUCT_IMAGE_PATH = "https://demo.mbrcables.com/notebookstore/public/uploads/rating/"
    const val BASE_IMAGE_PATH = "https://demo.mbrcables.com/notebookstore/public/uploads/"
    const val MERCHANT_BASE_IMAGE_PATH = "https://demo.mbrcables.com/notebookstore/public/uploads/register/"
    const val DATE_FORMAT = "dd/MM/yyyy"
    const val DATE_FORMAT_SERVER = "yyyy-MM-dd"


    const val GOOGLE_LOGIN = "google"
    const val FACEBOOK_LOGIN = "facebook"
    const val WITHOUT_SOCIAL_LOGIN = "server"

    const val  USER_TYPE_NORMAL_USER = 0
    const val  USER_TYPE_GOOGLE_USER = 1
    const val  USER_TYPE_FACEBOOK_USER = 2


    //Apply Coupon constant field here....
    const val  COUPON_USER_TYPE_NORMAL = 0 //0,9 -> Regular or Normal USER APPLICABLE
    const val  COUPON_USER_TYPE_PRIME = 1 //1 -> Prime User applicable
    const val  COUPON_USER_TYPE_SPECIAL = 2 // email validates in this case
    const val  COUPON_USER_TYPE_BULK = 3 // bulk case me institute yanni register for check -> 2(Institution)

    const val  COUPON_USER_TYPE_NORMAL_GENERIC = 0 //0,9 -> Regular or Normal USER APPLICABLE
    const val  COUPON_USER_TYPE_GENERIC_INSTITUTE = 1 //1 -> Prime User applicable
    const val  COUPON_USER_TYPE_PRODUCT_ONLY = "p" // email validates in this case

    const val CALL_CENTER_NUMBER = "+91-7900609609"

    const val PAYMENT_METHOD_CASHFREE = "cashfree"
    const val PAYMENT_METHOD_WALLET = "wallet"
    const val PAYMENT_METHOD_COD = "Cash On Delivery"

    const val PRIME_MERCHANT_TYPE = 1
    const val REGULAR_MERCHANT_TYPE = 0
    const val NORMAL_MERCHANT_TYPE = 9

    //filter data type constant here....
    const val FILTER_CATEGORY_TYPE = "category"
    const val FILTER_SUB_CATEGORY_TYPE = "subcategory"
    const val FILTER_SUB_SUB_CATEGORY_TYPE = "subsubcategory"
    const val FILTER_LATEST_PRODUCT_TYPE = "latest"
    const val FILTER_BEST_PRODUCT_TYPE = "best"
    const val FILTER_DISCOUNTED_PRODUCT_TYPE = "discount"

    //order status
    const val  ORDER_STATUS_CONFIRM = "DELIVERED"
    const val  ORDER_STATUS_PENDING = "PENDING"
    const val  ORDER_STATUS_NEW = "NEW"
    const val  ORDER_STATUS_CANCEL = "CANCEL"

    //merchant constant declared here....
    const val MERCHANT_PRIME_BENEFIT = "Prime Merchant"
    const val MERCHANT_REGULAR_BENEFIT = "Merchant"


    //banner type constant for getting data
    const val BANNER_TYPE_HOME = 1
    const val BANNER_TYPE_MERCHANT = 2
    const val BANNER_TYPE_BULK_QUERY = 3
}
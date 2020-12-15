package com.notebook.android.ui.orderSummary

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.notebook.android.R
import com.notebook.android.adapter.order.OrderSummaryAdapter
import com.notebook.android.data.db.entities.*
import com.notebook.android.data.preferences.NotebookPrefs
import com.notebook.android.databinding.FragmentOrderSummaryBinding
import com.notebook.android.model.coupon.CouponData
import com.notebook.android.model.home.FreeDeliveryData
import com.notebook.android.model.home.ProductCoupon
import com.notebook.android.model.orderSummary.OrderPaymentDetail
import com.notebook.android.model.productDetail.ProductDetailData
import com.notebook.android.model.productDetail.RatingData
import com.notebook.android.ui.dashboard.cart.CartResponseListener
import com.notebook.android.ui.myOrder.OrderSummaryPage
import com.notebook.android.ui.popupDialogFrag.LoadingDialog
import com.notebook.android.ui.popupDialogFrag.RequiredAddressDialog
import com.notebook.android.ui.popupDialogFrag.UserLogoutDialog
import com.notebook.android.ui.productDetail.DetailProductVM
import com.notebook.android.ui.productDetail.DetailProductVMFactory
import com.notebook.android.ui.productDetail.SharedVM
import com.notebook.android.ui.productDetail.listener.DiscountProdResponseListener
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance
import java.math.RoundingMode
import java.text.DecimalFormat
import kotlin.math.roundToInt

class OrderSummary : Fragment(), KodeinAware, View.OnClickListener,
    UserLogoutDialog.UserLoginPopupListener, RequiredAddressDialog.AddressRequiredListener,
    CartResponseListener, DiscountProdResponseListener, RadioGroup.OnCheckedChangeListener {

    override val kodein by kodein()
    private val viewModelFactory : DetailProductVMFactory by instance()
    private lateinit var fragOrderSummBinding: FragmentOrderSummaryBinding
    private val detailVM: DetailProductVM by lazy {
        ViewModelProvider(mActivity, viewModelFactory).get(DetailProductVM::class.java)
    }

    private val sharedVM: SharedVM by lazy {
        ViewModelProvider(mActivity).get(SharedVM::class.java)
    }

    private lateinit var rdTripSelect: RadioButton
    private var userNameValue:String ?= null
    val df = DecimalFormat("#.##")
    var discountOnApplyingCoupon = 0f
    private var isCouponCodeFromDetailPage = true
    private var deliveryCharge: Float?= null
    private var totalAmountPayable:Float = 0f

    private var offerCode:String ?= null
    private var offerDiscountPriceInDiscount = 0
    private var offerDiscountPriceInAmount = 0

    private var offerDiscountPriceInDiscountStorage = 0
    private var offerDiscountPriceInAmountStorage = 0
    private var paymentType = 0
    private var totalAmountPayableAfterDiscount = 0f

    private lateinit var navController: NavController
    private lateinit var orderAdapter:OrderSummaryAdapter

    private val notebookPrefs:NotebookPrefs by lazy{
        NotebookPrefs(mContext)
    }

    private lateinit var errorToast: Toast
    private lateinit var successToast: Toast
    private lateinit var errorToastTextView: TextView
    private lateinit var successToastTextView: TextView

    private lateinit var mContext:Context
    private lateinit var mActivity: FragmentActivity
    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
        mActivity = requireActivity()
//        detailVM.getFreeDeliveryData()
        applyCoupon = null
    }
    companion object{
        var applyCoupon:CouponApply ?= null
        var totalAmountPayableCouponCheck:Float = 0f
        var couponCanApplyListData:List<CouponData.CouponCanApply> = ArrayList()

        var userData: User?= null
        var productID:String ?= null
        lateinit var prodList:ArrayList<OrderPaymentDetail.ProductData>
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        fragOrderSummBinding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_order_summary, container, false)
        detailVM.cartRespListener = this
        detailVM.discProdListener = this
        setupRecyclerView()
        df.roundingMode = RoundingMode.CEILING

        //success toast layout initialization here....
        val successToastLayout:View = inflater.inflate(R.layout.custom_toast_layout,
            fragOrderSummBinding.root.findViewById(R.id.custom_toast_layout) as? ViewGroup)
        successToastTextView= (successToastLayout.findViewById(R.id.custom_toast_message) as TextView)
        successToast = Toast(mContext)
        successToast.setView(successToastLayout)
        successToast.setDuration(Toast.LENGTH_SHORT)
        successToast.setGravity(OrderSummaryPage.GRAVITY_BOTTOM, 0, 80)

        //error toast layout here....
        val errorToastLayout:View = inflater.inflate(R.layout.error_custom_toast_layout,
            fragOrderSummBinding.root.findViewById(R.id.custom_toast_error_layout) as? ViewGroup)
        errorToastTextView = (errorToastLayout.findViewById(R.id.custom__error_toast_message) as TextView)
        errorToast = Toast(mContext)
        errorToast.setView(errorToastLayout)
        errorToast.setDuration(Toast.LENGTH_SHORT)
        errorToast.setGravity(OrderSummaryPage.GRAVITY_BOTTOM, 0, 80)

        rdTripSelect = fragOrderSummBinding.rgInstitutionSelection.findViewById(fragOrderSummBinding.rgInstitutionSelection.checkedRadioButtonId)
        return fragOrderSummBinding.root
    }

    private fun setupRecyclerView(){
        val layoutManagerOrderSummary = LinearLayoutManager(mContext)
        fragOrderSummBinding.recViewOrderSummary.apply {
            layoutManager = layoutManagerOrderSummary
            itemAnimator = DefaultItemAnimator()
            hasFixedSize()
        }
    }

    private var freeDeliveryAmount = 0f
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)

        navController.currentBackStackEntry?.savedStateHandle?.getLiveData<String>("applyCoupon")?.observe(
            viewLifecycleOwner, Observer {
                if(!it.isNullOrEmpty()){
                    if(!isCouponCodeFromDetailPage){
                        applyCoupon = Gson().fromJson(it, CouponApply::class.java)
                        offerCode = applyCoupon?.code
                        if(!applyCoupon?.percent.isNullOrEmpty() && !applyCoupon?.discountedprice.isNullOrEmpty()){
                            offerDiscountPriceInDiscount = applyCoupon?.percent?.toInt() ?:0
                            offerDiscountPriceInAmount = applyCoupon?.discountedprice?.toInt()?:0

                            offerDiscountPriceInAmountStorage = applyCoupon?.discountedprice?.toInt() ?:0
                            offerDiscountPriceInDiscountStorage = applyCoupon?.percent?.toInt()?:0
                        }else if(!applyCoupon?.percent.isNullOrEmpty()){
                            offerDiscountPriceInDiscountStorage = applyCoupon?.discountedprice?.toInt()?:0
                            offerDiscountPriceInDiscount = applyCoupon?.percent?.toInt() ?:0
                        }else{
                            offerDiscountPriceInAmountStorage = applyCoupon?.discountedprice?.toInt() ?:0
                            offerDiscountPriceInAmount = applyCoupon?.discountedprice?.toInt()?:0
                        }
                        Log.e("apply couponBack", " :: ${applyCoupon?.code} " +
                                ":: ${applyCoupon?.percent} :: ${applyCoupon?.discountedprice}")
                        fragOrderSummBinding.clCouponData.visibility = View.VISIBLE
                        fragOrderSummBinding.tvCouponCode.text = offerCode
                        fragOrderSummBinding.tvApplyCouponOrder.visibility = View.GONE
                    }
                }
            })

        sharedVM.freeDeliveryAmountLiveData.observe(viewLifecycleOwner, {
            if(it != null){
                freeDeliveryAmount = it.toFloat()
                Log.e("freeDeliveryAmount", " :: $freeDeliveryAmount")
            }
        })

        detailVM.couponCanApplyData.observe(viewLifecycleOwner, {
            if (it.isNotEmpty()){
                couponCanApplyListData = it
            }
        })
        //set value to view....
        fragOrderSummBinding.imgCloseCoupon.setOnClickListener(this)
        fragOrderSummBinding.tvApplyCouponOrder.setOnClickListener(this)
        fragOrderSummBinding.tvEditOrAddAddress.setOnClickListener(this)
        fragOrderSummBinding.btnProceedToPay.setOnClickListener(this)

        sharedVM.codOptionLiveData.observe(viewLifecycleOwner, Observer {
            notebookPrefs.codOptionPaymentAvail = it == 1
            Log.e("cashon", " :: ${it} :: ${notebookPrefs.codOptionPaymentAvail}")
        })

       /* sharedVM.prodCouponLiveData.observe(viewLifecycleOwner, Observer {
            if(it != null){
                if (isCouponCodeFromDetailPage){
                    offerCode = it.code
                    if(!it.percent.isNullOrEmpty()){
                        offerDiscountPriceInDiscount = it.percent?.toInt()  ?:0
                    }else{
                        offerDiscountPriceInAmount = it.discountedprice?.toInt()?:0
                    }
                    Log.e("apply coupon", " :: ${it.code} :: ${it.percent} :: ${it.discountedprice}")
                    fragOrderSummBinding.clCouponData.visibility = View.VISIBLE
                    fragOrderSummBinding.tvCouponCode.text = offerCode
                    fragOrderSummBinding.tvApplyCouponOrder.visibility = View.GONE
                }
            }
        })*/

        fragOrderSummBinding.rgInstitutionSelection.setOnCheckedChangeListener(this)
        detailVM.getUserData().observe(viewLifecycleOwner, Observer {user ->
            if(user != null){
                userData = user
                val registerForValue = user.registerfor
                if (registerForValue == 2) {
                    fragOrderSummBinding.clInstitutionLayout.visibility = View.VISIBLE
                    if(rdTripSelect.text.toString().equals("Individual", true)){
                        if(userData != null){
                            userNameValue = userData!!.name?:userData!!.username?:""
                            fragOrderSummBinding.tvBillOriginate.text = "Bill for :- $userNameValue"
                        }
                    }else{
                        if(userData != null){
                            userNameValue = userData!!.institute_name
                            fragOrderSummBinding.tvBillOriginate.text = "Bill for :- $userNameValue"
                        }
                    }
                }else if (registerForValue == 1){
                    userNameValue = userData!!.name?:userData!!.username?:""
                    fragOrderSummBinding.clInstitutionLayout.visibility = View.GONE
                }else {
                    userNameValue = userData!!.name?:userData!!.username?:""
                    fragOrderSummBinding.clInstitutionLayout.visibility = View.GONE
                }

                fragOrderSummBinding.tvAddressDetail.text = user.address?: "29, Shivaji Marg, Moti Nagar, New Delhi - 110015"
                fragOrderSummBinding.tvMobileOrder.text = user.phone?: "9876543210"
            }else{
                userData = null
                fragOrderSummBinding.clInstitutionLayout.visibility = View.GONE
                fragOrderSummBinding.tvAddressDetail.text = "29, Shivaji Marg, Moti Nagar, New Delhi - 110015"
                fragOrderSummBinding.tvMobileOrder.text = "9876543210"
            }
        })

        sharedVM.productOrderList.observe(viewLifecycleOwner, Observer {
            Log.e("product list size", " :: ${it.size}")
            populateOrderDetails(it)
            orderAdapter = OrderSummaryAdapter(mContext, it, getListener(it))
            fragOrderSummBinding.recViewOrderSummary.adapter = orderAdapter
        })
    }

    private fun populateOrderDetails(orderSummaryProducts: List<OrderSummaryProduct>) {
        deliveryCharge = getDeliveryCharge(orderSummaryProducts, freeDeliveryAmount)
        fragOrderSummBinding.tvDeliverCharges.text = String.format("₹ %.2f", deliveryCharge?: 0.0f)
        prodList = ArrayList()
        var cartQty = 0
        var cartTotalAmount = 0f

        for(orderData in orderSummaryProducts){
            cartQty += orderData.cartQuantity
            cartTotalAmount += orderData.cartQuantity.times(orderData.price)
            if(orderData.discount != 0){
                val discountQty = orderData.cartQuantity
                val discountAmt = (orderData.price.times(orderData.discount)).div(100f)
                cartTotalAmount -= discountQty.times(discountAmt)
            }
            paymentType = orderData.isBuyNow
            prodList.add(OrderPaymentDetail.ProductData(orderData.cartQuantity,
                    orderData.price, orderData.price, orderData.id, orderData.cartTotalAmount))
        }
        Log.e("cartDetails", " :: $cartQty :: $cartTotalAmount")

        userData?.let { user ->
            if(paymentType == 1){
                productID = prodList[0].id
                detailVM.getApplyCouponData(user.id, prodList[0].id)
            }else{
                detailVM.getApplyCouponData(user.id, "")
            }
        }

        Log.e("deliveryChargesAdapter", " :: $deliveryCharge")
        //setting text value to view
        fragOrderSummBinding.tvPriceWithItems.text = String.format("Price(%d items)", cartQty)
        fragOrderSummBinding.tvPriceTotalAmount.text = String.format("₹ %s", df.format(cartTotalAmount))
        totalAmountPayable = cartTotalAmount.plus(deliveryCharge?:0f)
        totalAmountPayableCouponCheck = cartTotalAmount
        fragOrderSummBinding.tvDeliverCharges.text = String.format("₹ %.2f", deliveryCharge?:0f)
        totalAmountPayableAfterDiscount = df.format(totalAmountPayable).toFloat()
        Log.e("apply coupon", " :: $offerDiscountPriceInDiscount :: $offerDiscountPriceInAmount")
        fragOrderSummBinding.tvTotalAmountPayable.text = String.format("₹ %s", df.format(totalAmountPayableAfterDiscount))

        Log.e("apply coupon", " :: $offerDiscountPriceInDiscount :: $offerDiscountPriceInAmount :: $freeDeliveryAmount")

        totalAmountPayable = cartTotalAmount.plus(deliveryCharge?:0f)
        totalAmountPayableAfterDiscount = df.format(totalAmountPayable).toFloat()

        if(offerDiscountPriceInDiscount != 0 && offerDiscountPriceInAmount != 0){
            discountOnApplyingCoupon = offerDiscountPriceInAmount.toFloat()
            totalAmountPayableAfterDiscount = df.format(totalAmountPayable.minus(offerDiscountPriceInAmount.toFloat())).toFloat()
        }else if(offerDiscountPriceInDiscount != 0){
            discountOnApplyingCoupon = (totalAmountPayable.times(offerDiscountPriceInDiscount)).div(100f)
            totalAmountPayableAfterDiscount = df.format(totalAmountPayable.minus((totalAmountPayable.times(offerDiscountPriceInDiscount)).div(100f))).toFloat()
        }else if(offerDiscountPriceInAmount != 0){
            discountOnApplyingCoupon = offerDiscountPriceInAmount.toFloat()
            totalAmountPayableAfterDiscount = df.format(totalAmountPayable.minus(offerDiscountPriceInAmount.toFloat())).toFloat()
        }else{
            discountOnApplyingCoupon = 0f
            totalAmountPayableAfterDiscount = totalAmountPayable
            Log.e("applyNormal", " :: $offerDiscountPriceInDiscount :: $offerDiscountPriceInAmount :: $totalAmountPayableAfterDiscount")
        }
        fragOrderSummBinding.tvTotalAmountPayable.text = String.format("₹ %s", df.format(totalAmountPayableAfterDiscount))

    }

    private fun getListener(orderSummaryProducts: List<OrderSummaryProduct>): OrderSummaryAdapter.OrderPriceListener {
        return object : OrderSummaryAdapter.OrderPriceListener {
            override fun onChangeProdQuantity(orderItemPosition: Int,
                                              prodID: Int, orderQty: Int, orderAmount: Float) {
                if (userData != null) {
                    detailVM.updateCartItem(userData!!.id, userData!!.token!!,
                            prodID.toString(), orderQty, 1)
                } else {
                    val userLoginRequestPopup = UserLogoutDialog()
                    userLoginRequestPopup.isCancelable = false
                    userLoginRequestPopup.setUserLoginRequestListener(this@OrderSummary)
                    userLoginRequestPopup.show(mActivity.supportFragmentManager, "User login request popup !!")
                }


                populateOrderDetails(orderSummaryProducts)
            }

            override fun onPriceCheckOnCoupon(isGreater: Boolean) {
                if (isGreater) {
                    populateOrderDetails(orderSummaryProducts)
                } else {
                    Log.e("amountCheckGreaterElse", " :: $totalAmountPayableCouponCheck :: $freeDeliveryAmount :: $deliveryCharge")
                    fragOrderSummBinding.clCouponData.visibility = View.GONE
                    fragOrderSummBinding.tvApplyCouponOrder.visibility = View.VISIBLE
                    totalAmountPayableAfterDiscount = df.format(totalAmountPayable).toFloat()
                    fragOrderSummBinding.tvTotalAmountPayable.text = "₹ ${df.format(totalAmountPayableAfterDiscount)}"
                    offerDiscountPriceInDiscount = 0
                    offerDiscountPriceInAmount = 0
                }
            }

            override fun errorMessage(msg: String) {

            }
        }
    }

    override fun onClick(p0: View?) {
        when(p0){
            fragOrderSummBinding.btnProceedToPay -> {
                if(userData != null){
                    if (!userData!!.phone.isNullOrEmpty()) {
                        if(notebookPrefs.isVerified == 1){
                            if (!userData!!.name.isNullOrEmpty()) {
                                if(!notebookPrefs.defaultAddr.isNullOrEmpty()){
                                    if(!notebookPrefs.defaultAddrModal.isNullOrEmpty()){
                                        Log.e("defaultaddr", " :: ${notebookPrefs.defaultAddrModal}")
                                        val addressData = Gson().fromJson(notebookPrefs.defaultAddrModal, Address::class.java)

                                        val orderPaymentJsonObj = OrderPaymentDetail(
                                            userData!!.id,
                                            userData!!.token!!,
                                            "${addressData.street}, ${addressData.locality}",
                                            userNameValue,
                                            userData!!.phone!!,
                                            userData!!.email!!,
                                            addressData.state!!,
                                            addressData.city!!,
                                            addressData.country!!,
                                            addressData.pincode!!,
                                            totalAmountPayable,
                                            "",
                                            totalAmountPayableAfterDiscount,
                                            offerCode ?: "",
                                            "${discountOnApplyingCoupon}",
                                            prodList,
                                            "2020-07-26",
                                            paymentType,
                                            deliveryCharge?:0f,0
                                        )

                                        Log.e("order summary data", ":: ${Gson().toJson(orderPaymentJsonObj)}")
                                        val orderSummaryDirections:OrderSummaryDirections.ActionOrderSummaryToPaymentMethodFrag =
                                            OrderSummaryDirections.actionOrderSummaryToPaymentMethodFrag(totalAmountPayableAfterDiscount,
                                                Gson().toJson(orderPaymentJsonObj))

                                        navController.navigate(orderSummaryDirections)
                                    }else{
                                        val requiredAddressDialog = RequiredAddressDialog()
                                        requiredAddressDialog.isCancelable = false
                                        requiredAddressDialog.setAddresListener(this)
                                        val bundle = Bundle()
                                        bundle.putString("defaultAddr", "Please should make one default address for proceed to payment !!")
                                        requiredAddressDialog.arguments = bundle
                                        requiredAddressDialog.show(
                                            mActivity.supportFragmentManager,
                                            "User login request popup !!"
                                        )
                                    }
                                }
                                else{
                                    val requiredAddressDialog = RequiredAddressDialog()
                                    requiredAddressDialog.isCancelable = false
                                    requiredAddressDialog.setAddresListener(this)
                                    val bundle = Bundle()
                                    bundle.putString("defaultAddr", "Please should add atleast one address !!")
                                    requiredAddressDialog.arguments = bundle
                                    requiredAddressDialog.show(
                                        mActivity.supportFragmentManager,
                                        "User login request popup !!"
                                    )
                                }
                            }else{
                                errorToastTextView.text = "Please update your username for proceed to payment"
                                errorToast.show()
                                navController.navigate(R.id.addDetailFrag)
                            }
                        }else{
                            errorToastTextView.text = "Please verify your phone number for proceed to payment"
                            errorToast.show()
                            navController.navigate(R.id.addDetailFrag)
                        }
                    }else{
                        errorToastTextView.text = "Please update your phone number for proceed to payment"
                        errorToast.show()
                        navController.navigate(R.id.addDetailFrag)
                    }
                }else{
                    val userLoginRequestPopup = UserLogoutDialog()
                    userLoginRequestPopup.isCancelable = false
                    userLoginRequestPopup.setUserLoginRequestListener(this)
                    userLoginRequestPopup.show(mActivity.supportFragmentManager, "User login request popup !!")
                }
            }

            fragOrderSummBinding.tvApplyCouponOrder -> {
                if (userData != null){
                    if(paymentType == 1){
                        val orderSummaryCouponDirections:OrderSummaryDirections.ActionOrderSummaryToApplyCoupon
                                = OrderSummaryDirections.actionOrderSummaryToApplyCoupon(prodList[0].id,userData!!.email?:"",
                            totalAmountPayableCouponCheck, userData!!.usertype!!, userData!!.registerfor?:0)
                        navController.navigate(orderSummaryCouponDirections)
                    }else{
                        val orderSummaryCouponDirections:OrderSummaryDirections.ActionOrderSummaryToApplyCoupon
                                = OrderSummaryDirections.actionOrderSummaryToApplyCoupon("",
                            userData!!.email?:"", totalAmountPayableCouponCheck, userData!!.usertype!!,
                            userData!!.registerfor?:0)
                        navController.navigate(orderSummaryCouponDirections)
                    }
                    isCouponCodeFromDetailPage = false
                }else{
                    val userLoginRequestPopup = UserLogoutDialog()
                    userLoginRequestPopup.isCancelable = false
                    userLoginRequestPopup.setUserLoginRequestListener(this)
                    userLoginRequestPopup.show(mActivity.supportFragmentManager, "User login request popup !!")
                }
            }

            fragOrderSummBinding.imgCloseCoupon -> {
                fragOrderSummBinding.clCouponData.visibility = View.GONE
                fragOrderSummBinding.tvApplyCouponOrder.visibility = View.VISIBLE
                totalAmountPayableAfterDiscount = totalAmountPayable
                fragOrderSummBinding.tvTotalAmountPayable.text = "₹ ${df.format(totalAmountPayableAfterDiscount)}"
                offerDiscountPriceInDiscount = 0
                offerDiscountPriceInAmount = 0
                applyCoupon = null
            }

            fragOrderSummBinding.tvEditOrAddAddress -> {
                navController.navigate(R.id.action_orderSummary_to_savedAddressFrag)
            }
        }
    }

    override fun onUserAccepted(isAccept: Boolean) {
        navController.navigate(R.id.loginFrag)
    }

    override fun onRequiredAddress(isAccept: Boolean) {
        navController.navigate(R.id.savedAddressFrag)
    }

    override fun onPause() {
        super.onPause()
        isCouponCodeFromDetailPage = !notebookPrefs.orderSummaryCoupon
    }

    override fun onApiCallStarted() {
    }

    override fun onSuccessProductData(prodData: ProductDetailData) {

    }

    override fun onSuccess(prod: List<DiscountedProduct>?) {

    }

    override fun onSuccessFreeDeliveryData(freeDeliveryData: List<FreeDeliveryData.FreeDelivery>) {
        /*for (freeDelObj in freeDeliveryData.indices){
            if(freeDeliveryData[freeDelObj].title.startsWith("free delivery", true)){
//                sharedVM.setFreeDeliveryData(freeDeliveryData[freeDelObj].price)
                Log.e("", " :: ")
                freeDeliveryAmount = freeDeliveryData[freeDelObj].price.toFloat()
                orderAdapter.notifyDataSetChanged()
                Log.e("freeDeliveryAmount", " :: ${freeDeliveryData[freeDelObj].price}")
            }
        }*/
    }

    override fun onCouponDataSuccess(couponProd: List<ProductCoupon.ProdCoupon>) {

    }

    override fun onUpdateOrDeleteCartStart() {
    }

    override fun onSuccessCart(prod: List<Cart>?) {
    }

    override fun onCartEmpty(isEmpty: Boolean) {
    }

    override fun onCartProductItemAdded(success: String?) {
    }

    override fun onCartItemDeleted(msg: String) {
    }

    override fun onFailure(msg: String, isAddCart: Boolean) {
    }

    override fun onApiFailure(msg: String) {
    }

    override fun onFailure(msg: String) {

    }



    override fun onNoInternetAvailable(msg: String) {
    }

    override fun onCartItemAdded(success: String) {
    }

    override fun onProductRatingData(it: RatingData) {

    }

    override fun pinSuccessful(mesg: String, date: String) {

    }

    override fun onDeliveryNotAvailable(mesg: String) {

    }

    override fun onInvalidCredential() {
    }

    override fun onProductDetailFailure(msg: String) {}

    override fun onFailureUpdateORDeleteCart(msg: String) {
    }

    override fun onApiFailureUpdateORDeleteCart(msg: String) {
    }

    override fun onNoInternetAvailableUpdateORDeleteCart(msg: String) {
    }

    override fun onCheckedChanged(p0: RadioGroup?, p1: Int) {
        rdTripSelect = fragOrderSummBinding.rgInstitutionSelection.findViewById(p1)
        Log.e("radio payment ", " :: ${rdTripSelect.text}")

        if(rdTripSelect.text.toString().equals("Individual", true)){
            if(userData != null){
                userNameValue = userData!!.name?:userData!!.username?:""
                fragOrderSummBinding.tvBillOriginate.text = "Bill for :- $userNameValue"
            }
        }else{
            if(userData != null){
                userNameValue = userData!!.institute_name
                fragOrderSummBinding.tvBillOriginate.text = "Bill for :- $userNameValue"
            }
        }
    }

    private fun getDeliveryCharge(orderSummaryProducts: List<OrderSummaryProduct>, freeDeliveryAmount: Float) : Float {
        var deliveryCharge = 0.0f
        orderSummaryProducts.forEach { orderSummaryProduct ->
            if (!orderSummaryProduct.isFreeDeliveryAvailable() ||
                    orderSummaryProduct.cartQuantity.times(orderSummaryProduct.price) < freeDeliveryAmount) {
                    deliveryCharge += orderSummaryProduct.delivery_charges ?: 0.0f
            }
        }
        return deliveryCharge
    }
}
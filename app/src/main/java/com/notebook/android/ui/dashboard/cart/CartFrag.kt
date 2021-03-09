package com.notebook.android.ui.dashboard.cart

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.max.ecomaxgo.maxpe.view.flight.utility.showSnackBar

import com.notebook.android.R
import com.notebook.android.adapter.cart.CartProductAdapter
import com.notebook.android.data.db.entities.*
import com.notebook.android.data.preferences.NotebookPrefs
import com.notebook.android.databinding.FragmentCartBinding
import com.notebook.android.model.ActivityState
import com.notebook.android.model.ErrorState
import com.notebook.android.model.ProgressState
import com.notebook.android.model.home.FreeDeliveryData
import com.notebook.android.model.orderSummary.OrderSummaryData
import com.notebook.android.ui.dashboard.listener.RemoveItemListener
import com.notebook.android.ui.myOrder.OrderSummaryPage
import com.notebook.android.ui.popupDialogFrag.LoadingDialog
import com.notebook.android.ui.popupDialogFrag.RemoveItemDialog
import com.notebook.android.ui.popupDialogFrag.UserLogoutDialog
import com.notebook.android.ui.productDetail.DetailProductVM
import com.notebook.android.ui.productDetail.DetailProductVMFactory
import com.notebook.android.ui.productDetail.SharedVM
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance
import java.util.ArrayList

class CartFrag : Fragment(), KodeinAware, CartResponseListener,
    SwipeRefreshLayout.OnRefreshListener, UserLogoutDialog.UserLoginPopupListener {

    override val kodein by kodein()
    private lateinit var cartFragBinding:FragmentCartBinding
    private val viewModelFactory : CartVMFactory by instance()
    private val cartVM: CartVM by lazy {
        ViewModelProvider(mActivity, viewModelFactory).get(CartVM::class.java)
    }

    private val notebookPrefs by lazy {
        NotebookPrefs(mContext)
    }

    private val sharedVM: SharedVM by lazy {
       ViewModelProvider(mActivity).get(SharedVM::class.java)
   }

    var cashOnAvailable = 0
    private lateinit var prodList:ArrayList<OrderSummaryProduct>
    private lateinit var navController: NavController
    private var userData:User ?= null
    private var cartItemCountOrder = 0
    private var cartItemTotalAmountOrder = 0F
    private var isUpdated = true
    private lateinit var cartAdapter:CartProductAdapter

    private val loadingDialog: LoadingDialog by lazy{
        LoadingDialog()
    }

    private lateinit var mContext:Context
    private lateinit var mActivity: FragmentActivity
    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
        mActivity = requireActivity()
    }

    private lateinit var myToast: Toast
    private lateinit var errorToast: Toast
    private lateinit var successToast: Toast
    private lateinit var errorToastTextView: TextView
    private lateinit var successToastTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        cartFragBinding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_cart, container, false)
        cartFragBinding.lifecycleOwner = this
        cartVM.cartRespListener = this
        setupRecyclerView()

        //custom toast initialize view here....
        val layouttoast = inflater.inflate(R.layout.custom_toast_layout,
            cartFragBinding.root.findViewById(R.id.custom_toast_layout) as? ViewGroup)
        (layouttoast.findViewById(R.id.custom_toast_message) as TextView).text = "Item added successfully !!"
        val GRAVITY_CENTER = 17
        myToast = Toast(mContext)
        myToast.setView(layouttoast)
        myToast.setDuration(Toast.LENGTH_SHORT)
        myToast.setGravity(GRAVITY_CENTER, 0, 0)

        //success toast layout initialization here....
        val successToastLayout:View = inflater.inflate(R.layout.custom_toast_layout,
            cartFragBinding.root.findViewById(R.id.custom_toast_layout) as? ViewGroup)
        successToastTextView= (successToastLayout.findViewById(R.id.custom_toast_message) as TextView)
        successToast = Toast(mContext)
        successToast.setView(successToastLayout)
        successToast.setDuration(Toast.LENGTH_SHORT)
        successToast.setGravity(OrderSummaryPage.GRAVITY_BOTTOM, 0, 80)

        //error toast layout here....
        val errorToastLayout:View = inflater.inflate(R.layout.error_custom_toast_layout,
            cartFragBinding.root.findViewById(R.id.custom_toast_error_layout) as? ViewGroup)
        errorToastTextView = (errorToastLayout.findViewById(R.id.custom__error_toast_message) as TextView)
        errorToast = Toast(mContext)
        errorToast.setView(errorToastLayout)
        errorToast.setDuration(Toast.LENGTH_SHORT)
        errorToast.setGravity(OrderSummaryPage.GRAVITY_BOTTOM, 0, 80)

        cartFragBinding.srlCartFrag.
        setColorSchemeColors(
            ContextCompat.getColor(mContext, android.R.color.holo_green_dark),
            ContextCompat.getColor(mContext, android.R.color.holo_red_dark),
            ContextCompat.getColor(mContext, android.R.color.holo_blue_dark),
            ContextCompat.getColor(mContext, android.R.color.holo_orange_dark))
        cartFragBinding.srlCartFrag.setOnRefreshListener(this)
        return cartFragBinding.root
    }

    private fun setupRecyclerView(){
        val layoutManagerCart = LinearLayoutManager(mContext)
        cartFragBinding.recViewCartItems.apply {
            layoutManager = layoutManagerCart
            itemAnimator = DefaultItemAnimator()
            hasFixedSize()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)

        cartFragBinding.btnShowNow.setOnClickListener {
            navController.popBackStack(R.id.homeFrag, false)
        }

        cartFragBinding.btnCheckoutNow.setOnClickListener {
            if (isUpdated) {
                navController.navigate(R.id.action_cartFrag_to_orderSummary)
                sharedVM.setCodOptionForPayment(cashOnAvailable)
            } else {
                errorToastTextView.text = "Please Update Your Cart For Changes Done"
                errorToast.show()
            }
        }

        cartVM.loadFreeDeliveryDataState.observe(viewLifecycleOwner, freeDeliveryDataObserver)
        cartVM.getUserData().observe(viewLifecycleOwner, Observer {user ->
            if(user != null){
                userData = user
                cartVM.getFreeDeliveryData()
            }else{
                cartFragBinding.clCartEmpty.visibility = View.VISIBLE
                cartFragBinding.nsvCartItemLayout.visibility = View.GONE
            }
        })

        cartVM.getCartData().observe(viewLifecycleOwner, Observer {cartList ->

            //nsvCartItemLayout
            if(cartList.isNotEmpty()){
                var cartItemCount = 0
                var cartItemTotalAmount = 0F
//                var deliveryCharges = 0F
                prodList = ArrayList()
                Log.e("cart prodlist size", " :: ${prodList.size}")
                for(cartItem in cartList){
                    cartItemCount += cartItem.cartquantity
                    cartItemTotalAmount += cartItem.carttotalamount!!
//                    deliveryCharges += cartItem.delivery_charges?:0f

                    Log.e("cartData", " :: ${cartItem.carttotalamount}")
                }

                cashOnAvailable = if (cartList.firstOrNull { cart -> cart.can_cashon?.toInt() == 0 } == null) {
                    1
                } else {
                    0
                }

                var cartID:String ?= null
                cartFragBinding.tvItemCount.text = "$cartItemCount"
                cartFragBinding.tvTotalCartAmount.text = "Rs.$cartItemTotalAmount"

                for(cartItem in cartList){
                    prodList.add(
                        OrderSummaryProduct(cartItem.cartproduct_id,
                            cartItem.cartquantity,
                            cartItem.carttotalamount?:0f,
                            cartItem.keyfeature, cartItem.material,
                            cartItem.title, cartItem.alias, cartItem.image, cartItem.status,
                            cartItem.short_description, cartItem.description, cartItem.data_sheet,
                            cartItem.quantity, cartItem.price, cartItem.offer_price!!, cartItem.product_code,
                            cartItem.product_condition, cartItem.discount!!, cartItem.latest,
                            cartItem.best, cartItem.brandtitle, cartItem.colortitle, cartItem.delivery_charges,
                            0, cartItem.can_free_delivery)
                    )

                    Log.e("cartData", " :: ${cartItem.cartquantity} :: ${cartItem.carttotalamount}")
                }

                cartList.forEach {ab->
                    Log.d("testing cart:", "${ab.carttotalamount}")
                }
                sharedVM.setProductOrderSummaryList(prodList)
//                Log.e("deliveryChargesCart", " :: $deliveryCharges")
//                sharedVM.setDeliveryCharge(deliveryCharges)
                // set delivery charge
                cartItemCountOrder = cartItemCount
                cartItemTotalAmountOrder = cartItemTotalAmount
                cartAdapter = CartProductAdapter(mContext, cartList as ArrayList<Cart>,
                    object : CartProductAdapter.CartActionListener, RemoveItemListener {
                        override fun cartDeleteItem(cartId: String) {
                            cartID = cartId
                            val removeItemDialog = RemoveItemDialog()
                            removeItemDialog.isCancelable = false
                            val bundle = Bundle()
                            removeItemDialog.setRemoveListener(this)
                            bundle.putString("dialogTitle", "Are you sure you want to remove this Item?")
                            removeItemDialog.arguments = bundle
                            removeItemDialog.show(mActivity.supportFragmentManager, "Remove Item Dialog !!")
                        }

                        override fun updateCartItem(cartId:String, cartQty:Int) {
                            isUpdated = true;
                            Log.e("updateCart"," :: ${userData!!.id}, ${userData!!.token!!}, ${cartId}, ${cartQty}")
                            cartVM.updateCartItem(userData!!.id, userData!!.token!!, cartId, cartQty, 1)
                        }

                        override fun cartErrorShows(msg: String) {
//                            showErrorView(msg)
                        }

                        override fun cartDeleteLastItem(qty: String) {
                            cartID = qty
                            val removeItemDialog = RemoveItemDialog()
                            removeItemDialog.isCancelable = false
                            val bundle = Bundle()
                            removeItemDialog.setRemoveListener(this)
                            bundle.putString("dialogTitle", "Are you sure you want to remove this Item?")
                            removeItemDialog.arguments = bundle
                            removeItemDialog.show(mActivity.supportFragmentManager, "Remove Item Dialog !!")
                        }

                        override fun cartProductDetail(cartProd: Cart) {
                            /*val prod = Product(cartProd.cartproduct_id,
                                cartProd.keyfeature, cartProd.material,
                                cartProd.title, cartProd.alias, cartProd.image,
                                cartProd.status, cartProd.short_description,
                                cartProd.description, cartProd.data_sheet,
                                cartProd.quantity, cartProd.price, cartProd.offer_price,
                                cartProd.product_code, cartProd.product_condition,
                                cartProd.discount, cartProd.latest,
                                cartProd.best, cartProd.brandtitle, cartProd.colortitle)

                            val cartToProductDetailFrag: CartDirections.ActionCartToProductDetail
                                    = CartDirections.actionCartToProductDetail(prod)
                            navController.navigate(cartToProductDetailFrag)*/
                        }

                        override fun cartUpdated(isUpdated: Boolean) {
                            this@CartFrag.isUpdated = isUpdated
                        }

                        override fun onRemovedYes(isPressed: Boolean) {
                            cartVM.deleteCartItem(userData?.id!!, userData?.token!!, cartID!!)
                        }
                    })

                cartFragBinding.recViewCartItems.adapter = cartAdapter
            }else{
                cartAdapter = CartProductAdapter(mContext, cartList as ArrayList<Cart>,
                    object : CartProductAdapter.CartActionListener, RemoveItemListener {
                        override fun cartDeleteItem(cartId: String) {
                        }

                        override fun updateCartItem(cartId:String, cartQty:Int) {
                        }

                        override fun cartErrorShows(msg: String) {
                        }

                        override fun cartDeleteLastItem(qty: String) {
                        }

                        override fun cartProductDetail(cartProd: Cart) {
                        }

                        override fun cartUpdated(isUpdated: Boolean) {

                        }

                        override fun onRemovedYes(isPressed: Boolean) {
                        }
                    })

                cartFragBinding.recViewCartItems.adapter = cartAdapter
                cartAdapter.notifyDataSetChanged()
            }
        })
    }

    private val freeDeliveryDataObserver = Observer<ActivityState> {
        when (it) {
            is ProgressState -> {
                showLoader()
            }
            is CartVM.LoadFreeDeliverySuccessState -> {
                hideLoader()
                sharedVM.setFreeDeliveryData(it.freeDeliveryAmount)
                loadCartData(userData)
            }
            is ErrorState -> {
                hideLoader()
                errorToastTextView.text = it.exception.message
                errorToast.show()
            }
        }
    }

    override fun onApiCallStarted() {
        showLoader()
    }

    private fun showLoader() {
        cartFragBinding.srlCartFrag.isRefreshing = true
    }

    private fun hideLoader() {
        cartFragBinding.srlCartFrag.isRefreshing = false
    }

    override fun onUpdateOrDeleteCartStart() {
        loadingDialog.show(mActivity.supportFragmentManager, "Show loading dialog")
    }

    override fun onSuccessCart(prod: List<Cart>?) {
        if (prod?.isEmpty() == true){
            cartFragBinding.clCartEmpty.visibility = View.VISIBLE
            cartFragBinding.nsvCartItemLayout.visibility = View.GONE
            loadCartData(userData)
        }
    }

    override fun onFailure(msg: String, isAddCart:Boolean) {
        if(isAddCart){
            errorToastTextView.text = msg
            errorToast.show()
        }
        cartFragBinding.srlCartFrag.isRefreshing = false
        cartFragBinding.clCartEmpty.visibility = View.VISIBLE
        cartFragBinding.nsvCartItemLayout.visibility = View.GONE
    }

    override fun onApiFailure(msg: String) {
        cartFragBinding.srlCartFrag.isRefreshing = false
        errorToastTextView.text = msg
        errorToast.show()
    }

    override fun onNoInternetAvailable(msg: String) {
        cartFragBinding.srlCartFrag.isRefreshing = false
        errorToastTextView.text = msg
        errorToast.show()
    }

    override fun onFailureUpdateORDeleteCart(msg: String) {
        loadingDialog.dismissAllowingStateLoss()
        errorToastTextView.text = msg
        errorToast.show()
    }

    override fun onApiFailureUpdateORDeleteCart(msg: String) {
        loadingDialog.dismissAllowingStateLoss()
        errorToastTextView.text = msg
        errorToast.show()
    }

    override fun onNoInternetAvailableUpdateORDeleteCart(msg: String) {
        loadingDialog.dismissAllowingStateLoss()
        errorToastTextView.text = msg
        errorToast.show()
    }

    override fun onInvalidCredential() {
        loadingDialog.dialog?.dismiss()
        cartFragBinding.srlCartFrag.isRefreshing = false
        notebookPrefs.clearPreference()
        cartVM.deleteUser()
        cartVM.clearCartTableFromDB()
        navController.navigate(R.id.loginFrag)
    }

    override fun onCartEmpty(isEmpty: Boolean) {
        cartFragBinding.srlCartFrag.isRefreshing = false
        if(isEmpty) {
            cartFragBinding.clCartEmpty.visibility = View.VISIBLE
            cartFragBinding.nsvCartItemLayout.visibility = View.GONE
        }else{
            cartFragBinding.clCartEmpty.visibility = View.GONE
            cartFragBinding.nsvCartItemLayout.visibility = View.VISIBLE
        }
    }

    override fun onCartProductItemAdded(success: String?) {
        loadingDialog.dismissAllowingStateLoss()
        successToastTextView.text = success
        successToast.show()
    }

    override fun onCartItemDeleted(msg: String) {
        loadingDialog.dismissAllowingStateLoss()
        successToastTextView.text = msg
        successToast.show()
    }

    override fun onRefresh() {
        loadCartData(userData)
    }

    override fun onUserAccepted(isAccept: Boolean) {
        cartVM.deleteUser()
        cartVM.clearCartTableFromDB()
        navController.navigate(R.id.loginFrag)
    }

    private fun loadCartData(user: User?) {
        user?.token?.let {
            cartVM.getCartData(user.id, it)
        }
    }
}

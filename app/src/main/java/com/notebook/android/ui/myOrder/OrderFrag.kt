package com.notebook.android.ui.myOrder

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.notebook.android.R
import com.notebook.android.adapter.order.OrderHistoryAdapter
import com.notebook.android.data.db.entities.OrderHistory
import com.notebook.android.data.db.entities.ProductDetailEntity
import com.notebook.android.data.db.entities.User
import com.notebook.android.data.preferences.NotebookPrefs
import com.notebook.android.databinding.FragmentOrderBinding
import com.notebook.android.ui.myOrder.listener.OrderHistoryListener
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance

class OrderFrag : Fragment(), KodeinAware, OrderHistoryListener, View.OnClickListener {

    private lateinit var fragmentOrderBinding: FragmentOrderBinding
    override val kodein by kodein()
    private val viewModelFactory: MyOrderVMFactory by instance()
    private val myOrderVM: MyOrderVM by lazy {
        ViewModelProvider(this, viewModelFactory).get(MyOrderVM::class.java)
    }
    private var userData: User? = null
    private lateinit var navController: NavController

    private val notebookPrefs by lazy {
        NotebookPrefs(mContext)
    }

    private lateinit var mContext: Context
    private lateinit var mActivity: FragmentActivity
    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
        mActivity = requireActivity()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        fragmentOrderBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_order, container, false
        )
        fragmentOrderBinding.lifecycleOwner = this
        myOrderVM.orderHistoryListener = this
        return fragmentOrderBinding.root
    }

    private fun setupRecyclerView(orderDataList: List<OrderHistory>) {
        val layoutManagerOrders = LinearLayoutManager(mContext)
        fragmentOrderBinding.recViewMyOrder.apply {
            layoutManager = layoutManagerOrders
            itemAnimator = DefaultItemAnimator()
            hasFixedSize()
        }

        Log.e("order list size", " :: ${orderDataList.size}")

        val orderAdapter = OrderHistoryAdapter(mContext, orderDataList,
            object : OrderHistoryAdapter.OrderDataListener {
                override fun onWriteReviewClick(orderData: OrderHistory) {
                    //without product data you not called write product rating submition....

                    val prodModel = ProductDetailEntity(orderData.cartproduct_id,
                        orderData.keyfeature, orderData.material, orderData.title, orderData.alias, orderData.image,
                        orderData.status, "", "",
                       "", orderData.quantity, orderData.price, orderData.offer_price,
                        "", "",
                        orderData.discount, 0, orderData.best, orderData.brandtitle, orderData.colortitle)
                    val prodDetailDirections: OrderFragDirections.ActionOrderFragToReviewProduct =
                        OrderFragDirections.actionOrderFragToReviewProduct(prodModel)
                    navController.navigate(prodDetailDirections)
                }

                override fun onItemViewClicked(orderData: OrderHistory) {
                    val orderSummaryDirections: OrderFragDirections.ActionOrderFragToOrderSummaryPage =
                        OrderFragDirections.actionOrderFragToOrderSummaryPage(orderData)
                    navController.navigate(orderSummaryDirections)
                }
            })
        fragmentOrderBinding.recViewMyOrder.adapter = orderAdapter
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)

        myOrderVM.getUserData().observe(viewLifecycleOwner, Observer {
            if(it != null){
                userData = it
                myOrderVM.getOrderHistoryFromServer(it.id, it.token!!)
                fragmentOrderBinding.clOrderNotAvailable.visibility = View.GONE
            }else{
                userData = null
                fragmentOrderBinding.clOrderNotAvailable.visibility = View.VISIBLE
                fragmentOrderBinding.tvAddItems.text = "If you want to see order history. So, please login first !!"
                fragmentOrderBinding.tvCartEmptyText.visibility = View.GONE
                fragmentOrderBinding.btnShopNow.text = "Login"
            }
        })

        fragmentOrderBinding.btnShopNow.setOnClickListener(this)
        myOrderVM.getAllOrderHistory().observe(viewLifecycleOwner,
            Observer {
                setupRecyclerView(it)
        })
    }

    override fun onApiCallStarted() {
    }

    override fun onSuccessResponse(orderHistory: List<OrderHistory>) {
        Log.e("Success", "on success")
        Log.e("order list size", " :: ${orderHistory.size}")
        if(orderHistory.isNullOrEmpty()){
            fragmentOrderBinding.clOrderNotAvailable.visibility = View.VISIBLE
            fragmentOrderBinding.tvAddItems.text = "Add items to it now."
            fragmentOrderBinding.tvCartEmptyText.visibility = View.VISIBLE
            fragmentOrderBinding.btnShopNow.text = "Shop Now"
        }else{
            fragmentOrderBinding.clOrderNotAvailable.visibility = View.GONE
        }
    }

    override fun onSuccessCancelReturn(msg: String) {

    }

    override fun onFailureResponse(msg: String) {
        Log.e("Error", "failureresponse = $msg")
    }

    override fun onApiFailureResponse(msg: String) {

    }

    override fun onInternetNotAvailable(msg: String) {
        Log.e("Error", "internet not available = $msg")
    }

    override fun onInvalidCredential() {
        notebookPrefs.clearPreference()
        myOrderVM.deleteUser()
        myOrderVM.clearCartTableFromDB()
        navController.navigate(R.id.loginFrag)
    }

    override fun onClick(p0: View?) {
        when(p0){
            fragmentOrderBinding.btnShopNow -> {
                val btnText = fragmentOrderBinding.btnShopNow.text.toString()
                if(btnText.equals("login", true)){
                    navController.navigate(R.id.loginFrag)
                }else{
                    navController.navigate(R.id.homeFrag)
                }
            }
        }
    }
}
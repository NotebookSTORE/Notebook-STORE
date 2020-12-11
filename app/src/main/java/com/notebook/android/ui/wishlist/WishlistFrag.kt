package com.notebook.android.ui.wishlist

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.notebook.android.R
import com.notebook.android.adapter.favourites.FavoriteProductAdapter
import com.notebook.android.data.db.entities.Cart
import com.notebook.android.data.db.entities.User
import com.notebook.android.data.db.entities.Wishlist
import com.notebook.android.data.preferences.NotebookPrefs
import com.notebook.android.databinding.FragmentWishlistBinding
import com.notebook.android.ui.dashboard.cart.CartResponseListener
import com.notebook.android.ui.dashboard.cart.CartVM
import com.notebook.android.ui.dashboard.cart.CartVMFactory
import com.notebook.android.ui.myOrder.OrderSummaryPage
import com.notebook.android.ui.popupDialogFrag.ConfirmationDialog
import com.notebook.android.ui.popupDialogFrag.LoadingDialog
import com.notebook.android.ui.popupDialogFrag.UserLogoutDialog
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance

class WishlistFrag : Fragment(), KodeinAware, UserLogoutDialog.UserLoginPopupListener,
    SwipeRefreshLayout.OnRefreshListener,
    ConfirmationDialog.ConfirmDialogDismiss, CartResponseListener {

    private lateinit var fragWishlistBinding:FragmentWishlistBinding
    override val kodein by kodein()
    private val viewModelFactory : CartVMFactory by instance()
    private val cartVM: CartVM by lazy {
        ViewModelProvider(requireActivity(), viewModelFactory).get(CartVM::class.java)
    }
    private lateinit var navController:NavController
    private var user: User?= null

    private val notebookPrefs by lazy {
        NotebookPrefs(mContext)
    }
    private val loadingDialog: LoadingDialog by lazy{
        LoadingDialog()
    }

    private lateinit var myToast: Toast
    private lateinit var errorToast:Toast
    private lateinit var successToast:Toast
    private lateinit var errorToastTextView:TextView
    private lateinit var successToastTextView:TextView

    private lateinit var mContext:Context
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

        fragWishlistBinding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_wishlist, container, false)
        fragWishlistBinding.lifecycleOwner = this
        cartVM.cartRespListener = this

        //custom toast initialize view here....
        val layouttoast = inflater.inflate(R.layout.custom_toast_layout,
            fragWishlistBinding.root.findViewById(R.id.custom_toast_layout) as? ViewGroup)
        (layouttoast.findViewById(R.id.custom_toast_message) as TextView).setText("Item added successfully !!")

//        layouttoast.findViewById(R.id.imagetoast)).setBackgroundResource(R.drawable.icon);
        val GRAVITY_BOTTOM = 80
        val GRAVITY_CENTER = 17
        myToast = Toast(mContext)
        myToast.setView(layouttoast)
        myToast.setDuration(Toast.LENGTH_SHORT)
        myToast.setGravity(GRAVITY_CENTER, 0, 0)

        //success toast layout initialization here....
        val successToastLayout:View = inflater.inflate(R.layout.custom_toast_layout,
            fragWishlistBinding.root.findViewById(R.id.custom_toast_layout) as? ViewGroup)
        successToastTextView= (successToastLayout.findViewById(R.id.custom_toast_message) as TextView)
        successToast = Toast(mContext)
        successToast.setView(successToastLayout)
        successToast.setDuration(Toast.LENGTH_SHORT)
        successToast.setGravity(OrderSummaryPage.GRAVITY_BOTTOM, 0, 80)

        //error toast layout here....
        val errorToastLayout:View = inflater.inflate(R.layout.error_custom_toast_layout,
            fragWishlistBinding.root.findViewById(R.id.custom_toast_error_layout) as? ViewGroup)
        errorToastTextView = (errorToastLayout.findViewById(R.id.custom__error_toast_message) as TextView)
        errorToast = Toast(mContext)
        errorToast.setView(errorToastLayout)
        errorToast.setDuration(Toast.LENGTH_SHORT)
        errorToast.setGravity(OrderSummaryPage.GRAVITY_BOTTOM, 0, 80)

        fragWishlistBinding.srlWishlist.
        setColorSchemeColors(
            ContextCompat.getColor(requireContext(), android.R.color.holo_green_dark),
            ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark),
            ContextCompat.getColor(requireContext(), android.R.color.holo_blue_dark),
            ContextCompat.getColor(requireContext(), android.R.color.holo_orange_dark))
        fragWishlistBinding.srlWishlist.setOnRefreshListener(this)

        return fragWishlistBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = Navigation.findNavController(view)
        cartVM.getUserData().observe(viewLifecycleOwner, Observer {
            if (it != null){
                user = it
            }
        })

        cartVM.getAllFavouritesData().observe(viewLifecycleOwner, Observer {
            if(it.isNullOrEmpty()){
                fragWishlistBinding.clTotalItemsInFav.visibility = View.GONE
                fragWishlistBinding.clRecordNoFound.visibility = View.VISIBLE
                fragWishlistBinding.tvTotalItems.text = "0"
            }else{
                fragWishlistBinding.clTotalItemsInFav.visibility = View.VISIBLE
                fragWishlistBinding.clRecordNoFound.visibility = View.GONE
                fragWishlistBinding.tvTotalItems.text = "${it.size}"
                setupRecyclerView(it)
            }
        })
    }

    private fun setupRecyclerView(it:List<Wishlist>){
        val layoutManagerWishlist = LinearLayoutManager(requireContext())
        val favAdapter = FavoriteProductAdapter(requireContext(), it as ArrayList<Wishlist>, object : FavoriteProductAdapter.FavDeleteListener{
            override fun onFavItemDelete(favID: Int) {
                cartVM.deleteFavItemFromDB(favID)
            }

            override fun onFavAddToCart(prodID: Int, prodQty: Int) {
                if(user != null){
                    cartVM.addItemsToCart(user!!.id, user!!.token!!, prodID.toString(), prodQty, 0)
                }else{
                    val userLoginRequestPopup = UserLogoutDialog()
                    userLoginRequestPopup.isCancelable = false
                    userLoginRequestPopup.setUserLoginRequestListener(this@WishlistFrag)
                    userLoginRequestPopup.show(requireActivity().supportFragmentManager, "User login request popup !!")
                }
            }
        })

        fragWishlistBinding.recViewFavItems.apply {
            layoutManager = layoutManagerWishlist
            itemAnimator = DefaultItemAnimator()
            hasFixedSize()
            adapter = favAdapter
        }
    }

    override fun onUserAccepted(isAccept: Boolean) {
        navController.navigate(R.id.loginFrag)
    }

    override fun onRefresh() {
        fragWishlistBinding.srlWishlist.isRefreshing = false
    }

    override fun onApiCallStarted() {
//        loadingDialog.show(mActivity.supportFragmentManager, "Loading Dialog show")
    }

    override fun onUpdateOrDeleteCartStart() {
//       loadingDialog.show(mActivity.supportFragmentManager, "Loading Dialog show")
    }

    override fun onSuccessCart(prod: List<Cart>?) {
    }

    override fun onFailure(msg: String, isAddCart:Boolean) {
//        loadingDialog.dismissAllowingStateLoss()
        errorToastTextView.text = msg
        errorToast.show()
    }

    override fun onApiFailure(msg: String) {
//       loadingDialog.dismissAllowingStateLoss()
        errorToastTextView.text = msg
        errorToast.show()
    }

    override fun onNoInternetAvailable(msg: String) {
//        loadingDialog.dismissAllowingStateLoss()
        errorToastTextView.text = msg
        errorToast.show()
    }

    override fun onInvalidCredential() {
//        loadingDialog.dismissAllowingStateLoss()
        notebookPrefs.clearPreference()
        cartVM.deleteUser()
        cartVM.clearCartTableFromDB()
        navController.navigate(R.id.loginFrag)
    }

    override fun onFailureUpdateORDeleteCart(msg: String) {

    }

    override fun onApiFailureUpdateORDeleteCart(msg: String) {

    }

    override fun onNoInternetAvailableUpdateORDeleteCart(msg: String) {

    }

    override fun onCartEmpty(isEmpty: Boolean) {
//        loadingDialog.dismissAllowingStateLoss()
    }

    override fun onCartProductItemAdded(success: String?) {
//        loadingDialog.dismissAllowingStateLoss()
        successToastTextView.text = success
        successToast.show()
    }

    override fun onCartItemDeleted(msg: String) {
//        loadingDialog.dismissAllowingStateLoss()
        successToastTextView.text = msg
        successToast.show()
    }

    override fun ondismissed() {
//        navController.navigate(R.id.cartFrag)
    }
}

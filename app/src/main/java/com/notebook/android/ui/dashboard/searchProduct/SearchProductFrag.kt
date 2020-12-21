package com.notebook.android.ui.dashboard.searchProduct

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
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
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

import com.notebook.android.R
import com.notebook.android.adapter.home.recyclerAdapter.SearchProductAdapter
import com.notebook.android.data.db.entities.Product
import com.notebook.android.data.db.entities.SearchProduct
import com.notebook.android.data.db.entities.User
import com.notebook.android.data.preferences.NotebookPrefs
import com.notebook.android.databinding.FragmentSearchProductBinding
import com.notebook.android.decoration.GridItemDecoration
import com.notebook.android.ui.myOrder.OrderSummaryPage
import com.notebook.android.ui.popupDialogFrag.LoadingDialog
import com.notebook.android.ui.popupDialogFrag.UserLogoutDialog
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance

class SearchProductFrag : Fragment(), KodeinAware, SearchProdResponseListener,
    SwipeRefreshLayout.OnRefreshListener {

    override val kodein by kodein()
    private val viewModelFactory : SearchProductVMFactory by instance()
    private lateinit var searchProdBinding:FragmentSearchProductBinding
    private val searchProdVM:SearchProductVM by lazy {
        ViewModelProvider(this, viewModelFactory).get(SearchProductVM::class.java)
    }
    private val loadingDialog: LoadingDialog by lazy{
        LoadingDialog()
    }
    private lateinit var navController:NavController
    private var userData: User?= null

    private lateinit var mContext:Context
    private lateinit var mActivity: FragmentActivity
    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
        mActivity = requireActivity()

        searchProdVM.clearSearchTable()
    }

    private lateinit var myToast: Toast
    private lateinit var errorToast:Toast
    private lateinit var successToast:Toast
    private lateinit var errorToastTextView:TextView
    private lateinit var successToastTextView:TextView

    private var imm:InputMethodManager? = null

    private fun showKeyboard() {
        searchProdBinding.edtSearchValue.requestFocus()
        imm?.showSoftInput(searchProdBinding.edtSearchValue, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun hideKeyboard() {
        searchProdBinding.edtSearchValue.clearFocus()
        imm?.hideSoftInputFromWindow(searchProdBinding.edtSearchValue.windowToken, 0)
    }

    override fun onPause() {
        super.onPause()
        hideKeyboard()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        searchProdBinding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_search_product, container, false)
        searchProdBinding.lifecycleOwner = this
        searchProdVM.searchProdResponseListener = this
        searchProdBinding.searchProdVM = searchProdVM

        //custom toast initialize view here....
        val layouttoast = inflater.inflate(R.layout.custom_toast_layout,
            searchProdBinding.root.findViewById(R.id.custom_toast_layout) as? ViewGroup)
        (layouttoast.findViewById(R.id.custom_toast_message) as TextView).setText("Item added successfully !!")

        val GRAVITY_CENTER = 17
        myToast = Toast(mContext)
        myToast.setView(layouttoast)
        myToast.setDuration(Toast.LENGTH_SHORT)
        myToast.setGravity(GRAVITY_CENTER, 0, 0)

        //success toast layout initialization here....
        val successToastLayout:View = inflater.inflate(R.layout.custom_toast_layout,
            searchProdBinding.root.findViewById(R.id.custom_toast_layout) as? ViewGroup)
        successToastTextView= (successToastLayout.findViewById(R.id.custom_toast_message) as TextView)
        successToast = Toast(mContext)
        successToast.setView(successToastLayout)
        successToast.setDuration(Toast.LENGTH_SHORT)
        successToast.setGravity(OrderSummaryPage.GRAVITY_BOTTOM, 0, 80)

        //error toast layout here....
        val errorToastLayout:View = inflater.inflate(R.layout.error_custom_toast_layout,
            searchProdBinding.root.findViewById(R.id.custom_toast_error_layout) as? ViewGroup)
        errorToastTextView = (errorToastLayout.findViewById(R.id.custom__error_toast_message) as TextView)
        errorToast = Toast(mContext)
        errorToast.setView(errorToastLayout)
        errorToast.setDuration(Toast.LENGTH_SHORT)
        errorToast.setGravity(OrderSummaryPage.GRAVITY_BOTTOM, 0, 80)

        searchProdBinding.srlSearchProduct.
        setColorSchemeColors(
            ContextCompat.getColor(requireContext(), android.R.color.holo_green_dark),
            ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark),
            ContextCompat.getColor(requireContext(), android.R.color.holo_blue_dark),
            ContextCompat.getColor(requireContext(), android.R.color.holo_orange_dark))
        searchProdBinding.srlSearchProduct.setOnRefreshListener(this)
        setupRecyclerView()
        return searchProdBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)

        searchProdVM.getUserData().observe(viewLifecycleOwner, Observer {
            if(it != null){
                userData = it
            }else{
                userData = null
            }
        })

        searchProdBinding.imgBackSearch.setOnClickListener {
            hideKeyboard()
            navController.popBackStack()
        }

        searchProdVM.getAllSearchProduct().observe(viewLifecycleOwner, Observer {
            val searchProdAdapter = SearchProductAdapter(requireActivity(), it as ArrayList<SearchProduct>,
                object : SearchProductAdapter.SearchProductListener,
                    UserLogoutDialog.UserLoginPopupListener {
                    override fun searchProductObj(searcgProd: SearchProduct) {
                        hideKeyboard()
                        val searchprod = Product(searcgProd.id.toString(), searcgProd.keyfeature,
                            searcgProd.material, searcgProd.title, searcgProd.alias, searcgProd.image,
                            searcgProd.status, searcgProd.short_description, searcgProd.description, searcgProd.data_sheet,
                            searcgProd.quantity, searcgProd.price, searcgProd.offer_price,
                            searcgProd.product_code, searcgProd.product_condition,
                            searcgProd.discount, searcgProd.latest, searcgProd.best, searcgProd.brandtitle, searcgProd.colortitle)
                        val searchToDetailViewFrag:SearchProductFragDirections.ActionSearchProductFragToDetailViewProductFrag = SearchProductFragDirections.actionSearchProductFragToDetailViewProductFrag(searchprod)
                        navController.navigate(searchToDetailViewFrag)
                    }

                    override fun searchAddToCart(prodID: Int, cartQty: Int) {
                        hideKeyboard()
                        if(userData != null){
                            searchProdVM.addItemsToCart(userData!!.id!!, userData!!.token!!, prodID, cartQty, 0)
                        }else{
                            val userLoginRequestPopup = UserLogoutDialog()
                            userLoginRequestPopup.isCancelable = false
                            userLoginRequestPopup.setUserLoginRequestListener(this)
                            userLoginRequestPopup.show(requireActivity().supportFragmentManager, "User login request popup !!")
                        }
                    }

                    override fun onUserAccepted(isAccept: Boolean) {
                        hideKeyboard()
                        navController.navigate(R.id.loginFrag)
                    }
                })

            searchProdBinding.recViewSearchProducts.adapter = searchProdAdapter
        })
        showKeyboard()
    }

    private fun setupRecyclerView(){
        val layoutManagerSearch = GridLayoutManager(requireContext(),
            2, RecyclerView.VERTICAL, false)

        searchProdBinding.recViewSearchProducts.apply {
            layoutManager = layoutManagerSearch
            addItemDecoration(GridItemDecoration(5,2))
            itemAnimator = DefaultItemAnimator()
            hasFixedSize()
        }
    }

    override fun onApiStarted() {
        searchProdBinding.srlSearchProduct.isRefreshing = true
    }

    override fun onCartItemAddCallStarted() {
        loadingDialog.show(mActivity.supportFragmentManager, "Show loading dialog")
    }

    override fun onSuccess(prod: List<SearchProduct>) {
        searchProdBinding.clNoResultFound.visibility = View.GONE
        searchProdBinding.srlSearchProduct.isRefreshing = false
    }

    override fun onCartItemAdded(cartMsg: String) {
     myToast.show()
        loadingDialog.dismissAllowingStateLoss()
    }

    private val notebookPrefs by lazy {
        NotebookPrefs(mContext)
    }

    override fun onInvalidCredential() {
        loadingDialog.dismissAllowingStateLoss()
        notebookPrefs.clearPreference()
        searchProdVM.deleteUser()
        searchProdVM.clearCartTableFromDB()
        navController.navigate(R.id.loginFrag)
    }

    override fun onSuccessListEmpty(product: List<SearchProduct>) {
        searchProdBinding.clNoResultFound.visibility = View.VISIBLE
        searchProdBinding.srlSearchProduct.isRefreshing = false
    }

    override fun onFailure(msg: String) {
        searchProdBinding.srlSearchProduct.isRefreshing = false
        loadingDialog.dismissAllowingStateLoss()
        errorToastTextView.text = msg
        errorToast.show()
    }

    override fun onApiFailure(msg: String) {
        loadingDialog.dismissAllowingStateLoss()
        errorToastTextView.text = msg
        errorToast.show()
    }

    override fun onNoInternetAvailable(msg: String) {
        loadingDialog.dismissAllowingStateLoss()
        errorToastTextView.text = msg
        errorToast.show()
    }

    override fun onRefresh() {
        searchProdBinding.srlSearchProduct.isRefreshing = false
    }
}

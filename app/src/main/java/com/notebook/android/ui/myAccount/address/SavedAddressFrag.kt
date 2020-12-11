package com.notebook.android.ui.myAccount.address

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.Navigation
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager

import com.notebook.android.R
import com.notebook.android.adapter.address.FetchAddressAdapter
import com.notebook.android.data.db.entities.Address
import com.notebook.android.data.db.entities.User
import com.notebook.android.data.preferences.NotebookPrefs
import com.notebook.android.databinding.FragmentSavedAddressBinding
import com.notebook.android.ui.myAccount.address.listener.AddressAddUpdateListener
import com.notebook.android.ui.myOrder.OrderSummaryPage
import com.notebook.android.ui.popupDialogFrag.LoadingDialog
import com.notebook.android.ui.popupDialogFrag.UserLogoutDialog
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance

class SavedAddressFrag : Fragment(), KodeinAware, UserLogoutDialog.UserLoginPopupListener,
    AddressAddUpdateListener {

    override val kodein by kodein()
    private val viewModelFactory : AddressVMFactory by instance<AddressVMFactory>()
    private val addressVM: AddressVM by lazy {
        ViewModelProvider(this, viewModelFactory).get(AddressVM::class.java)
    }
    private lateinit var fragSavedAddressBinding: FragmentSavedAddressBinding
    private lateinit var navController: NavController
    private var userData:User ?= null
    private val notebookPrefs: NotebookPrefs by lazy {
        NotebookPrefs(mContext)
    }

    private val loadingDialog: LoadingDialog by lazy{
        LoadingDialog()
    }

    private lateinit var errorToast: Toast
    private lateinit var successToast: Toast
    private lateinit var errorToastTextView: TextView
    private lateinit var successToastTextView: TextView

    private lateinit var mContext: Context
    private lateinit var mActivity: FragmentActivity
    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
        mActivity = requireActivity()
//        addressVM.getCountryFromServer()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        fragSavedAddressBinding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_saved_address, container, false)
        addressVM.addrAddUpdateListener = this
        fragSavedAddressBinding.lifecycleOwner = this
        setupTextClickable()

        //success toast layout initialization here....
        val successToastLayout:View = inflater.inflate(R.layout.custom_toast_layout,
            fragSavedAddressBinding.root.findViewById(R.id.custom_toast_layout) as? ViewGroup)
        successToastTextView= (successToastLayout.findViewById(R.id.custom_toast_message) as TextView)
        successToast = Toast(mContext)
        successToast.setView(successToastLayout)
        successToast.setDuration(Toast.LENGTH_SHORT)
        successToast.setGravity(OrderSummaryPage.GRAVITY_BOTTOM, 0, 80)

        //error toast layout here....
        val errorToastLayout:View = inflater.inflate(R.layout.error_custom_toast_layout,
            fragSavedAddressBinding.root.findViewById(R.id.custom_toast_error_layout) as? ViewGroup)
        errorToastTextView = (errorToastLayout.findViewById(R.id.custom__error_toast_message) as TextView)
        errorToast = Toast(mContext)
        errorToast.setView(errorToastLayout)
        errorToast.setDuration(Toast.LENGTH_SHORT)
        errorToast.setGravity(OrderSummaryPage.GRAVITY_BOTTOM, 0, 80)

        return fragSavedAddressBinding.root
    }

    private fun setupTextClickable(){
        val ssAddAddress = SpannableString(resources.getString(R.string.strAddNewsAddress))
        ssAddAddress.setSpan(
            ForegroundColorSpan(Color.parseColor("#1979BC")),
            0, ssAddAddress.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        ssAddAddress.setSpan(StyleSpan(Typeface.BOLD), 0, ssAddAddress.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        val spanRegNow =  object : ClickableSpan() {
            override fun onClick(widget: View) {
                val addressData = Address(0, "", "", "", "", "", "")
                val savedAddressFragDirections:SavedAddressFragDirections.ActionSavedAddressFragToAddAddressFrag =
                    SavedAddressFragDirections.actionSavedAddressFragToAddAddressFrag(addressData, "add")
                val navController = Navigation.findNavController(widget)
                navController.navigate(savedAddressFragDirections)
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = true
            }
        }
        ssAddAddress.setSpan(spanRegNow, 0, ssAddAddress.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
        fragSavedAddressBinding.tvAddNewAddress.movementMethod = LinkMovementMethod.getInstance()
        fragSavedAddressBinding.tvAddNewAddress.text = ssAddAddress
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)

        addressVM.getUserData().observe(viewLifecycleOwner, Observer {user ->
            if(user != null){
                userData = user
                addressVM.fetchAddressFromServer(user.id, user.token!!)
            }else{
                userData = user
            }
        })

        addressVM.getAllAddressDataFromDB().observe(viewLifecycleOwner, Observer {
            if(it.isNullOrEmpty()){
                notebookPrefs.defaultAddr = ""
                notebookPrefs.defaultAddrModal = ""
                setupRecyclerView(it)
            }else{
                setupRecyclerView(it)
            }
        })
    }

    private fun setupRecyclerView(addrList:List<Address>){
        val layoutManagerSaveAddr = LinearLayoutManager(mContext)
        val addressAdapter = FetchAddressAdapter(mContext, addrList as ArrayList<Address>,
            object : FetchAddressAdapter.AddressActionListener{
            override fun addressDelete(multiAddrID: Int) {
                if(userData != null){
                    if (!userData!!.phone.isNullOrEmpty()) {
                        if (notebookPrefs.isVerified == 1) {
                            addressVM.deleteAddressFromServer(userData!!.id, userData!!.token!!, multiAddrID)
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
                    userLoginRequestPopup.setUserLoginRequestListener(this@SavedAddressFrag)
                    userLoginRequestPopup.show(mActivity.supportFragmentManager, "User login request popup !!")
                }
            }

            override fun addressMakeDefault(multiAddrID: Int) {
                if(userData != null){
                    if (!userData!!.phone.isNullOrEmpty()) {
                        if (notebookPrefs.isVerified == 1) {
                            addressVM.makeDefaultAddress(userData!!.id!!, userData!!.token!!, multiAddrID)
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
                    userLoginRequestPopup.setUserLoginRequestListener(this@SavedAddressFrag)
                    userLoginRequestPopup.show(mActivity.supportFragmentManager, "User login request popup !!")
                }
            }

                override fun addressAlreadyDefault(alreadyDefault: Boolean) {
                    navController.popBackStack()
                }

                override fun addressUpdateData(address: Address) {
                    val savedAddressFragDirections:SavedAddressFragDirections.ActionSavedAddressFragToAddAddressFrag =
                        SavedAddressFragDirections.actionSavedAddressFragToAddAddressFrag(address, "update")
                    navController.navigate(savedAddressFragDirections)
                }
            })
        fragSavedAddressBinding.recViewAddresses.apply {
            layoutManager = layoutManagerSaveAddr
            itemAnimator = DefaultItemAnimator()
            hasFixedSize()
            adapter = addressAdapter
        }
    }

    override fun onUserAccepted(isAccept: Boolean) {
        navController.navigate(R.id.loginFrag)
    }

    override fun onApiStarted() {
        if(mActivity.supportFragmentManager.findFragmentByTag("Show loading dialog") == null){
            loadingDialog.show(mActivity.supportFragmentManager, "Show loading dialog")
        }else{
            loadingDialog.requireDialog().show()
        }
    }

    override fun onSuccess() {
        loadingDialog.dialog?.dismiss()
    }

    override fun onApiCountryStarted() {
    }

    override fun onSuccessCountry() {
        loadingDialog.dialog?.dismiss()
    }

    override fun onInvalidCredential() {
        loadingDialog.dismissAllowingStateLoss()
        notebookPrefs.clearPreference()
        addressVM.deleteUser()
        addressVM.clearCartTableFromDB()
        navController.navigate(R.id.loginFrag)
    }

    override fun makeDefaultOrDeleteAddressSuccess() {
        loadingDialog.dialog?.dismiss()
        if(userData != null){
            addressVM.fetchAddressFromServer(userData!!.id, userData!!.token!!)
        }
    }

    override fun onFailure(msg: String) {
        loadingDialog.dialog?.dismiss()
        errorToastTextView.text = msg
        errorToast.show()
    }

    override fun onApiFailure(msg: String) {
        loadingDialog.dialog?.dismiss()
        errorToastTextView.text = msg
        errorToast.show()
    }

    override fun onNoInternetAvailable(msg: String) {
        loadingDialog.dialog?.dismiss()
        errorToastTextView.text = msg
        errorToast.show()
    }

    override fun onCallCountryDialogOpen() {}
}

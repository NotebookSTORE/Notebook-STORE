package com.notebook.android.ui.myAccount.address

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.TextView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.google.gson.Gson

import com.notebook.android.R
import com.notebook.android.data.db.entities.Address
import com.notebook.android.data.db.entities.Country
import com.notebook.android.data.db.entities.User
import com.notebook.android.data.preferences.NotebookPrefs
import com.notebook.android.databinding.FragmentAddAddressBinding
import com.notebook.android.ui.dashboard.MainDashboardPage
import com.notebook.android.ui.myAccount.address.listener.AddressAddUpdateListener
import com.notebook.android.ui.myOrder.OrderSummaryPage
import com.notebook.android.ui.popupDialogFrag.CountrySelectionDialog
import com.notebook.android.ui.popupDialogFrag.LoadingDialog
import com.notebook.android.ui.popupDialogFrag.UserLogoutDialog
import com.notebook.android.utility.slideDown
import com.notebook.android.utility.slideUp
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance
import java.lang.Exception

class AddAddressFrag : Fragment(), KodeinAware, AddressAddUpdateListener,
    CountrySelectionDialog.CountrySelectedValueListener, View.OnClickListener,
    UserLogoutDialog.UserLoginPopupListener {

    override val kodein by kodein()
    private val viewModelFactory : AddressVMFactory by instance()
    private val addressVM: AddressVM by lazy {
        ViewModelProvider(this, viewModelFactory).get(AddressVM::class.java)
    }
    private lateinit var fragAddAddressBinding:FragmentAddAddressBinding
    private lateinit var navController: NavController
    private var user: User?= null
    private lateinit var addressType:String
    private var addrID:Int = 0

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

        fragAddAddressBinding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_add_address, container, false)
        addressVM.addrAddUpdateListener = this
        fragAddAddressBinding.lifecycleOwner = this
        fragAddAddressBinding.addressVM = addressVM

        if(arguments != null){
            val addrSafeArgs = AddAddressFragArgs.fromBundle(requireArguments())
            addressType = addrSafeArgs.addressType
            setFieldViewWithValue(addrSafeArgs.addressData)
            if(addressType.equals("add", true)){
                fragAddAddressBinding.btnAddressSubmit.setText("Submit")
                (mActivity as MainDashboardPage).setSubCategoryTitle("Add Address")
            }else{
                fragAddAddressBinding.btnAddressSubmit.setText("Update")
                (mActivity as MainDashboardPage).setSubCategoryTitle("Update Address")
            }
        }

        //success toast layout initialization here....
        val successToastLayout:View = inflater.inflate(R.layout.custom_toast_layout,
            fragAddAddressBinding.root.findViewById(R.id.custom_toast_layout) as? ViewGroup)
        successToastTextView= (successToastLayout.findViewById(R.id.custom_toast_message) as TextView)
        successToast = Toast(mContext)
        successToast.setView(successToastLayout)
        successToast.setDuration(Toast.LENGTH_SHORT)
        successToast.setGravity(OrderSummaryPage.GRAVITY_BOTTOM, 0, 80)

        //error toast layout here....
        val errorToastLayout:View = inflater.inflate(R.layout.error_custom_toast_layout,
            fragAddAddressBinding.root.findViewById(R.id.custom_toast_error_layout) as? ViewGroup)
        errorToastTextView = (errorToastLayout.findViewById(R.id.custom__error_toast_message) as TextView)
        errorToast = Toast(mContext)
        errorToast.setView(errorToastLayout)
        errorToast.setDuration(Toast.LENGTH_SHORT)
        errorToast.setGravity(OrderSummaryPage.GRAVITY_BOTTOM, 0, 80)

        return fragAddAddressBinding.root
    }

    private fun setFieldViewWithValue(address: Address){
        addrID = address.id
        fragAddAddressBinding.edtBuildingStreet.setText(address.street)
        fragAddAddressBinding.edtLocality.setText(address.locality)
        fragAddAddressBinding.edtCity.setText(address.city)
        fragAddAddressBinding.edtState.setText(address.state)
        fragAddAddressBinding.edtPincode.setText(address.pincode)
        fragAddAddressBinding.edtCountry.setText("India")
    }

    private lateinit var countryList:List<Country>
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        fragAddAddressBinding.btnAddressSubmit.setOnClickListener(this)
        fragAddAddressBinding.edtCountry.setOnClickListener(this)

        addressVM.getUserData().observe(viewLifecycleOwner, Observer {user ->
            if(user!=null){
                this.user = user
            }else{
                this.user = null
            }
        })

        addressVM.getAllCountryDataFromDB().observe(viewLifecycleOwner, Observer {
            countryList = it
        })
    }

    override fun onApiStarted() {
        loadingDialog.show(mActivity.supportFragmentManager, "Show loading dialog")
    }

    override fun onApiCountryStarted() {
        loadingDialog.show(mActivity.supportFragmentManager, "Show loading dialog")
    }

    override fun onSuccessCountry() {
        loadingDialog.dialog?.dismiss()
    }

    override fun onSuccess() {
        loadingDialog.dialog?.dismiss()
        try {
            Handler().postDelayed({
                navController.popBackStack()
            }, 1200)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun makeDefaultOrDeleteAddressSuccess() {

    }

    override fun onInvalidCredential() {
        loadingDialog.dismissAllowingStateLoss()
        notebookPrefs.clearPreference()
        addressVM.deleteUser()
        addressVM.clearCartTableFromDB()
        navController.navigate(R.id.loginFrag)
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

    override fun onCallCountryDialogOpen() {
        val countrySelectionDialog = CountrySelectionDialog()
        countrySelectionDialog.setCountrySelectionListener(this)
        val bundle = Bundle()
        val gson = Gson()
        bundle.putString("countryList", gson.toJson(countryList))
        countrySelectionDialog.arguments = bundle
        countrySelectionDialog.show(mActivity.supportFragmentManager, "Country Dialog !!")
    }

    override fun onSelectedValue(selectValue: String) {
//        addressVM.setCountryValue(selectValue)
        fragAddAddressBinding.edtCountry.setText(selectValue)
    }

    override fun onClick(p0: View?) {
        when(p0){

            fragAddAddressBinding.edtCountry ->{
               /* val countrySelectionDialog = CountrySelectionDialog()
                countrySelectionDialog.setCountrySelectionListener(this)
                val bundle = Bundle()
                val gson = Gson()
                bundle.putString("countryList", gson.toJson(countryList))
                countrySelectionDialog.arguments = bundle
                countrySelectionDialog.show(mActivity.supportFragmentManager, "Country Dialog !!")*/
            }

            fragAddAddressBinding.btnAddressSubmit -> {
                val edtBuildingStreet = fragAddAddressBinding.edtBuildingStreet.text.toString()
                val edtLocality = fragAddAddressBinding.edtLocality.text.toString()
                val edtCity:String = fragAddAddressBinding.edtCity.text.toString()
                val edtState:String = fragAddAddressBinding.edtState.text.toString()
                val edtPincode:String = fragAddAddressBinding.edtPincode.text.toString()
                val edtCountry:String = fragAddAddressBinding.edtCountry.text.toString()

                if(TextUtils.isEmpty(edtBuildingStreet)){
                    onFailure("Enter building/street here")
                }else if(TextUtils.isEmpty(edtLocality)){
                    onFailure("Enter locality here")
                }else if(TextUtils.isEmpty(edtCity)){
                    onFailure("Enter city here")
                }else if(TextUtils.isEmpty(edtState)){
                    onFailure("Enter state here")
                }else if(TextUtils.isEmpty(edtPincode)){
                    onFailure("Enter pincode here")
                }else if(edtPincode.length<6){
                    onFailure("Enter 6 digit pincode here")
                }else if(TextUtils.isEmpty(edtCountry)){
                    onFailure("Enter country here")
                }else{
                    if(user != null){
                        if(addressType.equals("add", true)){
                            addressVM.addAddressToServer(user!!.id!!, user!!.token!!, edtBuildingStreet,
                                edtLocality, edtState, edtPincode, edtCountry, edtCity)
                        }else{
                            addressVM.updateAddressToServer(user!!.id!!, user!!.token!!, edtBuildingStreet,
                                edtLocality, edtState, edtPincode, edtCountry, edtCity, addrID)
                        }
                    }else{
                        val userLoginRequestPopup = UserLogoutDialog()
                        userLoginRequestPopup.isCancelable = false
                        userLoginRequestPopup.setUserLoginRequestListener(this@AddAddressFrag)
                        userLoginRequestPopup.show(mActivity.supportFragmentManager, "User login request popup !!")
                    }
                }
            }
        }
    }

    override fun onUserAccepted(isAccept: Boolean) {
        navController.navigate(R.id.loginFrag)
    }
}

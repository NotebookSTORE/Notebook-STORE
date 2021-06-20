package com.notebook.android.ui.merchant.frag

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.MediaStore
import android.text.*
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.bumptech.glide.Glide
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import com.max.ecomaxgo.maxpe.view.flight.utility.showPermissionExplaination
import com.max.ecomaxgo.maxpe.view.flight.utility.validateEmail
import com.notebook.android.R
import com.notebook.android.data.db.entities.Address
import com.notebook.android.data.db.entities.Banner
import com.notebook.android.data.db.entities.User
import com.notebook.android.data.preferences.NotebookPrefs
import com.notebook.android.data.preferences.RefferalPreferance
import com.notebook.android.databinding.FragmentPrimeMerchantFormBinding
import com.notebook.android.listener.GetSelectIntentListener
import com.notebook.android.model.merchant.AadharData
import com.notebook.android.model.merchant.PrimeMerchantResponse
import com.notebook.android.model.orderSummary.OrderPaymentDetail
import com.notebook.android.ui.auth.responseListener.OtpVerificationListener
import com.notebook.android.ui.bottomSheet.TakePhotoFromCamORGallerySheet
import com.notebook.android.ui.dashboard.MainDashboardPage
import com.notebook.android.ui.merchant.MerchantVMFactory
import com.notebook.android.ui.merchant.MerchantViewModel
import com.notebook.android.ui.merchant.responseListener.MerchantBenefitListener
import com.notebook.android.ui.merchant.responseListener.PrimeRegisterRespListener
import com.notebook.android.ui.myAccount.profile.AddDetailFrag
import com.notebook.android.ui.myOrder.OrderSummaryPage
import com.notebook.android.ui.popupDialogFrag.LoadingDialog
import com.notebook.android.ui.popupDialogFrag.RegisterForDialog
import com.notebook.android.ui.popupDialogFrag.VerificationPopupDialog
import com.notebook.android.utility.Constant
import com.notebook.android.utility.CustomTextWatcher
import com.canhub.cropper.CropImage
import com.canhub.cropper.CropImageView
import com.notebook.android.utility.getAppFilePath
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONException
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class PrimeMerchantFormFrag : Fragment(), View.OnClickListener, KodeinAware,
    PrimeRegisterRespListener, OtpVerificationListener, GetSelectIntentListener,
    RegisterForDialog.RegisterForListener, MerchantBenefitListener {

    private lateinit var fragPrimeMerchantBinding:FragmentPrimeMerchantFormBinding
    private lateinit var navController:NavController
    private lateinit var myCalendar: Calendar
    private lateinit var dateDOB: DatePickerDialog.OnDateSetListener
    private var identityImage:String ?= null
    private var identityImage2:String ?= null
    private var pancardImage:String ?= null
    private var isTermAccepted = false
    private var isRegisterType = 0
    private var isFieldUpdated = false

    private var cancelledChequeImage: String?= null
    private var imageUri: Uri?= null
    private var imageFile: File ?= null

    companion object{
        const val GALLERY_REQUEST_CODE = 6011
        const val CAMERA_REQUEST_CODE = 6021
    }

    private val refferalPrefs: RefferalPreferance by lazy {
        RefferalPreferance(mContext)
    }

    private val notebookPrefs: NotebookPrefs by lazy {
        NotebookPrefs(mContext)
    }
    override val kodein by kodein()
    private val viewModelFactory:MerchantVMFactory by instance()
    private val merchantVM:MerchantViewModel by lazy {
        ViewModelProvider(this, viewModelFactory).get(MerchantViewModel::class.java)
    }

    private lateinit var errorToast: Toast
    private lateinit var successToast: Toast
    private lateinit var errorToastTextView: TextView
    private lateinit var successToastTextView: TextView

    private var addressData:Address ?= null
    private lateinit var mContext:Context
    private lateinit var mActivity: FragmentActivity
    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
        mActivity = requireActivity()

        notebookPrefs.aadharBackImage = ""
        notebookPrefs.aadharFrontImage = ""
        notebookPrefs.pancardImage = ""

        merchantVM.merchantBenefitData()
    }

    private val loadingDialog: LoadingDialog by lazy{
        LoadingDialog()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        fragPrimeMerchantBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_prime_merchant_form, container, false
        )
        fragPrimeMerchantBinding.lifecycleOwner = this
        merchantVM.primeRespListener = this
        merchantVM.merchantBenefitListener = this

        fragPrimeMerchantBinding.tvAnnualCharges.text = "Annual Subscription Charges ₹ ${notebookPrefs.primeSubscriptionCharge}/-"
        setTermsLoginTextClickable()
        myCalendar = Calendar.getInstance();
        dateDOB = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->

            myCalendar.set(Calendar.YEAR, year)
            myCalendar.set(Calendar.MONTH, monthOfYear)
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateLabelFrom()
        }

//        fragPrimeMerchantBinding.edtAddress.setText(notebookPrefs.defaultAddr)
        //success toast layout initialization here....
        val successToastLayout:View = inflater.inflate(
            R.layout.custom_toast_layout,
            fragPrimeMerchantBinding.root.findViewById(R.id.custom_toast_layout) as? ViewGroup
        )
        successToastTextView= (successToastLayout.findViewById(R.id.custom_toast_message) as TextView)
        successToast = Toast(mContext)
        successToast.view = successToastLayout
        successToast.duration = Toast.LENGTH_SHORT
        successToast.setGravity(OrderSummaryPage.GRAVITY_BOTTOM, 0, 80)

        //error toast layout here....
        val errorToastLayout:View = inflater.inflate(
            R.layout.error_custom_toast_layout,
            fragPrimeMerchantBinding.root.findViewById(R.id.custom_toast_error_layout) as? ViewGroup
        )
        errorToastTextView = (errorToastLayout.findViewById(R.id.custom__error_toast_message) as TextView)
        errorToast = Toast(mContext)
        errorToast.view = errorToastLayout
        errorToast.duration = Toast.LENGTH_SHORT
        errorToast.setGravity(OrderSummaryPage.GRAVITY_BOTTOM, 0, 80)

        addressData = Gson().fromJson(notebookPrefs.defaultAddrModal, Address::class.java)

        fragPrimeMerchantBinding.edtName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                notebookPrefs.merchantName = p0.toString()
            }

            override fun afterTextChanged(p0: Editable?) {}
        })

        fragPrimeMerchantBinding.edtEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                notebookPrefs.merchantEmail = p0.toString()
            }

            override fun afterTextChanged(p0: Editable?) {}
        })
        fragPrimeMerchantBinding.edtPhone.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                notebookPrefs.merchantPhone = p0.toString()
            }

            override fun afterTextChanged(p0: Editable?) {}
        })
        fragPrimeMerchantBinding.edtAadharId.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                notebookPrefs.merchantAadharNumber = p0.toString()
            }

            override fun afterTextChanged(p0: Editable?) {}
        })
        fragPrimeMerchantBinding.edtPanCard.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                notebookPrefs.merchantPanNumber = p0.toString()
            }

            override fun afterTextChanged(p0: Editable?) {}
        })
        fragPrimeMerchantBinding.edtAddress.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                notebookPrefs.merchantAddressBuilding = p0.toString()
            }

            override fun afterTextChanged(p0: Editable?) {}
        })
        fragPrimeMerchantBinding.edtMerchantLocality.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                notebookPrefs.merchantAddressLocality = p0.toString()
            }

            override fun afterTextChanged(p0: Editable?) {}
        })
        fragPrimeMerchantBinding.edtMerchantCity.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                notebookPrefs.merchantAddressCity = p0.toString()
            }

            override fun afterTextChanged(p0: Editable?) {}
        })
        fragPrimeMerchantBinding.edtMerchantState.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                notebookPrefs.merchantAddressState = p0.toString()
            }

            override fun afterTextChanged(p0: Editable?) {}
        })
        fragPrimeMerchantBinding.edtMerchantPincode.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                notebookPrefs.merchantAddressPincode = p0.toString()
            }

            override fun afterTextChanged(p0: Editable?) {}
        })
        fragPrimeMerchantBinding.edtAccountNumber.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                notebookPrefs.merchantAccountNumber = p0.toString()
            }

            override fun afterTextChanged(p0: Editable?) {}
        })
        fragPrimeMerchantBinding.edtBankName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                notebookPrefs.merchantBankName = p0.toString()
            }

            override fun afterTextChanged(p0: Editable?) {}
        })
        fragPrimeMerchantBinding.edtBankLocation.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                notebookPrefs.merchantBankLocation = p0.toString()
            }

            override fun afterTextChanged(p0: Editable?) {}
        })
        fragPrimeMerchantBinding.edtIFSCCode.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                notebookPrefs.merchantIfscCode = p0.toString()
            }

            override fun afterTextChanged(p0: Editable?) {}
        })

        fragPrimeMerchantBinding.edtInstituteName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                notebookPrefs.merchantInstituteName = p0.toString()
            }

            override fun afterTextChanged(p0: Editable?) {}
        })
        fragPrimeMerchantBinding.edtUpiId.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                notebookPrefs.merchantUpiID = p0.toString()
            }

            override fun afterTextChanged(p0: Editable?) {}
        })
        fragPrimeMerchantBinding.edtReferralID.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                notebookPrefs.merchantRefferalID = p0.toString()
            }

            override fun afterTextChanged(p0: Editable?) {}
        })

        if (isFieldUpdated){
            onRestoreInstanceState()
        }
        return fragPrimeMerchantBinding.root
    }

    private var dateForServer:String ?= null
    private fun updateLabelFrom() {
        val sdf = SimpleDateFormat(Constant.DATE_FORMAT, Locale.US)
        dateForServer = SimpleDateFormat(Constant.DATE_FORMAT_SERVER, Locale.US).format(myCalendar.time)
        notebookPrefs.merchantDOB = dateForServer
        fragPrimeMerchantBinding.edtDOB.setText(sdf.format(myCalendar.time))
    }

    private var userData: User?= null
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)

        if(refferalPrefs.refferCode?.isNotEmpty() == true || notebookPrefs.merchantRefferalID?.isNotEmpty() == true){
            fragPrimeMerchantBinding.edtReferralID.apply {
                if (notebookPrefs.merchantRefferalID.isNullOrEmpty()) {
                    notebookPrefs.merchantRefferalID = refferalPrefs.refferCode
                }
                setText(notebookPrefs.merchantRefferalID)
                isEnabled = false
                isFocusable = false
            }
        }else{
            fragPrimeMerchantBinding.edtReferralID.apply {
                isEnabled = true
                isFocusable = true
            }
        }
        if (notebookPrefs.merchantRefferalID.isNullOrEmpty()) {
            fragPrimeMerchantBinding.edtReferralID.apply {
                isEnabled = false
                isFocusable = false
                isCursorVisible = false
            }
        }

        merchantVM.getUserData().observe(viewLifecycleOwner, {
            if (it != null) {
                userData = it
                Log.e("refferalData", " :: ${refferalPrefs.refferCode}")
                if(userData!!.referral_id!!> 0){
                    refferalPrefs.clearPreference()
                }
            } else {
                userData = null
            }
            if (!isFieldUpdated) {
                fillPrimeMerchantData(userData)
            }
        })

        if(isRegisterType == 2){
            fragPrimeMerchantBinding.clInstituteName.visibility = View.VISIBLE
        }else{
            fragPrimeMerchantBinding.clInstituteName.visibility = View.GONE
        }

        navController.currentBackStackEntry?.savedStateHandle?.getLiveData<String>("panCardImageUri")?.observe(
            viewLifecycleOwner, {
                pancardImage = it
            })

        navController.currentBackStackEntry?.savedStateHandle?.getLiveData<String>("aadharCardImageUri")?.observe(
            viewLifecycleOwner, {
                val aadharData: AadharData = Gson().fromJson(it, AadharData::class.java)
                identityImage = aadharData.frontImage
                identityImage2 = aadharData.backImage
            })

        fragPrimeMerchantBinding.edtAadharId.addTextChangedListener(object : CustomTextWatcher(
            ' ',
            4
        ) {
            override fun onAfterTextChanged(text: String) {
                fragPrimeMerchantBinding.edtAadharId.run {
                    setText(text)
                    setSelection(text.length)
                }
            }
        })

        fragPrimeMerchantBinding.cbTermCondition.setOnCheckedChangeListener { _, p1 ->
            isTermAccepted = p1
        }

        fragPrimeMerchantBinding.edtDOB.setOnClickListener(this)
        fragPrimeMerchantBinding.btnProceedToPay.setOnClickListener(this)
        fragPrimeMerchantBinding.clCancelChequeUploadPhoto.setOnClickListener(this)
        fragPrimeMerchantBinding.imgRemovePhoto.setOnClickListener(this)

        fragPrimeMerchantBinding.imgAttachIdentityDetails.setOnClickListener(this)
        fragPrimeMerchantBinding.imgAttachPanCardDetails.setOnClickListener(this)
        fragPrimeMerchantBinding.edtSeletRegisterType.setOnClickListener(this)
    }

    private fun onRestoreInstanceState() {
        fragPrimeMerchantBinding.edtName.setText(notebookPrefs.merchantName)
        val sdf = SimpleDateFormat(Constant.DATE_FORMAT, Locale.US)
        if (notebookPrefs.merchantDOB.isNullOrEmpty()) {
            fragPrimeMerchantBinding.edtDOB.setText("")
            dateForServer = ""
        } else {
            dateForServer = notebookPrefs.merchantDOB
            val serverdate =
                SimpleDateFormat(Constant.DATE_FORMAT_SERVER, Locale.US).parse(dateForServer!!)
            fragPrimeMerchantBinding.edtDOB.setText(sdf.format(serverdate!!))
        }

        if (userData?.cancled_cheque_image.isNullOrEmpty()) {
            fragPrimeMerchantBinding.imgRemovePhoto.visibility = View.VISIBLE
        } else {
            fragPrimeMerchantBinding.imgRemovePhoto.visibility = View.GONE
        }

        if (notebookPrefs.cancelledChequeImage.isNullOrEmpty()) {
            fragPrimeMerchantBinding.clCancelChequeUploadPhoto.visibility = View.VISIBLE
            fragPrimeMerchantBinding.clImageShown.visibility = View.GONE
        } else {
            imageUri = notebookPrefs.cancelledChequeImage!!.toUri()
            cancelledChequeImage = notebookPrefs.cancelledChequeImage
            fragPrimeMerchantBinding.clCancelChequeUploadPhoto.visibility = View.GONE
            fragPrimeMerchantBinding.clImageShown.visibility = View.VISIBLE
            Glide.with(mActivity).load(imageUri).into(fragPrimeMerchantBinding.imgReportProblem)
        }

        isRegisterType = notebookPrefs.merchantRegisterFor ?: 0
        when (isRegisterType) {
            2 -> {
                fragPrimeMerchantBinding.clInstituteName.visibility = View.VISIBLE
                fragPrimeMerchantBinding.edtSeletRegisterType.setText("Institute")
                fragPrimeMerchantBinding.edtInstituteName.setText(notebookPrefs.merchantInstituteName)
            }
            1 -> {
                fragPrimeMerchantBinding.clInstituteName.visibility = View.GONE
                fragPrimeMerchantBinding.edtSeletRegisterType.setText("Individual")
            }
            else -> {
                fragPrimeMerchantBinding.clInstituteName.visibility = View.GONE
                fragPrimeMerchantBinding.edtSeletRegisterType.setText("")
            }
        }
        fragPrimeMerchantBinding.edtEmail.setText(notebookPrefs.merchantEmail)
        fragPrimeMerchantBinding.edtPhone.setText(notebookPrefs.merchantPhone)
        fragPrimeMerchantBinding.edtAadharId.setText(notebookPrefs.merchantAadharNumber)
        fragPrimeMerchantBinding.edtPanCard.setText(notebookPrefs.merchantPanNumber)
        fragPrimeMerchantBinding.edtAddress.setText(notebookPrefs.merchantAddressBuilding)
        fragPrimeMerchantBinding.edtMerchantLocality.setText(notebookPrefs.merchantAddressLocality)
        fragPrimeMerchantBinding.edtMerchantCity.setText(notebookPrefs.merchantAddressCity)
        fragPrimeMerchantBinding.edtMerchantState.setText(notebookPrefs.merchantAddressState)
        fragPrimeMerchantBinding.edtMerchantPincode.setText(notebookPrefs.merchantAddressPincode)
        fragPrimeMerchantBinding.edtReferralID.setText(notebookPrefs.merchantRefferalID)
        if (notebookPrefs.merchantRefferalID.isNullOrEmpty()) {
            fragPrimeMerchantBinding.edtReferralID.apply {
                isEnabled = false
                isFocusable = false
                isCursorVisible = false
            }
        }
        fragPrimeMerchantBinding.edtAccountNumber.setText(notebookPrefs.merchantAccountNumber)
        fragPrimeMerchantBinding.edtBankName.setText(notebookPrefs.merchantBankName)
        fragPrimeMerchantBinding.edtIFSCCode.setText(notebookPrefs.merchantIfscCode)
        fragPrimeMerchantBinding.edtBankLocation.setText(notebookPrefs.merchantBankLocation)
        fragPrimeMerchantBinding.edtUpiId.setText(notebookPrefs.merchantUpiID)
        fragPrimeMerchantBinding.edtMerchantCountry.setText("India")

        if (notebookPrefs.cancelledChequeImage.isNullOrEmpty()) {
            fragPrimeMerchantBinding.clCancelChequeUploadPhoto.visibility = View.VISIBLE
            fragPrimeMerchantBinding.clImageShown.visibility = View.GONE
        } else {
            cancelledChequeImage = notebookPrefs.cancelledChequeImage
            fragPrimeMerchantBinding.clCancelChequeUploadPhoto.visibility = View.GONE
            fragPrimeMerchantBinding.clImageShown.visibility = View.VISIBLE
            Glide.with(mActivity).load(cancelledChequeImage).into(fragPrimeMerchantBinding.imgReportProblem)
        }

        if(userData?.status == 1){
            fragPrimeMerchantBinding.clCancelChequeUploadPhoto.visibility = View.VISIBLE
            fragPrimeMerchantBinding.clImageShown.visibility = View.GONE
            fragPrimeMerchantBinding.imgRemovePhoto.visibility = View.VISIBLE
            cancelledChequeImage = null
            imageUri = null
        }

        if(userData?.usertype == 1){
            if (userData?.status == 0){
                fragPrimeMerchantBinding.btnProceedToPay.text = "Renew Subsciption"
            }else{
                fragPrimeMerchantBinding.btnProceedToPay.text = "Update"
            }
        }else{
            fragPrimeMerchantBinding.btnProceedToPay.visibility = View.VISIBLE
            fragPrimeMerchantBinding.btnProceedToPay.text = "Submit"
        }


        if(!userData?.identity_image.isNullOrEmpty()){
            if (userData?.status == 1){
                identityImage = null
                identityImage2 = null
                pancardImage = null
                cancelledChequeImage = null
            }else{
                identityImage = notebookPrefs.aadharFrontImage
                identityImage2 = notebookPrefs.aadharBackImage
                pancardImage = notebookPrefs.pancardImage
                cancelledChequeImage = notebookPrefs.cancelledChequeImage
                fragPrimeMerchantBinding.clPanImageShow.visibility = View.VISIBLE
                fragPrimeMerchantBinding.clAadharImageShow.visibility = View.VISIBLE
                Glide.with(mContext).load(notebookPrefs.aadharFrontImage).into(
                    fragPrimeMerchantBinding.imgFrontAadhar
                )
                Glide.with(mContext).load(notebookPrefs.aadharBackImage).into(
                    fragPrimeMerchantBinding.imgBackAadhar
                )
                Glide.with(mContext).load(notebookPrefs.pancardImage).into(fragPrimeMerchantBinding.imgPancard)
            }
        }else{
            identityImage = null
            identityImage2 = null
            pancardImage = null
            cancelledChequeImage = null
        }


        if (notebookPrefs.merchantKycStatus == 0) {
            fragPrimeMerchantBinding.edtAadharId.isFocusable = false
            fragPrimeMerchantBinding.edtAadharId.isEnabled = false
            fragPrimeMerchantBinding.edtAadharId.isCursorVisible = false

            fragPrimeMerchantBinding.edtPanCard.isFocusable = false
            fragPrimeMerchantBinding.edtPanCard.isEnabled = false
            fragPrimeMerchantBinding.edtPanCard.isCursorVisible = false

            fragPrimeMerchantBinding.imgAttachIdentityDetails.isEnabled = false
            fragPrimeMerchantBinding.imgAttachPanCardDetails.isEnabled = false

            fragPrimeMerchantBinding.clCancelChequeUploadPhoto.isEnabled =
                userData?.usertype != Constant.PRIME_MERCHANT_TYPE
        } else {
            fragPrimeMerchantBinding.edtAadharId.isFocusable = true
            fragPrimeMerchantBinding.edtAadharId.isEnabled = true
            fragPrimeMerchantBinding.edtAadharId.isCursorVisible = true

            fragPrimeMerchantBinding.edtPanCard.isFocusable = true
            fragPrimeMerchantBinding.edtPanCard.isEnabled = true
            fragPrimeMerchantBinding.edtPanCard.isCursorVisible = true

            fragPrimeMerchantBinding.imgAttachIdentityDetails.isEnabled = true
            fragPrimeMerchantBinding.imgAttachPanCardDetails.isEnabled = true
            fragPrimeMerchantBinding.clCancelChequeUploadPhoto.isEnabled = true
        }

        if (notebookPrefs.primeUserUpgradeAvail == 1){
            fragPrimeMerchantBinding.edtSeletRegisterType.isFocusable = false
            fragPrimeMerchantBinding.edtSeletRegisterType.isEnabled = false
            fragPrimeMerchantBinding.edtSeletRegisterType.isCursorVisible = false

            fragPrimeMerchantBinding.edtInstituteName.isFocusable = false
            fragPrimeMerchantBinding.edtInstituteName.isEnabled = false
            fragPrimeMerchantBinding.edtInstituteName.isCursorVisible = false

            fragPrimeMerchantBinding.edtName.isFocusable = false
            fragPrimeMerchantBinding.edtName.isEnabled = false
            fragPrimeMerchantBinding.edtName.isCursorVisible = false

            fragPrimeMerchantBinding.edtDOB.isFocusable = false
            fragPrimeMerchantBinding.edtDOB.isEnabled = false
            fragPrimeMerchantBinding.edtDOB.isCursorVisible = false

            fragPrimeMerchantBinding.edtEmail.isFocusable = false
            fragPrimeMerchantBinding.edtEmail.isEnabled = false
            fragPrimeMerchantBinding.edtEmail.isCursorVisible = false

            fragPrimeMerchantBinding.edtPhone.isFocusable = false
            fragPrimeMerchantBinding.edtPhone.isEnabled = false
            fragPrimeMerchantBinding.edtPhone.isCursorVisible = false

            fragPrimeMerchantBinding.edtAadharId.isFocusable = false
            fragPrimeMerchantBinding.edtAadharId.isEnabled = false
            fragPrimeMerchantBinding.edtAadharId.isCursorVisible = false

            fragPrimeMerchantBinding.edtPanCard.isFocusable = false
            fragPrimeMerchantBinding.edtPanCard.isEnabled = false
            fragPrimeMerchantBinding.edtPanCard.isCursorVisible = false

            fragPrimeMerchantBinding.imgAttachIdentityDetails.isEnabled = false
            fragPrimeMerchantBinding.imgAttachPanCardDetails.isEnabled = false

            fragPrimeMerchantBinding.edtAddress.isFocusable = false
            fragPrimeMerchantBinding.edtAddress.isEnabled = false
            fragPrimeMerchantBinding.edtAddress.isCursorVisible = false

            fragPrimeMerchantBinding.edtMerchantLocality.isFocusable = false
            fragPrimeMerchantBinding.edtMerchantLocality.isEnabled = false
            fragPrimeMerchantBinding.edtMerchantLocality.isCursorVisible = false

            fragPrimeMerchantBinding.edtMerchantCity.isFocusable = false
            fragPrimeMerchantBinding.edtMerchantCity.isEnabled = false
            fragPrimeMerchantBinding.edtMerchantCity.isCursorVisible = false

            fragPrimeMerchantBinding.edtMerchantState.isFocusable = false
            fragPrimeMerchantBinding.edtMerchantState.isEnabled = false
            fragPrimeMerchantBinding.edtMerchantState.isCursorVisible = false

            fragPrimeMerchantBinding.edtMerchantPincode.isFocusable = false
            fragPrimeMerchantBinding.edtMerchantPincode.isEnabled = false
            fragPrimeMerchantBinding.edtMerchantPincode.isCursorVisible = false

            fragPrimeMerchantBinding.edtMerchantCountry.isFocusable = false
            fragPrimeMerchantBinding.edtMerchantCountry.isEnabled = false
            fragPrimeMerchantBinding.edtMerchantCountry.isCursorVisible = false

            fragPrimeMerchantBinding.edtAccountNumber.isFocusable = false
            fragPrimeMerchantBinding.edtAccountNumber.isEnabled = false
            fragPrimeMerchantBinding.edtAccountNumber.isCursorVisible = false

            fragPrimeMerchantBinding.edtBankName.isFocusable = false
            fragPrimeMerchantBinding.edtBankName.isEnabled = false
            fragPrimeMerchantBinding.edtBankName.isCursorVisible = false

            fragPrimeMerchantBinding.edtIFSCCode.isFocusable = false
            fragPrimeMerchantBinding.edtIFSCCode.isEnabled = false
            fragPrimeMerchantBinding.edtIFSCCode.isCursorVisible = false

            fragPrimeMerchantBinding.edtBankLocation.isFocusable = false
            fragPrimeMerchantBinding.edtBankLocation.isEnabled = false
            fragPrimeMerchantBinding.edtBankLocation.isCursorVisible = false

            fragPrimeMerchantBinding.edtUpiId.isFocusable = false
            fragPrimeMerchantBinding.edtUpiId.isEnabled = false
            fragPrimeMerchantBinding.edtUpiId.isCursorVisible = false

            fragPrimeMerchantBinding.edtReferralID.isFocusable = false
            fragPrimeMerchantBinding.edtReferralID.isEnabled = false
            fragPrimeMerchantBinding.edtReferralID.isCursorVisible = false

            fragPrimeMerchantBinding.btnProceedToPay.text = "Renew Subscription"
        }
    }

    private fun fillPrimeMerchantData(userData: User?) {

        if (!userData?.address.isNullOrEmpty()){
            try {
                val arrayStr: List<String> = userData?.address!!.split("\\s*,\\s*")
                val mJSONArray = JSONArray()
                for (s in arrayStr) {
                    mJSONArray.put(s)
                }
                Log.e("parsedArray", " :: ${mJSONArray[0]} :: ${mJSONArray[4]}")
                val address = Address(
                    0,
                    mJSONArray[0].toString(),
                    mJSONArray[5].toString(),
                    mJSONArray[1].toString(),
                    mJSONArray[2].toString(),
                    mJSONArray[4].toString(),
                    mJSONArray[3].toString()
                )
                val pincode = mJSONArray[3].toString().replaceFirst("^ *", "")
                fragPrimeMerchantBinding.edtMerchantCity.setText("${mJSONArray[1]}")
                fragPrimeMerchantBinding.edtMerchantState.setText("${mJSONArray[2]}")
                fragPrimeMerchantBinding.edtMerchantLocality.setText("${mJSONArray[5]}")
                fragPrimeMerchantBinding.edtAddress.setText("${mJSONArray[0]}")
                fragPrimeMerchantBinding.edtMerchantPincode.setText(pincode)
                fragPrimeMerchantBinding.edtMerchantCountry.setText("${mJSONArray[4]}")
                notebookPrefs.defaultAddrModal = Gson().toJson(address)
                Log.e("jsonAddressArray", " :: $mJSONArray")
                addressData = Gson().fromJson(notebookPrefs.defaultAddrModal, Address::class.java)
                Log.e(
                    "aadharImages",
                    " :: ${addressData?.street} :: ${addressData?.locality} :: ${addressData?.state}"
                )
            } catch (exception: JSONException) {
                // how you handle the exception
                // e.printStackTrace();
            }

        }else{
            addressData?.street = ""
            addressData?.city = ""
            addressData?.state = ""
            addressData?.country = ""
            addressData?.pincode = ""
            addressData?.locality = ""
        }

        notebookPrefs.merchantName = userData?.name ?: userData?.username ?: ""
        notebookPrefs.merchantEmail = userData?.email
        notebookPrefs.merchantPhone = userData?.phone
        notebookPrefs.merchantAadharNumber = userData?.identity_detail
        notebookPrefs.merchantPanNumber = userData?.pancardno
        notebookPrefs.merchantAddressBuilding = addressData?.street
        notebookPrefs.merchantAddressCity = addressData?.city
        notebookPrefs.merchantAddressState = addressData?.state
        notebookPrefs.merchantAddressLocality = addressData?.locality
        notebookPrefs.merchantAddressPincode = addressData?.pincode
        notebookPrefs.merchantAccountNumber = userData?.accountno
        notebookPrefs.merchantBankLocation = userData?.banklocation
        notebookPrefs.merchantBankName = userData?.bankname
        notebookPrefs.merchantIfscCode = userData?.ifsccode
        notebookPrefs.merchantUpiID = userData?.upi
        notebookPrefs.merchantRegisterFor = userData?.registerfor
        notebookPrefs.merchantKycStatus = userData?.status

        fragPrimeMerchantBinding.edtName.setText(userData?.name ?: userData?.username ?: "")
        val sdf = SimpleDateFormat(Constant.DATE_FORMAT, Locale.US)
        if (userData?.dob.isNullOrEmpty()) {
            fragPrimeMerchantBinding.edtDOB.setText("")
            dateForServer = ""
            notebookPrefs.merchantDOB = ""
        } else {
            dateForServer = userData?.dob
            val serverdate =
                SimpleDateFormat(Constant.DATE_FORMAT_SERVER, Locale.US).parse(userData?.dob!!)
            fragPrimeMerchantBinding.edtDOB.setText(sdf.format(serverdate!!))
            notebookPrefs.merchantDOB = userData.dob
        }

        isRegisterType = userData?.registerfor ?: 0
        when (isRegisterType) {
            2 -> {
                fragPrimeMerchantBinding.clInstituteName.visibility = View.VISIBLE
                fragPrimeMerchantBinding.edtSeletRegisterType.setText("Institute")
                fragPrimeMerchantBinding.edtInstituteName.setText(userData?.institute_name)
                notebookPrefs.merchantInstituteName = userData?.institute_name
            }
            1 -> {
                fragPrimeMerchantBinding.clInstituteName.visibility = View.GONE
                fragPrimeMerchantBinding.edtSeletRegisterType.setText("Individual")
            }
            else -> {
                fragPrimeMerchantBinding.clInstituteName.visibility = View.GONE
                fragPrimeMerchantBinding.edtSeletRegisterType.setText("")
            }
        }

        if(userData?.status == 1){
            fragPrimeMerchantBinding.clCancelChequeUploadPhoto.visibility = View.VISIBLE
            fragPrimeMerchantBinding.clImageShown.visibility = View.GONE
            fragPrimeMerchantBinding.imgRemovePhoto.visibility = View.VISIBLE
            cancelledChequeImage = null
            imageUri = null
        }else{
            if (userData?.cancled_cheque_image.isNullOrEmpty()) {
                fragPrimeMerchantBinding.clCancelChequeUploadPhoto.visibility = View.VISIBLE
                fragPrimeMerchantBinding.clImageShown.visibility = View.GONE
                fragPrimeMerchantBinding.imgRemovePhoto.visibility = View.VISIBLE
                cancelledChequeImage = null
                imageUri = null
            } else {
                imageUri = userData?.cancled_cheque_image?.toUri()
                fragPrimeMerchantBinding.clCancelChequeUploadPhoto.visibility = View.GONE
                fragPrimeMerchantBinding.imgRemovePhoto.visibility = View.GONE
                fragPrimeMerchantBinding.clImageShown.visibility = View.VISIBLE
                Glide.with(mActivity).load("${Constant.MERCHANT_BASE_IMAGE_PATH}${userData?.cancled_cheque_image}").into(
                    fragPrimeMerchantBinding.imgReportProblem
                )
                cancelledChequeImage = userData?.cancled_cheque_image
                notebookPrefs.cancelledChequeImage = "${Constant.MERCHANT_BASE_IMAGE_PATH}${userData?.cancled_cheque_image}"
            }
        }

        val identityImageFromServer = userData?.identity_image
        if(!identityImageFromServer.isNullOrEmpty()){
            val items = listOf(identityImageFromServer.split("\\s*,\\s*")).toString()
            val data = items.replace("[[", "[").replace("]]", "]")
//            identityImage2 = userData.identity_image
            try{
                val jsonArrayList = JSONArray(data)
                identityImage = jsonArrayList[0].toString()
                identityImage2 = jsonArrayList[1].toString()
                Log.e("aadharImages", " :: $identityImage :: $identityImage2")
                pancardImage = userData.pancardimage

            } catch (exception: JSONException) {
            // how you handle the exception
            // e.printStackTrace();
        }

            if (userData.status == 1){
                identityImage = null
                identityImage2 = null
                pancardImage = null
                cancelledChequeImage = null
            }else{
                notebookPrefs.aadharFrontImage = "${Constant.MERCHANT_BASE_IMAGE_PATH}$identityImage"
                notebookPrefs.aadharBackImage = "${Constant.MERCHANT_BASE_IMAGE_PATH}$identityImage2"
                notebookPrefs.pancardImage = "${Constant.MERCHANT_BASE_IMAGE_PATH}$pancardImage"
                fragPrimeMerchantBinding.clPanImageShow.visibility = View.VISIBLE
                fragPrimeMerchantBinding.clAadharImageShow.visibility = View.VISIBLE
                Glide.with(mContext).load("${Constant.MERCHANT_BASE_IMAGE_PATH}$identityImage").into(
                    fragPrimeMerchantBinding.imgFrontAadhar
                )
                Glide.with(mContext).load("${Constant.MERCHANT_BASE_IMAGE_PATH}$identityImage2").into(
                    fragPrimeMerchantBinding.imgBackAadhar
                )
                Glide.with(mContext).load("${Constant.MERCHANT_BASE_IMAGE_PATH}$pancardImage").into(
                    fragPrimeMerchantBinding.imgPancard
                )
            }
        }else{
            fragPrimeMerchantBinding.clAadharImageShow.visibility = View.GONE
            fragPrimeMerchantBinding.clPanImageShow.visibility = View.GONE
            identityImage = null
            identityImage2 = null
            pancardImage = null
        }

        if(userData?.usertype == 1){
            if (userData.status == 0){
                fragPrimeMerchantBinding.btnProceedToPay.text = "Renew Subsciption"
            }else{
                fragPrimeMerchantBinding.btnProceedToPay.text = "Update"
            }
        }else{
            fragPrimeMerchantBinding.btnProceedToPay.visibility = View.VISIBLE
            fragPrimeMerchantBinding.btnProceedToPay.text = "Submit"
        }

        if (userData?.status == 0) {
            fragPrimeMerchantBinding.edtAadharId.isFocusable = false
            fragPrimeMerchantBinding.edtAadharId.isEnabled = false
            fragPrimeMerchantBinding.edtAadharId.isCursorVisible = false

            fragPrimeMerchantBinding.edtPanCard.isFocusable = false
            fragPrimeMerchantBinding.edtPanCard.isEnabled = false
            fragPrimeMerchantBinding.edtPanCard.isCursorVisible = false

            fragPrimeMerchantBinding.imgAttachIdentityDetails.isEnabled = false
            fragPrimeMerchantBinding.imgAttachPanCardDetails.isEnabled = false

            fragPrimeMerchantBinding.clCancelChequeUploadPhoto.isEnabled =
                userData.usertype != Constant.PRIME_MERCHANT_TYPE
        } else {
            fragPrimeMerchantBinding.edtAadharId.isFocusable = true
            fragPrimeMerchantBinding.edtAadharId.isEnabled = true
            fragPrimeMerchantBinding.edtAadharId.isCursorVisible = true

            fragPrimeMerchantBinding.edtPanCard.isFocusable = true
            fragPrimeMerchantBinding.edtPanCard.isEnabled = true
            fragPrimeMerchantBinding.edtPanCard.isCursorVisible = true

            fragPrimeMerchantBinding.imgAttachIdentityDetails.isEnabled = true
            fragPrimeMerchantBinding.imgAttachPanCardDetails.isEnabled = true
            fragPrimeMerchantBinding.clCancelChequeUploadPhoto.isEnabled = true
            fragPrimeMerchantBinding.imgRemovePhoto.visibility = View.VISIBLE
        }

        if (notebookPrefs.primeUserUpgradeAvail == 1){
            fragPrimeMerchantBinding.edtSeletRegisterType.isFocusable = false
            fragPrimeMerchantBinding.edtSeletRegisterType.isEnabled = false
            fragPrimeMerchantBinding.edtSeletRegisterType.isCursorVisible = false

            fragPrimeMerchantBinding.edtInstituteName.isFocusable = false
            fragPrimeMerchantBinding.edtInstituteName.isEnabled = false
            fragPrimeMerchantBinding.edtInstituteName.isCursorVisible = false

            fragPrimeMerchantBinding.edtName.isFocusable = false
            fragPrimeMerchantBinding.edtName.isEnabled = false
            fragPrimeMerchantBinding.edtName.isCursorVisible = false

            fragPrimeMerchantBinding.edtDOB.isFocusable = false
            fragPrimeMerchantBinding.edtDOB.isEnabled = false
            fragPrimeMerchantBinding.edtDOB.isCursorVisible = false

            fragPrimeMerchantBinding.edtEmail.isFocusable = false
            fragPrimeMerchantBinding.edtEmail.isEnabled = false
            fragPrimeMerchantBinding.edtEmail.isCursorVisible = false

            fragPrimeMerchantBinding.edtPhone.isFocusable = false
            fragPrimeMerchantBinding.edtPhone.isEnabled = false
            fragPrimeMerchantBinding.edtPhone.isCursorVisible = false

            fragPrimeMerchantBinding.edtAadharId.isFocusable = false
            fragPrimeMerchantBinding.edtAadharId.isEnabled = false
            fragPrimeMerchantBinding.edtAadharId.isCursorVisible = false

            fragPrimeMerchantBinding.edtPanCard.isFocusable = false
            fragPrimeMerchantBinding.edtPanCard.isEnabled = false
            fragPrimeMerchantBinding.edtPanCard.isCursorVisible = false

            fragPrimeMerchantBinding.imgAttachIdentityDetails.isEnabled = false
            fragPrimeMerchantBinding.imgAttachPanCardDetails.isEnabled = false

            fragPrimeMerchantBinding.edtAddress.isFocusable = false
            fragPrimeMerchantBinding.edtAddress.isEnabled = false
            fragPrimeMerchantBinding.edtAddress.isCursorVisible = false

            fragPrimeMerchantBinding.edtMerchantLocality.isFocusable = false
            fragPrimeMerchantBinding.edtMerchantLocality.isEnabled = false
            fragPrimeMerchantBinding.edtMerchantLocality.isCursorVisible = false

            fragPrimeMerchantBinding.edtMerchantCity.isFocusable = false
            fragPrimeMerchantBinding.edtMerchantCity.isEnabled = false
            fragPrimeMerchantBinding.edtMerchantCity.isCursorVisible = false

            fragPrimeMerchantBinding.edtMerchantState.isFocusable = false
            fragPrimeMerchantBinding.edtMerchantState.isEnabled = false
            fragPrimeMerchantBinding.edtMerchantState.isCursorVisible = false

            fragPrimeMerchantBinding.edtMerchantPincode.isFocusable = false
            fragPrimeMerchantBinding.edtMerchantPincode.isEnabled = false
            fragPrimeMerchantBinding.edtMerchantPincode.isCursorVisible = false

            fragPrimeMerchantBinding.edtMerchantCountry.isFocusable = false
            fragPrimeMerchantBinding.edtMerchantCountry.isEnabled = false
            fragPrimeMerchantBinding.edtMerchantCountry.isCursorVisible = false

            fragPrimeMerchantBinding.imgRemovePhoto.visibility = View.GONE

            fragPrimeMerchantBinding.edtAccountNumber.isFocusable = false
            fragPrimeMerchantBinding.edtAccountNumber.isEnabled = false
            fragPrimeMerchantBinding.edtAccountNumber.isCursorVisible = false

            fragPrimeMerchantBinding.edtBankName.isFocusable = false
            fragPrimeMerchantBinding.edtBankName.isEnabled = false
            fragPrimeMerchantBinding.edtBankName.isCursorVisible = false

            fragPrimeMerchantBinding.edtIFSCCode.isFocusable = false
            fragPrimeMerchantBinding.edtIFSCCode.isEnabled = false
            fragPrimeMerchantBinding.edtIFSCCode.isCursorVisible = false

            fragPrimeMerchantBinding.edtBankLocation.isFocusable = false
            fragPrimeMerchantBinding.edtBankLocation.isEnabled = false
            fragPrimeMerchantBinding.edtBankLocation.isCursorVisible = false

            fragPrimeMerchantBinding.edtUpiId.isFocusable = false
            fragPrimeMerchantBinding.edtUpiId.isEnabled = false
            fragPrimeMerchantBinding.edtUpiId.isCursorVisible = false

            fragPrimeMerchantBinding.edtReferralID.isFocusable = false
            fragPrimeMerchantBinding.edtReferralID.isEnabled = false
            fragPrimeMerchantBinding.edtReferralID.isCursorVisible = false

            fragPrimeMerchantBinding.btnProceedToPay.text = "Renew Subscription"
        }

        fragPrimeMerchantBinding.edtEmail.setText(userData?.email)
        fragPrimeMerchantBinding.edtPhone.setText(userData?.phone)
        val s = StringBuilder(userData?.identity_detail ?: "")
        var i = 4
        while (i < s.length) {
            s.insert(i, " ")
            i += 5
        }
        fragPrimeMerchantBinding.edtAadharId.setText(s.toString())
        fragPrimeMerchantBinding.edtPanCard.setText(userData?.pancardno)
        fragPrimeMerchantBinding.edtAddress.setText(addressData?.street)
        fragPrimeMerchantBinding.edtMerchantLocality.setText(addressData?.locality)
        fragPrimeMerchantBinding.edtMerchantCity.setText(addressData?.city)
        fragPrimeMerchantBinding.edtMerchantState.setText(addressData?.state)
        fragPrimeMerchantBinding.edtMerchantPincode.setText(addressData?.pincode)
        fragPrimeMerchantBinding.edtMerchantCountry.setText("India")
        fragPrimeMerchantBinding.edtAccountNumber.setText(userData?.accountno)
        fragPrimeMerchantBinding.edtBankName.setText(userData?.bankname)
        fragPrimeMerchantBinding.edtIFSCCode.setText(userData?.ifsccode)
        fragPrimeMerchantBinding.edtBankLocation.setText(userData?.banklocation)
        fragPrimeMerchantBinding.edtUpiId.setText(userData?.upi)

        if (userData != null) {
            if (userData.origincode?.isNotEmpty() == true) {
                notebookPrefs.merchantRefferalID = userData.origincode
            } else if (refferalPrefs.refferCode != userData.referralcode) {
                notebookPrefs.merchantRefferalID = refferalPrefs.refferCode
            }
        } else if (refferalPrefs.refferCode?.isNotEmpty() == true) {
            notebookPrefs.merchantRefferalID = refferalPrefs.refferCode
        }
        fragPrimeMerchantBinding.edtReferralID.setText(notebookPrefs.merchantRefferalID)
        if (notebookPrefs.merchantRefferalID.isNullOrEmpty()) {
            fragPrimeMerchantBinding.edtReferralID.apply {
                isEnabled = false
                isFocusable = false
                isCursorVisible = false
            }
        }
    }
    private val startFromTerms = 82
    private val endToTerms = 102
    private fun setTermsLoginTextClickable(){
        val ssTermsText = SpannableString(resources.getString(R.string.strTermsCondition))
        ssTermsText.setSpan(
            ForegroundColorSpan(Color.parseColor("#ffffff")),
            startFromTerms, endToTerms, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        ssTermsText.setSpan(
            StyleSpan(Typeface.BOLD),
            startFromTerms,
            endToTerms,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        val spanTerms =  object : ClickableSpan() {
            override fun onClick(widget: View) {
                val navController = Navigation.findNavController(widget)
                val bundle = Bundle()
                bundle.putString("policyLink", notebookPrefs.TermsConditionLink)
                navController.navigate(R.id.policyPart, bundle)
                (mActivity as MainDashboardPage).setSubCategoryTitle("Terms & Condition")
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = true
            }
        }
        ssTermsText.setSpan(spanTerms, startFromTerms, endToTerms, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
        fragPrimeMerchantBinding.tvTermsText.movementMethod = LinkMovementMethod.getInstance()
        fragPrimeMerchantBinding.tvTermsText.text = ssTermsText
    }

    override fun onClick(view: View?) {
        when(view){

            fragPrimeMerchantBinding.edtDOB -> {
                DatePickerDialog(
                    mContext, dateDOB, 1980,
                    myCalendar.get(Calendar.MONTH),
                    myCalendar.get(Calendar.DAY_OF_MONTH)
                ).show()
            }

            fragPrimeMerchantBinding.imgAttachIdentityDetails -> {
                isFieldUpdated = true
                navController.navigate(R.id.action_primeMerchantFormFrag_to_identityProofUploadFrag)
            }

            fragPrimeMerchantBinding.imgAttachPanCardDetails -> {
                isFieldUpdated = true
                navController.navigate(R.id.action_primeMerchantFormFrag_to_panCardUploadFrag)
            }

            fragPrimeMerchantBinding.clCancelChequeUploadPhoto -> {
                checkCameraPermission()
            }

            fragPrimeMerchantBinding.edtSeletRegisterType -> {
                val registerDialog = RegisterForDialog()
                registerDialog.isCancelable = true
                registerDialog.setRegisterForListener(this)
                registerDialog.show(mActivity.supportFragmentManager, "Show register for dialog")
            }

            fragPrimeMerchantBinding.imgRemovePhoto -> {
                fragPrimeMerchantBinding.clCancelChequeUploadPhoto.visibility = View.VISIBLE
                fragPrimeMerchantBinding.clImageShown.visibility = View.GONE
                fragPrimeMerchantBinding.imgReportProblem.setImageBitmap(null)
                imageUri = null
            }

            fragPrimeMerchantBinding.btnProceedToPay -> {
                callRegisterPrimeMerchantApi()
            }
        }
    }

    private fun takePictureFromGallery(){
        try {
            val galleryIntent = Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            )
            startActivityForResult(
                galleryIntent,
                GALLERY_REQUEST_CODE
            )
        } catch (e: Exception) {
            showErrorView("Image Size is too large")
        }
    }

    fun captureImage() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(mActivity.packageManager) != null) {
            startActivityForResult(
                takePictureIntent,
                CAMERA_REQUEST_CODE
            )
        }
    }

    private fun getPhotoSheet(){
        val photoDialogFragment = TakePhotoFromCamORGallerySheet()
        photoDialogFragment.setPhotoFromListener(this)
        photoDialogFragment.show(mActivity.supportFragmentManager, "Photo Sheet Dialog")
    }

    override fun getValue(photoFrom: String) {
        when (photoFrom) {
            "gallery" -> {
                takePictureFromGallery()
            }
            "camera" -> {
                checkCameraPermission()
            }
            else -> {
                fragPrimeMerchantBinding.imgReportProblem.setImageBitmap(null)
                fragPrimeMerchantBinding.clCancelChequeUploadPhoto.visibility = View.VISIBLE
                fragPrimeMerchantBinding.clImageShown.visibility = View.GONE
                imageUri = null
            }
        }
    }

    private fun checkCameraPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            try {
                when {
                    ContextCompat.checkSelfPermission(
                        mActivity,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                        mActivity,
                        Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_GRANTED -> {
                        openCropImage()

                    }
                    ActivityCompat.shouldShowRequestPermissionRationale(
                        mActivity,
                        Manifest.permission.CAMERA
                    ) or ActivityCompat.shouldShowRequestPermissionRationale(
                        mActivity,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )or ActivityCompat.shouldShowRequestPermissionRationale(
                        mActivity,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        ) -> mContext.showPermissionExplaination(
                        getString(
                            R.string.camera_permission_explanation
                        )
                    ) {
                        requestPermissions(
                            arrayOf(
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.CAMERA
                            ),
                            CAMERA_REQUEST_CODE
                        )
                    }
                    else -> requestPermissions(
                        arrayOf(
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.CAMERA
                        ),
                        CAMERA_REQUEST_CODE
                    )
                }
            } catch (e: NullPointerException) {

            }
        } else {
            openCropImage()
        }
    }

    private fun openCropImage() {
        CropImage.activity()
            .setGuidelines(CropImageView.Guidelines.ON)
            .setAspectRatio(1, 1)
            .start(mActivity)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            AddDetailFrag.GALLERY_REQUEST_CODE -> if (null != data && resultCode == Activity.RESULT_OK) {
                try {
                    val imageUri = data.data ?: Uri.EMPTY
                    Log.e("image uri", " :: $imageUri")
                    // start cropping activity for pre-acquired image saved on the device
                    CropImage.activity(imageUri).start(mActivity, this)

                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                    errorToastTextView.text = "Image not found"
                    errorToast.show()
                }
            }
            else -> {
                if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
                    val result = CropImage.getActivityResult(data)
                    Log.e("kdfjdl", ":: fdfdkj")
                    if (resultCode == Activity.RESULT_OK) {
                        val resultUri = result?.uri
                        imageUri = resultUri
                        cancelledChequeImage = imageUri.toString()
                        notebookPrefs.cancelledChequeImage = resultUri.toString()
                        fragPrimeMerchantBinding.clCancelChequeUploadPhoto.visibility = View.GONE
                        fragPrimeMerchantBinding.clImageShown.visibility = View.VISIBLE
                        Glide.with(mActivity).load(imageUri).into(fragPrimeMerchantBinding.imgReportProblem)
                        Log.e("imageUri", " :: $imageUri")
                        try {
                            imageFile = File(resultUri?.getAppFilePath(mContext).toString())

                        } catch (e: java.lang.NullPointerException) {
                            Toast.makeText(
                                mActivity, "Image not found",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        }
                    } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                        val error = result?.error
                        errorToastTextView.text = error.toString()
                        errorToast.show()
                        Log.e("crop error :: ", "$error")
                    }
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == CAMERA_REQUEST_CODE && grantResults.isNotEmpty()
            && grantResults[0] == PackageManager.PERMISSION_GRANTED
            && grantResults[1] == PackageManager.PERMISSION_GRANTED
        ) {
            checkCameraPermission()
        }
    }

    private fun saveImage(finalBitmap: Bitmap) {
        val root = Environment.getExternalStorageDirectory().toString()
        val myDir = File(root + "/Notebook Store/Camera")
        myDir.mkdirs()

        val username = "notebook_store".replace(" ", "_")
        val fname = "$username.jpg"
        val file = File(myDir, fname)
        if (file.exists()) {
            file.delete ()
            Log.e("save bitmap ", " :: file exist")
        }
        Log.e("bitmap dir", " :: $myDir")
        Log.e("bitmap file", " :: $file")

        CropImage.activity(Uri.fromFile(file))
            .start(mActivity, this)
        try {
            val out = FileOutputStream(file)
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            out.flush()
            out.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onStarted() {
        loadingDialog.show(mActivity.supportFragmentManager, "Show loading dialog")
    }

    override fun onSuccessResponse(it: PrimeMerchantResponse) {
        val verificationPopupDialog = VerificationPopupDialog()
        verificationPopupDialog.isCancelable = false
        val bundle = Bundle()
        bundle.putString("mobile", fragPrimeMerchantBinding.edtPhone.text.toString())
        bundle.putString("otp", it.otp)
        verificationPopupDialog.arguments = bundle
        verificationPopupDialog.setVerificationListener(this)
        verificationPopupDialog.show(mActivity.supportFragmentManager, "Show Verification Popup !!")
        loadingDialog.dialog?.dismiss()
    }

    override fun onApiCallStarted() {
    }

    override fun onSuccessResponse(successMsg: String, primeSubscriptionCharge: String) {
        notebookPrefs.primeSubscriptionCharge = primeSubscriptionCharge
    }

    override fun onSuccessBannerResponse(bannerresponse: List<Banner>?) {
    }

    override fun onSuccessDefaultAddress(defaultAddr: String) {
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

    override fun onPrimeMerchantOTPVerify(user: User) {
        loadingDialog.dialog?.dismiss()

        if (!user.address.isNullOrEmpty()){
            notebookPrefs.defaultAddr = user.address
        }
        notebookPrefs.isVerified = user.is_verified?:0
        notebookPrefs.walletAmount = user.wallet_amounts
        notebookPrefs.userID = user.id
        notebookPrefs.userToken = user.token

        val items = listOf(user.address?.split("\\s*,\\s*")).toString()
        Log.e("addressArray", " :: $items")
        val data = items.replace("[[", "[").replace("]]", "]")
//            identityImage2 = userData.identity_image
        try{
            val jsonArrayList = JSONArray(data)
            val address = Address(
                0,
                jsonArrayList[0].toString(),
                jsonArrayList[5].toString(),
                jsonArrayList[1].toString(),
                jsonArrayList[2].toString(),
                jsonArrayList[4].toString(),
                jsonArrayList[3].toString()
            )
            notebookPrefs.defaultAddrModal = Gson().toJson(address)
            Log.e("jsonAddressArray", " :: $jsonArrayList")
            addressData = Gson().fromJson(notebookPrefs.defaultAddrModal, Address::class.java)
            Log.e(
                "aadharImages",
                " :: ${addressData?.street} :: ${addressData?.locality} :: ${addressData?.state}"
            )

        } catch (exception: JSONException) {
            // how you handle the exception
            // e.printStackTrace();
        }

        val orderPaymentJsonObj = OrderPaymentDetail(
            user.id, user.token!!,
            "${addressData?.street}, ${addressData?.locality}",
            user.name, user.phone!!, user.email!!, addressData?.state ?: "",
            addressData?.city ?: "", addressData?.country ?: "",
            addressData?.pincode ?: "",
            notebookPrefs.primeSubscriptionCharge?.toFloat() ?: 0f,
            "",
            notebookPrefs.primeSubscriptionCharge?.toFloat() ?: 0f,
            "", "", ArrayList(), "", 0,
            0f, 1
        )

        val primeMerchantPaymentDirections: PrimeMerchantFormFragDirections.ActionPrimeMerchantFormFragToPaymentMethodFrag =
            PrimeMerchantFormFragDirections.actionPrimeMerchantFormFragToPaymentMethodFrag(
                notebookPrefs.primeSubscriptionCharge?.toFloat() ?: 0f,
                Gson().toJson(orderPaymentJsonObj)
            )

        when (user.usertype) {
            1 -> {
                when {
                    notebookPrefs.primeUserUpgradeAvail == 1 -> {
                        navController.navigate(primeMerchantPaymentDirections)
                    }
                    user.status == 1 -> {
                        navController.popBackStack(R.id.homeFrag, false)
                    }
                    else -> {
                        navController.popBackStack(R.id.homeFrag, false)
                    }
                }
            }
            0 -> {
                when (user.status) {
                    0 -> {
                        navController.popBackStack(R.id.homeFrag, false)
                    }
                    1 -> {
                        navController.navigate(primeMerchantPaymentDirections)
                    }
                    else -> {
                        navController.navigate(primeMerchantPaymentDirections)
                    }
                }
            }
            else -> {
                navController.popBackStack(R.id.homeFrag, false)
            }
        }

    }

    override fun onUpdatedRegularMerchant(user: User) {
        loadingDialog.dialog?.dismiss()

        if (!user.address.isNullOrEmpty()){
            notebookPrefs.defaultAddr = user.address
        }
        notebookPrefs.isVerified = user.is_verified?:0
        notebookPrefs.walletAmount = user.wallet_amounts
        notebookPrefs.userID = user.id
        notebookPrefs.userToken = user.token
        val items = listOf(user.address?.split("\\s*,\\s*")).toString()
        Log.e("addressArray", " :: $items")
        val data = items.replace("[[", "[").replace("]]", "]")
//            identityImage2 = userData.identity_image

        try{
            val jsonArrayList = JSONArray(data)
            val address = Address(
                0,
                jsonArrayList[0].toString(),
                jsonArrayList[5].toString(),
                jsonArrayList[1].toString(),
                jsonArrayList[2].toString(),
                jsonArrayList[4].toString(),
                jsonArrayList[3].toString()
            )
            notebookPrefs.defaultAddrModal = Gson().toJson(address)
            Log.e("jsonAddressArray", " :: $jsonArrayList")
            addressData = Gson().fromJson(notebookPrefs.defaultAddrModal, Address::class.java)
            Log.e(
                "aadharImages",
                " :: ${addressData?.street} :: ${addressData?.locality} :: ${addressData?.state}"
            )

        } catch (exception: JSONException) {
            // how you handle the exception
            // e.printStackTrace();
        }

        val orderPaymentJsonObj = OrderPaymentDetail(
            user.id,
            user.token!!,
            "${addressData?.street}, ${addressData?.locality}",
            user.name,
            user.phone!!,
            user.email!!,
            addressData?.state
                ?: "", //Rv -> street,Fsv -> City, Qdac -> state,India, 584848,Gw -> locality
            addressData?.city ?: "",
            addressData?.country ?: "",
            addressData?.pincode ?: "",
            notebookPrefs.primeSubscriptionCharge?.toFloat() ?: 0f,
            "",
            notebookPrefs.primeSubscriptionCharge?.toFloat() ?: 0f,
            "",
            "",
            ArrayList(),
            "",
            0,
            0f, 1
        )

        val primeMerchantPaymentDirections: PrimeMerchantFormFragDirections.ActionPrimeMerchantFormFragToPaymentMethodFrag =
            PrimeMerchantFormFragDirections.actionPrimeMerchantFormFragToPaymentMethodFrag(
                notebookPrefs.primeSubscriptionCharge?.toFloat() ?: 0f,
                Gson().toJson(orderPaymentJsonObj)
            )

        when (user.usertype) {
            1 -> {
                when {
                    notebookPrefs.primeUserUpgradeAvail == 1 -> {
                        navController.navigate(primeMerchantPaymentDirections)
                    }
                    user.status == 1 -> {
                        navController.popBackStack(R.id.homeFrag, false)
                    }
                    else -> {
                        navController.popBackStack(R.id.homeFrag, false)
                    }
                }
            }
            0 -> {
                navController.navigate(primeMerchantPaymentDirections)
            }
            else -> {
                navController.popBackStack(R.id.homeFrag, false)
            }
        }
    }

    override fun otpVerifyData(otpValue: String) {
        merchantVM.verifyOtpPrime(
            fragPrimeMerchantBinding.edtPhone.text.toString(),
            otpValue,
            userData
        )
    }

    override fun resendOtpCall(resend: Boolean) {
       callRegisterPrimeMerchantApi()
    }

    private fun showErrorView(msg: String){
        fragPrimeMerchantBinding.clErrorView.visibility = View.VISIBLE
        fragPrimeMerchantBinding.tvErrorText.text = msg
        fragPrimeMerchantBinding.clErrorView.startAnimation(
            AnimationUtils.loadAnimation(
                mContext,
                R.anim.slide_down
            )
        )

        Handler().postDelayed({
            fragPrimeMerchantBinding.clErrorView.visibility = View.GONE
            fragPrimeMerchantBinding.clErrorView.startAnimation(
                AnimationUtils.loadAnimation(
                    mContext,
                    R.anim.slide_up
                )
            )
        }, 1500)
    }

    private fun callRegisterPrimeMerchantApi(){
        val fullname = fragPrimeMerchantBinding.edtName.text.toString()
        val dob = fragPrimeMerchantBinding.edtDOB.text.toString()
        val email = fragPrimeMerchantBinding.edtEmail.text.toString()
        val phone = fragPrimeMerchantBinding.edtPhone.text.toString()
        val aadharDetail = fragPrimeMerchantBinding.edtAadharId.text.toString().replace(" ", "")
        val panCardDetail = fragPrimeMerchantBinding.edtPanCard.text.toString()
        val address = fragPrimeMerchantBinding.edtAddress.text.toString()
        val accountNumber = fragPrimeMerchantBinding.edtAccountNumber.text.toString()
        val bankName = fragPrimeMerchantBinding.edtBankName.text.toString()
        val ifscCode = fragPrimeMerchantBinding.edtIFSCCode.text.toString()
        val bankLocation = fragPrimeMerchantBinding.edtBankLocation.text.toString()
        val upiID = fragPrimeMerchantBinding.edtUpiId.text.toString()
        val refferalID = fragPrimeMerchantBinding.edtReferralID.text.toString()

        val edtLocality = fragPrimeMerchantBinding.edtMerchantLocality.text.toString()
        val edtCity:String = fragPrimeMerchantBinding.edtMerchantCity.text.toString()
        val edtState:String = fragPrimeMerchantBinding.edtMerchantState.text.toString()
        val edtPincode:String = fragPrimeMerchantBinding.edtMerchantPincode.text.toString()
        val edtCountry:String = fragPrimeMerchantBinding.edtMerchantCountry.text.toString()
        FirebaseMessaging.getInstance().token.addOnSuccessListener {
            Log.e("instance token", " :: $it")
            notebookPrefs.firebaseDeviceID = it
        }

        if(userData != null){
            if (userData!!.usertype == 0){
                if (userData!!.status == 0){
                    if(isRegisterType == 0){
                        showErrorView("Please Select Register for")
                    }else if(TextUtils.isEmpty(fullname)){
                        showErrorView("Enter full name")
                    }else if(TextUtils.isEmpty(dob)){
                        showErrorView("Enter your date of birth")
                    }else if(TextUtils.isEmpty(email)){
                        showErrorView("Enter your email address")
                    }else if(!validateEmail(email)){
                        showErrorView("Enter valid email address")
                    }else if(TextUtils.isEmpty(phone)){
                        showErrorView("Enter your 10 digit mobile number")
                    }else if(phone.length < 10){
                        showErrorView("Enter valid mobile number")
                    }else if(TextUtils.isEmpty(aadharDetail)){
                        showErrorView("Enter your aadhar number")
                    }else if(aadharDetail.length < 12){
                        showErrorView("Enter valid aadhar number")
                    }else if(TextUtils.isEmpty(panCardDetail)){
                        showErrorView("Enter your pancard detail")
                    }else if(panCardDetail.length < 10){
                        showErrorView("Enter valid pancard detail")
                    }else if(TextUtils.isEmpty(address)){
                        showErrorView("Enter your address")
                    }else if(TextUtils.isEmpty(edtLocality)){
                        showErrorView("Enter locality here")
                    }else if(TextUtils.isEmpty(edtCity)){
                        showErrorView("Enter city here")
                    }else if(TextUtils.isEmpty(edtState)){
                        showErrorView("Enter state here")
                    }else if(TextUtils.isEmpty(edtPincode)){
                        showErrorView("Enter pincode here")
                    }else if(edtPincode.length<6){
                        onFailure("Enter 6 digit pincode here")
                    }else if(TextUtils.isEmpty(edtCountry)){
                        showErrorView("Enter country here")
                    }else if(cancelledChequeImage == null){
                        showErrorView("Please upload cancel cheque")
                    }else if(TextUtils.isEmpty(accountNumber)){
                        showErrorView("Enter your bank account number")
                    }else if(TextUtils.isEmpty(bankName)){
                        showErrorView("Enter your bank name")
                    }else if(TextUtils.isEmpty(ifscCode)){
                        showErrorView("Enter your IFSC Code")
                    }else if(TextUtils.isEmpty(bankLocation)){
                        showErrorView("Enter your bank location")
                    }else if(!isTermAccepted){
                        showErrorView("Please accept terms & condition of Notebook Store app")
                    }else{
                        Log.e("dob server", dateForServer!!)
                        val namePart: RequestBody = fullname.toRequestBody(MultipartBody.FORM)
                        val emailPart: RequestBody = email.toRequestBody(MultipartBody.FORM)
                        val dobPart: RequestBody = dateForServer!!.toRequestBody(MultipartBody.FORM)
                        val phonePart: RequestBody = phone.toRequestBody(MultipartBody.FORM)
                        val aadharDetailPart: RequestBody = aadharDetail.toRequestBody(MultipartBody.FORM)
                        val pancardDetailPart: RequestBody = panCardDetail.toRequestBody(MultipartBody.FORM)
                        val addressPart: RequestBody = address.toRequestBody(MultipartBody.FORM)
                        val accoutNumbPart: RequestBody = accountNumber.toRequestBody(MultipartBody.FORM)
                        val bankNamePart: RequestBody = bankName.toRequestBody(MultipartBody.FORM)
                        val ifscCodePart: RequestBody = ifscCode.toRequestBody(MultipartBody.FORM)
                        val bankLocPart: RequestBody = bankLocation.toRequestBody(MultipartBody.FORM)
                        val upiIDPart: RequestBody = upiID.toRequestBody(MultipartBody.FORM)
                        val refferalIDPart: RequestBody = refferalID.toRequestBody(MultipartBody.FORM)

                        val localityPart: RequestBody = edtLocality.toRequestBody(MultipartBody.FORM)
                        val cityPart: RequestBody = edtCity.toRequestBody(MultipartBody.FORM)
                        val statePart: RequestBody = edtState.toRequestBody(MultipartBody.FORM)
                        val pincodePart: RequestBody = edtPincode.toRequestBody(MultipartBody.FORM)
                        val countryPart: RequestBody = edtCountry.toRequestBody(MultipartBody.FORM)
                        val fbDeviceID: RequestBody = notebookPrefs.firebaseDeviceID!!.toRequestBody(MultipartBody.FORM)
                        val registerForPart:RequestBody = isRegisterType.toString().toRequestBody(MultipartBody.FORM)
                        val imgFileCancelCheque = File(imageUri?.getAppFilePath(mContext)!!)
                        val requestFileCancelCheque = imgFileCancelCheque
                            .asRequestBody("image/*".toMediaTypeOrNull())
                        val cancelCheque = MultipartBody.Part.createFormData(
                            "cancled_cheque_image",
                            imgFileCancelCheque.name,
                            requestFileCancelCheque
                        )

                        val instituteValue = fragPrimeMerchantBinding.edtInstituteName.text.toString()
                        if(isRegisterType == 2){
                            if (TextUtils.isEmpty(instituteValue)){
                                showErrorView("Please enter institute name")
                            }else{
                                val institutePart: RequestBody = instituteValue.toRequestBody(MultipartBody.FORM)

                                if(!userData?.cancled_cheque_image.isNullOrEmpty()){
                                    if(!userData?.identity_image.isNullOrEmpty()){
                                        val items = listOf(userData?.identity_image?.split("\\s*,\\s*")).toString()
                                        val data = items.replace("[[", "[")
                                            .replace("]]", "]")

                                        try{
                                            val jsonArrayList = JSONArray(data)
                                            Log.e(
                                                "aadharImages",
                                                " :: $identityImage :: $identityImage2"
                                            )
                                            val identityPartUpload = jsonArrayList[0].toString().toRequestBody(MultipartBody.FORM)
                                            val identityPartUpload2 = jsonArrayList[1].toString().toRequestBody(MultipartBody.FORM)

                                            val panCardPartUpload = userData?.pancardimage!!.toRequestBody(MultipartBody.FORM)
                                            val cancelChequeUpload = userData?.cancled_cheque_image!!.toRequestBody(MultipartBody.FORM)

                                            //todo: crash on updating the merchant registration

                                            merchantVM.registerPrimeUpdateUsingDetails(
                                                namePart,
                                                emailPart,
                                                dobPart,
                                                phonePart,
                                                addressPart,
                                                localityPart,
                                                cityPart,
                                                statePart,
                                                pincodePart,
                                                countryPart,
                                                aadharDetailPart,
                                                pancardDetailPart,
                                                identityPartUpload,
                                                panCardPartUpload,
                                                identityPartUpload2,
                                                cancelChequeUpload,
                                                accoutNumbPart,
                                                bankNamePart,
                                                ifscCodePart,
                                                bankLocPart,
                                                upiIDPart,
                                                refferalIDPart,
                                                fbDeviceID,
                                                registerForPart,
                                                institutePart
                                            )

                                        } catch (exception: JSONException) {
                                            // how you handle the exception
                                            // e.printStackTrace();
                                        }

                                    }else{
                                        val identityPartUpload = "".toRequestBody(MultipartBody.FORM)
                                        val identityPartUpload2 = "".toRequestBody(MultipartBody.FORM)
                                        val panCardPartUpload = "".toRequestBody(MultipartBody.FORM)
                                        val cancelChequeUpload = userData?.cancled_cheque_image!!.toRequestBody(MultipartBody.FORM)

                                        merchantVM.registerPrimeUpdateUsingDetails(
                                            namePart,
                                            emailPart,
                                            dobPart,
                                            phonePart,
                                            addressPart,
                                            localityPart,
                                            cityPart,
                                            statePart,
                                            pincodePart,
                                            countryPart,
                                            aadharDetailPart,
                                            pancardDetailPart,
                                            identityPartUpload,
                                            panCardPartUpload,
                                            identityPartUpload2,
                                            cancelChequeUpload,
                                            accoutNumbPart,
                                            bankNamePart,
                                            ifscCodePart,
                                            bankLocPart,
                                            upiIDPart,
                                            refferalIDPart,
                                            fbDeviceID,
                                            registerForPart,
                                            institutePart
                                        )
                                    }
                                }else{
                                    val identityPartUpload = "".toRequestBody(MultipartBody.FORM)
                                    val identityPartUpload2 = "".toRequestBody(MultipartBody.FORM)
                                    val panCardPartUpload = "".toRequestBody(MultipartBody.FORM)

                                    merchantVM.registerPrimeUpdateUsingDetailsOnlyCheque(
                                        namePart,
                                        emailPart,
                                        dobPart,
                                        phonePart,
                                        addressPart,
                                        localityPart,
                                        cityPart,
                                        statePart,
                                        pincodePart,
                                        countryPart,
                                        aadharDetailPart,
                                        pancardDetailPart,
                                        identityPartUpload,
                                        panCardPartUpload,
                                        identityPartUpload2,
                                        cancelCheque,
                                        accoutNumbPart,
                                        bankNamePart,
                                        ifscCodePart,
                                        bankLocPart,
                                        upiIDPart,
                                        refferalIDPart,
                                        fbDeviceID,
                                        registerForPart,
                                        institutePart
                                    )
                                }

                            }
                        }else{
                            val institutePart: RequestBody = "".toRequestBody(MultipartBody.FORM)

                            if (!userData?.cancled_cheque_image.isNullOrEmpty()){
                                if(!userData?.identity_image.isNullOrEmpty()){
                                    val items = listOf(userData?.identity_image?.split("\\s*,\\s*")).toString()
                                    val data = items.replace("[[", "[")
                                        .replace("]]", "]")

                                    try{
                                        val jsonArrayList = JSONArray(data)
                                        Log.e(
                                            "aadharImages",
                                            " :: $identityImage :: $identityImage2"
                                        )
                                        val identityPartUpload = (if (jsonArrayList.length() > 0) {
                                            jsonArrayList[0].toString()
                                        } else {
                                            ""
                                        }
                                                ).toRequestBody(MultipartBody.FORM)
                                        val identityPartUpload2 = (if (jsonArrayList.length() > 1) {
                                            jsonArrayList[1].toString()
                                        } else {
                                            ""
                                        }
                                                ).toRequestBody(MultipartBody.FORM)
                                        val panCardPartUpload = userData?.pancardimage!!
                                            .toRequestBody(MultipartBody.FORM)
                                        val cancelChequeUpload = ""
                                            .toRequestBody(MultipartBody.FORM)

                                        merchantVM.registerPrimeUpdateUsingDetails(
                                            namePart,
                                            emailPart,
                                            dobPart,
                                            phonePart,
                                            addressPart,
                                            localityPart,
                                            cityPart,
                                            statePart,
                                            pincodePart,
                                            countryPart,
                                            aadharDetailPart,
                                            pancardDetailPart,
                                            identityPartUpload,
                                            panCardPartUpload,
                                            identityPartUpload2,
                                            cancelChequeUpload,
                                            accoutNumbPart,
                                            bankNamePart,
                                            ifscCodePart,
                                            bankLocPart,
                                            upiIDPart,
                                            refferalIDPart,
                                            fbDeviceID,
                                            registerForPart,
                                            institutePart
                                        )
                                    }catch (exception: JSONException){
                                        Log.d("as", "asafdcddsc")
                                    }

                                }else{
                                    val identityPartUpload = "".toRequestBody(MultipartBody.FORM)
                                    val identityPartUpload2 = "".toRequestBody(MultipartBody.FORM)
                                    val panCardPartUpload = "".toRequestBody(MultipartBody.FORM)

                                    merchantVM.registerPrimeUpdateUsingDetailsOnlyCheque(
                                        namePart,
                                        emailPart,
                                        dobPart,
                                        phonePart,
                                        addressPart,
                                        localityPart,
                                        cityPart,
                                        statePart,
                                        pincodePart,
                                        countryPart,
                                        aadharDetailPart,
                                        pancardDetailPart,
                                        identityPartUpload,
                                        panCardPartUpload,
                                        identityPartUpload2,
                                        cancelCheque,
                                        accoutNumbPart,
                                        bankNamePart,
                                        ifscCodePart,
                                        bankLocPart,
                                        upiIDPart,
                                        refferalIDPart,
                                        fbDeviceID,
                                        registerForPart,
                                        institutePart
                                    )
                                }
                            }else{
                                val identityPartUpload = "".toRequestBody(MultipartBody.FORM)
                                val identityPartUpload2 = "".toRequestBody(MultipartBody.FORM)
                                val panCardPartUpload = "".toRequestBody(MultipartBody.FORM)

                                merchantVM.registerPrimeUpdateUsingDetailsOnlyCheque(
                                    namePart,
                                    emailPart,
                                    dobPart,
                                    phonePart,
                                    addressPart,
                                    localityPart,
                                    cityPart,
                                    statePart,
                                    pincodePart,
                                    countryPart,
                                    aadharDetailPart,
                                    pancardDetailPart,
                                    identityPartUpload,
                                    panCardPartUpload,
                                    identityPartUpload2,
                                    cancelCheque,
                                    accoutNumbPart,
                                    bankNamePart,
                                    ifscCodePart,
                                    bankLocPart,
                                    upiIDPart,
                                    refferalIDPart,
                                    fbDeviceID,
                                    registerForPart,
                                    institutePart
                                )
                            }
                        }
                    }
                }else if(userData!!.status == 1){
                    if(isRegisterType == 0){
                        showErrorView("Please Select Register for")
                    }else if(TextUtils.isEmpty(fullname)){
                        showErrorView("Enter full name")
                    }else if(TextUtils.isEmpty(dob)){
                        showErrorView("Enter your date of birth")
                    }else if(TextUtils.isEmpty(email)){
                        showErrorView("Enter your email address")
                    }else if(!validateEmail(email)){
                        showErrorView("Enter valid email address")
                    }else if(TextUtils.isEmpty(phone)){
                        showErrorView("Enter your 10 digit mobile number")
                    }else if(phone.length < 10){
                        showErrorView("Enter valid mobile number")
                    }else if(TextUtils.isEmpty(aadharDetail)){
                        showErrorView("Enter your aadhar number")
                    }else if(aadharDetail.length < 12){
                        showErrorView("Enter valid aadhar number")
                    }else if(TextUtils.isEmpty(panCardDetail)){
                        showErrorView("Enter your pancard detail")
                    }else if(panCardDetail.length < 10){
                        showErrorView("Enter valid pancard detail")
                    }else if(TextUtils.isEmpty(address)){
                        showErrorView("Enter your address")
                    }else if(TextUtils.isEmpty(edtLocality)){
                        showErrorView("Enter locality here")
                    }else if(TextUtils.isEmpty(edtCity)){
                        showErrorView("Enter city here")
                    }else if(TextUtils.isEmpty(edtState)){
                        showErrorView("Enter state here")
                    }else if(TextUtils.isEmpty(edtPincode)){
                        showErrorView("Enter pincode here")
                    }else if(edtPincode.length<6){
                        onFailure("Enter 6 digit pincode here")
                    }else if(TextUtils.isEmpty(edtCountry)){
                        showErrorView("Enter country here")
                    }else if(cancelledChequeImage == null){
                        showErrorView("Please upload cancel cheque")
                    }else if(TextUtils.isEmpty(accountNumber)){
                        showErrorView("Enter your bank account number")
                    }else if(TextUtils.isEmpty(bankName)){
                        showErrorView("Enter your bank name")
                    }else if(TextUtils.isEmpty(ifscCode)){
                        showErrorView("Enter your IFSC Code")
                    }else if(TextUtils.isEmpty(bankLocation)){
                        showErrorView("Enter your bank location")
                    }else if(identityImage.isNullOrEmpty()){
                        showErrorView("Please attach your aadhar card image")
                    }else if(pancardImage.isNullOrEmpty()){
                        showErrorView("Please attach your pan card image")
                    }else if(!isTermAccepted){
                        showErrorView("Please accept terms & condition of Notebook Store app")
                    }else{
                        Log.e("dob server", dateForServer!!)
                        val namePart: RequestBody = fullname.toRequestBody(MultipartBody.FORM)
                        val emailPart: RequestBody = email.toRequestBody(MultipartBody.FORM)
                        val dobPart: RequestBody = dateForServer!!.toRequestBody(MultipartBody.FORM)
                        val phonePart: RequestBody = phone.toRequestBody(MultipartBody.FORM)
                        val aadharDetailPart: RequestBody = aadharDetail.toRequestBody(MultipartBody.FORM)
                        val pancardDetailPart: RequestBody = panCardDetail.toRequestBody(MultipartBody.FORM)
                        val addressPart: RequestBody = address.toRequestBody(MultipartBody.FORM)
                        val accoutNumbPart: RequestBody = accountNumber.toRequestBody(MultipartBody.FORM)
                        val bankNamePart: RequestBody = bankName.toRequestBody(MultipartBody.FORM)
                        val ifscCodePart: RequestBody = ifscCode.toRequestBody(MultipartBody.FORM)
                        val bankLocPart: RequestBody = bankLocation.toRequestBody(MultipartBody.FORM)
                        val upiIDPart: RequestBody = upiID.toRequestBody(MultipartBody.FORM)
                        val refferalIDPart: RequestBody = refferalID.toRequestBody(MultipartBody.FORM)

                        val localityPart: RequestBody = edtLocality.toRequestBody(MultipartBody.FORM)
                        val cityPart: RequestBody = edtCity.toRequestBody(MultipartBody.FORM)
                        val statePart: RequestBody = edtState.toRequestBody(MultipartBody.FORM)
                        val pincodePart: RequestBody = edtPincode.toRequestBody(MultipartBody.FORM)
                        val countryPart: RequestBody = edtCountry.toRequestBody(MultipartBody.FORM)
                        val fbDeviceID: RequestBody = notebookPrefs.firebaseDeviceID!!.toRequestBody(MultipartBody.FORM)
                        val registerForPart:RequestBody = isRegisterType.toString().toRequestBody(MultipartBody.FORM)

                        val imgFilePan = File(pancardImage!!.toUri().getAppFilePath(mContext)!!)
                        val imgFileAadhar = File(identityImage!!.toUri().getAppFilePath(mContext)!!)
                        val imgFileAadhar2 = File(identityImage2!!.toUri().getAppFilePath(mContext)!!)
                        val requestFileAadhar = imgFileAadhar.asRequestBody("image/*".toMediaTypeOrNull())
                        val requestFileAadhar2 = imgFileAadhar2.asRequestBody("image/*".toMediaTypeOrNull())
                        val imgFileCancelCheque = File(imageUri?.getAppFilePath(mContext)!!)
                        val requestFileCancelCheque = imgFileCancelCheque.asRequestBody("image/*".toMediaTypeOrNull())
                        val requestFilePan = imgFilePan.asRequestBody("image/*".toMediaTypeOrNull())
                        val identityPart = MultipartBody.Part.createFormData(
                            "identity_image",
                            imgFileAadhar.name,
                            requestFileAadhar
                        )
                        val identityPart2 = MultipartBody.Part.createFormData(
                            "identity_image2",
                            imgFileAadhar2.name,
                            requestFileAadhar2
                        )
                        val panCardPart = MultipartBody.Part.createFormData(
                            "pancardimage",
                            imgFilePan.name,
                            requestFilePan
                        )
                        val cancelCheque = MultipartBody.Part.createFormData(
                            "cancled_cheque_image",
                            imgFileCancelCheque.name,
                            requestFileCancelCheque
                        )

                        val instituteValue = fragPrimeMerchantBinding.edtInstituteName.text.toString()
                        if(isRegisterType == 2){
                            if (TextUtils.isEmpty(instituteValue)){
                                showErrorView("Please enter institute name")
                            }else{
                                val institutePart: RequestBody = instituteValue.toRequestBody(MultipartBody.FORM)
                                merchantVM.registerPrimeUsingDetails(
                                    namePart,
                                    emailPart,
                                    dobPart,
                                    phonePart,
                                    addressPart,
                                    localityPart,
                                    cityPart,
                                    statePart,
                                    pincodePart,
                                    countryPart,
                                    aadharDetailPart,
                                    pancardDetailPart,
                                    identityPart,
                                    panCardPart,
                                    identityPart2,
                                    cancelCheque,
                                    accoutNumbPart,
                                    bankNamePart,
                                    ifscCodePart,
                                    bankLocPart,
                                    upiIDPart,
                                    refferalIDPart,
                                    fbDeviceID,
                                    registerForPart,
                                    institutePart
                                )
                            }
                        }else{
                            val institutePart: RequestBody = "".toRequestBody(MultipartBody.FORM)
                            merchantVM.registerPrimeUsingDetails(
                                namePart,
                                emailPart,
                                dobPart,
                                phonePart,
                                addressPart,
                                localityPart,
                                cityPart,
                                statePart,
                                pincodePart,
                                countryPart,
                                aadharDetailPart,
                                pancardDetailPart,
                                identityPart,
                                panCardPart,
                                identityPart2,
                                cancelCheque,
                                accoutNumbPart,
                                bankNamePart,
                                ifscCodePart,
                                bankLocPart,
                                upiIDPart,
                                refferalIDPart,
                                fbDeviceID,
                                registerForPart,
                                institutePart
                            )
                        }
                    }
                }
            }else if(userData!!.usertype == 1){
                if(userData!!.status == 1){
                    if(isRegisterType == 0){
                        showErrorView("Please Select Register for")
                    }else if(TextUtils.isEmpty(fullname)){
                        showErrorView("Enter full name")
                    }else if(TextUtils.isEmpty(dob)){
                        showErrorView("Enter your date of birth")
                    }else if(TextUtils.isEmpty(email)){
                        showErrorView("Enter your email address")
                    }else if(!validateEmail(email)){
                        showErrorView("Enter valid email address")
                    }else if(TextUtils.isEmpty(phone)){
                        showErrorView("Enter your 10 digit mobile number")
                    }else if(phone.length < 10){
                        showErrorView("Enter valid mobile number")
                    }else if(TextUtils.isEmpty(aadharDetail)){
                        showErrorView("Enter your aadhar number")
                    }else if(aadharDetail.length < 12){
                        showErrorView("Enter valid aadhar number")
                    }else if(TextUtils.isEmpty(panCardDetail)){
                        showErrorView("Enter your pancard detail")
                    }else if(panCardDetail.length < 10){
                        showErrorView("Enter valid pancard detail")
                    }else if(TextUtils.isEmpty(address)){
                        showErrorView("Enter your address")
                    }else if(TextUtils.isEmpty(edtLocality)){
                        showErrorView("Enter locality here")
                    }else if(TextUtils.isEmpty(edtCity)){
                        showErrorView("Enter city here")
                    }else if(TextUtils.isEmpty(edtState)){
                        showErrorView("Enter state here")
                    }else if(TextUtils.isEmpty(edtPincode)){
                        showErrorView("Enter pincode here")
                    }else if(edtPincode.length<6){
                        onFailure("Enter 6 digit pincode here")
                    }else if(TextUtils.isEmpty(edtCountry)){
                        showErrorView("Enter country here")
                    }else if(cancelledChequeImage == null){
                        showErrorView("Please upload cancel cheque")
                    }else if(TextUtils.isEmpty(accountNumber)){
                        showErrorView("Enter your bank account number")
                    }else if(TextUtils.isEmpty(bankName)){
                        showErrorView("Enter your bank name")
                    }else if(TextUtils.isEmpty(ifscCode)){
                        showErrorView("Enter your IFSC Code")
                    }else if(TextUtils.isEmpty(bankLocation)){
                        showErrorView("Enter your bank location")
                    }else if(identityImage.isNullOrEmpty()){
                        showErrorView("Please attach your aadhar card image")
                    }else if(pancardImage.isNullOrEmpty()){
                        showErrorView("Please attach your pan card image")
                    }else if(!isTermAccepted){
                        showErrorView("Please accept terms & condition of Notebook Store app")
                    }else{
                        Log.e("dob server", dateForServer!!)
                        val namePart: RequestBody = fullname.toRequestBody(MultipartBody.FORM)
                        val emailPart: RequestBody = email.toRequestBody(MultipartBody.FORM)
                        val dobPart: RequestBody = dateForServer!!.toRequestBody(MultipartBody.FORM)
                        val phonePart: RequestBody = phone.toRequestBody(MultipartBody.FORM)
                        val aadharDetailPart: RequestBody = aadharDetail.toRequestBody(MultipartBody.FORM)
                        val pancardDetailPart: RequestBody = panCardDetail.toRequestBody(MultipartBody.FORM)
                        val addressPart: RequestBody = address.toRequestBody(MultipartBody.FORM)
                        val accoutNumbPart: RequestBody = accountNumber.toRequestBody(MultipartBody.FORM)
                        val bankNamePart: RequestBody = bankName.toRequestBody(MultipartBody.FORM)
                        val ifscCodePart: RequestBody = ifscCode.toRequestBody(MultipartBody.FORM)
                        val bankLocPart: RequestBody = bankLocation.toRequestBody(MultipartBody.FORM)
                        val upiIDPart: RequestBody = upiID.toRequestBody(MultipartBody.FORM)
                        val refferalIDPart: RequestBody = refferalID.toRequestBody(MultipartBody.FORM)

                        val localityPart: RequestBody = edtLocality.toRequestBody(MultipartBody.FORM)
                        val cityPart: RequestBody = edtCity.toRequestBody(MultipartBody.FORM)
                        val statePart: RequestBody = edtState.toRequestBody(MultipartBody.FORM)
                        val pincodePart: RequestBody = edtPincode.toRequestBody(MultipartBody.FORM)
                        val countryPart: RequestBody = edtCountry.toRequestBody(MultipartBody.FORM)
                        val fbDeviceID: RequestBody = notebookPrefs.firebaseDeviceID!!.toRequestBody(MultipartBody.FORM)
                        val registerForPart:RequestBody = isRegisterType.toString().toRequestBody(MultipartBody.FORM)

                        val imgFilePan = File(pancardImage!!.toUri().getAppFilePath(mContext)!!)
                        val imgFileAadhar = File(identityImage!!.toUri().getAppFilePath(mContext)!!)
                        val imgFileAadhar2 = File(identityImage2!!.toUri().getAppFilePath(mContext)!!)
                        val requestFileAadhar = imgFileAadhar.asRequestBody("image/*".toMediaTypeOrNull())
                        val requestFileAadhar2 = imgFileAadhar2.asRequestBody("image/*".toMediaTypeOrNull())
                        val imgFileCancelCheque = File(imageUri?.getAppFilePath(mContext)!!)
                        val requestFileCancelCheque = imgFileCancelCheque.asRequestBody("image/*".toMediaTypeOrNull())
                        val requestFilePan = imgFilePan.asRequestBody("image/*".toMediaTypeOrNull())
                        val identityPart = MultipartBody.Part.createFormData(
                            "identity_image",
                            imgFileAadhar.name,
                            requestFileAadhar
                        )
                        val identityPart2 = MultipartBody.Part.createFormData(
                            "identity_image2",
                            imgFileAadhar2.name,
                            requestFileAadhar2
                        )
                        val panCardPart = MultipartBody.Part.createFormData(
                            "pancardimage",
                            imgFilePan.name,
                            requestFilePan
                        )
                        val cancelCheque = MultipartBody.Part.createFormData(
                            "cancled_cheque_image",
                            imgFileCancelCheque.name,
                            requestFileCancelCheque
                        )

                        val instituteValue = fragPrimeMerchantBinding.edtInstituteName.text.toString()
                        if(isRegisterType == 2){
                            if (TextUtils.isEmpty(instituteValue)){
                                showErrorView("Please enter institute name")
                            }else{
                                val institutePart: RequestBody = instituteValue.toRequestBody(MultipartBody.FORM)
                                merchantVM.registerPrimeUsingDetails(
                                    namePart,
                                    emailPart,
                                    dobPart,
                                    phonePart,
                                    addressPart,
                                    localityPart,
                                    cityPart,
                                    statePart,
                                    pincodePart,
                                    countryPart,
                                    aadharDetailPart,
                                    pancardDetailPart,
                                    identityPart,
                                    panCardPart,
                                    identityPart2,
                                    cancelCheque,
                                    accoutNumbPart,
                                    bankNamePart,
                                    ifscCodePart,
                                    bankLocPart,
                                    upiIDPart,
                                    refferalIDPart,
                                    fbDeviceID,
                                    registerForPart,
                                    institutePart
                                )
                            }
                        }else{
                            val institutePart: RequestBody = "".toRequestBody(MultipartBody.FORM)
                            merchantVM.registerPrimeUsingDetails(
                                namePart,
                                emailPart,
                                dobPart,
                                phonePart,
                                addressPart,
                                localityPart,
                                cityPart,
                                statePart,
                                pincodePart,
                                countryPart,
                                aadharDetailPart,
                                pancardDetailPart,
                                identityPart,
                                panCardPart,
                                identityPart2,
                                cancelCheque,
                                accoutNumbPart,
                                bankNamePart,
                                ifscCodePart,
                                bankLocPart,
                                upiIDPart,
                                refferalIDPart,
                                fbDeviceID,
                                registerForPart,
                                institutePart
                            )
                        }
                    }
                }else if(userData!!.status == 0){
                    if (notebookPrefs.primeUserUpgradeAvail == 1){
                        val namePart: RequestBody = fullname.toRequestBody(MultipartBody.FORM)
                        val emailPart: RequestBody = email.toRequestBody(MultipartBody.FORM)
                        val dobPart: RequestBody = dateForServer!!.toRequestBody(MultipartBody.FORM)
                        val phonePart: RequestBody = phone.toRequestBody(MultipartBody.FORM)
                        val aadharDetailPart: RequestBody = aadharDetail.toRequestBody(MultipartBody.FORM)
                        val pancardDetailPart: RequestBody = panCardDetail.toRequestBody(MultipartBody.FORM)
                        val addressPart: RequestBody = address.toRequestBody(MultipartBody.FORM)
                        val accoutNumbPart: RequestBody = accountNumber.toRequestBody(MultipartBody.FORM)
                        val bankNamePart: RequestBody = bankName.toRequestBody(MultipartBody.FORM)
                        val ifscCodePart: RequestBody = ifscCode.toRequestBody(MultipartBody.FORM)
                        val bankLocPart: RequestBody = bankLocation.toRequestBody(MultipartBody.FORM)
                        val upiIDPart: RequestBody = upiID.toRequestBody(MultipartBody.FORM)
                        val refferalIDPart: RequestBody = refferalID.toRequestBody(MultipartBody.FORM)

                        val localityPart: RequestBody = edtLocality.toRequestBody(MultipartBody.FORM)
                        val cityPart: RequestBody = edtCity.toRequestBody(MultipartBody.FORM)
                        val statePart: RequestBody = edtState.toRequestBody(MultipartBody.FORM)
                        val pincodePart: RequestBody = edtPincode.toRequestBody(MultipartBody.FORM)
                        val countryPart: RequestBody = edtCountry.toRequestBody(MultipartBody.FORM)
                        val fbDeviceID: RequestBody = notebookPrefs.firebaseDeviceID!!.toRequestBody(MultipartBody.FORM)
                        val registerForPart:RequestBody = isRegisterType.toString().toRequestBody(MultipartBody.FORM)

                        if(!isTermAccepted){
                            showErrorView("Please accept terms & condition")
                        }else{
                            val instituteValue = fragPrimeMerchantBinding.edtInstituteName.text.toString()
                            if(isRegisterType == 2){
                                if (TextUtils.isEmpty(instituteValue)){
                                    showErrorView("Please enter institute name")
                                }else{
                                    val institutePart: RequestBody = instituteValue.toRequestBody(MultipartBody.FORM)
                                    val identityUpdatePart: RequestBody = "".toRequestBody(MultipartBody.FORM)
                                    val identityUpdate2Part: RequestBody = "".toRequestBody(MultipartBody.FORM)
                                    val panUpdatePart: RequestBody = "".toRequestBody(MultipartBody.FORM)
                                    val cancelChequeUpdate: RequestBody = "".toRequestBody(MultipartBody.FORM)
                                    merchantVM.registerPrimeUpdateUsingDetails(
                                        namePart,
                                        emailPart,
                                        dobPart,
                                        phonePart,
                                        addressPart,
                                        localityPart,
                                        cityPart,
                                        statePart,
                                        pincodePart,
                                        countryPart,
                                        aadharDetailPart,
                                        pancardDetailPart,
                                        identityUpdatePart,
                                        identityUpdate2Part,
                                        panUpdatePart,
                                        cancelChequeUpdate,
                                        accoutNumbPart,
                                        bankNamePart,
                                        ifscCodePart,
                                        bankLocPart,
                                        upiIDPart,
                                        refferalIDPart,
                                        fbDeviceID,
                                        registerForPart,
                                        institutePart
                                    )
                                }
                            }else{
                                val institutePart: RequestBody = "".toRequestBody(MultipartBody.FORM)
                                val identityUpdatePart: RequestBody = "".toRequestBody(MultipartBody.FORM)
                                val identityUpdate2Part: RequestBody = "".toRequestBody(MultipartBody.FORM)
                                val panUpdatePart: RequestBody = "".toRequestBody(MultipartBody.FORM)
                                val cancelChequeUpdate: RequestBody = "".toRequestBody(MultipartBody.FORM)

                                merchantVM.registerPrimeUpdateUsingDetails(
                                    namePart,
                                    emailPart,
                                    dobPart,
                                    phonePart,
                                    addressPart,
                                    localityPart,
                                    cityPart,
                                    statePart,
                                    pincodePart,
                                    countryPart,
                                    aadharDetailPart,
                                    pancardDetailPart,
                                    identityUpdatePart,
                                    identityUpdate2Part,
                                    panUpdatePart,
                                    cancelChequeUpdate,
                                    accoutNumbPart,
                                    bankNamePart,
                                    ifscCodePart,
                                    bankLocPart,
                                    upiIDPart,
                                    refferalIDPart,
                                    fbDeviceID,
                                    registerForPart,
                                    institutePart
                                )
                            }
                        }
                    }
                }
            }else{
                if(isRegisterType == 0){
                    showErrorView("Please Select Register for")
                }else if(TextUtils.isEmpty(fullname)){
                    showErrorView("Enter full name")
                }else if(TextUtils.isEmpty(dob)){
                    showErrorView("Enter your date of birth")
                }else if(TextUtils.isEmpty(email)){
                    showErrorView("Enter your email address")
                }else if(!validateEmail(email)){
                    showErrorView("Enter valid email address")
                }else if(TextUtils.isEmpty(phone)){
                    showErrorView("Enter your 10 digit mobile number")
                }else if(phone.length < 10){
                    showErrorView("Enter valid mobile number")
                }else if(TextUtils.isEmpty(aadharDetail)){
                    showErrorView("Enter your aadhar number")
                }else if(aadharDetail.length < 12){
                    showErrorView("Enter valid aadhar number")
                }else if(TextUtils.isEmpty(panCardDetail)){
                    showErrorView("Enter your pancard detail")
                }else if(panCardDetail.length < 10){
                    showErrorView("Enter valid pancard detail")
                }else if(TextUtils.isEmpty(address)){
                    showErrorView("Enter your address")
                }else if(TextUtils.isEmpty(edtLocality)){
                    showErrorView("Enter locality here")
                }else if(TextUtils.isEmpty(edtCity)){
                    showErrorView("Enter city here")
                }else if(TextUtils.isEmpty(edtState)){
                    showErrorView("Enter state here")
                }else if(TextUtils.isEmpty(edtPincode)){
                    showErrorView("Enter pincode here")
                }else if(edtPincode.length<6){
                    onFailure("Enter 6 digit pincode here")
                }else if(TextUtils.isEmpty(edtCountry)){
                    showErrorView("Enter country here")
                }else if(cancelledChequeImage == null){
                    showErrorView("Please upload cancel cheque")
                }else if(TextUtils.isEmpty(accountNumber)){
                    showErrorView("Enter your bank account number")
                }else if(TextUtils.isEmpty(bankName)){
                    showErrorView("Enter your bank name")
                }else if(TextUtils.isEmpty(ifscCode)){
                    showErrorView("Enter your IFSC Code")
                }else if(TextUtils.isEmpty(bankLocation)){
                    showErrorView("Enter your bank location")
                }else if(identityImage.isNullOrEmpty()){
                    showErrorView("Please attach your aadhar card image")
                }else if(pancardImage.isNullOrEmpty()){
                    showErrorView("Please attach your pan card image")
                }else if(!isTermAccepted){
                    showErrorView("Please accept terms & condition of Notebook Store app")
                }else{
                    Log.e("dob server", dateForServer!!)
                    val namePart: RequestBody = fullname.toRequestBody(MultipartBody.FORM)
                    val emailPart: RequestBody = email.toRequestBody(MultipartBody.FORM)
                    val dobPart: RequestBody = dateForServer!!.toRequestBody(MultipartBody.FORM)
                    val phonePart: RequestBody = phone.toRequestBody(MultipartBody.FORM)
                    val aadharDetailPart: RequestBody = aadharDetail.toRequestBody(MultipartBody.FORM)
                    val pancardDetailPart: RequestBody = panCardDetail.toRequestBody(MultipartBody.FORM)
                    val addressPart: RequestBody = address.toRequestBody(MultipartBody.FORM)
                    val accoutNumbPart: RequestBody = accountNumber.toRequestBody(MultipartBody.FORM)
                    val bankNamePart: RequestBody = bankName.toRequestBody(MultipartBody.FORM)
                    val ifscCodePart: RequestBody = ifscCode.toRequestBody(MultipartBody.FORM)
                    val bankLocPart: RequestBody = bankLocation.toRequestBody(MultipartBody.FORM)
                    val upiIDPart: RequestBody = upiID.toRequestBody(MultipartBody.FORM)
                    val refferalIDPart: RequestBody = refferalID.toRequestBody(MultipartBody.FORM)

                    val localityPart: RequestBody = edtLocality.toRequestBody(MultipartBody.FORM)
                    val cityPart: RequestBody = edtCity.toRequestBody(MultipartBody.FORM)
                    val statePart: RequestBody = edtState.toRequestBody(MultipartBody.FORM)
                    val pincodePart: RequestBody = edtPincode.toRequestBody(MultipartBody.FORM)
                    val countryPart: RequestBody = edtCountry.toRequestBody(MultipartBody.FORM)
                    val fbDeviceID: RequestBody = notebookPrefs.firebaseDeviceID!!.toRequestBody(MultipartBody.FORM)
                    val registerForPart:RequestBody = isRegisterType.toString().toRequestBody(MultipartBody.FORM)

                    val imgFilePan = File(pancardImage!!.toUri().getAppFilePath(mContext)!!)
                    val imgFileAadhar = File(identityImage!!.toUri().getAppFilePath(mContext)!!)
                    val imgFileAadhar2 = File(identityImage2!!.toUri().getAppFilePath(mContext)!!)
                    val requestFileAadhar = imgFileAadhar.asRequestBody("image/*".toMediaTypeOrNull())
                    val requestFileAadhar2 = imgFileAadhar2.asRequestBody("image/*".toMediaTypeOrNull())
                    val imgFileCancelCheque = File(imageUri?.getAppFilePath(mContext)!!)
                    val requestFileCancelCheque = imgFileCancelCheque.asRequestBody("image/*".toMediaTypeOrNull())
                    val requestFilePan = imgFilePan.asRequestBody("image/*".toMediaTypeOrNull())
                    val identityPart = MultipartBody.Part.createFormData(
                        "identity_image",
                        imgFileAadhar.name,
                        requestFileAadhar
                    )
                    val identityPart2 = MultipartBody.Part.createFormData(
                        "identity_image2",
                        imgFileAadhar2.name,
                        requestFileAadhar2
                    )
                    val panCardPart = MultipartBody.Part.createFormData(
                        "pancardimage",
                        imgFilePan.name,
                        requestFilePan
                    )
                    val cancelCheque = MultipartBody.Part.createFormData(
                        "cancled_cheque_image",
                        imgFileCancelCheque.name,
                        requestFileCancelCheque
                    )

                    val instituteValue = fragPrimeMerchantBinding.edtInstituteName.text.toString()
                    if(isRegisterType == 2){
                        if (TextUtils.isEmpty(instituteValue)){
                            showErrorView("Please enter institute name")
                        }else{
                            val institutePart: RequestBody = instituteValue.toRequestBody(MultipartBody.FORM)
                            merchantVM.registerPrimeUsingDetails(
                                namePart,
                                emailPart,
                                dobPart,
                                phonePart,
                                addressPart,
                                localityPart,
                                cityPart,
                                statePart,
                                pincodePart,
                                countryPart,
                                aadharDetailPart,
                                pancardDetailPart,
                                identityPart,
                                panCardPart,
                                identityPart2,
                                cancelCheque,
                                accoutNumbPart,
                                bankNamePart,
                                ifscCodePart,
                                bankLocPart,
                                upiIDPart,
                                refferalIDPart,
                                fbDeviceID,
                                registerForPart,
                                institutePart
                            )
                        }
                    }else{
                        val institutePart: RequestBody = "".toRequestBody(MultipartBody.FORM)
                        merchantVM.registerPrimeUsingDetails(
                            namePart, emailPart, dobPart, phonePart,
                            addressPart, localityPart, cityPart, statePart,
                            pincodePart, countryPart, aadharDetailPart, pancardDetailPart,
                            identityPart, panCardPart, identityPart2, cancelCheque,
                            accoutNumbPart, bankNamePart, ifscCodePart, bankLocPart,
                            upiIDPart, refferalIDPart, fbDeviceID, registerForPart, institutePart
                        )
                    }
                }
            }
        }else{
            if(isRegisterType == 0){
                showErrorView("Please Select Register for")
            }else if(TextUtils.isEmpty(fullname)){
                showErrorView("Enter full name")
            }else if(TextUtils.isEmpty(dob)){
                showErrorView("Enter your date of birth")
            }else if(TextUtils.isEmpty(email)){
                showErrorView("Enter your email address")
            }else if(!validateEmail(email)){
                showErrorView("Enter valid email address")
            }else if(TextUtils.isEmpty(phone)){
                showErrorView("Enter your 10 digit mobile number")
            }else if(phone.length < 10){
                showErrorView("Enter valid mobile number")
            }else if(TextUtils.isEmpty(aadharDetail)){
                showErrorView("Enter your aadhar number")
            }else if(aadharDetail.length < 12){
                showErrorView("Enter valid aadhar number")
            }else if(TextUtils.isEmpty(panCardDetail)){
                showErrorView("Enter your pancard detail")
            }else if(panCardDetail.length < 10){
                showErrorView("Enter valid pancard detail")
            }else if(TextUtils.isEmpty(address)){
                showErrorView("Enter your address")
            }else if(TextUtils.isEmpty(edtLocality)){
                showErrorView("Enter locality here")
            }else if(TextUtils.isEmpty(edtCity)){
                showErrorView("Enter city here")
            }else if(TextUtils.isEmpty(edtState)){
                showErrorView("Enter state here")
            }else if(TextUtils.isEmpty(edtPincode)){
                showErrorView("Enter pincode here")
            }else if(edtPincode.length<6){
                onFailure("Enter 6 digit pincode here")
            }else if(TextUtils.isEmpty(edtCountry)){
                showErrorView("Enter country here")
            }else if(cancelledChequeImage == null){
                showErrorView("Please upload cancel cheque")
            }else if(TextUtils.isEmpty(accountNumber)){
                showErrorView("Enter your bank account number")
            }else if(TextUtils.isEmpty(bankName)){
                showErrorView("Enter your bank name")
            }else if(TextUtils.isEmpty(ifscCode)){
                showErrorView("Enter your IFSC Code")
            }else if(TextUtils.isEmpty(bankLocation)){
                showErrorView("Enter your bank location")
            }else if(identityImage.isNullOrEmpty()){
                showErrorView("Please attach your aadhar card image")
            }else if(pancardImage.isNullOrEmpty()){
                showErrorView("Please attach your pan card image")
            }else if(!isTermAccepted){
                showErrorView("Please accept terms & condition of Notebook Store app")
            }else{
                Log.e("dob server", dateForServer!!)
                val namePart: RequestBody = fullname.toRequestBody(MultipartBody.FORM)
                val emailPart: RequestBody = email.toRequestBody(MultipartBody.FORM)
                val dobPart: RequestBody = dateForServer!!.toRequestBody(MultipartBody.FORM)
                val phonePart: RequestBody = phone.toRequestBody(MultipartBody.FORM)
                val aadharDetailPart: RequestBody = aadharDetail.toRequestBody(MultipartBody.FORM)
                val pancardDetailPart: RequestBody = panCardDetail.toRequestBody(MultipartBody.FORM)
                val addressPart: RequestBody = address.toRequestBody(MultipartBody.FORM)
                val accoutNumbPart: RequestBody = accountNumber.toRequestBody(MultipartBody.FORM)
                val bankNamePart: RequestBody = bankName.toRequestBody(MultipartBody.FORM)
                val ifscCodePart: RequestBody = ifscCode.toRequestBody(MultipartBody.FORM)
                val bankLocPart: RequestBody = bankLocation.toRequestBody(MultipartBody.FORM)
                val upiIDPart: RequestBody = upiID.toRequestBody(MultipartBody.FORM)
                val refferalIDPart: RequestBody = refferalID.toRequestBody(MultipartBody.FORM)

                val localityPart: RequestBody = edtLocality.toRequestBody(MultipartBody.FORM)
                val cityPart: RequestBody = edtCity.toRequestBody(MultipartBody.FORM)
                val statePart: RequestBody = edtState.toRequestBody(MultipartBody.FORM)
                val pincodePart: RequestBody = edtPincode.toRequestBody(MultipartBody.FORM)
                val countryPart: RequestBody = edtCountry.toRequestBody(MultipartBody.FORM)
                val fbDeviceID: RequestBody = notebookPrefs.firebaseDeviceID!!.toRequestBody(MultipartBody.FORM)
                val registerForPart:RequestBody = isRegisterType.toString().toRequestBody(MultipartBody.FORM)

                val imgFilePan = File(pancardImage!!.toUri().getAppFilePath(mContext)!!)
                val imgFileAadhar = File(identityImage!!.toUri().getAppFilePath(mContext)!!)
                val imgFileAadhar2 = File(identityImage2!!.toUri().getAppFilePath(mContext)!!)
                val requestFileAadhar = imgFileAadhar.asRequestBody("image/*".toMediaTypeOrNull())
                val requestFileAadhar2 = imgFileAadhar2.asRequestBody("image/*".toMediaTypeOrNull())
                val imgFileCancelCheque = File(imageUri?.getAppFilePath(mContext)!!)
                val requestFileCancelCheque = imgFileCancelCheque.asRequestBody("image/*".toMediaTypeOrNull())
                val requestFilePan = imgFilePan.asRequestBody("image/*".toMediaTypeOrNull())
                val identityPart = MultipartBody.Part.createFormData(
                    "identity_image",
                    imgFileAadhar.name,
                    requestFileAadhar
                )
                val identityPart2 = MultipartBody.Part.createFormData(
                    "identity_image2",
                    imgFileAadhar2.name,
                    requestFileAadhar2
                )
                val panCardPart = MultipartBody.Part.createFormData(
                    "pancardimage",
                    imgFilePan.name,
                    requestFilePan
                )
                val cancelCheque = MultipartBody.Part.createFormData(
                    "cancled_cheque_image",
                    imgFileCancelCheque.name,
                    requestFileCancelCheque
                )

                val instituteValue = fragPrimeMerchantBinding.edtInstituteName.text.toString()
                if(isRegisterType == 2){
                    if (TextUtils.isEmpty(instituteValue)){
                        showErrorView("Please enter institute name")
                    }else{
                        val institutePart: RequestBody = instituteValue.toRequestBody(MultipartBody.FORM)
                        merchantVM.registerPrimeUsingDetails(
                            namePart, emailPart, dobPart, phonePart,
                            addressPart, localityPart, cityPart, statePart,
                            pincodePart, countryPart, aadharDetailPart, pancardDetailPart,
                            identityPart, panCardPart, identityPart2, cancelCheque,
                            accoutNumbPart, bankNamePart, ifscCodePart, bankLocPart,
                            upiIDPart, refferalIDPart, fbDeviceID, registerForPart, institutePart
                        )
                    }
                }else{
                    val institutePart: RequestBody = "".toRequestBody(MultipartBody.FORM)
                    merchantVM.registerPrimeUsingDetails(
                        namePart, emailPart, dobPart, phonePart,
                        addressPart, localityPart, cityPart, statePart,
                        pincodePart, countryPart, aadharDetailPart, pancardDetailPart,
                        identityPart, panCardPart, identityPart2, cancelCheque,
                        accoutNumbPart, bankNamePart, ifscCodePart, bankLocPart,
                        upiIDPart, refferalIDPart, fbDeviceID, registerForPart, institutePart
                    )
                }
            }
        }
    }

    override fun getRegisterType(data: String, type: Int) {
        fragPrimeMerchantBinding.edtSeletRegisterType.setText(data)
        isRegisterType = type
        notebookPrefs.merchantRegisterFor = type

        if(isRegisterType == 2){
            fragPrimeMerchantBinding.clInstituteName.visibility = View.VISIBLE
        }else{
            fragPrimeMerchantBinding.clInstituteName.visibility = View.GONE
        }
    }
}

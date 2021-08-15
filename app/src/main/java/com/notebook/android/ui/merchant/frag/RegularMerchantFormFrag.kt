package com.notebook.android.ui.merchant.frag

import android.app.DatePickerDialog
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
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
import androidx.core.net.toUri
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.bumptech.glide.Glide
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import com.max.ecomaxgo.maxpe.view.flight.utility.validateEmail
import com.notebook.android.R
import com.notebook.android.data.db.entities.Address
import com.notebook.android.data.db.entities.User
import com.notebook.android.data.preferences.NotebookPrefs
import com.notebook.android.data.preferences.RefferalPreferance
import com.notebook.android.databinding.FragmentRegularMerchantFormBinding
import com.notebook.android.model.merchant.AadharData
import com.notebook.android.model.merchant.RegularMerchantResponse
import com.notebook.android.ui.auth.responseListener.OtpVerificationListener
import com.notebook.android.ui.dashboard.MainDashboardPage
import com.notebook.android.ui.merchant.MerchantVMFactory
import com.notebook.android.ui.merchant.MerchantViewModel
import com.notebook.android.ui.merchant.responseListener.RegularRegisterRespListener
import com.notebook.android.ui.myOrder.OrderSummaryPage
import com.notebook.android.ui.popupDialogFrag.LoadingDialog
import com.notebook.android.ui.popupDialogFrag.RegisterForDialog
import com.notebook.android.ui.popupDialogFrag.VerificationPopupDialog
import com.notebook.android.utility.Constant
import com.notebook.android.utility.CustomTextWatcher
import com.notebook.android.utility.getAppFilePath
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.json.JSONArray
import org.json.JSONException
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class RegularMerchantFormFrag : Fragment(), View.OnClickListener, KodeinAware,
    RegularRegisterRespListener, OtpVerificationListener, RegisterForDialog.RegisterForListener {

    private lateinit var fragRegulaMerchantBinding: FragmentRegularMerchantFormBinding
    private lateinit var navController: NavController
    private lateinit var myCalendar: Calendar
    private lateinit var dateDOB: DatePickerDialog.OnDateSetListener

    private var identityImage: String? = null
    private var identityImage2: String? = null
    private var pancardImage: String? = null
    private var isTermAccepted = false
    private var addressData: Address? = null
    private var isFieldUpdated = false

    private val notebookPrefs: NotebookPrefs by lazy {
        NotebookPrefs(mContext)
    }

    private val refferalPrefs: RefferalPreferance by lazy {
        RefferalPreferance(mContext)
    }

    private var isRegisterType = 0
    override val kodein by kodein()
    private val viewModelFactory: MerchantVMFactory by instance()
    private val merchantVM: MerchantViewModel by lazy {
        ViewModelProvider(this, viewModelFactory).get(MerchantViewModel::class.java)
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

        notebookPrefs.aadharBackImage = ""
        notebookPrefs.aadharFrontImage = ""
        notebookPrefs.pancardImage = ""
    }

//    var commaSeparated = "item1 , item2 , item3"
//    var items = Arrays.asList(commaSeparated.split("\\s*,\\s*"))

    private val loadingDialog: LoadingDialog by lazy {
        LoadingDialog()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragRegulaMerchantBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_regular_merchant_form, container, false
        )

        setTermsLoginTextClickable()
        merchantVM.regularRespListener = this
        myCalendar = Calendar.getInstance()
        dateDOB = DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->

            myCalendar.set(Calendar.YEAR, year)
            myCalendar.set(Calendar.MONTH, monthOfYear)
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateLabelFrom()
        }

        //success toast layout initialization here....
        val successToastLayout: View = inflater.inflate(
            R.layout.custom_toast_layout,
            fragRegulaMerchantBinding.root.findViewById(R.id.custom_toast_layout) as? ViewGroup
        )
        successToastTextView =
            (successToastLayout.findViewById(R.id.custom_toast_message) as TextView)
        successToast = Toast(mContext)
        successToast.setView(successToastLayout)
        successToast.setDuration(Toast.LENGTH_SHORT)
        successToast.setGravity(OrderSummaryPage.GRAVITY_BOTTOM, 0, 80)

        //error toast layout here....
        val errorToastLayout: View = inflater.inflate(
            R.layout.error_custom_toast_layout,
            fragRegulaMerchantBinding.root.findViewById(R.id.custom_toast_error_layout) as? ViewGroup
        )
        errorToastTextView =
            (errorToastLayout.findViewById(R.id.custom__error_toast_message) as TextView)
        errorToast = Toast(mContext)
        errorToast.setView(errorToastLayout)
        errorToast.setDuration(Toast.LENGTH_SHORT)
        errorToast.setGravity(OrderSummaryPage.GRAVITY_BOTTOM, 0, 80)

        addressData = Gson().fromJson(notebookPrefs.defaultAddrModal, Address::class.java)

        fragRegulaMerchantBinding.edtName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                notebookPrefs.merchantName = p0.toString()
            }

            override fun afterTextChanged(p0: Editable?) {}
        })

        fragRegulaMerchantBinding.edtEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                notebookPrefs.merchantEmail = p0.toString()
            }

            override fun afterTextChanged(p0: Editable?) {}
        })
        fragRegulaMerchantBinding.edtPhone.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                notebookPrefs.merchantPhone = p0.toString()
            }

            override fun afterTextChanged(p0: Editable?) {}
        })
        fragRegulaMerchantBinding.edtAadharId.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                notebookPrefs.merchantAadharNumber = p0.toString()
            }

            override fun afterTextChanged(p0: Editable?) {}
        })
        fragRegulaMerchantBinding.edtPanCard.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                notebookPrefs.merchantPanNumber = p0.toString()
            }

            override fun afterTextChanged(p0: Editable?) {}
        })
        fragRegulaMerchantBinding.edtAddress.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                notebookPrefs.merchantAddressBuilding = p0.toString()
            }

            override fun afterTextChanged(p0: Editable?) {}
        })
        fragRegulaMerchantBinding.edtMerchantLocality.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                notebookPrefs.merchantAddressLocality = p0.toString()
            }

            override fun afterTextChanged(p0: Editable?) {}
        })
        fragRegulaMerchantBinding.edtMerchantCity.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                notebookPrefs.merchantAddressCity = p0.toString()
            }

            override fun afterTextChanged(p0: Editable?) {}
        })
        fragRegulaMerchantBinding.edtMerchantState.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                notebookPrefs.merchantAddressState = p0.toString()
            }

            override fun afterTextChanged(p0: Editable?) {}
        })
        fragRegulaMerchantBinding.edtMerchantPincode.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                notebookPrefs.merchantAddressPincode = p0.toString()
            }

            override fun afterTextChanged(p0: Editable?) {}
        })
        fragRegulaMerchantBinding.edtInstituteName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                notebookPrefs.merchantInstituteName = p0.toString()
            }

            override fun afterTextChanged(p0: Editable?) {}
        })

        fragRegulaMerchantBinding.edtReferralID.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                notebookPrefs.merchantRefferalID = p0.toString()
            }

            override fun afterTextChanged(p0: Editable?) {}
        })

        if (isFieldUpdated) {
            onRestoreInstanceState()
        }
        return fragRegulaMerchantBinding.root
    }

    var dateForServer: String? = null
    private fun updateLabelFrom() {
        val sdf = SimpleDateFormat(Constant.DATE_FORMAT, Locale.US)
        dateForServer =
            SimpleDateFormat(Constant.DATE_FORMAT_SERVER, Locale.US).format(myCalendar.time)
        notebookPrefs.merchantDOB = dateForServer
        fragRegulaMerchantBinding.edtDOB.setText(sdf.format(myCalendar.time))
    }

    private var userData: User? = null
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)


        if (refferalPrefs.refferCode?.isNotEmpty() == true || notebookPrefs.merchantRefferalID?.isNotEmpty() == true) {
            fragRegulaMerchantBinding.edtReferralID.apply {
                if (notebookPrefs.merchantRefferalID.isNullOrEmpty()) {
                    notebookPrefs.merchantRefferalID = refferalPrefs.refferCode;
                }
                setText(notebookPrefs.merchantRefferalID)
                isEnabled = false
                isFocusable = false
            }
        } else {
            fragRegulaMerchantBinding.edtReferralID.apply {
                isEnabled = true
                isFocusable = true
            }
        }

        merchantVM.getUserData().observe(viewLifecycleOwner, Observer {
            if (it != null) {
                userData = it

                Log.e("refferalData", " :: ${refferalPrefs.refferCode}")
                if (userData!!.referral_id!! > 0) {
                    refferalPrefs.clearPreference()
                }
//                merchantVM.fetchAddressFromServer(userData!!.id, userData!!.token!!)
            } else {
                userData = null
            }

            if (!isFieldUpdated) {
                fillRegularMerchantData(userData)
            }
        })

        navController.currentBackStackEntry?.savedStateHandle?.getLiveData<String>("panCardImageUri")
            ?.observe(
                viewLifecycleOwner, Observer {
                    pancardImage = it
                })

        navController.currentBackStackEntry?.savedStateHandle?.getLiveData<String>("aadharCardImageUri")
            ?.observe(
                viewLifecycleOwner, Observer {
                    val aadharData: AadharData = Gson().fromJson(it, AadharData::class.java)
                    identityImage = aadharData.frontImage
                    identityImage2 = aadharData.backImage
                })

        fragRegulaMerchantBinding.edtAadharId.addTextChangedListener(object : CustomTextWatcher(
            ' ',
            4
        ) {
            override fun onAfterTextChanged(text: String) {
                fragRegulaMerchantBinding.edtAadharId.run {
                    setText(text)
                    setSelection(text.length)
                }
            }
        })

//        fragRegulaMerchantBinding.edtAddress.setText(notebookPrefs.defaultAddr)
        fragRegulaMerchantBinding.cbTermCondition.setOnCheckedChangeListener { p0, p1 ->
            isTermAccepted = p1
        }
        fragRegulaMerchantBinding.edtDOB.setOnClickListener(this)
        fragRegulaMerchantBinding.btnSubmitRegulaMerchant.setOnClickListener(this)

        fragRegulaMerchantBinding.imgAttachIdentityDetails.setOnClickListener(this)
        fragRegulaMerchantBinding.imgAttachPanDetails.setOnClickListener(this)
        fragRegulaMerchantBinding.edtSeletRegisterType.setOnClickListener(this)
    }

    private fun onRestoreInstanceState() {
        fragRegulaMerchantBinding.edtName.setText(notebookPrefs.merchantName)
        val sdf = SimpleDateFormat(Constant.DATE_FORMAT, Locale.US)
        if (notebookPrefs.merchantDOB.isNullOrEmpty()) {
            fragRegulaMerchantBinding.edtDOB.setText("")
            dateForServer = ""
        } else {
            dateForServer = notebookPrefs.merchantDOB
            val serverdate =
                SimpleDateFormat(Constant.DATE_FORMAT_SERVER, Locale.US).parse(dateForServer!!)
            fragRegulaMerchantBinding.edtDOB.setText(sdf.format(serverdate!!))
        }

        isRegisterType = notebookPrefs.merchantRegisterFor ?: 0
        if (isRegisterType == 2) {
            fragRegulaMerchantBinding.clInstituteName.visibility = View.VISIBLE
            fragRegulaMerchantBinding.edtSeletRegisterType.setText("Institute")
            fragRegulaMerchantBinding.edtInstituteName.setText(notebookPrefs.merchantInstituteName)
        } else if (isRegisterType == 1) {
            fragRegulaMerchantBinding.clInstituteName.visibility = View.GONE
            fragRegulaMerchantBinding.edtSeletRegisterType.setText("Individual")
        } else {
            fragRegulaMerchantBinding.clInstituteName.visibility = View.GONE
            fragRegulaMerchantBinding.edtSeletRegisterType.setText("")
        }
        fragRegulaMerchantBinding.edtEmail.setText(notebookPrefs.merchantEmail)
        fragRegulaMerchantBinding.edtPhone.setText(notebookPrefs.merchantPhone)
        fragRegulaMerchantBinding.edtAadharId.setText(notebookPrefs.merchantAadharNumber)
        fragRegulaMerchantBinding.edtPanCard.setText(notebookPrefs.merchantPanNumber)
        fragRegulaMerchantBinding.edtAddress.setText(notebookPrefs.merchantAddressBuilding)
        fragRegulaMerchantBinding.edtMerchantLocality.setText(notebookPrefs.merchantAddressLocality)
        fragRegulaMerchantBinding.edtMerchantCity.setText(notebookPrefs.merchantAddressCity)
        fragRegulaMerchantBinding.edtMerchantState.setText(notebookPrefs.merchantAddressState)
        fragRegulaMerchantBinding.edtMerchantPincode.setText(notebookPrefs.merchantAddressPincode)
        fragRegulaMerchantBinding.edtReferralID.setText(notebookPrefs.merchantRefferalID)
        fragRegulaMerchantBinding.edtMerchantCountry.setText("India")

        if (!userData?.identity_image.isNullOrEmpty()) {
            if (userData?.status == 1) {
                notebookPrefs.aadharFrontImage = ""
                notebookPrefs.aadharBackImage = ""
                notebookPrefs.pancardImage = ""
                identityImage = null
                identityImage2 = null
                pancardImage = null
            } else {
                identityImage = notebookPrefs.aadharFrontImage
                identityImage2 = notebookPrefs.aadharBackImage
                pancardImage = notebookPrefs.pancardImage
                fragRegulaMerchantBinding.clPanImageShow.visibility = View.VISIBLE
                fragRegulaMerchantBinding.clAadharImageShow.visibility = View.VISIBLE
                Glide.with(mContext).load(notebookPrefs.aadharFrontImage)
                    .into(fragRegulaMerchantBinding.imgFrontAadhar)
                Glide.with(mContext).load(notebookPrefs.aadharBackImage)
                    .into(fragRegulaMerchantBinding.imgBackAadhar)
                Glide.with(mContext).load(notebookPrefs.pancardImage)
                    .into(fragRegulaMerchantBinding.imgPancard)
            }
        } else {
            identityImage = null
            identityImage2 = null
            pancardImage = null
        }

        if (notebookPrefs.merchantKycStatus == 0) {
            fragRegulaMerchantBinding.edtAadharId.setFocusable(false)
            fragRegulaMerchantBinding.edtAadharId.setEnabled(false)
            fragRegulaMerchantBinding.edtAadharId.setCursorVisible(false)

            fragRegulaMerchantBinding.edtPanCard.setFocusable(false)
            fragRegulaMerchantBinding.edtPanCard.setEnabled(false)
            fragRegulaMerchantBinding.edtPanCard.setCursorVisible(false)

            fragRegulaMerchantBinding.imgAttachIdentityDetails.setEnabled(false)
            fragRegulaMerchantBinding.imgAttachPanDetails.setEnabled(false)
        } else {
            fragRegulaMerchantBinding.edtAadharId.setFocusable(true)
            fragRegulaMerchantBinding.edtAadharId.setEnabled(true)
            fragRegulaMerchantBinding.edtAadharId.setCursorVisible(true)

            fragRegulaMerchantBinding.edtPanCard.setFocusable(true)
            fragRegulaMerchantBinding.edtPanCard.setEnabled(true)
            fragRegulaMerchantBinding.edtPanCard.setCursorVisible(true)

            fragRegulaMerchantBinding.imgAttachIdentityDetails.setEnabled(true)
            fragRegulaMerchantBinding.imgAttachPanDetails.setEnabled(true)
        }
    }

    private fun fillRegularMerchantData(userData: User?) {
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

        if (userData != null) {
            if (userData.origincode?.isNotEmpty() == true) {
                notebookPrefs.merchantRefferalID = userData.origincode
            } else if (refferalPrefs.refferCode != userData.referralcode) {
                notebookPrefs.merchantRefferalID = refferalPrefs.refferCode
            }
        } else if (refferalPrefs.refferCode?.isNotEmpty() == true) {
            notebookPrefs.merchantRefferalID = refferalPrefs.refferCode
        }
        fragRegulaMerchantBinding.edtReferralID.setText(notebookPrefs.merchantRefferalID)

        fragRegulaMerchantBinding.edtName.setText(userData?.name ?: userData?.username ?: "")
        val sdf = SimpleDateFormat(Constant.DATE_FORMAT, Locale.US)
        if (userData?.dob.isNullOrEmpty()) {
            fragRegulaMerchantBinding.edtDOB.setText("")
            dateForServer = ""
            notebookPrefs.merchantDOB = ""
        } else {
            dateForServer = userData?.dob
            val serverdate =
                SimpleDateFormat(Constant.DATE_FORMAT_SERVER, Locale.US).parse(userData?.dob!!)
            fragRegulaMerchantBinding.edtDOB.setText(sdf.format(serverdate!!))
            notebookPrefs.merchantDOB = userData.dob
        }

        val identityImageFromServer = userData?.identity_image
        if (!identityImageFromServer.isNullOrEmpty()) {
            val items = Arrays.asList(identityImageFromServer.split("\\s*,\\s*")).toString()
            val data = items.replace("[[", "[").replace("]]", "]")
//            identityImage2 = userData.identity_image
            try {
                val jsonArrayList = JSONArray(data)
                identityImage = jsonArrayList[0].toString()
                identityImage2 = jsonArrayList[1].toString()
                Log.e("aadharImages", " :: $identityImage :: $identityImage2")
                pancardImage = userData.pancardimage
                Log.e(pancardImage, "fillRegularMerchantData: " + pancardImage)
            } catch (exception: JSONException) {

            }


            if (userData.status == 1) {
                notebookPrefs.aadharFrontImage = ""
                notebookPrefs.aadharBackImage = ""
                notebookPrefs.pancardImage = ""
                identityImage = null
                identityImage2 = null
                pancardImage = null
            } else {
                notebookPrefs.aadharFrontImage =
                    "${Constant.MERCHANT_BASE_IMAGE_PATH}$identityImage"
                notebookPrefs.aadharBackImage =
                    "${Constant.MERCHANT_BASE_IMAGE_PATH}$identityImage2"
                notebookPrefs.pancardImage = "${Constant.MERCHANT_BASE_IMAGE_PATH}$pancardImage"
                fragRegulaMerchantBinding.clPanImageShow.visibility = View.VISIBLE
                fragRegulaMerchantBinding.clAadharImageShow.visibility = View.VISIBLE
                Glide.with(mContext).load("${Constant.MERCHANT_BASE_IMAGE_PATH}$identityImage")
                    .into(fragRegulaMerchantBinding.imgFrontAadhar)
                Glide.with(mContext).load("${Constant.MERCHANT_BASE_IMAGE_PATH}$identityImage2")
                    .into(fragRegulaMerchantBinding.imgBackAadhar)
                Glide.with(mContext).load("${Constant.MERCHANT_BASE_IMAGE_PATH}$pancardImage")
                    .into(fragRegulaMerchantBinding.imgPancard)
            }
        } else {
            fragRegulaMerchantBinding.clAadharImageShow.visibility = View.GONE
            fragRegulaMerchantBinding.clPanImageShow.visibility = View.GONE
            identityImage = null
            identityImage2 = null
            pancardImage = null
        }

        isRegisterType = userData?.registerfor ?: 0
        if (isRegisterType == 2) {
            fragRegulaMerchantBinding.clInstituteName.visibility = View.VISIBLE
            fragRegulaMerchantBinding.edtSeletRegisterType.setText("Institute")
            fragRegulaMerchantBinding.edtInstituteName.setText(userData?.institute_name)
            notebookPrefs.merchantInstituteName = userData?.institute_name
        } else if (isRegisterType == 1) {
            fragRegulaMerchantBinding.clInstituteName.visibility = View.GONE
            fragRegulaMerchantBinding.edtSeletRegisterType.setText("Individual")
        } else {
            fragRegulaMerchantBinding.clInstituteName.visibility = View.GONE
            fragRegulaMerchantBinding.edtSeletRegisterType.setText("")
        }

        if (userData?.status == 0) {
            fragRegulaMerchantBinding.edtAadharId.setFocusable(false)
            fragRegulaMerchantBinding.edtAadharId.setEnabled(false)
            fragRegulaMerchantBinding.edtAadharId.setCursorVisible(false)

            fragRegulaMerchantBinding.edtPanCard.setFocusable(false)
            fragRegulaMerchantBinding.edtPanCard.setEnabled(false)
            fragRegulaMerchantBinding.edtPanCard.setCursorVisible(false)

            fragRegulaMerchantBinding.imgAttachIdentityDetails.setEnabled(false)
            fragRegulaMerchantBinding.imgAttachPanDetails.setEnabled(false)
        } else {
            fragRegulaMerchantBinding.edtAadharId.setFocusable(true)
            fragRegulaMerchantBinding.edtAadharId.setEnabled(true)
            fragRegulaMerchantBinding.edtAadharId.setCursorVisible(true)

            fragRegulaMerchantBinding.edtPanCard.setFocusable(true)
            fragRegulaMerchantBinding.edtPanCard.setEnabled(true)
            fragRegulaMerchantBinding.edtPanCard.setCursorVisible(true)

            fragRegulaMerchantBinding.imgAttachIdentityDetails.setEnabled(true)
            fragRegulaMerchantBinding.imgAttachPanDetails.setEnabled(true)
        }

        fragRegulaMerchantBinding.edtEmail.setText(userData?.email)
        fragRegulaMerchantBinding.edtPhone.setText(userData?.phone)
        val s = StringBuilder(userData?.identity_detail ?: "")
        var i = 4
        while (i < s.length) {
            s.insert(i, " ")
            i += 5
        }
        fragRegulaMerchantBinding.edtAadharId.setText(s.toString())
        fragRegulaMerchantBinding.edtPanCard.setText(userData?.pancardno)
        fragRegulaMerchantBinding.edtAddress.setText(addressData?.street)
        fragRegulaMerchantBinding.edtMerchantLocality.setText(addressData?.locality)
        fragRegulaMerchantBinding.edtMerchantCity.setText(addressData?.city)
        fragRegulaMerchantBinding.edtMerchantState.setText(addressData?.state)
        fragRegulaMerchantBinding.edtMerchantPincode.setText(addressData?.pincode)
        fragRegulaMerchantBinding.edtMerchantCountry.setText("India")
    }

    private val startFromTerms = 82
    private val endToTerms = 102
    private fun setTermsLoginTextClickable() {
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
        val spanTerms = object : ClickableSpan() {
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
        fragRegulaMerchantBinding.tvTermsText.movementMethod = LinkMovementMethod.getInstance()
        fragRegulaMerchantBinding.tvTermsText.text = ssTermsText
    }

    private fun showErrorView(msg: String) {
        fragRegulaMerchantBinding.clErrorView.visibility = View.VISIBLE
        fragRegulaMerchantBinding.tvErrorText.text = msg
        fragRegulaMerchantBinding.clErrorView.startAnimation(
            AnimationUtils.loadAnimation(
                mContext,
                R.anim.slide_down
            )
        )

        Handler().postDelayed({
            fragRegulaMerchantBinding.clErrorView.visibility = View.GONE
            fragRegulaMerchantBinding.clErrorView.startAnimation(
                AnimationUtils.loadAnimation(
                    mContext,
                    R.anim.slide_up
                )
            )
        }, 1500)
    }

    override fun onClick(view: View?) {
        when (view) {

            fragRegulaMerchantBinding.edtDOB -> {
                DatePickerDialog(
                    mContext, dateDOB, 1980,
                    myCalendar.get(Calendar.MONTH),
                    myCalendar.get(Calendar.DAY_OF_MONTH)
                ).show()
            }

            fragRegulaMerchantBinding.imgAttachIdentityDetails -> {
                isFieldUpdated = true
                navController.navigate(R.id.action_regularMerchantFormFrag_to_identityProofUploadFrag)
            }

            fragRegulaMerchantBinding.edtSeletRegisterType -> {
                val registerDialog = RegisterForDialog()
                registerDialog.isCancelable = true
                registerDialog.setRegisterForListener(this)
                registerDialog.show(mActivity.supportFragmentManager, "Show register for dialog")
            }

            fragRegulaMerchantBinding.imgAttachPanDetails -> {
                isFieldUpdated = true
                navController.navigate(R.id.action_regularMerchantFormFrag_to_panCardUploadFrag)
            }

            fragRegulaMerchantBinding.btnSubmitRegulaMerchant -> {
                callRegisterMerchantApi()
            }
        }
    }

    override fun onStarted() {
        loadingDialog.show(mActivity.supportFragmentManager, "Show loading dialog")
    }

    override fun onSuccessResponse(it: RegularMerchantResponse) {
        val verificationPopupDialog = VerificationPopupDialog()
        verificationPopupDialog.isCancelable = false
        val bundle = Bundle()
        bundle.putString("mobile", fragRegulaMerchantBinding.edtPhone.text.toString())
        bundle.putString("otp", it.otp)
        verificationPopupDialog.arguments = bundle
        verificationPopupDialog.setVerificationListener(this)
        verificationPopupDialog.show(mActivity.supportFragmentManager, "Show Verification Popup !!")
        loadingDialog.dialog?.dismiss()
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

    override fun onRegularMerchantOTPVerify(user: User) {
        loadingDialog.dialog?.dismiss()

        if (!user.address.isNullOrEmpty()) {
            notebookPrefs.defaultAddr = user.address
        }

        /*notebookPrefs.defaultAddrModal= user.address
        addressData = Gson().fromJson(notebookPrefs.defaultAddrModal, Address::class.java)*/
        notebookPrefs.isVerified = user.is_verified ?: 0
        notebookPrefs.walletAmount = user.wallet_amounts
        notebookPrefs.userID = user.id
        notebookPrefs.userToken = user.token
        navController.popBackStack(R.id.homeFrag, false)
    }

    override fun onUpdatedRegularMerchant(user: User) {
        loadingDialog.dialog?.dismiss()

        if (!user.address.isNullOrEmpty()) {
            notebookPrefs.defaultAddr = user.address
        }
        notebookPrefs.userID = user.id
        notebookPrefs.userToken = user.token

        /* notebookPrefs.defaultAddrModal= user.address
         addressData = Gson().fromJson(notebookPrefs.defaultAddrModal, Address::class.java)*/
        notebookPrefs.isVerified = user.is_verified ?: 0
        notebookPrefs.walletAmount = user.wallet_amounts
        navController.popBackStack(R.id.homeFrag, false)
    }

    override fun otpVerifyData(otpValue: String) {
        merchantVM.verifyOtpRegular(
            fragRegulaMerchantBinding.edtPhone.text.toString(),
            otpValue,
            userData
        )
    }

    override fun resendOtpCall(resend: Boolean) {
        callRegisterMerchantApi()
    }

    private fun callRegisterMerchantApi() {
        val fullname = fragRegulaMerchantBinding.edtName.text.toString()
        val dob = fragRegulaMerchantBinding.edtDOB.text.toString()
        val email = fragRegulaMerchantBinding.edtEmail.text.toString()
        val phone = fragRegulaMerchantBinding.edtPhone.text.toString()
        val aadharDetail = fragRegulaMerchantBinding.edtAadharId.text.toString().replace(" ", "")
        val panCardDetail = fragRegulaMerchantBinding.edtPanCard.text.toString()
        val address = fragRegulaMerchantBinding.edtAddress.text.toString()
        val refferalID = fragRegulaMerchantBinding.edtReferralID.text.toString()

        val edtLocality = fragRegulaMerchantBinding.edtMerchantLocality.text.toString()
        val edtCity: String = fragRegulaMerchantBinding.edtMerchantCity.text.toString()
        val edtState: String = fragRegulaMerchantBinding.edtMerchantState.text.toString()
        val edtPincode: String = fragRegulaMerchantBinding.edtMerchantPincode.text.toString()
        val edtCountry: String = fragRegulaMerchantBinding.edtMerchantCountry.text.toString()

        FirebaseMessaging.getInstance().token.addOnSuccessListener {
            Log.e("instance token", " :: $it")
            notebookPrefs.firebaseDeviceID = it
        }

        if (userData != null) {
            if (userData!!.usertype == 0) {
                if (userData!!.status == 1) {
                    if (TextUtils.isEmpty(fullname)) {
                        showErrorView("Enter full name")
                    } else if (isRegisterType == 0) {
                        showErrorView("Please Select Register for")
                    } else if (TextUtils.isEmpty(dob)) {
                        showErrorView("Enter your date of birth")
                    } else if (TextUtils.isEmpty(email)) {
                        showErrorView("Enter your email address")
                    } else if (!validateEmail(email)) {
                        showErrorView("Enter valid email")
                    } else if (TextUtils.isEmpty(phone)) {
                        showErrorView("Enter your 10 digit mobile number")
                    } else if (phone.length < 10) {
                        showErrorView("Enter valid mobile number")
                    } else if (TextUtils.isEmpty(aadharDetail)) {
                        showErrorView("Enter your aadhar number")
                    } else if (aadharDetail.length < 12) {
                        showErrorView("Enter valid aadhar number")
                    } else if (TextUtils.isEmpty(panCardDetail)) {
                        showErrorView("Enter your pancard detail")
                    } else if (panCardDetail.length < 10) {
                        showErrorView("Enter valid pancard detail")
                    } else if (identityImage.isNullOrEmpty()) {
                        showErrorView("Attach your aadhar card image")
                    } else if (pancardImage.isNullOrEmpty()) {
                        showErrorView("Attach your pancard image")
                    } else if (TextUtils.isEmpty(address)) {
                        showErrorView("Enter your address")
                    } else if (TextUtils.isEmpty(edtLocality)) {
                        showErrorView("Enter locality here")
                    } else if (TextUtils.isEmpty(edtCity)) {
                        showErrorView("Enter city here")
                    } else if (TextUtils.isEmpty(edtState)) {
                        showErrorView("Enter state here")
                    } else if (TextUtils.isEmpty(edtPincode)) {
                        showErrorView("Enter pincode here")
                    } else if (edtPincode.length < 6) {
                        onFailure("Enter 6 digit pincode here")
                    } else if (TextUtils.isEmpty(edtCountry)) {
                        showErrorView("Enter country here")
                    } else if (!isTermAccepted) {
                        showErrorView("Please accept terms & condition of Notebook Store app")
                    } else {
                        Log.e("dob server", dateForServer!!)
                        val namePart: RequestBody = RequestBody.create(MultipartBody.FORM, fullname)
                        val emailPart: RequestBody = RequestBody.create(MultipartBody.FORM, email)
                        val dobPart: RequestBody =
                            RequestBody.create(MultipartBody.FORM, dateForServer!!)
                        val phonePart: RequestBody = RequestBody.create(MultipartBody.FORM, phone)
                        val aadharDetailPart: RequestBody =
                            RequestBody.create(MultipartBody.FORM, aadharDetail)
                        val pancardDetailPart: RequestBody = RequestBody.create(
                            MultipartBody.FORM,
                            panCardDetail
                        )
                        val addressPart: RequestBody =
                            RequestBody.create(MultipartBody.FORM, address)
                        val refferalIDPart: RequestBody =
                            RequestBody.create(MultipartBody.FORM, refferalID)

                        val localityPart: RequestBody =
                            RequestBody.create(MultipartBody.FORM, edtLocality)
                        val cityPart: RequestBody = RequestBody.create(MultipartBody.FORM, edtCity)
                        val statePart: RequestBody =
                            RequestBody.create(MultipartBody.FORM, edtState)
                        val pincodePart: RequestBody =
                            RequestBody.create(MultipartBody.FORM, edtPincode)
                        val countryPart: RequestBody =
                            RequestBody.create(MultipartBody.FORM, edtCountry)
                        val fbDeviceID: RequestBody = RequestBody.create(
                            MultipartBody.FORM,
                            notebookPrefs.firebaseDeviceID!!
                        )
                        val registerForPart: RequestBody = RequestBody.create(
                            MultipartBody.FORM,
                            isRegisterType.toString()
                        )


                        var panCardPart: MultipartBody.Part? = null
                        var identityPart: MultipartBody.Part? = null
                        var identityPart2: MultipartBody.Part? = null

                        if (pancardImage != userData?.pancardimage) {
                            val imgFilePan = File(pancardImage!!.toUri().getAppFilePath(mContext)!!)
                            val requestFilePan =
                                RequestBody.create("image/*".toMediaTypeOrNull(), imgFilePan)
                            panCardPart = MultipartBody.Part.createFormData(
                                "pancardimage",
                                imgFilePan.name,
                                requestFilePan
                            )
                        }

                        if (identityImage != getIdentityImageFromServer(0)) {
                            val imgFileAadhar =
                                File(identityImage!!.toUri().getAppFilePath(mContext)!!)
                            val requestFileAadhar =
                                RequestBody.create("image/*".toMediaTypeOrNull(), imgFileAadhar)
                            identityPart = MultipartBody.Part.createFormData(
                                "identity_image",
                                imgFileAadhar.name,
                                requestFileAadhar
                            )
                        }

                        if (identityImage2 != getIdentityImageFromServer(1)) {
                            val imgFileAadhar2 =
                                File(identityImage2!!.toUri().getAppFilePath(mContext)!!)
                            val requestFileAadhar2 =
                                RequestBody.create("image/*".toMediaTypeOrNull(), imgFileAadhar2)
                            identityPart2 = MultipartBody.Part.createFormData(
                                "identity_image2",
                                imgFileAadhar2.name,
                                requestFileAadhar2
                            )
                        }


                        val instituteValue =
                            fragRegulaMerchantBinding.edtInstituteName.text.toString()
                        if (isRegisterType == 2) {
                            if (TextUtils.isEmpty(instituteValue)) {
                                showErrorView("Please enter institute name")
                            } else {
                                val institutePart: RequestBody =
                                    RequestBody.create(MultipartBody.FORM, instituteValue)

                                merchantVM.registerRegularUsingDetails(
                                    namePart, emailPart, dobPart, phonePart, addressPart,
                                    localityPart, cityPart, statePart, pincodePart,
                                    countryPart, aadharDetailPart, pancardDetailPart, identityPart,
                                    panCardPart, identityPart2, refferalIDPart, fbDeviceID,
                                    registerForPart, institutePart
                                )
                            }
                        } else {
                            val institutePart: RequestBody =
                                RequestBody.create(MultipartBody.FORM, "")
                            merchantVM.registerRegularUsingDetails(
                                namePart, emailPart, dobPart, phonePart, addressPart,
                                localityPart, cityPart, statePart, pincodePart,
                                countryPart, aadharDetailPart, pancardDetailPart, identityPart,
                                panCardPart, identityPart2, refferalIDPart, fbDeviceID,
                                registerForPart, institutePart
                            )
                        }
                    }
                }
            } else if (userData!!.usertype == 9) {
                if (TextUtils.isEmpty(fullname)) {
                    showErrorView("Enter full name")
                } else if (isRegisterType == 0) {
                    showErrorView("Please Select Register for")
                } else if (TextUtils.isEmpty(dob)) {
                    showErrorView("Enter your date of birth")
                } else if (TextUtils.isEmpty(email)) {
                    showErrorView("Enter your email address")
                } else if (!validateEmail(email)) {
                    showErrorView("Enter valid email")
                } else if (TextUtils.isEmpty(phone)) {
                    showErrorView("Enter your 10 digit mobile number")
                } else if (phone.length < 10) {
                    showErrorView("Enter valid mobile number")
                } else if (TextUtils.isEmpty(aadharDetail)) {
                    showErrorView("Enter your aadhar number")
                } else if (aadharDetail.length < 12) {
                    showErrorView("Enter valid aadhar number")
                } else if (TextUtils.isEmpty(panCardDetail)) {
                    showErrorView("Enter your pancard detail")
                } else if (panCardDetail.length < 10) {
                    showErrorView("Enter valid pancard detail")
                } else if (identityImage.isNullOrEmpty()) {
                    showErrorView("Attach your aadhar card image")
                } else if (pancardImage.isNullOrEmpty()) {
                    showErrorView("Attach your pancard image")
                } else if (TextUtils.isEmpty(address)) {
                    showErrorView("Enter your address")
                } else if (TextUtils.isEmpty(edtLocality)) {
                    showErrorView("Enter locality here")
                } else if (TextUtils.isEmpty(edtCity)) {
                    showErrorView("Enter city here")
                } else if (TextUtils.isEmpty(edtState)) {
                    showErrorView("Enter state here")
                } else if (TextUtils.isEmpty(edtPincode)) {
                    showErrorView("Enter pincode here")
                } else if (edtPincode.length < 6) {
                    onFailure("Enter 6 digit pincode here")
                } else if (TextUtils.isEmpty(edtCountry)) {
                    showErrorView("Enter country here")
                } else if (!isTermAccepted) {
                    showErrorView("Please accept terms & condition of Notebook Store app")
                } else {
                    Log.e("dob server", dateForServer!!)
                    val namePart: RequestBody = RequestBody.create(MultipartBody.FORM, fullname)
                    val emailPart: RequestBody = RequestBody.create(MultipartBody.FORM, email)
                    val dobPart: RequestBody =
                        RequestBody.create(MultipartBody.FORM, dateForServer!!)
                    val phonePart: RequestBody = RequestBody.create(MultipartBody.FORM, phone)
                    val aadharDetailPart: RequestBody =
                        RequestBody.create(MultipartBody.FORM, aadharDetail)
                    val pancardDetailPart: RequestBody = RequestBody.create(
                        MultipartBody.FORM,
                        panCardDetail
                    )
                    val addressPart: RequestBody = RequestBody.create(MultipartBody.FORM, address)
                    val refferalIDPart: RequestBody =
                        RequestBody.create(MultipartBody.FORM, refferalID)

                    val localityPart: RequestBody =
                        RequestBody.create(MultipartBody.FORM, edtLocality)
                    val cityPart: RequestBody = RequestBody.create(MultipartBody.FORM, edtCity)
                    val statePart: RequestBody = RequestBody.create(MultipartBody.FORM, edtState)
                    val pincodePart: RequestBody =
                        RequestBody.create(MultipartBody.FORM, edtPincode)
                    val countryPart: RequestBody =
                        RequestBody.create(MultipartBody.FORM, edtCountry)
                    val fbDeviceID: RequestBody = RequestBody.create(
                        MultipartBody.FORM,
                        notebookPrefs.firebaseDeviceID!!
                    )
                    val registerForPart: RequestBody = RequestBody.create(
                        MultipartBody.FORM,
                        isRegisterType.toString()
                    )
                    var panCardPart: MultipartBody.Part? = null
                    var identityPart: MultipartBody.Part? = null
                    var identityPart2: MultipartBody.Part? = null

                    if (pancardImage != userData?.pancardimage) {
                        val imgFilePan = File(pancardImage!!.toUri().getAppFilePath(mContext)!!)
                        val requestFilePan =
                            RequestBody.create("image/*".toMediaTypeOrNull(), imgFilePan)
                        panCardPart = MultipartBody.Part.createFormData(
                            "pancardimage",
                            imgFilePan.name,
                            requestFilePan
                        )
                    }

                    if (identityImage != getIdentityImageFromServer(0)) {
                        val imgFileAadhar =
                            File(identityImage!!.toUri().getAppFilePath(mContext)!!)
                        val requestFileAadhar =
                            RequestBody.create("image/*".toMediaTypeOrNull(), imgFileAadhar)
                        identityPart = MultipartBody.Part.createFormData(
                            "identity_image",
                            imgFileAadhar.name,
                            requestFileAadhar
                        )
                    }

                    if (identityImage2 != getIdentityImageFromServer(1)) {
                        val imgFileAadhar2 =
                            File(identityImage2!!.toUri().getAppFilePath(mContext)!!)
                        val requestFileAadhar2 =
                            RequestBody.create("image/*".toMediaTypeOrNull(), imgFileAadhar2)
                        identityPart2 = MultipartBody.Part.createFormData(
                            "identity_image2",
                            imgFileAadhar2.name,
                            requestFileAadhar2
                        )
                    }

                    val instituteValue = fragRegulaMerchantBinding.edtInstituteName.text.toString()
                    if (isRegisterType == 2) {
                        if (TextUtils.isEmpty(instituteValue)) {
                            showErrorView("Please enter institute name")
                        } else {
                            val institutePart: RequestBody =
                                RequestBody.create(MultipartBody.FORM, instituteValue)

                            merchantVM.registerRegularUsingDetails(
                                namePart, emailPart, dobPart, phonePart, addressPart,
                                localityPart, cityPart, statePart, pincodePart,
                                countryPart, aadharDetailPart, pancardDetailPart, identityPart,
                                panCardPart, identityPart2, refferalIDPart, fbDeviceID,
                                registerForPart, institutePart
                            )
                        }
                    } else {
                        val institutePart: RequestBody = RequestBody.create(MultipartBody.FORM, "")
                        merchantVM.registerRegularUsingDetails(
                            namePart, emailPart, dobPart, phonePart, addressPart,
                            localityPart, cityPart, statePart, pincodePart,
                            countryPart, aadharDetailPart, pancardDetailPart, identityPart,
                            panCardPart, identityPart2, refferalIDPart, fbDeviceID,
                            registerForPart, institutePart
                        )
                    }
                }
            }
        } else {
            if (TextUtils.isEmpty(fullname)) {
                showErrorView("Enter full name")
            } else if (isRegisterType == 0) {
                showErrorView("Please Select Register for")
            } else if (TextUtils.isEmpty(dob)) {
                showErrorView("Enter your date of birth")
            } else if (TextUtils.isEmpty(email)) {
                showErrorView("Enter your email address")
            } else if (!validateEmail(email)) {
                showErrorView("Enter valid email")
            } else if (TextUtils.isEmpty(phone)) {
                showErrorView("Enter your 10 digit mobile number")
            } else if (phone.length < 10) {
                showErrorView("Enter valid mobile number")
            } else if (TextUtils.isEmpty(aadharDetail)) {
                showErrorView("Enter your aadhar number")
            } else if (aadharDetail.length < 12) {
                showErrorView("Enter valid aadhar number")
            } else if (TextUtils.isEmpty(panCardDetail)) {
                showErrorView("Enter your pancard detail")
            } else if (panCardDetail.length < 10) {
                showErrorView("Enter valid pancard detail")
            } else if (identityImage.isNullOrEmpty()) {
                showErrorView("Attach your aadhar card image")
            } else if (pancardImage.isNullOrEmpty()) {
                showErrorView("Attach your pancard image")
            } else if (TextUtils.isEmpty(address)) {
                showErrorView("Enter your address")
            } else if (TextUtils.isEmpty(edtLocality)) {
                showErrorView("Enter locality here")
            } else if (TextUtils.isEmpty(edtCity)) {
                showErrorView("Enter city here")
            } else if (TextUtils.isEmpty(edtState)) {
                showErrorView("Enter state here")
            } else if (TextUtils.isEmpty(edtPincode)) {
                showErrorView("Enter pincode here")
            } else if (edtPincode.length < 6) {
                onFailure("Enter 6 digit pincode here")
            } else if (TextUtils.isEmpty(edtCountry)) {
                showErrorView("Enter country here")
            } else if (!isTermAccepted) {
                showErrorView("Please accept terms & condition of Notebook Store app")
            } else {
                Log.e("dob server", dateForServer!!)
                val namePart: RequestBody = RequestBody.create(MultipartBody.FORM, fullname)
                val emailPart: RequestBody = RequestBody.create(MultipartBody.FORM, email)
                val dobPart: RequestBody = RequestBody.create(MultipartBody.FORM, dateForServer!!)
                val phonePart: RequestBody = RequestBody.create(MultipartBody.FORM, phone)
                val aadharDetailPart: RequestBody =
                    RequestBody.create(MultipartBody.FORM, aadharDetail)
                val pancardDetailPart: RequestBody = RequestBody.create(
                    MultipartBody.FORM,
                    panCardDetail
                )
                val addressPart: RequestBody = RequestBody.create(MultipartBody.FORM, address)
                val refferalIDPart: RequestBody = RequestBody.create(MultipartBody.FORM, refferalID)

                val localityPart: RequestBody = RequestBody.create(MultipartBody.FORM, edtLocality)
                val cityPart: RequestBody = RequestBody.create(MultipartBody.FORM, edtCity)
                val statePart: RequestBody = RequestBody.create(MultipartBody.FORM, edtState)
                val pincodePart: RequestBody = RequestBody.create(MultipartBody.FORM, edtPincode)
                val countryPart: RequestBody = RequestBody.create(MultipartBody.FORM, edtCountry)
                val fbDeviceID: RequestBody = RequestBody.create(
                    MultipartBody.FORM,
                    notebookPrefs.firebaseDeviceID!!
                )
                val registerForPart: RequestBody = RequestBody.create(
                    MultipartBody.FORM,
                    isRegisterType.toString()
                )
                var panCardPart: MultipartBody.Part? = null
                var identityPart: MultipartBody.Part? = null
                var identityPart2: MultipartBody.Part? = null

                if (pancardImage != userData?.pancardimage) {
                    val imgFilePan = File(pancardImage!!.toUri().getAppFilePath(mContext)!!)
                    val requestFilePan =
                        RequestBody.create("image/*".toMediaTypeOrNull(), imgFilePan)
                    panCardPart = MultipartBody.Part.createFormData(
                        "pancardimage",
                        imgFilePan.name,
                        requestFilePan
                    )
                }

                if (identityImage != getIdentityImageFromServer(0)) {
                    val imgFileAadhar =
                        File(identityImage!!.toUri().getAppFilePath(mContext)!!)
                    val requestFileAadhar =
                        RequestBody.create("image/*".toMediaTypeOrNull(), imgFileAadhar)
                    identityPart = MultipartBody.Part.createFormData(
                        "identity_image",
                        imgFileAadhar.name,
                        requestFileAadhar
                    )
                }

                if (identityImage2 != getIdentityImageFromServer(1)) {
                    val imgFileAadhar2 =
                        File(identityImage2!!.toUri().getAppFilePath(mContext)!!)
                    val requestFileAadhar2 =
                        RequestBody.create("image/*".toMediaTypeOrNull(), imgFileAadhar2)
                    identityPart2 = MultipartBody.Part.createFormData(
                        "identity_image2",
                        imgFileAadhar2.name,
                        requestFileAadhar2
                    )
                }

                val instituteValue = fragRegulaMerchantBinding.edtInstituteName.text.toString()
                if (isRegisterType == 2) {
                    if (TextUtils.isEmpty(instituteValue)) {
                        showErrorView("Please enter institute name")
                    } else {
                        val institutePart: RequestBody =
                            RequestBody.create(MultipartBody.FORM, instituteValue)

                        merchantVM.registerRegularUsingDetails(
                            namePart, emailPart, dobPart, phonePart, addressPart,
                            localityPart, cityPart, statePart, pincodePart,
                            countryPart, aadharDetailPart, pancardDetailPart, identityPart,
                            panCardPart, identityPart2, refferalIDPart, fbDeviceID,
                            registerForPart, institutePart
                        )
                    }
                } else {
                    val institutePart: RequestBody = RequestBody.create(MultipartBody.FORM, "")
                    merchantVM.registerRegularUsingDetails(
                        namePart, emailPart, dobPart, phonePart, addressPart,
                        localityPart, cityPart, statePart, pincodePart,
                        countryPart, aadharDetailPart, pancardDetailPart, identityPart,
                        panCardPart, identityPart2, refferalIDPart, fbDeviceID,
                        registerForPart, institutePart
                    )
                }
            }
        }
    }


    fun getIdentityImageFromServer(index: Int): String? {
        val identityImageFromServer = userData?.identity_image
        if (!identityImageFromServer.isNullOrEmpty()) {
            val items = Arrays.asList(identityImageFromServer.split("\\s*,\\s*")).toString()
            val data = items.replace("[[", "[").replace("]]", "]")
            try {
                val jsonArrayList = JSONArray(data)
                return jsonArrayList[index].toString()
            } catch (exception: JSONException) {
                return null
            }
        }
        return null
    }

    override fun getRegisterType(data: String, type: Int) {
        fragRegulaMerchantBinding.edtSeletRegisterType.setText(data)
        isRegisterType = type

        notebookPrefs.merchantRegisterFor = type
        if (isRegisterType == 2) {
            fragRegulaMerchantBinding.clInstituteName.visibility = View.VISIBLE
        } else {
            fragRegulaMerchantBinding.clInstituteName.visibility = View.GONE
        }
    }

}
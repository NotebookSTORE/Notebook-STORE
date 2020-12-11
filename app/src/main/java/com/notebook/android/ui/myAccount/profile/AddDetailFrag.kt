package com.notebook.android.ui.myAccount.profile

import android.Manifest
import android.app.Activity
import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.bumptech.glide.Glide
import com.max.ecomaxgo.maxpe.view.flight.utility.getUserImageFullPath
import com.max.ecomaxgo.maxpe.view.flight.utility.showPermissionExplaination
import com.max.ecomaxgo.maxpe.view.flight.utility.toastShow
import com.notebook.android.R
import com.notebook.android.data.db.entities.User
import com.notebook.android.data.preferences.NotebookPrefs
import com.notebook.android.databinding.FragmentAddDetailBinding
import com.notebook.android.listener.GetSelectIntentListener
import com.notebook.android.ui.auth.responseListener.OtpVerificationListener
import com.notebook.android.ui.auth.responseListener.SuccessVerificationListener
import com.notebook.android.ui.bottomSheet.TakePhotoFromCamORGallerySheet
import com.notebook.android.ui.dashboard.factory.DashboardViewModelFactory
import com.notebook.android.ui.dashboard.listener.UserProfileUpdateListener
import com.notebook.android.ui.dashboard.viewmodel.DashboardViewModel
import com.notebook.android.ui.myOrder.OrderSummaryPage
import com.notebook.android.ui.popupDialogFrag.LoadingDialog
import com.notebook.android.ui.popupDialogFrag.VerificationPopupDialog
import com.notebook.android.ui.popupDialogFrag.VerificationSuccesDialog
import com.notebook.android.utility.Constant
import com.notebook.android.utility.Constant.DATE_FORMAT
import com.notebook.android.utility.Constant.DATE_FORMAT_SERVER
import com.notebook.android.utility.Constant.MERCHANT_IMAGE_PATH
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.android.synthetic.main.fragment_add_detail.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class AddDetailFrag : Fragment(), KodeinAware, View.OnClickListener, GetSelectIntentListener,
    UserProfileUpdateListener, OtpVerificationListener, SuccessVerificationListener {

    private lateinit var addDetailBinding: FragmentAddDetailBinding
    override val kodein by kodein()
    private val viewModelFactory: ProfileVMFactory by instance()
    private val profileVM: ProfileVM by lazy{
        ViewModelProvider(this, viewModelFactory).get(ProfileVM::class.java)
    }

    private var imageUri:Uri ?= null
    var imageFile: File ?= null
    private var genderValue = "male"
    //Date Select View....
    private lateinit var myCalendar: Calendar
    private lateinit var dateDOB: DatePickerDialog.OnDateSetListener
    private lateinit var email:String
    private var imgFile:File ?= null
    private var serverImgUrl:String ?= null
    private val notebookPrefs by lazy {
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
    }

    private lateinit var userData:User

    companion object{
        const val GALLERY_REQUEST_CODE = 5011
        const val CAMERA_REQUEST_CODE = 5021

        const val MULTIPLE_PERMISSIONS = 1110
        const val CAMERA_PERMISSION_REQUEST_CODE = 1120
        const val STORAGE_PERMISSION_REQUEST_CODE = 1130
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        addDetailBinding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_add_detail, container, false)
        addDetailBinding.lifecycleOwner = this
        profileVM.profileUpdateListener = this

        myCalendar = Calendar.getInstance();
        dateDOB = DatePickerDialog.OnDateSetListener {view, year, monthOfYear, dayOfMonth ->
            myCalendar.set(Calendar.YEAR, year)
            myCalendar.set(Calendar.MONTH, monthOfYear)
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateLabelFrom()
        }

        //success toast layout initialization here....
        val successToastLayout:View = inflater.inflate(R.layout.custom_toast_layout,
            addDetailBinding.root.findViewById(R.id.custom_toast_layout) as? ViewGroup)
        successToastTextView= (successToastLayout.findViewById(R.id.custom_toast_message) as TextView)
        successToast = Toast(mContext)
        successToast.setView(successToastLayout)
        successToast.setDuration(Toast.LENGTH_SHORT)
        successToast.setGravity(OrderSummaryPage.GRAVITY_BOTTOM, 0, 80)

        //error toast layout here....
        val errorToastLayout:View = inflater.inflate(R.layout.error_custom_toast_layout,
            addDetailBinding.root.findViewById(R.id.custom_toast_error_layout) as? ViewGroup)
        errorToastTextView = (errorToastLayout.findViewById(R.id.custom__error_toast_message) as TextView)
        errorToast = Toast(mContext)
        errorToast.setView(errorToastLayout)
        errorToast.setDuration(Toast.LENGTH_SHORT)
        errorToast.setGravity(OrderSummaryPage.GRAVITY_BOTTOM, 0, 80)

        profileVM.getUserData().observe(viewLifecycleOwner, Observer {user ->
            if(user != null){
                userData = user

                if(user.name.isNullOrEmpty()){
                    addDetailBinding.edtFullName.setText(user.username?:"User Name")
                }else{
                    addDetailBinding.edtFullName.setText(user.name)
                }
                addDetailBinding.edtDOB.setText(user.dob)
                addDetailBinding.edtEmail.setText(user.email)
                addDetailBinding.edtMobileNumber.setText(user.phone)
                email = user.email!!

                if (!TextUtils.isEmpty(addDetailBinding.edtEmail.text.toString())) {
                    addDetailBinding.edtEmail.isFocusable = false
                }

                serverImgUrl = user.profile_image
                notebookPrefs.isVerified = user.is_verified?:0

                val sdf = SimpleDateFormat(DATE_FORMAT_SERVER, Locale.US)
                val sdfFieldFormat = SimpleDateFormat(DATE_FORMAT, Locale.US)
                dateForServer = user.dob
                if(!dateForServer.isNullOrEmpty()){
                    val date = sdf.parse(dateForServer!!)
                    addDetailBinding.edtDOB.setText(sdfFieldFormat.format(date!!))
                }

                if(notebookPrefs.loginType.equals(Constant.GOOGLE_LOGIN, true)){
                    Glide.with(this)
                        .load(user.profile_image?:"")
                        .placeholder(R.drawable.my_account_profile)
                        .into(addDetailBinding.imgUserProfile)
                }else if(notebookPrefs.loginType.equals(Constant.FACEBOOK_LOGIN, true)){
                    Glide.with(this)
                        .load(user.profile_image?:"")
                        .placeholder(R.drawable.my_account_profile)
                        .into(addDetailBinding.imgUserProfile)
                }else{
                    if(!user.profile_image.isNullOrEmpty()){
                        Glide.with(this)
                            .load(getUserImageFullPath(user.profile_image?:""))
                            .placeholder(R.drawable.my_account_profile)
                            .into(addDetailBinding.imgUserProfile)
                    }else{
                        addDetailBinding.imgUserProfile.setImageResource(R.drawable.my_account_profile)
                    }
                }

                if(user.gender.equals("male", true)){
                    addDetailBinding.tvGenderMale.background = ContextCompat.getDrawable(mContext, R.drawable.user_profile_gender_select)
                    addDetailBinding.tvGenderMale.setTextColor(ContextCompat.getColor(mContext, R.color.colorPrimary))

                    addDetailBinding.tvGenderFemale.background = ContextCompat.getDrawable(mContext, R.drawable.user_profile_gender_unselect)
                    addDetailBinding.tvGenderFemale.setTextColor(ContextCompat.getColor(mContext, R.color.colorLightGrey))
                    genderValue = user.gender!!
                }else{
                    addDetailBinding.tvGenderFemale.background = ContextCompat.getDrawable(mContext, R.drawable.user_profile_gender_select)
                    addDetailBinding.tvGenderFemale.setTextColor(ContextCompat.getColor(mContext, R.color.colorPrimary))

                    addDetailBinding.tvGenderMale.background = ContextCompat.getDrawable(mContext, R.drawable.user_profile_gender_unselect)
                    addDetailBinding.tvGenderMale.setTextColor(ContextCompat.getColor(mContext, R.color.colorLightGrey))
                    genderValue = user.gender?:""
                }
            }
        })
        return addDetailBinding.root
    }

    private lateinit var navController: NavController
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = Navigation.findNavController(view)
        addDetailBinding.tvGenderMale.setOnClickListener(this)
        addDetailBinding.tvGenderFemale.setOnClickListener(this)
        addDetailBinding.edtDOB.setOnClickListener(this)
        addDetailBinding.btnFinishUpdate.setOnClickListener(this)
        addDetailBinding.imgAddPhoto.setOnClickListener(this)

        profileVM.profileImageRemoveData.observe(viewLifecycleOwner,
            Observer {
            if(it){
                serverImgUrl = ""
                addDetailBinding.imgUserProfile.setImageBitmap(null)
                addDetailBinding.imgUserProfile.setImageResource(R.drawable.ic_user_profile)
            }
        })
    }

    private fun takePictureFromGallery(){
        try {
            val galleryIntent = Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            )
            startActivityForResult(galleryIntent,
                GALLERY_REQUEST_CODE
            )
        } catch (e: Exception) {
            errorToastTextView.text = "Image Size is too large"
            errorToast.show()
        }
    }

    private fun getPhotoSheet(){
        val photoDialogFragment = TakePhotoFromCamORGallerySheet()
        photoDialogFragment.setPhotoFromListener(this)
        photoDialogFragment.show(mActivity.supportFragmentManager, "Photo Sheet Dialog")
    }

    override fun getValue(photoFrom: String) {
        if(photoFrom.equals("gallery")){
            takePictureFromGallery()
        }else if(photoFrom.equals("camera")){
            checkCameraPermission()
        }else{
            profileVM.profileImageRemove(email)
            addDetailBinding.imgUserProfile.setImageBitmap(null)
            addDetailBinding.imgUserProfile.setImageResource(R.drawable.ic_user_profile)
            serverImgUrl = ""
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
                    ) -> mContext.showPermissionExplaination(
                        getString(
                            R.string.camera_permission_explanation
                        )
                    ) {
                        requestPermissions(
                            arrayOf(
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.CAMERA
                            ),
                            CAMERA_REQUEST_CODE
                        )
                    }
                    else -> requestPermissions(
                        arrayOf(
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
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
            GALLERY_REQUEST_CODE -> if (null != data && resultCode == Activity.RESULT_OK) {
                try {
                    val imageUri = data.data?:Uri.EMPTY
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
                    if (resultCode == RESULT_OK) {
                        val resultUri = result.uri
                        imageUri = resultUri
                        Glide.with(mActivity).load(imageUri).into(addDetailBinding.imgUserProfile)
                        try {
                            imageFile = File(resultUri.path.toString())

                        } catch (e: java.lang.NullPointerException) {
                            Toast.makeText(mActivity, "Image not found",
                                Toast.LENGTH_SHORT)
                                .show()
                        }
                    } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                        val error = result.error
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

    var dateForServer:String ?= null
    private fun updateLabelFrom() {
        val sdf = SimpleDateFormat(DATE_FORMAT, Locale.US)
        dateForServer = SimpleDateFormat(DATE_FORMAT_SERVER, Locale.US).format(myCalendar.time)
        addDetailBinding.edtDOB.setText(sdf.format(myCalendar.time))
    }

    override fun onClick(v: View?) {
        when(v){

            addDetailBinding.imgAddPhoto -> {
                checkCameraPermission()
            }

            addDetailBinding.tvGenderMale -> {
                addDetailBinding.tvGenderMale.background = ContextCompat.getDrawable(mContext, R.drawable.user_profile_gender_select)
                addDetailBinding.tvGenderMale.setTextColor(ContextCompat.getColor(mContext, R.color.colorPrimary))

                addDetailBinding.tvGenderFemale.background = ContextCompat.getDrawable(mContext, R.drawable.user_profile_gender_unselect)
                addDetailBinding.tvGenderFemale.setTextColor(ContextCompat.getColor(mContext, R.color.colorLightGrey))
                genderValue = "male"
            }

            addDetailBinding.tvGenderFemale -> {
                addDetailBinding.tvGenderFemale.background = ContextCompat.getDrawable(mContext, R.drawable.user_profile_gender_select)
                addDetailBinding.tvGenderFemale.setTextColor(ContextCompat.getColor(mContext, R.color.colorPrimary))

                addDetailBinding.tvGenderMale.background = ContextCompat.getDrawable(mContext, R.drawable.user_profile_gender_unselect)
                addDetailBinding.tvGenderMale.setTextColor(ContextCompat.getColor(mContext, R.color.colorLightGrey))
                genderValue = "female"
            }

            addDetailBinding.edtDOB -> {
                val datePickerDialog = DatePickerDialog(mContext, dateDOB, myCalendar.get(Calendar.YEAR),
                    myCalendar.get(Calendar.MONTH),
                    myCalendar.get(Calendar.DAY_OF_MONTH))

                datePickerDialog.apply {
                    this.datePicker.layoutMode = 1
                    this.show()
                }
            }

            addDetailBinding.btnFinishUpdate -> {
                val fullname = addDetailBinding.edtFullName.text.toString()
                val dob = addDetailBinding.edtDOB.text.toString()
                val mobile = addDetailBinding.edtMobileNumber.text.toString()

                if(TextUtils.isEmpty(fullname)){
                    errorToastTextView.text = "Please enter full name"
                    errorToast.show()
                }else if(TextUtils.isEmpty(mobile)){
                    errorToastTextView.text = "Please enter mobile number"
                    errorToast.show()
                }else if(mobile.length < 10){
                    errorToastTextView.text = "Please enter 10-digit mobile number"
                    errorToast.show()
                }else if(TextUtils.isEmpty(dob)){
                    errorToastTextView.text = "Please enter your date of birth"
                    errorToast.show()
                }else if(genderValue.isEmpty()){
                    errorToastTextView.text = "Please select gender"
                    errorToast.show()
                }else{

                    if (TextUtils.isEmpty(email)) {
                        email = edtEmail.text.toString()
                    }

                    val namePart:RequestBody = RequestBody.create(MultipartBody.FORM, fullname)
                    val emailPart:RequestBody = RequestBody.create(MultipartBody.FORM, email)
                    val dobPart:RequestBody = RequestBody.create(MultipartBody.FORM, dateForServer!!)
                    val genderPart:RequestBody = RequestBody.create(MultipartBody.FORM, genderValue)
                    val mobilePart:RequestBody = RequestBody.create(MultipartBody.FORM, mobile)
                    val userIDPart:RequestBody = RequestBody.create(MultipartBody.FORM, userData.id.toString())

                    Log.e("isVerified", " :: userdata -> ${userData.is_verified} :: preference -> ${notebookPrefs.isVerified}")

                    if(userData.phone != addDetailBinding.edtMobileNumber.text.toString()){
                        if(imageUri != null){
                            imgFile = File(imageUri?.path!!)
                            notebookPrefs.loginType = Constant.WITHOUT_SOCIAL_LOGIN
                            val requestFile = RequestBody.create("image/*".toMediaTypeOrNull(), imgFile!!)
                            val body:MultipartBody.Part = MultipartBody.Part.createFormData("profile_image",
                                imgFile!!.name, requestFile)
                            profileVM.profileUpdate(userIDPart, emailPart, namePart, body, dobPart, genderPart, mobilePart)
                        }else {
                            if(!serverImgUrl.isNullOrEmpty()){
                                profileVM.profileStringUpdate(userData.id, email, fullname,
                                    "$MERCHANT_IMAGE_PATH$serverImgUrl", dateForServer!!, genderValue, mobile)
                            }else{
                                profileVM.profileStringUpdate(userData.id, email, fullname, "",
                                    dateForServer!!, genderValue, mobile)
                            }
                            Log.e("image url", " :: ${serverImgUrl}")
                        }
                    }else{
                        if (notebookPrefs.isVerified == 0){
                            profileVM.verifyOtp(addDetailBinding.edtMobileNumber.text.toString(), userData.otp!!)
                        }else{
                            if(imageUri != null){
                                imgFile = File(imageUri?.path!!)
                                notebookPrefs.loginType = Constant.WITHOUT_SOCIAL_LOGIN
                                val requestFile = RequestBody.create("image/*".toMediaTypeOrNull(), imgFile!!)
                                val body:MultipartBody.Part = MultipartBody.Part.createFormData("profile_image",
                                    imgFile!!.name, requestFile)
                                profileVM.profileUpdate(userIDPart, emailPart, namePart, body, dobPart, genderPart, mobilePart)
                            }else {
                                if(!serverImgUrl.isNullOrEmpty()){
                                    profileVM.profileStringUpdate(userData.id, email, fullname,
                                        "$MERCHANT_IMAGE_PATH$serverImgUrl", dateForServer!!, genderValue, mobile)
                                }else{
                                    profileVM.profileStringUpdate(userData.id, email, fullname, "",
                                        dateForServer!!, genderValue, mobile)
                                }
                                Log.e("image url", " :: ${serverImgUrl}")
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onApiCallStarted() {
        loadingDialog.show(mActivity.supportFragmentManager, "Show loading dialog")
    }

    override fun onApiCallOtpVerifyStarted() {
        loadingDialog.dialog?.show()
    }

    override fun onSuccess(user: User?) {
        loadingDialog.dialog?.dismiss()
        successToastTextView.text = "Profile updated successfully"
        successToast.show()

        if(user?.imageupdated == 1){
            notebookPrefs.loginType  = Constant.WITHOUT_SOCIAL_LOGIN
            if(notebookPrefs.loginType.equals("google", true)){
                notebookPrefs.loginTypeOnImageUpdated = Constant.GOOGLE_LOGIN
            }else{
                notebookPrefs.loginTypeOnImageUpdated = Constant.FACEBOOK_LOGIN
            }
        }else{
            if(notebookPrefs.loginType.equals("google", true)){
                notebookPrefs.loginType  = Constant.GOOGLE_LOGIN
            }else{
                notebookPrefs.loginType  = Constant.FACEBOOK_LOGIN
            }
        }
        Handler().postDelayed({
            navController.popBackStack()
        }, 800)
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

    override fun onOtpSuccess(resp: User?) {
        if(resp != null){
            if (!resp.address.isNullOrEmpty()){
                notebookPrefs.defaultAddr = resp.address
            }
            notebookPrefs.userID = resp.id
            notebookPrefs.userToken = resp.token
            notebookPrefs.isVerified = resp.is_verified?:0
            notebookPrefs.walletAmount = resp.wallet_amounts
            val verificationSuccessDialog = VerificationSuccesDialog()
            verificationSuccessDialog.isCancelable = false
            val bundle = Bundle()
            bundle.putString("successText", "Mobile verified successfully")
            verificationSuccessDialog.setSuccessListener(this)
            verificationSuccessDialog.arguments = bundle
            verificationSuccessDialog.show(mActivity.supportFragmentManager, "Verification Successful !!")
        }else{
            errorToastTextView.text = "User data is not available"
            errorToast.show()
        }
        loadingDialog.dialog?.dismiss()
    }

    override fun otpVerifyWhenProfileUpdate(otp: String?) {
        loadingDialog.dialog?.dismiss()
        val verificationPopupDialog = VerificationPopupDialog()
        verificationPopupDialog.isCancelable = false
        val bundle = Bundle()
        bundle.putString("mobile", addDetailBinding.edtMobileNumber.text.toString())
        bundle.putString("otp", otp)
        verificationPopupDialog.arguments = bundle
        verificationPopupDialog.setVerificationListener(this)
        verificationPopupDialog.show(mActivity.supportFragmentManager, "Show Verification Popup !!")
    }

    override fun onNoInternetAvailable(msg: String) {
        loadingDialog.dialog?.dismiss()
        errorToastTextView.text = msg
        errorToast.show()
    }

    override fun onInvalidCredential() {
        loadingDialog.dialog?.dismiss()
        notebookPrefs.clearPreference()
        profileVM.deleteUser()
        profileVM.clearCartTableFromDB()
        navController.navigate(R.id.loginFrag)
    }

    override fun onSuccessLogout() {

    }

    override fun walletAmount(amount: String) {

    }

    override fun otpVerifyData(otpValue: String) {
        profileVM.verifyOtp(addDetailBinding.edtMobileNumber.text.toString(), otpValue)
    }

    override fun resendOtpCall(resend: Boolean) {
        val fullname = addDetailBinding.edtFullName.text.toString()
        val dob = addDetailBinding.edtDOB.text.toString()
        val mobile = addDetailBinding.edtMobileNumber.text.toString()

        if(TextUtils.isEmpty(fullname)){
            errorToastTextView.text = "Please enter full name"
            errorToast.show()
        }else if(TextUtils.isEmpty(mobile)){
            errorToastTextView.text = "Please enter mobile number"
            errorToast.show()
        }else if(mobile.length < 10){
            errorToastTextView.text = "Please enter 10-digit mobile number"
            errorToast.show()
        }else if(TextUtils.isEmpty(dob)){
            errorToastTextView.text = "Please enter your date of birth"
            errorToast.show()
        }else if(genderValue.isEmpty()){
            errorToastTextView.text = "Please select gender"
            errorToast.show()
        }else{
            val namePart:RequestBody = RequestBody.create(MultipartBody.FORM, fullname)
            val emailPart:RequestBody = RequestBody.create(MultipartBody.FORM, email)
            val dobPart:RequestBody = RequestBody.create(MultipartBody.FORM, dateForServer!!)
            val genderPart:RequestBody = RequestBody.create(MultipartBody.FORM, genderValue)
            val mobilePart:RequestBody = RequestBody.create(MultipartBody.FORM, mobile)
            val userIDPart:RequestBody = RequestBody.create(MultipartBody.FORM, userData.id.toString())

            if(imageUri != null){
                imgFile = File(imageUri?.path!!)
                notebookPrefs.loginType = Constant.WITHOUT_SOCIAL_LOGIN
                val requestFile = RequestBody.create("image/*".toMediaTypeOrNull(), imgFile!!)
                val body:MultipartBody.Part = MultipartBody.Part.createFormData("profile_image",
                    imgFile!!.name, requestFile)
                profileVM.profileUpdate(userIDPart, emailPart, namePart, body, dobPart, genderPart, mobilePart)
            }else {
                if(!serverImgUrl.isNullOrEmpty()){
                    profileVM.profileStringUpdate(userData.id, email, fullname,
                        "$MERCHANT_IMAGE_PATH$serverImgUrl", dateForServer!!, genderValue, mobile)
                }else{
                    profileVM.profileStringUpdate(userData.id, email, fullname, "",
                        dateForServer!!, genderValue, mobile)
                }
                Log.e("image url", " :: ${serverImgUrl}")

            }
        }
    }

    override fun userRegisteredSuccessfully(isSuccess: Boolean) {
        Handler().postDelayed({
            navController.popBackStack()
        }, 1200)
    }
}

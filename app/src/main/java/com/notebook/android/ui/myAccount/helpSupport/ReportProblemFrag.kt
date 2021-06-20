package com.notebook.android.ui.myAccount.helpSupport

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
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
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.bumptech.glide.Glide
import com.max.ecomaxgo.maxpe.view.flight.utility.showPermissionExplaination

import com.notebook.android.R
import com.notebook.android.data.db.entities.User
import com.notebook.android.data.preferences.NotebookPrefs
import com.notebook.android.databinding.FragmentReportProblemBinding
import com.notebook.android.listener.GetSelectIntentListener
import com.notebook.android.model.helpSupport.HelpSupportData
import com.notebook.android.ui.bottomSheet.TakePhotoFromCamORGallerySheet
import com.notebook.android.ui.myAccount.helpSupport.listener.HelpSupportListener
import com.notebook.android.ui.myAccount.profile.AddDetailFrag
import com.notebook.android.ui.myOrder.OrderSummaryPage
import com.notebook.android.ui.popupDialogFrag.ConfirmationDialog
import com.notebook.android.ui.popupDialogFrag.LoadingDialog
import com.notebook.android.ui.popupDialogFrag.UserLogoutDialog
import com.canhub.cropper.CropImage
import com.canhub.cropper.CropImageView
import com.notebook.android.utility.getAppFilePath
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.lang.Exception

class ReportProblemFrag : Fragment(), KodeinAware, HelpSupportListener, View.OnClickListener,
    GetSelectIntentListener, UserLogoutDialog.UserLoginPopupListener,
    ConfirmationDialog.ConfirmDialogDismiss {

    private lateinit var fragmentReportProblemBinding: FragmentReportProblemBinding
    override val kodein by kodein()
    private val viewModelFactory : HelpSupportVMFactory by instance()
    private val helpSupportVM: HelpSupportVM by lazy {
        ViewModelProvider(this, viewModelFactory).get(HelpSupportVM::class.java)
    }

    private val notebookPrefs: NotebookPrefs by lazy {
        NotebookPrefs(mContext)
    }

    var imageFile: File ?= null
    private var imageUri: Uri?= null
    private val loadingDialog: LoadingDialog by lazy{
        LoadingDialog()
    }
    private lateinit var navController: NavController
    private var user: User?= null

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

    companion object{
        const val GALLERY_REQUEST_CODE = 6011
        const val CAMERA_REQUEST_CODE = 6021
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        fragmentReportProblemBinding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_report_problem, container, false)
        helpSupportVM.helpSupportListener = this
        fragmentReportProblemBinding.lifecycleOwner = this

        //success toast layout initialization here....
        val successToastLayout:View = inflater.inflate(R.layout.custom_toast_layout,
            fragmentReportProblemBinding.root.findViewById(R.id.custom_toast_layout) as? ViewGroup)
        successToastTextView= (successToastLayout.findViewById(R.id.custom_toast_message) as TextView)
        successToast = Toast(mContext)
        successToast.setView(successToastLayout)
        successToast.setDuration(Toast.LENGTH_SHORT)
        successToast.setGravity(OrderSummaryPage.GRAVITY_BOTTOM, 0, 80)

        //error toast layout here....
        val errorToastLayout:View = inflater.inflate(R.layout.error_custom_toast_layout,
            fragmentReportProblemBinding.root.findViewById(R.id.custom_toast_error_layout) as? ViewGroup)
        errorToastTextView = (errorToastLayout.findViewById(R.id.custom__error_toast_message) as TextView)
        errorToast = Toast(mContext)
        errorToast.setView(errorToastLayout)
        errorToast.setDuration(Toast.LENGTH_SHORT)
        errorToast.setGravity(OrderSummaryPage.GRAVITY_BOTTOM, 0, 80)

        return fragmentReportProblemBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = Navigation.findNavController(view)
        fragmentReportProblemBinding.btnReportProbSubmit.setOnClickListener(this)
        fragmentReportProblemBinding.clReportUploadPhoto.setOnClickListener(this)
        fragmentReportProblemBinding.imgRemovePhoto.setOnClickListener(this)

        helpSupportVM.getUserData().observe(viewLifecycleOwner, Observer { user ->
            if(user != null){
                this.user = user
            }else{
                this.user = null
            }
        })
    }

    private fun showErrorView(msg:String){
        fragmentReportProblemBinding.clErrorView.visibility = View.VISIBLE
        fragmentReportProblemBinding.tvErrorText.text = msg
        fragmentReportProblemBinding.clErrorView.startAnimation(AnimationUtils.loadAnimation(mContext,R.anim.slide_down))

        Handler().postDelayed({
            fragmentReportProblemBinding.clErrorView.visibility = View.GONE
            fragmentReportProblemBinding.clErrorView.startAnimation(AnimationUtils.loadAnimation(mContext,R.anim.slide_up))
        }, 1200)
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
            showErrorView("Image Size is too large")
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
            fragmentReportProblemBinding.imgReportProblem.setImageBitmap(null)
            fragmentReportProblemBinding.clReportUploadPhoto.visibility = View.VISIBLE
            fragmentReportProblemBinding.clImageShown.visibility = View.GONE
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
                    val imageUri = data.data?:Uri.EMPTY
                    Log.e("image uri", " :: $imageUri")
                    // start cropping activity for pre-acquired image saved on the device
                    CropImage.activity(imageUri).start(mActivity, this)

                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                    showErrorView("Image not found")
                }
            }
            else -> {
                if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
                    val result = CropImage.getActivityResult(data)
                    Log.e("kdfjdl", ":: fdfdkj")
                    if (resultCode == Activity.RESULT_OK) {
                        val resultUri = result?.uri
                        imageUri = resultUri
                        fragmentReportProblemBinding.clReportUploadPhoto.visibility = View.GONE
                        fragmentReportProblemBinding.clImageShown.visibility = View.VISIBLE
                        Glide.with(mActivity).load(imageUri).into(fragmentReportProblemBinding.imgReportProblem)
                        Log.e("imageUri", " :: $imageUri")
                        try {
                            imageFile = File(resultUri?.getAppFilePath(mContext).toString())

                        } catch (e: java.lang.NullPointerException) {
                            Toast.makeText(mActivity, "Image not found",
                                Toast.LENGTH_SHORT)
                                .show()
                        }
                    } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                        val error = result?.error
                        showErrorView(error.toString())
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

    override fun onClick(p0: View?) {
        when(p0){
            fragmentReportProblemBinding.btnReportProbSubmit -> {
                val reportMsg = fragmentReportProblemBinding.edtReportProblem.text.toString()

                if (TextUtils.isEmpty(reportMsg)) {
                    showErrorView("Please type report message here....")
                } else if (imageUri == null) {
                    showErrorView("Please upload report image here....")
                } else {
                    if (user != null) {
                        val reportMsgPart: RequestBody = RequestBody.create(MultipartBody.FORM, reportMsg)
                        val userIDPart: RequestBody =
                            RequestBody.create(MultipartBody.FORM, user!!.id.toString())
                        val tokenPart: RequestBody = RequestBody.create(MultipartBody.FORM, user!!.token!!)
                        val emailPart: RequestBody = RequestBody.create(MultipartBody.FORM, user!!.email!!)
                        val namePart: RequestBody = RequestBody.create(MultipartBody.FORM, user!!.name!!)

                        imageFile = File(imageUri?.getAppFilePath(mContext)!!)
                        val requestFile =
                            RequestBody.create("image/*".toMediaTypeOrNull(), imageFile!!)
                        val body: MultipartBody.Part =
                            MultipartBody.Part.createFormData("image", imageFile!!.name, requestFile)
                        helpSupportVM.registerAppReportProblem(
                            userIDPart,
                            tokenPart,
                            emailPart,
                            namePart,
                            reportMsgPart,
                            body
                        )
                    } else {
                        val userLoginRequestPopup = UserLogoutDialog()
                        userLoginRequestPopup.isCancelable = false
                        userLoginRequestPopup.setUserLoginRequestListener(this)
                        userLoginRequestPopup.show(mActivity.supportFragmentManager, "User login request popup !!")
                    }
                }
            }

            fragmentReportProblemBinding.clReportUploadPhoto -> {
                checkCameraPermission()
            }

            fragmentReportProblemBinding.imgRemovePhoto -> {
                fragmentReportProblemBinding.clReportUploadPhoto.visibility = View.VISIBLE
                fragmentReportProblemBinding.clImageShown.visibility = View.GONE
                fragmentReportProblemBinding.imgReportProblem.setImageBitmap(null)
                imageUri = null
            }
        }
    }

    override fun onApiCallStarted() {
        loadingDialog.show(mActivity.supportFragmentManager, "Show loading dialog")
    }

    override fun onSuccesHelpSupportData(helpSupportData: HelpSupportData.HelpSupportMain) {

    }

    override fun onInvalidCredential() {
        loadingDialog.dismissAllowingStateLoss()
        notebookPrefs.clearPreference()
        helpSupportVM.deleteUser()
        helpSupportVM.clearCartTableFromDB()
        navController.navigate(R.id.loginFrag)
    }

    override fun onSuccess(msg: String) {
        loadingDialog.dismissAllowingStateLoss()
        val confirmDialog = ConfirmationDialog()
        confirmDialog.isCancelable = false
        val bundle = Bundle()
        confirmDialog.setDialogListener(this)
        bundle.putString("toastMsg", "Report register successfully !!")
        confirmDialog.arguments = bundle
        confirmDialog.show(mActivity.supportFragmentManager, "Custom Toast Popup !!")
    }

    override fun onFailure(msg: String) {
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

    override fun ondismissed() {
        Handler().postDelayed({
            navController.popBackStack()
        }, 1200)
    }

    override fun onUserAccepted(isAccept: Boolean) {
        navController.navigate(R.id.loginFrag)
    }

}

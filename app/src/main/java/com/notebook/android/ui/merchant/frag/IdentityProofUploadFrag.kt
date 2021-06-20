package com.notebook.android.ui.merchant.frag

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
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.max.ecomaxgo.maxpe.view.flight.utility.showPermissionExplaination

import com.notebook.android.R
import com.notebook.android.data.preferences.NotebookPrefs
import com.notebook.android.databinding.FragmentIdentityProofUploadBinding
import com.notebook.android.listener.GetSelectIntentListener
import com.notebook.android.model.merchant.AadharData
import com.notebook.android.ui.bottomSheet.TakePhotoFromCamORGallerySheet
import com.notebook.android.ui.myAccount.profile.AddDetailFrag
import com.canhub.cropper.CropImage
import com.canhub.cropper.CropImageView
import com.notebook.android.utility.getAppFilePath
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream

class IdentityProofUploadFrag : Fragment(), View.OnClickListener, GetSelectIntentListener {

    companion object{
        const val GALLERY_REQUEST_CODE = 5011
        const val CAMERA_REQUEST_CODE = 5021
    }

    private val notebookPrefs: NotebookPrefs by lazy {
        NotebookPrefs(mContext)
    }

    private lateinit var mContext: Context
    private lateinit var mActivity: FragmentActivity
    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
        mActivity = requireActivity()
    }

    var imageFile: File ?= null
    private lateinit var fragIdentityProofBinding:FragmentIdentityProofUploadBinding
    private lateinit var navController: NavController
    private var isFrontImageSelected = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        fragIdentityProofBinding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_identity_proof_upload, container, false)

        return fragIdentityProofBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)

        if (!notebookPrefs.aadharFrontImage.isNullOrEmpty()){
            imageUriFront = notebookPrefs.aadharFrontImage?.toUri()
            fragIdentityProofBinding.clUploadFrontImageLayout.visibility = View.GONE
            fragIdentityProofBinding.clImageShownFront.visibility = View.VISIBLE
            Glide.with(requireActivity()).load(imageUriFront).into(fragIdentityProofBinding.imgFrontAadhar)

            imageUriBack = notebookPrefs.aadharBackImage?.toUri()
            fragIdentityProofBinding.clUploadBackPhotoAadhar.visibility = View.GONE
            fragIdentityProofBinding.clImageBackShown.visibility = View.VISIBLE
            Glide.with(requireActivity()).load(imageUriBack).into(fragIdentityProofBinding.imgBackAadhar)
        }

        fragIdentityProofBinding.clUploadFrontImageLayout.setOnClickListener(this)
        fragIdentityProofBinding.clUploadBackPhotoAadhar.setOnClickListener(this)
        fragIdentityProofBinding.imgRemovePhotoFront.setOnClickListener(this)
        fragIdentityProofBinding.imgRemoveBackPhoto.setOnClickListener(this)
        fragIdentityProofBinding.btnNextIdentityProof.setOnClickListener(this)

        fragIdentityProofBinding.clCameraIdentitySelectionView.setOnClickListener(this)
        fragIdentityProofBinding.clSelectGalleryPhoto.setOnClickListener(this)
    }

    private fun showErrorView(msg:String){
        fragIdentityProofBinding.clErrorView.visibility = View.VISIBLE
        fragIdentityProofBinding.tvErrorText.text = msg
        fragIdentityProofBinding.clErrorView.startAnimation(AnimationUtils.loadAnimation(requireContext(),R.anim.slide_down))

        Handler().postDelayed({
            fragIdentityProofBinding.clErrorView.visibility = View.GONE
            fragIdentityProofBinding.clErrorView.startAnimation(AnimationUtils.loadAnimation(requireContext(),R.anim.slide_up))
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
        photoDialogFragment.show(requireActivity().supportFragmentManager, "Photo Sheet Dialog")
    }

    override fun getValue(photoFrom: String) {
        if(photoFrom.equals("gallery")){
            takePictureFromGallery()
        }else if(photoFrom.equals("camera")){
            checkCameraPermission()
        }else{
            if(isFrontImageSelected){
                imageUriFront = null
                fragIdentityProofBinding.clUploadFrontImageLayout.visibility = View.GONE
                fragIdentityProofBinding.clImageShownFront.visibility = View.VISIBLE
                fragIdentityProofBinding.imgFrontAadhar.setImageBitmap(null)
                Log.e("imageUri", " :: $imageUriFront")
            }else{
                imageUriBack = null
                fragIdentityProofBinding.clUploadBackPhotoAadhar.visibility = View.GONE
                fragIdentityProofBinding.clImageBackShown.visibility = View.VISIBLE
                fragIdentityProofBinding.imgBackAadhar.setImageBitmap(null)
                Log.e("imageUri", " :: $imageUriBack")
            }
        }
    }

    private var imageUriFront: Uri?= null
    private var imageUriBack: Uri?= null

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
                        if(isFrontImageSelected){
                            imageUriFront = resultUri
                            fragIdentityProofBinding.clUploadFrontImageLayout.visibility = View.GONE
                            fragIdentityProofBinding.clImageShownFront.visibility = View.VISIBLE
                            Glide.with(requireActivity()).load(imageUriFront).into(fragIdentityProofBinding.imgFrontAadhar)
                            Log.e("imageUri", " :: $imageUriFront")
                        }else{
                            imageUriBack = resultUri
                            fragIdentityProofBinding.clUploadBackPhotoAadhar.visibility = View.GONE
                            fragIdentityProofBinding.clImageBackShown.visibility = View.VISIBLE
                            Glide.with(requireActivity()).load(imageUriBack).into(fragIdentityProofBinding.imgBackAadhar)
                            Log.e("imageUri", " :: $imageUriBack")
                        }
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

    override fun onClick(view: View?) {
        when(view){
            fragIdentityProofBinding.clUploadFrontImageLayout -> {
                isFrontImageSelected = true
                checkCameraPermission()
            }

            fragIdentityProofBinding.clUploadBackPhotoAadhar -> {
                isFrontImageSelected = false
                checkCameraPermission()
            }

            fragIdentityProofBinding.clSelectGalleryPhoto -> {
                checkCameraPermission()
            }

            fragIdentityProofBinding.imgRemovePhotoFront -> {
                fragIdentityProofBinding.clUploadFrontImageLayout.visibility = View.VISIBLE
                fragIdentityProofBinding.clImageShownFront.visibility = View.GONE
                fragIdentityProofBinding.imgFrontAadhar.setImageBitmap(null)
                imageUriFront = null
            }

            fragIdentityProofBinding.imgRemoveBackPhoto -> {
                fragIdentityProofBinding.clUploadBackPhotoAadhar.visibility = View.VISIBLE
                fragIdentityProofBinding.clImageBackShown.visibility = View.GONE
                fragIdentityProofBinding.imgBackAadhar.setImageBitmap(null)
                imageUriBack = null
            }

            fragIdentityProofBinding.clCameraIdentitySelectionView -> {
                checkCameraPermission()
            }

            fragIdentityProofBinding.btnNextIdentityProof -> {
                if (imageUriFront == null){
                    showErrorView("Upload your front aadhar card image")
                }else if (imageUriBack == null){
                    showErrorView("Upload your back aadhar card image")
                }else if(imageUriBack != null && imageUriFront != null){
                    val aadharData = AadharData(imageUriFront.toString(), imageUriBack.toString())
                    notebookPrefs.aadharFrontImage = imageUriFront.toString()
                    notebookPrefs.aadharBackImage = imageUriBack.toString()
                    navController.previousBackStackEntry?.savedStateHandle?.set("aadharCardImageUri", Gson().toJson(aadharData))
                    navController.popBackStack()
                }else{
                    showErrorView("Upload your aadhar card image")
                }
            }
        }
    }
}

package com.notebook.android.ui.bottomSheet

import android.annotation.SuppressLint
import android.app.Dialog
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.notebook.android.R
import com.notebook.android.listener.GetSelectIntentListener

class TakePhotoFromCamORGallerySheet : BottomSheetDialogFragment(), View.OnClickListener{

    private var contentView: View? = null
    private lateinit var cvGallerySelectIntent: LinearLayout
    private lateinit var cvCameraSelectIntent: LinearLayout
    private lateinit var cvRemoveImage : LinearLayout

    private var getSelectIntentListener: GetSelectIntentListener?=null

    fun setPhotoFromListener(getSelectIntentListener: GetSelectIntentListener){
        this.getSelectIntentListener = getSelectIntentListener
    }

    override fun onClick(v: View?) {
        if(v!!.id == R.id.cvCameraSelectIntent){
            Log.e("select input ", " :: Camera")
            getSelectIntentListener!!.getValue("camera")
        }else if(v.id == R.id.cvGallerySelectIntent){
            Log.e("select input ", " :: Gallery")
            getSelectIntentListener!!.getValue("gallery")
        }else if(v.id == R.id.cvRemoveImage){
            Log.e("select input ", " :: Remove Image")
            getSelectIntentListener!!.getValue("remove")
        }
        dismiss()
    }

    //Bottom Sheet Callback
    private val mBottomSheetBehaviorCallback
            = object : BottomSheetBehavior.BottomSheetCallback() {

        override fun onStateChanged(bottomSheet: View, newState: Int) {
            if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                dismiss()
            }
        }

        override fun onSlide(bottomSheet: View, slideOffset: Float) {
            Log.e("onSlide", "onSlide $slideOffset");
        }
    }

    @SuppressLint("RestrictedApi")
    override fun setupDialog(dialog: Dialog, style: Int) {
        super.setupDialog(dialog, style)

        //Get the content View
        contentView = View.inflate(context, R.layout.take_photo_layout, null)
        dialog.setContentView(contentView!!)
        initViews()

//        maxPreference = MaxSharedPreference(context!!)
        cvGallerySelectIntent.setOnClickListener(this)
        cvCameraSelectIntent.setOnClickListener(this)
        cvRemoveImage.setOnClickListener(this)

        val params = (contentView!!.parent as View).layoutParams as CoordinatorLayout.LayoutParams
        val behavior = params.behavior
        (contentView!!.parent as View).setBackgroundColor(resources.getColor(android.R.color.transparent))

        //Set callback
        if (behavior != null) {
            if (behavior is BottomSheetBehavior<*>) {
                behavior.setBottomSheetCallback(mBottomSheetBehaviorCallback)
            }
        }
    }

    private fun initViews(){
        cvCameraSelectIntent = contentView!!.findViewById(R.id.cvCameraSelectIntent)
        cvGallerySelectIntent = contentView!!.findViewById(R.id.cvGallerySelectIntent)
        cvRemoveImage = contentView!!.findViewById(R.id.cvRemoveImage)
    }
}
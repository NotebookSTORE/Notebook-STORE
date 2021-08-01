package com.notebook.android.utility

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.widget.ImageView
import com.bumptech.glide.Glide
import java.io.File

fun Uri.getAppFilePath(context: Context): String? {
    return this.path?.replace("/my_images/Pictures", (context.getExternalFilesDirs(Environment.DIRECTORY_PICTURES).first() as File).path)
}

fun ImageView.loadImage(url:String){
    Log.d(this.javaClass.name, "loadImage() called with: url = $url")
    val temp = url.replace(" ","")
    Glide.with(this).load(temp).into(this)
}
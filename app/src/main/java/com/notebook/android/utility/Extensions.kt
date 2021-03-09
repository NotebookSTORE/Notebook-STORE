package com.notebook.android.utility

import android.content.Context
import android.net.Uri
import android.os.Environment
import java.io.File

fun Uri.getAppFilePath(context: Context): String? {
    return this.path?.replace("/my_images/Pictures", (context.getExternalFilesDirs(Environment.DIRECTORY_PICTURES).first() as File).path)
}
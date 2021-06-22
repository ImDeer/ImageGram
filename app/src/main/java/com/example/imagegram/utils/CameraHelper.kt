package com.example.imagegram.utils

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class CameraHelper(private val activity: Activity) {
    var imageUri: Uri? = null
    private val simpleDateFormat = SimpleDateFormat(
        "yyyyMMdd_HHmmss",
        Locale.US
    )
    val REQUEST_CODE = 1

    fun takeCameraPicture() {
        // open camera
        val intent =
            Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(activity.packageManager) != null) {
            val imageFile = createImageFile()
            imageUri = FileProvider.getUriForFile(
                activity, "com.example.imagegram.fileprovider", imageFile
            )
            // please Mr Intent put camera output to the passed uri
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
            activity.startActivityForResult(
                intent,
                REQUEST_CODE
            ) // request code=1 means successfull end of operation
        }
        // get photo
        // save to  firebase

    }

    private fun createImageFile(): File {
        // Create an image file name
        val storageDir = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${simpleDateFormat.format(Date())}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        )
    }
}
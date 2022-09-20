package com.dapascript.memogram.utils

import android.app.Application
import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.os.Environment
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.dapascript.memogram.R
import com.google.android.material.snackbar.Snackbar
import java.io.*
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

fun getSnackBar(context: Context, view: View, message: String, button: CardView) {
    val snackBar = Snackbar.make(
        view,
        message,
        Snackbar.LENGTH_LONG
    ).apply {
        anchorView = button
        setBackgroundTint(
            ContextCompat.getColor(
                context,
                R.color.red
            )
        )
    }
    val layoutParams = snackBar.view.layoutParams as ViewGroup.MarginLayoutParams
    layoutParams.setMargins(60, 0, 60, 60)
    snackBar.view.layoutParams = layoutParams
    snackBar.show()
}

private const val FILENAME_FORMAT = "dd-MMM-yyyy"

val timeStamp: String = SimpleDateFormat(
    FILENAME_FORMAT,
    Locale.US
).format(System.currentTimeMillis())

fun createCustomTempFile(context: Context): File {
    val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    return File.createTempFile(timeStamp, ".jpg", storageDir)
}

fun createFile(application: Application): File {
    val mediaDir = application.externalMediaDirs.firstOrNull()?.let {
        File(it, application.resources.getString(R.string.app_name)).apply { mkdirs() }
    }

    val outputDirectory = if (
        mediaDir != null && mediaDir.exists()
    ) mediaDir else application.filesDir

    return File(outputDirectory, "$timeStamp.jpg")
}

fun rotateBitmap(bitmap: Bitmap, isBackCamera: Boolean = false): Bitmap {
    val matrix = Matrix()
    return if (isBackCamera) {
        matrix.postRotate(90f)
        Bitmap.createBitmap(
            bitmap,
            0,
            0,
            bitmap.width,
            bitmap.height,
            matrix,
            true
        )
    } else {
        matrix.postRotate(-90f)
        matrix.postScale(-1f, 1f, bitmap.width / 2f, bitmap.height / 2f)
        Bitmap.createBitmap(
            bitmap,
            0,
            0,
            bitmap.width,
            bitmap.height,
            matrix,
            true
        )
    }
}

fun uriToFile(selectedImg: Uri, context: Context): File {
    val contentResolver: ContentResolver = context.contentResolver
    val myFile = createCustomTempFile(context)

    val inputStream = contentResolver.openInputStream(selectedImg) as InputStream
    val outputStream: OutputStream = FileOutputStream(myFile)
    val buf = ByteArray(1024)
    var len: Int
    while (inputStream.read(buf).also { len = it } > 0) outputStream.write(buf, 0, len)
    outputStream.close()
    inputStream.close()

    return myFile
}

fun reduceFileImage(file: File): File {
    val bitmap = BitmapFactory.decodeFile(file.path)

    var compressQuality = 100
    var streamLength: Int

    do {
        val bmpStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, compressQuality, bmpStream)
        val bmpPicByteArray = bmpStream.toByteArray()
        streamLength = bmpPicByteArray.size
        compressQuality -= 5
    } while (streamLength > 1000000)

    bitmap.compress(Bitmap.CompressFormat.JPEG, compressQuality, FileOutputStream(file))

    return file
}

fun formatDate(date: String): String {
    val currentFormat = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
    val targetFormat = "dd MMM yyyy | HH:mm"
    val timeZone = "GMT"
    val id = Locale("in", "ID")
    val currentDateFormat: DateFormat = SimpleDateFormat(currentFormat, id)
    currentDateFormat.timeZone = TimeZone.getTimeZone(timeZone)
    val targetDateFormat: DateFormat = SimpleDateFormat(targetFormat, id)
    var targetDate: String? = null

    try {
        val currentDate = currentDateFormat.parse(date)
        if (currentDate != null) {
            targetDate = targetDateFormat.format(currentDate)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return targetDate.toString()
}

fun getAddressName(context: Context, tv: TextView? = null, myLat: Double, myLong: Double) {
//    var addressName: String? = null
    var countryName: String? = null
    var cityName: String? = null
    val geocoder = Geocoder(context, Locale.getDefault())
    try {
        val addresses: List<Address> = geocoder.getFromLocation(myLat, myLong, 1)
        if (addresses.isNotEmpty()) {
//            addressName = addresses[0].getAddressLine(0)
            countryName = addresses[0].countryName
            cityName = addresses[0].locality
        }
    } catch (e: IOException) {
        e.printStackTrace()
    }
    tv?.text = String.format(context.resources.getString(R.string.location), cityName, countryName)
}

fun getAddressSnippet(context: Context, myLat: Double, myLong: Double): String {
    var countryName: String? = null
    var cityName: String? = null
    var stateName: String? = null
    val geocoder = Geocoder(context, Locale.getDefault())
    try {
        val addresses: List<Address> = geocoder.getFromLocation(myLat, myLong, 1)
        if (addresses.isNotEmpty()) {
            countryName = addresses[0].countryName
            cityName = addresses[0].locality
            stateName = addresses[0].adminArea
        }
    } catch (e: IOException) {
        e.printStackTrace()
    }
    return listOf(cityName, stateName, countryName).joinToString(", ")
}
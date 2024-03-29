package com.msd.stockaverage.viewmodel

import android.app.Application
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.SystemClock
import android.provider.MediaStore
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import com.msd.stockaverage.domain.UIEvent
import com.msd.stockaverage.util.TAG
import java.io.File
import java.io.FileOutputStream

class MainViewModel(application: Application): AndroidViewModel(
    application
){

    //TODO:: Use savedStateHandle

    private val _companyName = mutableStateOf("")
    val companyName: State<String> = _companyName

    private val _holdingQuantity = mutableStateOf("")
    val holdingQuantity: State<String> = _holdingQuantity

    private val _purchasePrice = mutableStateOf("")
    val purchasePrice: State<String> = _purchasePrice

    private val _newQuantity = mutableStateOf("")
    val newQuantity: State<String> = _newQuantity

    private val _newPurchasePrice = mutableStateOf("")
    val newPurchasePrice: State<String> = _newPurchasePrice

    private val _totalBuyPrice = mutableStateOf("")
    val totalBuyPrice: State<String> = _totalBuyPrice

    private val _totalShares = mutableStateOf("")
    val totalShares: State<String> = _totalShares

    private val _averagePricePerShare = mutableStateOf("")
    val averagePricePerShare: State<String> = _averagePricePerShare


    fun onEvent(event: UIEvent) {
        when(event) {
            is UIEvent.CompanyNameChanged -> {
                _companyName.value = event.companyName
            }
            is UIEvent.HoldingQuantityChanged -> {
                _holdingQuantity.value = event.holdingQuantity
            }

            is UIEvent.PurchasePriceChanged -> {
                _purchasePrice.value = event.purchasePrice
            }

            is UIEvent.NewQuantityChanged -> {
                _newQuantity.value = event.newQuantity
            }

            is UIEvent.NewPurchasePriceChanged -> {
                _newPurchasePrice.value = event.newPurchasePrice
            }

            is UIEvent.Calculate -> {
                val holdingQuantityInt = if (holdingQuantity.value.isEmpty()) 0 else holdingQuantity.value.toIntOrNull() ?: 0
                val newQuantityInt = if (newQuantity.value.isEmpty()) 0 else newQuantity.value.toIntOrNull() ?: 0
                val purchasePriceDouble = if (purchasePrice.value.isEmpty()) 0.0 else purchasePrice.value.toDoubleOrNull() ?: 0.0
                val newPurchasePriceDouble = if (newPurchasePrice.value.isEmpty()) 0.0 else newPurchasePrice.value.toDoubleOrNull() ?: 0.0

                val totalBuyPrice = holdingQuantityInt * purchasePriceDouble + newQuantityInt * newPurchasePriceDouble
                val totalShares = holdingQuantityInt + newQuantityInt

                _totalBuyPrice.value = String.format("%.2f", totalBuyPrice)
                _totalShares.value = totalShares.toString()
                _averagePricePerShare.value = String.format("%.2f", if (totalShares != 0) (totalBuyPrice / totalShares) else 0.0)

            }

            is UIEvent.Reset -> {
                _companyName.value = ""
                _holdingQuantity.value = ""
                _purchasePrice.value = ""
                _newQuantity.value = ""
                _newPurchasePrice.value = ""
                _totalShares.value = ""
                _totalBuyPrice.value = ""
                _averagePricePerShare.value = ""
            }
        }
    }

   fun saveScreenshot(bitmap: Bitmap, fileName: String): File {
        val dirPath = getApplication<Application>().cacheDir.path + "/screenshots"
        val dir = File(dirPath)
        if (!dir.exists()) {
            dir.mkdirs()
        }

        val file = File(dirPath, fileName)
        FileOutputStream(file).use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 85, it)
        }

       writeToGallery(getApplication(), bitmap)

       return file

    }

    private fun writeToGallery(context: Context, bitmap: Bitmap) {
        Log.i(TAG, "Writing to Gallery")
        val values = ContentValues()
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png")
        values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis())
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
        values.put(MediaStore.Images.Media.DISPLAY_NAME, "img_${SystemClock.uptimeMillis()}")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Screenshots")
            values.put(MediaStore.Images.Media.IS_PENDING, true)
        }

        val uri: Uri? = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        uri?.run {
            context.contentResolver.openOutputStream(this).use {
                bitmap.compress(Bitmap.CompressFormat.PNG, 85, it)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                values.put(MediaStore.Images.Media.IS_PENDING, false)
            }
            context.contentResolver.update(this, values, null, null)
        }
    }

}
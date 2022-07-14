package com.msd.stockaverage.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.msd.stockaverage.domain.UIEvent

class MainViewModel(private val savedStateHandle: SavedStateHandle): ViewModel(){

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
                val holdingQuantityInt = if (holdingQuantity.value.isEmpty()) 0 else holdingQuantity.value.toInt()
                val newQuantityInt = if (newQuantity.value.isEmpty()) 0 else newQuantity.value.toInt()
                val purchasePriceDouble = if (purchasePrice.value.isEmpty()) 0.0 else purchasePrice.value.toDouble()
                val newPurchasePriceDouble = if (newPurchasePrice.value.isEmpty()) 0.0 else newPurchasePrice.value.toDouble()

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




}
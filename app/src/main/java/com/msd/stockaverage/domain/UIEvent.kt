package com.msd.stockaverage.domain

sealed class UIEvent {

    data class CompanyNameChanged(val companyName: String): UIEvent()
    data class HoldingQuantityChanged (val holdingQuantity: String): UIEvent()
    data class PurchasePriceChanged(val purchasePrice: String): UIEvent()
    data class NewQuantityChanged(val newQuantity: String): UIEvent()
    data class NewPurchasePriceChanged(val newPurchasePrice: String): UIEvent()

    object Calculate: UIEvent()
    object Reset: UIEvent()


}

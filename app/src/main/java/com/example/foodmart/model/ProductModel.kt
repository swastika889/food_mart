package com.example.foodmart.model

data class ProductModel(
    var productID: String = "",
    var productName: String = "",
    var price: Double = 0.0,
    var description: String = "",
    var image: String = "",
    var category: String = "",
    var dateAdded: Long = System.currentTimeMillis()
)
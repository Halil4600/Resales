package com.example.resales.Models

data class SalesItem(
    val id: Int,
    val description: String,
    val price: Int,
    val sellerEmail: String,
    val sellerPhone: String,
    val time: Int,
    val pictureUrl: String?
) {
    constructor(
        description: String,
        price: Int,
        sellerEmail: String,
        sellerPhone: String,
        time: Int,
        pictureUrl: String?
    ) : this(-1, description, price, sellerEmail, sellerPhone, time, pictureUrl)

    override fun toString(): String = "$id  $description, ${price}kr"
}
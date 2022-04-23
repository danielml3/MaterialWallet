package com.danielml.materialwallet.priceprovider

interface PriceChangeListener {
    /*
     * This function shall be executed when the price has changes from the
     * previously fetched price
     */
    fun onPriceChange(price: Float, fiat: String)
}
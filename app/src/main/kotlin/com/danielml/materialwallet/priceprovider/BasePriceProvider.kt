package com.danielml.materialwallet.priceprovider

import java.util.ArrayList

abstract class BasePriceProvider {
    val listeners = ArrayList<PriceChangeListener>()

    var currentPrice: Float = 0f

    /*
     * Starts the loop that checks for the latest prices and notifies
     * using the listeners
     */
    abstract fun start()

    /*
     * Stops the loop that checks for the latest prices
     */
    abstract fun stop()

    /*
     * @returns the current coin price
     */
    fun getPrice(): Float {
        return currentPrice
    }

    /*
     * Adds a listener to be executed when the price changes
     */
    fun addOnPriceChangeListener(listener: PriceChangeListener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener)
        }
    }

    /*
     * Removes the given listener if found
     */
    fun removeOnPriceChangeListener(listener: PriceChangeListener) {
        if (listeners.contains(listener)) {
            listeners.remove(listener)
        }
    }
}
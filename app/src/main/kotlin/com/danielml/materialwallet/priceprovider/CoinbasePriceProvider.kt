package com.danielml.materialwallet.priceprovider

import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.URL

class CoinbasePriceProvider : BasePriceProvider() {
    companion object {
        const val FIAT_CURRENCY = "USD"
        private val API_URL = URL("https://api.coinbase.com/v2/prices/spot?currency=${FIAT_CURRENCY}")
        private const val PRICE_CHECK_DELAY = 10000L
    }

    private var priceLoop: Thread? = null

    override fun start() {
        if (priceLoop == null) {
            priceLoop = Thread {
                while (true) {
                    try {
                        val connection = API_URL.openConnection()
                        val inputStreamReader = InputStreamReader(connection.getInputStream())
                        val reader = BufferedReader(inputStreamReader)
                        var inputLine: String?
                        val fullResponse = StringBuilder()

                        while (reader.readLine().also { inputLine = it } != null) {
                            fullResponse.append(inputLine)
                        }

                        reader.close()

                        val newPrice = getPriceFromResponse(fullResponse.toString())
                        if (newPrice != currentPrice) {
                            for (i in listeners.indices) {
                                val listener = listeners[i]
                                listener.onPriceChange(newPrice, FIAT_CURRENCY)
                            }
                        }

                        currentPrice = newPrice
                    } catch (e: IOException) {
                        e.printStackTrace()
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }

                    try {
                        Thread.sleep(PRICE_CHECK_DELAY)
                    } catch (e: InterruptedException) {
                        break
                    }
                }
            }
        }

        if (!priceLoop!!.isAlive) {
            priceLoop!!.start()
        }
    }

    override fun stop() {
        priceLoop!!.interrupt()
    }

    private fun getPriceFromResponse(response: String): Float {
        val responseJson = JSONObject(response)
        val data = responseJson.getJSONObject("data")
        return data.getString("amount").toFloat()
    }
}
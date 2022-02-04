package com.danielml.materialwallet.utils

import com.danielml.materialwallet.Global
import com.danielml.materialwallet.managers.CoinManager
import org.bitcoinj.core.Coin
import org.bitcoinj.params.TestNet3Params
import java.math.BigDecimal
import java.text.DecimalFormat

class CurrencyUtils {
    companion object {
        fun toString(coin: Coin): String {
            val value = coin.toBtc()
            val unitString = Global.selectedCoin?.getUnitString()

            val format = "0.######## " + if (Global.selectedCoin?.isTestNet() == true) {
                "t$unitString"
            } else {
                unitString
            }
            return DecimalFormat(format).format(value)
        }

        fun toString(satoshis: Long): String {
            return toString(Coin.ofSat(satoshis))
        }

        fun toString(btc: BigDecimal): String {
            return toString(Coin.ofBtc(btc))
        }

        fun toNumericString(coin: Coin): String {
            val value = coin.toBtc()
            val format = "0.########"
            return DecimalFormat(format).format(value)
        }

        fun toNumericString(satoshis: Long): String {
            return toNumericString(Coin.ofSat(satoshis))
        }

        fun toNumericString(btc: BigDecimal): String {
            return toNumericString(Coin.ofBtc(btc))
        }
    }
}
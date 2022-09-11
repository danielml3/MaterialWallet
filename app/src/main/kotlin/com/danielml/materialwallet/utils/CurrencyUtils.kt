package com.danielml.materialwallet.utils

import com.danielml.materialwallet.Global
import org.bitcoinj.core.Coin
import org.bitcoinj.params.TestNet3Params
import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*

class CurrencyUtils {
    companion object {
        fun toString(coin: Coin): String {
            val unit = if (Global.NETWORK_PARAMS == TestNet3Params.get()) {
                "tBTC"
            } else {
                "BTC"
            }

            return toNumericString(coin) + " $unit"
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

            return DecimalFormat(format, getSymbols()).format(value)
        }

        fun toNumericString(satoshis: Long): String {
            return toNumericString(Coin.ofSat(satoshis))
        }

        fun toNumericString(btc: BigDecimal): String {
            return toNumericString(Coin.ofBtc(btc))
        }

        private fun getSymbols(): DecimalFormatSymbols {
            val symbols = DecimalFormatSymbols(Locale.getDefault())
            symbols.decimalSeparator = '.'
            symbols.groupingSeparator = ','

            return symbols
        }
    }
}
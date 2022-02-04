package com.danielml.materialwallet.managers

import android.content.Context
import android.util.Log
import com.danielml.materialwallet.Global.Companion.TAG
import com.danielml.materialwallet.coins.AbstractCoin
import com.danielml.materialwallet.coins.Bitcoin
import com.danielml.materialwallet.coins.Litecoin

class CoinManager {
    companion object {
        private val DEFAULT_COIN = Bitcoin.get()

        fun getSupportedCoins() : List<AbstractCoin> {
            return listOf(Bitcoin.get(), Litecoin.get())
        }

        fun getSelectedCoin(context: Context) : AbstractCoin {
            val selectedCoinName = WalletDatabaseManager.getWalletInformation(context).getString(WalletDatabaseManager.selectedCoinKey)
            for (coin: AbstractCoin in getSupportedCoins()) {
                if (coin.getName() == selectedCoinName) {
                    return coin
                }
            }

            return DEFAULT_COIN
        }
    }
}
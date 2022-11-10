package com.danielml.materialwallet

import com.danielml.materialwallet.priceprovider.CoinbasePriceProvider
import org.bitcoinj.core.NetworkParameters
import org.bitcoinj.kits.WalletAppKit
import org.bitcoinj.params.MainNetParams
import java.security.MessageDigest

class Global {
    companion object {
        const val TAG: String = "MaterialWallet"

        // Define various values used when initializing a wallet
        var NETWORK_PARAMS: NetworkParameters = MainNetParams.get()
        const val SAT_PER_KB_DEF: Long = 4000

        var globalWalletKit: WalletAppKit? = null
        var allowBackPress = false
        var lastWalletBackStack = ""
        var setupFinished = false
        var walletSetupFinished = false

        val globalPriceProvider = CoinbasePriceProvider()

        const val SETUP_BACKSTACK = "setup"
        const val WALLET_BACKSTACK = "wallet"
        const val SECURITY_BACKSTACK = "security"
        const val SEND_COINS_BACKSTACK = "send_coins"
        const val RECEIVE_COINS_BACKSTACK = "receive_coins"
        const val SETTINGS_BACKSTACK = "settings"

        /*
         * @returns the sha256 of the given string
         */
        fun sha256(text: String): String {
            val bytes = text.toByteArray()
            val digest = MessageDigest.getInstance("SHA-256").digest(bytes)
            return digest.fold("") { str, it -> str + "%02x".format(it) }
        }
    }
}
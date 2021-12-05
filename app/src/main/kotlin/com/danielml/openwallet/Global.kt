package com.danielml.openwallet

import org.bitcoinj.core.NetworkParameters
import org.bitcoinj.kits.WalletAppKit
import org.bitcoinj.params.TestNet3Params
import java.security.MessageDigest

class Global {
    companion object {
        const val TAG: String = "OpenWallet"

        // Define various values used when initializing a wallet
        val NETWORK_PARAMS: NetworkParameters = TestNet3Params.get()
        const val SAT_PER_KB_DEF: Long = 4000

        var globalWalletKit: WalletAppKit? = null

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
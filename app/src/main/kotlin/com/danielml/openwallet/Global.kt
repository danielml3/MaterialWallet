package com.danielml.openwallet

import io.horizontalsystems.bitcoincore.BitcoinCore
import io.horizontalsystems.bitcoinkit.BitcoinKit
import java.security.MessageDigest
import java.text.SimpleDateFormat

class Global {
    companion object {
        const val TAG: String = "OpenWallet"

        // Define the different SharedPreferences categories
        const val SHARED_PREFS_MNEMONICS: String = "shared-prefs-mnemonics"
        const val SHARED_PREFS_MNEMONICS_LIST: String = "mnemonics-list"

        // Define various values used when initializing a wallet
        val NETWORK_TYPE: BitcoinKit.NetworkType = BitcoinKit.NetworkType.TestNet
        val SYNC_MODE: BitcoinCore.SyncMode = BitcoinCore.SyncMode.Api()
        const val MIN_CONFIRMATIONS: Int = 1
        const val PEER_SIZE: Int = 5
        const val FEE_RATE: Int = 5

        /*
         * @returns the sha256 of the given string
         */
        fun sha256(text: String): String {
            val bytes = text.toByteArray()
            val digest = MessageDigest.getInstance("SHA-256").digest(bytes)
            return digest.fold("") { str, it -> str + "%02x".format(it) }
        }

        /*
         * @returns a formatted date from the given timestamp (in seconds)
         */
        fun timestampToDate(timestamp: Long): String {
            val formatter = SimpleDateFormat.getDateTimeInstance()
            val date = java.util.Date(timestamp * 1000)
            return formatter.format(date)
        }
    }
}
package com.danielml.openwallet

import android.app.Activity
import android.content.Context
import com.danielml.openwallet.layouts.DraggableLinearLayout
import com.danielml.openwallet.managers.SettingsManager
import com.danielml.openwallet.managers.SettingsManager.Companion.MAIN_NET_SETTING_VALUE
import com.danielml.openwallet.managers.SettingsManager.Companion.NETWORK_TYPE_SETTING
import com.danielml.openwallet.managers.SettingsManager.Companion.TEST_NET_SETTING_VALUE
import com.danielml.openwallet.managers.WalletManager
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
        val SYNC_MODE: BitcoinCore.SyncMode = BitcoinCore.SyncMode.Full()
        const val MIN_CONFIRMATIONS: Int = 1
        const val PEER_SIZE: Int = 5
        const val FEE_RATE: Int = 5

        val walletManager: WalletManager = WalletManager()

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

        /*
         * @returns the bottom draggable container
         */
        fun getDraggableWalletContainer(context: Context): DraggableLinearLayout {
            return ((context as Activity).findViewById(R.id.wallet_draggable_layout) as DraggableLinearLayout)
        }

        /*
         * @returns the network type set by the user
         */
        fun getNetworkType(context: Context) : BitcoinKit.NetworkType {
            val networkTypeSetting = SettingsManager.getSetting(context, NETWORK_TYPE_SETTING, MAIN_NET_SETTING_VALUE)
            var networkType = BitcoinKit.NetworkType.MainNet

            if (networkTypeSetting == TEST_NET_SETTING_VALUE) {
                networkType = BitcoinKit.NetworkType.TestNet
            }

            return networkType
        }
    }
}
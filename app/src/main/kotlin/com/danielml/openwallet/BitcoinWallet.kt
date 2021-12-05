package com.danielml.openwallet

import android.content.Context
import io.horizontalsystems.bitcoincore.core.Bip
import io.horizontalsystems.bitcoinkit.BitcoinKit

class BitcoinWallet(context: Context, mnemonic: List<String>, private val name: String) {
    private val bitcoinKit: BitcoinKit

    init {
        val identifier = mnemonic.joinToString("").replace(" ", "")
        val walletId = Global.sha256(identifier)
        bitcoinKit =
            BitcoinKit(
                context,
                mnemonic,
                String(),
                walletId,
                Global.NETWORK_TYPE,
                Global.PEER_SIZE,
                Global.SYNC_MODE,
                Global.MIN_CONFIRMATIONS,
                Bip.BIP84
            )
        bitcoinKit.start()
    }

    /*
     * Sets the bitcoin kit listener to the given one
     */
    fun setListener(listener: BitcoinKit.Listener) {
        bitcoinKit.listener = listener
    }

    /*
     * @returns the wallet name
     */
    fun getWalletName(): String {
        return name
    }

    /*
     * @returns the bitcoin kit
     */
    fun getWalletKit(): BitcoinKit {
        return bitcoinKit
    }
}
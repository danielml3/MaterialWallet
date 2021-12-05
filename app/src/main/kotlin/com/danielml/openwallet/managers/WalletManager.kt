package com.danielml.openwallet.managers

import android.content.Context
import android.util.Log
import android.widget.LinearLayout
import com.danielml.openwallet.BitcoinWallet
import com.danielml.openwallet.Global
import com.danielml.openwallet.R
import com.danielml.openwallet.layouts.WalletCard
import com.danielml.openwallet.utils.DialogBuilder
import io.horizontalsystems.hdwalletkit.Mnemonic

class WalletManager {
    private var runningWallets: ArrayList<String> = ArrayList()
    private var runningWalletsObjects: ArrayList<BitcoinWallet> = ArrayList()

    private var lastWalletId: Int = 1

    fun createWallet(context: Context, mnemonic: String, container: LinearLayout): BitcoinWallet? {
        return createWallet(context, mnemonic.split(" "), container)
    }

    /*
     * Sets up a wallet with the given details and generates a card for the wallet
     * which gets attached automatically to the container
     *
     * @returns the BitcoinWallet object if the operation was successful
     * @returns null if the operation failed
     */
    fun createWallet(context: Context, mnemonic: List<String>, container: LinearLayout): BitcoinWallet? {
        if (runningWallets.contains(mnemonic.toString())) {
            Log.e(Global.TAG, "Tried to initialize an existing wallet")
            return null
        }

        try {
            val wallet = BitcoinWallet(context, mnemonic, "Wallet $lastWalletId")
            WalletCard(context, wallet, container)
            wallet.getWalletKit().start()

            MnemonicManager.storeMnemonic(context, mnemonic)
            runningWallets.add(mnemonic.toString())
            runningWalletsObjects.add(wallet)
            lastWalletId++

            return wallet
        } catch (e: Exception) {
            when (e) {
                is Mnemonic.MnemonicException -> {
                    DialogBuilder.buildDialog(
                        context,
                        { _, _ -> },
                        null,
                        null,
                        true,
                        R.string.invalid_mnemonic,
                        R.string.invalid_mnemonic_hint
                    ).show()
                }
                else -> {
                    e.printStackTrace()
                }
            }
        }

        return null
    }

    /*
     * Stops all bitcoin kits
     *
     * This function shall be called when a fragment is destroyed on a
     * configuration change, in order to reinitialize the wallets later
     */
    fun stopAllWallets() {
        for (wallet: BitcoinWallet in runningWalletsObjects) {
            wallet.getWalletKit().stop()
        }
    }
}
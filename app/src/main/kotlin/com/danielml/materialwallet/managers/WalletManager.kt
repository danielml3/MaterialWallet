package com.danielml.materialwallet.managers

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.FragmentActivity
import com.danielml.materialwallet.Global
import com.danielml.materialwallet.Global.Companion.TAG
import com.danielml.materialwallet.R
import com.danielml.materialwallet.fragments.WalletFragment
import org.bitcoinj.kits.WalletAppKit
import org.bitcoinj.script.Script
import org.bitcoinj.wallet.DeterministicSeed

class WalletManager {
    private var handler = Handler(Looper.getMainLooper())

    private var lastWalletNumber = 1

    /*
     * Sets up a wallet with the given details and generates a card for the wallet
     * which gets attached automatically to the container
     *
     * @returns the BitcoinWallet object if the operation was successful
     * @returns null if the operation failed
     */
    fun setupWallet(context: Context, selectedWalletId: String, mnemonic: String?): WalletAppKit? {
        try {
            val walletId: String = if (selectedWalletId.isEmpty() && mnemonic?.isNotEmpty() == true) {
                Global.sha256(mnemonic)
            } else if (selectedWalletId.isNotEmpty()) {
                selectedWalletId
            } else {
                return null
            }

            if (Global.globalWalletKit != null) {
                Log.i(TAG, "A wallet has already been created")
                return null
            }

            val walletKit = object : WalletAppKit(
                Global.NETWORK_PARAMS,
                Script.ScriptType.P2WPKH,
                null,
                context.applicationContext.dataDir,
                walletId + Global.NETWORK_PARAMS.id
            ) {
                override fun onSetupCompleted() {
                    super.onSetupCompleted()
                    val mnemonicString = wallet().keyChainSeed.mnemonicString ?: ""
                    WalletDatabaseManager.storeWalletInformation(context, mnemonicString, walletId)

                    handler.post {
                        val walletFragment = WalletFragment()

                        (context as FragmentActivity).supportFragmentManager
                            .beginTransaction()
                            .add(R.id.wallet_fragment_container, walletFragment)
                            .commit()
                    }
                }
            }

            Log.e(TAG, "Creating wallet $walletId")

            if (walletKit.isChainFileLocked) {
                Log.e(TAG, "Chain file is locked")
                return null
            }

            if (mnemonic?.isNotEmpty() == true) {
                val seed = DeterministicSeed(mnemonic, null, "", 0L)
                walletKit.restoreWalletFromSeed(seed)
            }

            walletKit.setAutoStop(false)
            walletKit.startAsync()

            lastWalletNumber++

            Global.globalWalletKit = walletKit
            return walletKit
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return null
    }
}
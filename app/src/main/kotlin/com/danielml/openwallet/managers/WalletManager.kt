package com.danielml.openwallet.managers

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.LinearLayout
import com.danielml.openwallet.Global
import com.danielml.openwallet.Global.Companion.TAG
import com.danielml.openwallet.layouts.WalletCard
import org.bitcoinj.kits.WalletAppKit
import org.bitcoinj.script.Script
import org.bitcoinj.wallet.DeterministicSeed

class WalletManager {
    private var runningWallets: ArrayList<String> = ArrayList()
    private var runningWalletsObjects: ArrayList<WalletAppKit> = ArrayList()

    private var handler = Handler(Looper.getMainLooper())

    private var lastWalletNumber = 1

    /*
     * Sets up a wallet with the given details and generates a card for the wallet
     * which gets attached automatically to the container
     *
     * @returns the BitcoinWallet object if the operation was successful
     * @returns null if the operation failed
     */
    fun setupWallet(context: Context, selectedWalletId: String, mnemonic: String?, container: LinearLayout): WalletAppKit? {
        try {
            val walletId: String = if (selectedWalletId.isEmpty() && mnemonic?.isNotEmpty() == true) {
                Global.sha256(mnemonic)
            } else if (selectedWalletId.isNotEmpty()) {
                selectedWalletId
            } else {
                return null
            }

            if (runningWallets.contains(walletId)) {
                Log.e(TAG, "Tried to initialize an existing wallet")
                return null
            }

            val walletKit = WalletAppKit(Global.NETWORK_PARAMS, Script.ScriptType.P2WPKH, null, context.applicationContext.dataDir, walletId + Global.NETWORK_PARAMS.id)
            Log.e(TAG, "Creating wallet $walletId")

            if (walletKit.isChainFileLocked) {
                Log.e(TAG, "Chain file is locked")
                return null
            }

            if (mnemonic?.isNotEmpty() == true) {
                val seed = DeterministicSeed(mnemonic, null, "", 0L)
                walletKit.restoreWalletFromSeed(seed)
            }

            walletKit.setBlockingStartup(false)
            walletKit.startAsync()

            runningWallets.add(walletId)
            runningWalletsObjects.add(walletKit)

            val walletNumber = lastWalletNumber
            Thread {
                while (walletKit.wallet() == null) {
                    //
                }

                val mnemonicString = walletKit.wallet().keyChainSeed.mnemonicString ?: ""
                WalletDatabaseManager.storeWalletInformation(context, mnemonicString, walletId)

                handler.post {
                    WalletCard(context, walletKit, "Wallet $walletNumber", container)
                }
            }.start()

            lastWalletNumber++
            return walletKit
        } catch (e: Exception) {
            e.printStackTrace()
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
        /*for (wallet: BitcoinWallet in runningWalletsObjects) {
            wallet.getWalletKit().stop()
        }*/
    }
}
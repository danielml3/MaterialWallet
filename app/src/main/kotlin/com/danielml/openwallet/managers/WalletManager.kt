package com.danielml.openwallet.managers

import android.content.Context
import android.util.Log
import android.widget.LinearLayout
import com.danielml.openwallet.BitcoinWallet
import com.danielml.openwallet.Global
import com.danielml.openwallet.R
import com.danielml.openwallet.fragments.SpecificWalletFragment
import com.danielml.openwallet.layouts.WalletCard
import com.danielml.openwallet.utils.DialogBuilder
import io.horizontalsystems.hdwalletkit.Mnemonic

class WalletManager {
    private var runningWallets: ArrayList<String> = ArrayList()
    private var runningWalletsObjects: ArrayList<BitcoinWallet> = ArrayList()
    private var runningWalletsCards: ArrayList<WalletCard> = ArrayList()

    fun createWallet(context: Context, mnemonic: String, walletName: String, container: LinearLayout): BitcoinWallet? {
        return createWallet(context, mnemonic.split(" "), walletName, container)
    }

    /*
     * Sets up a wallet with the given details and generates a card for the wallet
     * which gets attached automatically to the container
     *
     * @returns the BitcoinWallet object if the operation was successful
     * @returns null if the operation failed
     */
    fun createWallet(context: Context, mnemonic: List<String>, walletName: String, container: LinearLayout): BitcoinWallet? {
        if (runningWallets.contains(mnemonic.toString())) {
            Log.e(Global.TAG, "Tried to initialize an existing wallet")
            return null
        }

        try {
            val wallet = BitcoinWallet(context, mnemonic, walletName)
            val walletCard = WalletCard(context, wallet, container)
            wallet.getWalletKit().start()

            WalletDatabaseManager.storeWalletInformation(context, mnemonic, walletName)
            runningWallets.add(mnemonic.toString())
            runningWalletsObjects.add(wallet)
            runningWalletsCards.add(walletCard)

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
     * Reattaches all wallets to a newly created container
     *
     * This should be called after the main fragment is recreated
     */
    fun reattachAllWallets(container: LinearLayout) {
        for (walletCard: WalletCard in runningWalletsCards) {
            walletCard.reattachToContainer(container)
        }
    }

    fun removeWallet(context: Context, wallet: BitcoinWallet) {
        for ((i, savedWallet: BitcoinWallet) in runningWalletsObjects.withIndex()) {
            if (wallet == savedWallet) {
                val walletCard = runningWalletsCards[i]
                wallet.getWalletKit().stop()
                walletCard.destroy()
                WalletDatabaseManager.deleteWallet(context, SpecificWalletFragment.lastWallet!!.getMnemonic())
            }
        }
    }
}
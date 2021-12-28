package com.danielml.materialwallet.utils

import android.util.Log
import com.danielml.materialwallet.Global
import com.danielml.materialwallet.Global.Companion.TAG
import org.bitcoinj.core.*
import org.bitcoinj.kits.WalletAppKit
import org.bitcoinj.wallet.SendRequest
import org.bitcoinj.wallet.Wallet
import java.math.BigDecimal

class WalletUtils {
    companion object {
        fun calculateTransactionValue(
            walletKit: WalletAppKit,
            transaction: Transaction,
            isIncoming: Boolean
        ): BigDecimal {
            var value = BigDecimal(0)

            for (output: TransactionOutput in transaction.outputs) {
                val address = output.scriptPubKey.getToAddress(Global.NETWORK_PARAMS)
                val addressMine = walletKit.wallet().isAddressMine(address)
                if ((addressMine && isIncoming) || (!addressMine && !isIncoming)) {
                    value += output.value.toBtc()
                }
            }

            return value
        }

        fun createTransaction(wallet: Wallet, addressString: String, amount: Coin): Transaction {
            return createTransactionInternal(wallet, addressString, amount)
        }

        private fun createTransactionInternal(
            wallet: Wallet,
            addressString: String,
            amount: Coin,
            recipientPayFees: Boolean = false
        ): Transaction {
            return try {
                val address = Address.fromString(Global.NETWORK_PARAMS, addressString)
                val request = SendRequest.to(address, amount)
                request.setFeePerVkb(Coin.ofSat((Global.SAT_PER_KB_DEF)))
                request.recipientsPayFees = recipientPayFees
                request.allowUnconfirmed()

                wallet.completeTx(request)
                request.tx
            } catch (e: Exception) {
                if (!recipientPayFees) {
                    Log.e(TAG, "Failed to transact, trying with recipientPayFees")
                    createTransactionInternal(wallet, addressString, amount, true)
                } else {
                    throw e
                }
            }
        }
    }
}
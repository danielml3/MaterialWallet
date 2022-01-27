package com.danielml.materialwallet.utils

import android.util.Log
import com.danielml.materialwallet.Global
import com.danielml.materialwallet.Global.Companion.TAG
import org.bitcoinj.core.Address
import org.bitcoinj.core.Coin
import org.bitcoinj.core.Transaction
import org.bitcoinj.core.TransactionOutput
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

        fun createTransaction(wallet: Wallet, addressString: String, amount: Coin, feePerKb: Long): Transaction {
            return createTransactionInternal(wallet, addressString, amount, feePerKb)
        }

        private fun createTransactionInternal(
            wallet: Wallet,
            addressString: String,
            amount: Coin,
            feePerKb: Long,
            recipientPayFees: Boolean = false
        ): Transaction {
            return try {
                val address = Address.fromString(Global.NETWORK_PARAMS, addressString)
                val request = SendRequest.to(address, amount)
                request.setFeePerVkb(Coin.ofSat(feePerKb))
                request.recipientsPayFees = recipientPayFees
                request.allowUnconfirmed()

                wallet.completeTx(request)
                request.tx
            } catch (e: Exception) {
                if (!recipientPayFees) {
                    Log.e(TAG, "Failed to transact, trying with recipientPayFees")
                    createTransactionInternal(wallet, addressString, amount, feePerKb, true)
                } else {
                    throw e
                }
            }
        }
    }
}
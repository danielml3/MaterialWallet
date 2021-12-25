package com.danielml.openwallet.utils

import com.danielml.openwallet.Global
import org.bitcoinj.core.Transaction
import org.bitcoinj.core.TransactionOutput
import org.bitcoinj.kits.WalletAppKit

class WalletUtils {
    companion object {
        fun calculateTransactionValue(walletKit: WalletAppKit, transaction: Transaction, isIncoming: Boolean): Long {
            var value: Long = 0

            for (output: TransactionOutput in transaction.outputs) {
                val address = output.scriptPubKey.getToAddress(Global.NETWORK_PARAMS)
                val addressMine = walletKit.wallet().isAddressMine(address)
                if ((addressMine && isIncoming) || (!addressMine && !isIncoming)) {
                    value += output.value.toSat()
                }
            }

            return value
        }
    }
}
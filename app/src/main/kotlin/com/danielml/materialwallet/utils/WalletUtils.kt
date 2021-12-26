package com.danielml.materialwallet.utils

import com.danielml.materialwallet.Global
import org.bitcoinj.core.Transaction
import org.bitcoinj.core.TransactionOutput
import org.bitcoinj.kits.WalletAppKit
import java.math.BigDecimal

class WalletUtils {
    companion object {
        fun calculateTransactionValue(walletKit: WalletAppKit, transaction: Transaction, isIncoming: Boolean): BigDecimal {
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
    }
}
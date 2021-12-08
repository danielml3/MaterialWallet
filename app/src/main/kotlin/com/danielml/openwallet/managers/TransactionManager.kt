package com.danielml.openwallet.managers

import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import com.danielml.openwallet.R
import com.google.android.material.card.MaterialCardView
import io.horizontalsystems.bitcoincore.models.TransactionInfo
import io.horizontalsystems.bitcoincore.models.TransactionOutputInfo
import io.horizontalsystems.bitcoincore.models.TransactionType

class TransactionManager {
    companion object {

        /*
         * Generates transaction cards based on the given TransactionInfo list, and attaches
         * the cards to the given container
         */
        fun generateTransactionCards(layoutInflater: LayoutInflater, container: LinearLayout, transactionList: List<TransactionInfo>) {
            for (transaction: TransactionInfo in transactionList) {
                var transactionValue: Long = 0
                val transactionCard: MaterialCardView = when (transaction.type) {
                    TransactionType.Outgoing -> {
                        for (outputInfo: TransactionOutputInfo in transaction.outputs) {
                            if (!outputInfo.mine) {
                                transactionValue += outputInfo.value
                            }
                        }

                        layoutInflater.inflate(R.layout.outgoing_transaction_card, container, false) as MaterialCardView
                    }

                    TransactionType.Incoming -> {
                        for (outputInfo: TransactionOutputInfo in transaction.outputs) {
                            if (outputInfo.mine) {
                                transactionValue += outputInfo.value
                            }
                        }

                        layoutInflater.inflate(R.layout.incoming_transaction_card, container, false) as MaterialCardView
                    }

                    else -> {
                        continue
                    }
                }

                val outputTextView = transactionCard.findViewById<TextView>(R.id.transaction_output)
                outputTextView.text = transactionValue.toString()
                container.addView(transactionCard)
            }
        }
    }
}
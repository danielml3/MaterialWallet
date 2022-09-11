package com.danielml.materialwallet.layouts

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.text.format.DateFormat
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.transition.TransitionManager
import com.danielml.materialwallet.Global
import com.danielml.materialwallet.R
import com.danielml.materialwallet.utils.ClipboardUtils
import com.danielml.materialwallet.utils.CurrencyUtils
import com.danielml.materialwallet.utils.WalletUtils
import com.google.android.material.card.MaterialCardView
import org.bitcoinj.core.Transaction
import org.bitcoinj.core.TransactionInput
import org.bitcoinj.core.listeners.TransactionConfidenceEventListener
import org.bitcoinj.wallet.Wallet

@SuppressLint("ViewConstructor")
class TransactionCard(context: Context, private val initialTransaction: Transaction, root: ViewGroup): TransactionConfidenceEventListener {
    private val expandedContainer: LinearLayout
    private val confirmationsTextView: TextView
    private val view: MaterialCardView

    private var isExpanded = false

    init {
        view = (context as Activity).layoutInflater.inflate(R.layout.transaction_card, root, false) as MaterialCardView

        val walletKit = Global.globalWalletKit!!
        val dateTextView = view.findViewById<TextView>(R.id.transaction_date)
        val valueTextView = view.findViewById<TextView>(R.id.transaction_value)
        val feeTextView = view.findViewById<TextView>(R.id.transaction_fee)
        val transactionIdTextView = view.findViewById<TextView>(R.id.transaction_id)
        val transactionIcon = view.findViewById<ImageView>(R.id.transaction_icon)
        confirmationsTextView = view.findViewById(R.id.transaction_confirmations)
        expandedContainer = view.findViewById(R.id.expanded_container)
        expandedContainer.visibility = View.GONE

        val formattedDate = DateFormat.format("dd/MM/yyyy - HH:mm:ss", initialTransaction.updateTime).toString()
        var isIncoming = true

        for (input: TransactionInput in initialTransaction.inputs) {
            val address = input.connectedOutput?.scriptPubKey?.getToAddress(Global.NETWORK_PARAMS)
            if (address != null && walletKit.wallet().isAddressMine(address)) {
                isIncoming = false
            }
        }

        if (isIncoming) {
            transactionIcon.setImageResource(R.drawable.south_west_arrow)
        } else {
            transactionIcon.setImageResource(R.drawable.north_east_arrow)
        }

        if (initialTransaction.fee != null) {
            feeTextView.text =
                String.format(
                    context.getString(R.string.transaction_fee),
                    CurrencyUtils.toString(initialTransaction.fee)
                )
        } else {
            feeTextView.visibility = View.GONE
        }

        valueTextView.text =
            CurrencyUtils.toString(WalletUtils.calculateTransactionValue(walletKit, initialTransaction, isIncoming))
        dateTextView.text = formattedDate
        transactionIdTextView.text = initialTransaction.txId.toString()

        updateConfirmations(initialTransaction)

        view.setOnClickListener {
            setExpanded(!isExpanded)
        }

        view.setOnLongClickListener {
            ClipboardUtils.copyToClipboard(context, initialTransaction.txId.toString())
            Toast.makeText(context, R.string.copied_tx_id, Toast.LENGTH_SHORT).show()
            true
        }

        val transactionCard = this
        view.addOnAttachStateChangeListener(object: View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) {
                walletKit.wallet()?.addTransactionConfidenceEventListener(transactionCard)
            }

            override fun onViewDetachedFromWindow(v: View) {
                walletKit.wallet().removeTransactionConfidenceEventListener(transactionCard)
            }
        })
    }

    fun getView(): MaterialCardView {
        return view
    }

    private fun setExpanded(expanded: Boolean) {
        expandedContainer.visibility = if (expanded) {
            View.VISIBLE
        } else {
            View.GONE
        }
        TransitionManager.beginDelayedTransition(view)

        isExpanded = expanded
    }

    private fun updateConfirmations(transaction: Transaction) {
        val confirmationsNumber = transaction.confidence.depthInBlocks
        val confirmationsText = if (confirmationsNumber >= 6) {
            Global.globalWalletKit?.wallet()?.removeTransactionConfidenceEventListener(this)
            "6+"
        } else {
            confirmationsNumber.toString()
        }

        confirmationsTextView.text = confirmationsText
    }

    override fun onTransactionConfidenceChanged(wallet: Wallet?, tx: Transaction?) {
        if (tx?.txId.toString() == initialTransaction.txId.toString() && tx != null) {
            Handler(Looper.getMainLooper()).post {
                updateConfirmations(tx)
            }
        }
    }
}
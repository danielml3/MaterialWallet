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
import androidx.core.view.updateLayoutParams
import androidx.core.view.updateMargins
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
class TransactionCard(
    context: Context,
    private val initialTransaction: Transaction
) : MaterialCardView(context, null, R.attr.materialCardViewFilledStyle), TransactionConfidenceEventListener {
    private val walletKit = Global.globalWalletKit!!
    private var expandedContainer: LinearLayout? = null
    private var confirmationsTextView: TextView? = null

    private var isExpanded = false

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        updateLayoutParams<LinearLayout.LayoutParams> {
            updateMargins(20, 20, 20, 20)
        }
        
        inflate(context, R.layout.transaction_card_content, this)

        confirmationsTextView = findViewById(R.id.transaction_confirmations)
        expandedContainer = findViewById(R.id.expanded_container)

        setupBasicViews()
        setupExpandedViews()
        updateConfirmations(initialTransaction)
        setupClickListeners()
        setupConfidenceListeners()
    }

    private fun setupBasicViews() {
        val dateTextView = findViewById<TextView>(R.id.transaction_date)
        val valueTextView = findViewById<TextView>(R.id.transaction_value)
        val transactionIcon = findViewById<ImageView>(R.id.transaction_icon)
        val formattedDate =
            DateFormat.format("dd/MM/yyyy - HH:mm:ss", initialTransaction.updateTime).toString()
        val isIncoming = isIncoming()

        if (isIncoming) {
            transactionIcon.setImageResource(R.drawable.south_west_arrow)
        } else {
            transactionIcon.setImageResource(R.drawable.north_east_arrow)
        }

        valueTextView.text =
            CurrencyUtils.toString(
                WalletUtils.calculateTransactionValue(
                    walletKit,
                    initialTransaction,
                    isIncoming
                )
            )
        dateTextView.text = formattedDate
    }

    private fun setupExpandedViews() {
        val feeTextView = findViewById<TextView>(R.id.transaction_fee)
        val transactionIdTextView = findViewById<TextView>(R.id.transaction_id)
        expandedContainer?.visibility = GONE

        if (initialTransaction.fee != null) {
            feeTextView.text =
                String.format(
                    context.getString(R.string.transaction_fee),
                    CurrencyUtils.toString(initialTransaction.fee)
                )
        } else {
            feeTextView.visibility = GONE
        }

        transactionIdTextView.text = initialTransaction.txId.toString()
    }

    private fun setupConfidenceListeners() {
        val transactionCard = this
        addOnAttachStateChangeListener(object : OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) {
                walletKit.wallet()?.addTransactionConfidenceEventListener(transactionCard)
            }

            override fun onViewDetachedFromWindow(v: View) {
                walletKit.wallet().removeTransactionConfidenceEventListener(transactionCard)
            }
        })
    }

    private fun setupClickListeners() {
        setOnClickListener {
            setExpanded(!isExpanded)
        }

        setOnLongClickListener {
            ClipboardUtils.copyToClipboard(context, initialTransaction.txId.toString())
            Toast.makeText(context, R.string.copied_tx_id, Toast.LENGTH_SHORT).show()
            true
        }
    }

    private fun isIncoming(): Boolean {
        var incoming = true

        for (input: TransactionInput in initialTransaction.inputs) {
            val address = input.connectedOutput?.scriptPubKey?.getToAddress(Global.NETWORK_PARAMS)
            if (address != null && walletKit.wallet().isAddressMine(address)) {
                incoming = false
            }
        }

        return incoming
    }

    private fun setExpanded(expanded: Boolean) {
        expandedContainer?.visibility = if (expanded) {
            VISIBLE
        } else {
            GONE
        }

        isExpanded = expanded
        TransitionManager.beginDelayedTransition(rootView as ViewGroup)
    }

    private fun updateConfirmations(transaction: Transaction) {
        val confirmationsNumber = transaction.confidence.depthInBlocks
        val confirmationsText = if (confirmationsNumber >= 6) {
            Global.globalWalletKit?.wallet()?.removeTransactionConfidenceEventListener(this)
            "6+"
        } else {
            confirmationsNumber.toString()
        }

        confirmationsTextView?.text = confirmationsText
    }

    override fun onTransactionConfidenceChanged(wallet: Wallet?, tx: Transaction?) {
        if (tx?.txId.toString() == initialTransaction.txId.toString() && tx != null) {
            Handler(Looper.getMainLooper()).post {
                updateConfirmations(tx)
            }
        }
    }
}
package com.danielml.materialwallet.fragments

import android.content.DialogInterface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.core.view.updateLayoutParams
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.danielml.materialwallet.Global
import com.danielml.materialwallet.R
import com.danielml.materialwallet.layouts.NumericPad
import com.danielml.materialwallet.layouts.SlideToAction
import com.danielml.materialwallet.utils.ClipboardUtils
import com.danielml.materialwallet.utils.CurrencyUtils
import com.danielml.materialwallet.utils.DialogBuilder
import com.danielml.materialwallet.utils.WalletUtils
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.bitcoinj.core.*
import org.bitcoinj.kits.WalletAppKit
import org.bitcoinj.wallet.Wallet
import java.math.BigDecimal


class SendCoinsFragment : Fragment() {

    private var sendCoinsSlider: SlideToAction? = null
    private var clipboardAddress = ""

    private val retractSlider = DialogInterface.OnClickListener { _, _ ->
        sendCoinsSlider?.retractSlider()
    }

    private val retractSliderDismiss = DialogInterface.OnDismissListener {
        sendCoinsSlider?.retractSlider()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Global.allowBackPress = true
        Global.lastWalletBackStack = Global.SEND_COINS_BACKSTACK
        return inflater.inflate(R.layout.send_coins_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val walletKit = Global.globalWalletKit!!

        sendCoinsSlider = view.findViewById(R.id.send_coins_button)
        val targetAddressText = view.findViewById<EditText>(R.id.target_address)
        val numericPad = view.findViewById<NumericPad>(R.id.amount_numeric_pad)
        val pasteAddressCard = view.findViewById<MaterialCardView>(R.id.paste_address_card)

        targetAddressText.addTextChangedListener {
            if (clipboardAddress.isEmpty() || clipboardAddress == targetAddressText.text.toString()) {
                pasteAddressCard
                    .animate()
                    .setDuration(250)
                    .alpha(0f)
                    .withEndAction {
                        pasteAddressCard.visibility = View.GONE
                        pasteAddressCard.alpha = 1f
                    }
                    .start()
            } else {
                pasteAddressCard.alpha = 0f
                pasteAddressCard.visibility = View.VISIBLE
                pasteAddressCard
                    .animate()
                    .setDuration(250)
                    .alpha(1f)
                    .start()
            }
        }

        sendCoinsSlider?.setOnActionTriggeredListener {
            val targetAddress = targetAddressText.text.toString()
            val amountString = numericPad.getValueString()

            val amountDecimal = BigDecimal(amountString)
            val amount = Coin.ofBtc(amountDecimal)

            try {
                val transaction = WalletUtils.createTransaction(walletKit.wallet(), targetAddress, amount)
                showTransactionPreview(walletKit, transaction, targetAddress)
            } catch (e: Exception) {
                if (context != null) {
                    when (e) {
                        is Wallet.DustySendRequested -> {
                            DialogBuilder.buildDialog(
                                context!!,
                                retractSlider,
                                null,
                                retractSliderDismiss,
                                null,
                                true,
                                R.string.amount_too_small,
                                0
                            ).show()
                        }

                        is InsufficientMoneyException -> {
                            val balance =
                                CurrencyUtils.toString(walletKit.wallet().getBalance(Wallet.BalanceType.ESTIMATED))
                            DialogBuilder.buildDialog(
                                context!!,
                                retractSlider,
                                null,
                                retractSliderDismiss,
                                null,
                                true,
                                context!!.getString(R.string.insufficient_balance),
                                (context!!.getString(R.string.current_balance) + " $balance")
                            ).show()
                        }

                        is AddressFormatException -> {
                            DialogBuilder.buildDialog(
                                context!!,
                                retractSlider,
                                null,
                                retractSliderDismiss,
                                null,
                                true,
                                R.string.invalid_address,
                                0
                            ).show()
                        }

                        else -> {
                            e.printStackTrace()
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Handler(Looper.getMainLooper()).post {
            configurePasteAddressCard()
        }
    }

    private fun configurePasteAddressCard() {
        val pasteAddressCard = view?.findViewById<MaterialCardView>(R.id.paste_address_card)
        val addressText = view?.findViewById<TextView>(R.id.address_placeholder)
        val targetAddressText = view?.findViewById<EditText>(R.id.target_address)
        val clipboardContent = ClipboardUtils.readClipboard(context!!)

        try {
            Address.fromString(Global.NETWORK_PARAMS, clipboardContent)
            addressText?.text = clipboardContent

            pasteAddressCard?.setOnClickListener {
                targetAddressText?.setText(clipboardContent)
            }

            clipboardAddress = clipboardContent

            pasteAddressCard?.visibility = if (clipboardAddress == (targetAddressText?.text?.toString() ?: "")) {
                View.GONE
            } else {
                View.VISIBLE
            }
        } catch (e: AddressFormatException) {
            clipboardAddress = ""
            pasteAddressCard?.visibility = View.GONE
        }
    }

    private fun showTransactionPreview(walletKit: WalletAppKit, transaction: Transaction, targetAddress: String) {
        if (context == null) {
            return
        }

        val dialogBuilder = MaterialAlertDialogBuilder(context!!)
        dialogBuilder.setTitle(R.string.preview_transaction)
        dialogBuilder.setPositiveButton(R.string.send_coins) { _, _ ->
            walletKit.wallet().commitTx(transaction)
            walletKit.peerGroup().broadcastTransaction(transaction)
            (context as FragmentActivity).supportFragmentManager.popBackStackImmediate()
        }
        dialogBuilder.setNegativeButton(android.R.string.cancel, retractSlider)

        val transactionPreview = layoutInflater.inflate(R.layout.transaction_preview, null, true)
        val receiverText = transactionPreview.findViewById<TextView>(R.id.transaction_receiver)
        val totalSentText = transactionPreview.findViewById<TextView>(R.id.total_sent)
        val transactionFeeText = transactionPreview.findViewById<TextView>(R.id.transaction_fee)
        val totalSpentText = transactionPreview.findViewById<TextView>(R.id.total_spent)
        val currentBalanceText = transactionPreview.findViewById<TextView>(R.id.current_balance)
        val futureBalanceText = transactionPreview.findViewById<TextView>(R.id.future_balance)

        val receiverOutput = WalletUtils.calculateTransactionValue(walletKit, transaction, false)
        val transactionFee = transaction.fee
        val currentBalance = walletKit.wallet().getBalance(Wallet.BalanceType.ESTIMATED)
        val futureBalance = currentBalance - transactionFee - Coin.ofBtc(receiverOutput)

        receiverText.text = String.format(context!!.getString(R.string.transaction_receiver, targetAddress))
        totalSentText.text =
            String.format(context!!.getString(R.string.total_sent, CurrencyUtils.toString(receiverOutput)))
        transactionFeeText.text =
            String.format(context!!.getString(R.string.transaction_fee), CurrencyUtils.toString(transactionFee))
        totalSpentText.text = String.format(
            context!!.getString(R.string.total_spent),
            CurrencyUtils.toString(receiverOutput + transactionFee.toBtc())
        )
        currentBalanceText.text =
            (context!!.getString(R.string.current_balance) + " ${CurrencyUtils.toString(currentBalance)}")
        futureBalanceText.text =
            String.format(context!!.getString(R.string.future_balance), CurrencyUtils.toString(futureBalance))

        dialogBuilder.setView(transactionPreview)

        val transactionDialog = dialogBuilder.create()
        transactionDialog.show()

        transactionPreview.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            setMargins(80, 20, 80, 20)
        }
    }
}
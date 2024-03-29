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
import com.danielml.materialwallet.barcode.BarcodeScanActivityLauncher
import com.danielml.materialwallet.layouts.SlideToAction
import com.danielml.materialwallet.listeners.PeersSyncedListener
import com.danielml.materialwallet.utils.CurrencyUtils
import com.danielml.materialwallet.utils.DialogBuilder
import com.danielml.materialwallet.utils.WalletUtils
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.bitcoinj.core.AddressFormatException
import org.bitcoinj.core.Coin
import org.bitcoinj.core.InsufficientMoneyException
import org.bitcoinj.core.Transaction
import org.bitcoinj.kits.WalletAppKit
import org.bitcoinj.wallet.Wallet
import java.lang.ArithmeticException
import java.math.BigDecimal


class SendCoinsFragment : Fragment() {

    private var sendCoinsButton: ExtendedFloatingActionButton? = null
    private var selectedFee = Global.SAT_PER_KB_DEF
    private var peerSyncListener: PeersSyncedListener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Global.allowBackPress = true
        Global.lastWalletBackStack = Global.SEND_COINS_BACKSTACK
        return inflater.inflate(R.layout.send_coins_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val walletKit = Global.globalWalletKit!!

        sendCoinsButton = view.findViewById(R.id.send_coins_button)
        val targetAddressText = view.findViewById<EditText>(R.id.target_address)
        val amountToSendText = view.findViewById<EditText>(R.id.amount_to_send)
        val sendEverythingButton = view.findViewById<FloatingActionButton>(R.id.empty_wallet_button)
        val scanAddressButton = view.findViewById<FloatingActionButton>(R.id.scan_address)

        val barcodeScanLauncher = BarcodeScanActivityLauncher()
        barcodeScanLauncher.registerScanActivity(this) { barcodes ->
            val barcode = barcodes[0]
            targetAddressText.setText(barcode)
        }

        scanAddressButton.setOnClickListener {
            barcodeScanLauncher.startScanActivity(this)
        }

        sendEverythingButton.setOnClickListener {
            amountToSendText.setText(
                CurrencyUtils.toNumericString(
                    walletKit.wallet().getBalance(Wallet.BalanceType.ESTIMATED)
                )
            )
        }

        peerSyncListener = object : PeersSyncedListener() {
            override fun onPeersSyncStatusChanged(synced: Boolean) {
                Handler(Looper.getMainLooper()).post {
                    sendCoinsButton?.isEnabled = synced
                }
            }
        }
        peerSyncListener?.register(walletKit)

        sendCoinsButton?.setOnClickListener {
            val targetAddress = targetAddressText.text.toString()
            val amountString = amountToSendText.text.toString()

            try {
                val amountDecimal = BigDecimal(amountString)
                val amount = Coin.ofBtc(amountDecimal)
                val transaction = WalletUtils.createTransaction(
                    walletKit.wallet(),
                    targetAddress,
                    amount,
                    selectedFee
                )
                showTransactionPreview(walletKit, transaction, targetAddress)
            } catch (e: Exception) {
                if (context != null) {
                    when (e) {
                        is Wallet.DustySendRequested -> {
                            DialogBuilder(requireContext())
                                .setTitle(R.string.amount_too_small)
                                .setOnPositiveButton { _, _ -> }
                                .setCancelable(true)
                                .buildDialog().show()
                        }

                        is ArithmeticException -> {
                            DialogBuilder(requireContext())
                                .setTitle(R.string.too_many_decimals)
                                .setMessage(R.string.too_many_decimals_hint)
                                .setOnPositiveButton { _, _ -> }
                                .setCancelable(true)
                                .buildDialog().show()
                        }

                        is InsufficientMoneyException -> {
                            val balance =
                                CurrencyUtils.toString(
                                    walletKit.wallet().getBalance(Wallet.BalanceType.ESTIMATED)
                                )
                            DialogBuilder(requireContext())
                                .setTitle(R.string.insufficient_balance)
                                .setMessage(resources.getString(R.string.current_balance, balance))
                                .setOnPositiveButton { _, _ -> }
                                .buildDialog().show()
                        }

                        is AddressFormatException -> {
                            DialogBuilder(requireContext())
                                .setTitle(R.string.invalid_address)
                                .setOnPositiveButton { _, _ -> }
                                .buildDialog().show()
                        }

                        is Wallet.CouldNotAdjustDownwards -> {
                            DialogBuilder(requireContext())
                                .setTitle(R.string.insufficient_balance)
                                .setMessage(R.string.insufficient_balance)
                                .setOnPositiveButton { _, _ -> }
                                .buildDialog().show()
                        }

                        else -> {
                            e.printStackTrace()
                        }
                    }
                }
            }
        }

        val feeText = view.findViewById<EditText>(R.id.network_fee_text)
        feeText.addTextChangedListener {
            if (feeText.text.isNotEmpty()) {
                updateFeeRate(feeText)
            }
        }

        feeText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus && feeText.text.isEmpty()) {
                feeText.setText((selectedFee / 1000).toInt().toString())
            }
        }

        updateFeeRate(feeText)
    }

    private fun updateFeeRate(feeText: EditText) {
        val feeRate = feeText.text.toString().toFloat()
        selectedFee = (feeRate * 1000).toLong()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        peerSyncListener?.unregister()
    }

    private fun showTransactionPreview(
        walletKit: WalletAppKit,
        transaction: Transaction,
        targetAddress: String
    ) {
        if (context == null) {
            return
        }

        val dialogBuilder = MaterialAlertDialogBuilder(requireContext())
        dialogBuilder.setTitle(R.string.preview_transaction)
        dialogBuilder.setPositiveButton(R.string.send_coins) { _, _ ->
            walletKit.wallet().commitTx(transaction)
            walletKit.peerGroup().broadcastTransaction(transaction, 1, true)
            (context as FragmentActivity).supportFragmentManager.popBackStackImmediate()
        }
        dialogBuilder.setNegativeButton(android.R.string.cancel) { _, _ -> }

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

        receiverText.text =
            String.format(requireContext().getString(R.string.transaction_receiver, targetAddress))
        totalSentText.text =
            String.format(
                requireContext().getString(
                    R.string.total_sent,
                    CurrencyUtils.toString(receiverOutput)
                )
            )
        transactionFeeText.text =
            String.format(
                requireContext().getString(R.string.transaction_fee),
                CurrencyUtils.toString(transactionFee)
            )
        totalSpentText.text = String.format(
            requireContext().getString(R.string.total_spent),
            CurrencyUtils.toString(receiverOutput + transactionFee.toBtc())
        )
        currentBalanceText.text =
            resources.getString(R.string.current_balance, CurrencyUtils.toString(currentBalance))
        futureBalanceText.text =
            String.format(
                requireContext().getString(R.string.future_balance),
                CurrencyUtils.toString(futureBalance)
            )

        dialogBuilder.setView(transactionPreview)

        val transactionDialog = dialogBuilder.create()
        transactionDialog.show()

        transactionPreview.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            setMargins(80, 20, 80, 20)
        }
    }
}
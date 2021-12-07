package com.danielml.openwallet.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import com.danielml.openwallet.BitcoinWallet
import com.danielml.openwallet.Global
import com.danielml.openwallet.R
import com.danielml.openwallet.utils.DialogBuilder
import com.google.android.material.button.MaterialButton
import io.horizontalsystems.bitcoincore.managers.SendValueErrors
import io.horizontalsystems.bitcoincore.models.TransactionDataSortType
import java.util.concurrent.atomic.AtomicReference

class SendCoinsFragment(private var wallet: BitcoinWallet?) : Fragment() {
    constructor() : this(null)

    companion object {
        var lastWallet: BitcoinWallet? = null
    }

    init {
        /*
         * Store the last wallet that used this fragment, since Android will initialize
         * this fragment again but with a null wallet after a configuration change
         */
        if (wallet == null) {
            wallet = lastWallet
        } else {
            lastWallet = wallet
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.send_coins_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val maximumSpendableText = view.findViewById<TextView>(R.id.maximum_spendable_text)
        maximumSpendableText.text = String.format(context!!.getString(R.string.maximum_spendable_text), "N/A")

        val sendCoinsButton = view.findViewById<MaterialButton>(R.id.send_coins_button)
        val targetAddressText = view.findViewById<EditText>(R.id.target_address)
        val amountText = view.findViewById<EditText>(R.id.amount_to_send)
        val maximumSpendableButton = view.findViewById<MaterialButton>(R.id.use_maximum_spendable)
        val maximumSpendable: AtomicReference<Long> = AtomicReference(0)
        amountText.isEnabled = false

        /*
         * This listener will enable the amount input when a valid address
         * is present on the address text box
         *
         * This way, the maximum spendable amount will be calculable
         */
        targetAddressText.addTextChangedListener {
            try {
                val address = targetAddressText.text.toString()
                wallet!!.getWalletKit().validateAddress(address, mapOf())

                maximumSpendable.set(wallet!!.getWalletKit().maximumSpendableValue(address, Global.FEE_RATE, mapOf()))
                maximumSpendableText.text =
                    String.format(context!!.getString(R.string.maximum_spendable_text), maximumSpendable.get())

                amountText.isEnabled = true
            } catch (e: Exception) {
                amountText.isEnabled = false
                amountText.setText("")
            }
        }

        maximumSpendableButton.setOnClickListener {
            if (amountText.isEnabled) {
                amountText.setText(maximumSpendable.get().toString())
            }
        }

        sendCoinsButton.setOnClickListener {
            val targetAddress = targetAddressText.text.toString()
            val amount = amountText.text.toString()

            // Validate the given details
            if (targetAddress.isEmpty() || amount.isEmpty()) {
                DialogBuilder.buildDialog(
                    context!!,
                    { _, _ -> },
                    null,
                    null,
                    true,
                    R.string.invalid_send_details,
                    0
                ).show()
                return@setOnClickListener
            }

            try {
                wallet!!.getWalletKit()
                    .send(targetAddress, amount.toLong(), true, Global.FEE_RATE, TransactionDataSortType.Shuffle)
                Global.getDraggableWalletContainer(context!!).expandAnimated()
            } catch (e: Exception) {
                when (e) {
                    // Let the user know if the balance is not enough
                    is SendValueErrors -> {
                        DialogBuilder.buildDialog(
                            context!!,
                            { _, _ -> },
                            null,
                            null,
                            true,
                            R.string.not_enough_coins,
                            0
                        ).show()
                    }
                }
            }
        }
    }
}
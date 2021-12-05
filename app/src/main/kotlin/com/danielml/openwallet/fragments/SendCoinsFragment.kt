package com.danielml.openwallet.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import com.danielml.openwallet.Global
import com.danielml.openwallet.R
import com.danielml.openwallet.utils.DialogBuilder
import com.google.android.material.button.MaterialButton
import org.bitcoinj.core.Address
import org.bitcoinj.core.Coin
import org.bitcoinj.core.Transaction
import org.bitcoinj.kits.WalletAppKit
import org.bitcoinj.wallet.SendRequest
import java.util.concurrent.atomic.AtomicReference


class SendCoinsFragment(private var wallet: WalletAppKit?) : Fragment() {
    companion object {
        var lastWallet: WalletAppKit? = null
    }

    constructor() : this(null)

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
            val balance = wallet!!.wallet().balance

            try {
                val addressText = targetAddressText.text.toString()
                val address = Address.fromString(Global.NETWORK_PARAMS, addressText)
                val request = SendRequest.to(address, Transaction.REFERENCE_DEFAULT_MIN_TX_FEE.multiply(3))
                request.setFeePerVkb(Coin.ofSat((Global.SAT_PER_KB_DEF)))
                wallet!!.wallet().completeTx(request)

                maximumSpendable.set(balance.toSat() - request.tx.fee.toSat())
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
                /*
                 * Create the final transaction and broadcast it to the network
                 *
                 * On this transaction, the fees are paid by the sender, so the target address will
                 * receive the same exact amount of coins as per the user's input
                 */
                val addressText = targetAddressText.text.toString()
                val address = Address.fromString(Global.NETWORK_PARAMS, addressText)
                val request = SendRequest.to(address, Coin.ofSat(amount.toLong()))
                request.setFeePerVkb(Coin.ofSat((Global.SAT_PER_KB_DEF)))

                wallet!!.wallet().sendCoins(request)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
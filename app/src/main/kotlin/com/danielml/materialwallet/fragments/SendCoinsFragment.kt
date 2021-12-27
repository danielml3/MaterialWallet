package com.danielml.materialwallet.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.danielml.materialwallet.Global
import com.danielml.materialwallet.R
import com.danielml.materialwallet.utils.DialogBuilder
import com.google.android.material.button.MaterialButton
import org.bitcoinj.core.Address
import org.bitcoinj.core.Coin
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
        Global.allowBackPress = true
        Global.lastWalletBackStack = Global.SEND_COINS_BACKSTACK
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
        maximumSpendableButton.isEnabled = false
        sendCoinsButton.isEnabled = false

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
                val request = SendRequest.to(address, Coin.ofSat(balance.toSat() - 1))
                request.setFeePerVkb(Coin.ofSat((Global.SAT_PER_KB_DEF)))
                request.recipientsPayFees = true
                wallet!!.wallet().completeTx(request)

                maximumSpendable.set(balance.toSat() - request.tx.fee.toSat())
                maximumSpendableText.text =
                    String.format(context!!.getString(R.string.maximum_spendable_text), maximumSpendable.get())

                amountText.isEnabled = true
                sendCoinsButton.isEnabled = true
                maximumSpendableButton.isEnabled = true
            } catch (e: Exception) {
                amountText.isEnabled = false
                sendCoinsButton.isEnabled = false
                maximumSpendableButton.isEnabled = false
                amountText.setText("")
                e.printStackTrace()
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

                wallet!!.wallet().completeTx(request)
                wallet!!.wallet().commitTx(request.tx)
                (context as FragmentActivity).supportFragmentManager.popBackStackImmediate()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
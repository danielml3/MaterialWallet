package com.danielml.materialwallet.fragments

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.danielml.materialwallet.Global
import com.danielml.materialwallet.R
import com.danielml.materialwallet.utils.ClipboardUtils
import com.google.android.material.button.MaterialButton
import org.bitcoinj.core.Address

class ReceiveCoinsFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Global.allowBackPress = true
        Global.lastWalletBackStack = Global.RECEIVE_COINS_BACKSTACK
        return inflater.inflate(R.layout.receive_coins_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val walletKit = Global.globalWalletKit!!
        val container = view.findViewById<LinearLayout>(R.id.address_card_container)

        for (address: Address in walletKit.wallet().issuedReceiveAddresses) {
            val addressString = address.toString()
            val addressCard = layoutInflater.inflate(R.layout.address_card, container, false)
            val addressTextView = addressCard.findViewById<TextView>(R.id.address_text)
            val copyAddressButton = addressCard.findViewById<MaterialButton>(R.id.copy_address)
            val internalContainer = addressCard.findViewById<LinearLayout>(R.id.internal_address_container)

            addressTextView.text = addressString
            copyAddressButton.setOnClickListener {
                ClipboardUtils.copyToClipboard(context!!, addressString)
            }

            if (addressString != walletKit.wallet().currentReceiveAddress().toString()) {
                internalContainer.background = ColorDrawable(context!!.getColor(R.color.gray))
            } else {
                internalContainer.background = ColorDrawable(context!!.getColor(R.color.secondaryColor))
            }

            container.addView(addressCard, 0)
        }
    }
}
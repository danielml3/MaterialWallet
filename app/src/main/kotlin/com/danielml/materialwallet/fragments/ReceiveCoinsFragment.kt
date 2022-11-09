package com.danielml.materialwallet.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.danielml.materialwallet.Global
import com.danielml.materialwallet.R
import com.danielml.materialwallet.layouts.addresscards.CurrentAddressCard
import com.danielml.materialwallet.layouts.addresscards.UsedAddressCard
import org.bitcoinj.core.Address

class ReceiveCoinsFragment : Fragment() {

    private val handler = Handler(Looper.getMainLooper())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Global.allowBackPress = true
        Global.lastWalletBackStack = Global.RECEIVE_COINS_BACKSTACK
        return inflater.inflate(R.layout.receive_coins_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val walletKit = Global.globalWalletKit!!
        val usedAddressesContainer = view.findViewById<LinearLayout>(R.id.used_address_container)
        val usedAddressesTitle = view.findViewById<TextView>(R.id.used_address_title)
        val currentAddressContainer =
            view.findViewById<LinearLayout>(R.id.current_address_container)

        usedAddressesTitle.visibility = View.GONE
        Thread {
            for (address: Address in walletKit.wallet().issuedReceiveAddresses.reversed()) {
                val isCurrentAddress =
                    (address.toString() == walletKit.wallet().currentReceiveAddress().toString())
                val container = if (isCurrentAddress) {
                    currentAddressContainer
                } else {
                    handler.postAtFrontOfQueue {
                        usedAddressesTitle.visibility = View.VISIBLE
                    }
                    usedAddressesContainer
                }

                if (context == null) {
                    break
                }

                handler.post {
                    val addressCard = if (isCurrentAddress) {
                        CurrentAddressCard(requireContext(), address)
                    } else {
                        UsedAddressCard(requireContext(), address)
                    }

                    container.addView(addressCard)
                }
            }
        }.start()
    }
}
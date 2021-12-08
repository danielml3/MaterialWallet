package com.danielml.openwallet.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import com.danielml.openwallet.BitcoinWallet
import com.danielml.openwallet.R
import com.danielml.openwallet.managers.TransactionManager

class SpecificWalletFragment(var wallet: BitcoinWallet?) : Fragment() {
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
        return inflater.inflate(R.layout.specific_wallet_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val walletContainer = view.findViewById<LinearLayout>(R.id.transactions_container)
        TransactionManager.generateTransactionCards(layoutInflater, walletContainer, lastWallet?.getWalletKit()?.transactions()?.blockingGet() ?: listOf())
    }
}
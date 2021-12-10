package com.danielml.openwallet.fragments

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.danielml.openwallet.Global
import com.danielml.openwallet.R
import com.google.android.material.button.MaterialButton
import io.horizontalsystems.hdwalletkit.Mnemonic

class CreateWalletFragment(var container: LinearLayout?) : Fragment() {
    constructor() : this(null)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (container == null) {
            return null
        }

        return inflater.inflate(R.layout.create_wallet_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (context as Activity).findViewById<TextView>(R.id.active_wallet_name)?.text = ""

        val createWalletButton = view.findViewById<MaterialButton>(R.id.perform_create_wallet_button)
        val mnemonic = Mnemonic()
        createWalletButton.setOnClickListener {
            if (Global.walletManager.createWallet(context!!, mnemonic.generate(), container!!) != null) {
                Global.getDraggableWalletContainer(context!!).expandAnimated()
            }
        }
    }
}
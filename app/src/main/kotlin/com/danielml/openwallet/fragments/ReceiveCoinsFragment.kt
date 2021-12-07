package com.danielml.openwallet.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.danielml.openwallet.R
import com.danielml.openwallet.utils.ClipboardUtils
import com.google.android.material.button.MaterialButton

class ReceiveCoinsFragment(var address: String?) : Fragment() {
    constructor() : this(null)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (address == null) {
            return null
        }

        return inflater.inflate(R.layout.receive_coins_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val addressTextView = view.findViewById<TextView>(R.id.address_text)
        val copyAddressButton = view.findViewById<MaterialButton>(R.id.copy_address_button)

        addressTextView.text = address
        copyAddressButton.setOnClickListener {
            ClipboardUtils.copyToClipboard(context!!, address!!)
        }
    }
}
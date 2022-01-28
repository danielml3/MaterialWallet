package com.danielml.materialwallet.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.danielml.materialwallet.Global
import com.danielml.materialwallet.Global.Companion.TAG
import com.danielml.materialwallet.R
import com.danielml.materialwallet.layouts.SlideToAction
import com.danielml.materialwallet.managers.WalletDatabaseManager

class SecurityFragment : Fragment() {

    private var mnemonicTextView: TextView? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Global.allowBackPress = false
        return inflater.inflate(R.layout.security_fragment, container, false)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mnemonicString = WalletDatabaseManager.getWalletInformation(context!!).getString(WalletDatabaseManager.mnemonicKey)
        val hiddenMnemonic = mnemonicString.replace(Regex("[a-zA-Z0-9\\\\s]"), "#")
        var mnemonicHidden = true

        mnemonicTextView = view.findViewById(R.id.mnemonic_textview)
        mnemonicTextView?.text = hiddenMnemonic

        val slider = view.findViewById<SlideToAction>(R.id.mnemonic_slider)
        slider.setHintText(context!!.getString(R.string.slide_to_show_hide))
        slider.setOnActionTriggeredListener {
            if (mnemonicHidden) {
                mnemonicTextView?.text = mnemonicString
                mnemonicHidden = false
            } else {
                mnemonicTextView?.text = hiddenMnemonic
                mnemonicHidden = true
            }

            slider.retractSlider()
        }
    }
}
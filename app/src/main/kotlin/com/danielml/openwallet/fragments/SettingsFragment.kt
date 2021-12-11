package com.danielml.openwallet.fragments

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.danielml.openwallet.Global
import com.danielml.openwallet.R
import com.danielml.openwallet.managers.SettingsManager
import com.danielml.openwallet.managers.SettingsManager.Companion.MAIN_NET_SETTING_VALUE
import com.danielml.openwallet.managers.SettingsManager.Companion.NETWORK_TYPE_SETTING
import com.danielml.openwallet.managers.SettingsManager.Companion.TEST_NET_SETTING_VALUE
import io.horizontalsystems.bitcoinkit.BitcoinKit

class SettingsFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.settings_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (context as Activity).findViewById<TextView>(R.id.active_wallet_name)?.text = ""

        val networkTypeRadioGroup = view.findViewById<RadioGroup>(R.id.network_type_radio_group)
        val mainNetRadioButton = view.findViewById<RadioButton>(R.id.main_net_radio_button)
        val testNetRadioButton = view.findViewById<RadioButton>(R.id.test_net_radio_button)

        networkTypeRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            var settingValue = MAIN_NET_SETTING_VALUE
            if (checkedId == testNetRadioButton.id) {
                settingValue = TEST_NET_SETTING_VALUE
            }

            SettingsManager.setSetting(context!!, NETWORK_TYPE_SETTING, settingValue)
        }

        val networkType = Global.getNetworkType(context!!)
        if (networkType == BitcoinKit.NetworkType.MainNet) {
            mainNetRadioButton.isChecked = true
        } else if (networkType == BitcoinKit.NetworkType.TestNet) {
            testNetRadioButton.isChecked = true
        }
    }
}
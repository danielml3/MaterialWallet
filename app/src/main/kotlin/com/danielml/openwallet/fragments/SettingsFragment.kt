package com.danielml.openwallet.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.danielml.openwallet.Global
import com.danielml.openwallet.R

class SettingsFragment : DialogFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Global.allowBackPress = false
        return inflater.inflate(R.layout.settings_fragment, container, false)
    }
}
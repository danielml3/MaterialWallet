package com.danielml.openwallet.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.danielml.openwallet.R
import com.danielml.openwallet.managers.WalletDatabaseManager
import com.danielml.openwallet.managers.WalletManager
import com.danielml.openwallet.utils.DialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton

class SettingsFragment : DialogFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.settings_fragment, container, false)
    }
}
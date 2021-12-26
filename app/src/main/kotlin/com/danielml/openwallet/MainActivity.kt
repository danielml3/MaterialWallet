package com.danielml.openwallet

import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.danielml.openwallet.fragments.SettingsFragment
import com.danielml.openwallet.managers.WalletDatabaseManager
import com.danielml.openwallet.managers.WalletManager
import com.danielml.openwallet.utils.DialogBuilder
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.navigation.NavigationBarView
import org.bitcoinj.params.TestNet3Params

class MainActivity : AppCompatActivity() {
    companion object {
        private var importWalletVisibility = View.VISIBLE
    }

    private val settingsFragment = SettingsFragment()

    private var walletManager = WalletManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setImportWalletVisibility(importWalletVisibility)

        val isDebuggable = (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0)
        if (isDebuggable) {
            Global.NETWORK_PARAMS = TestNet3Params.get()
        }

        val navigationBarView = findViewById<NavigationBarView>(R.id.bottom_navigation)
        navigationBarView.setOnItemSelectedListener { item ->

            when (item.itemId) {
                R.id.wallet_page -> {
                    supportFragmentManager.popBackStack(Global.lastWalletBackStack, 0)
                    true
                }

                R.id.settings_page -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_fragment_container, settingsFragment)
                        .addToBackStack(Global.SETTINGS_BACKSTACK)
                        .commit()
                    true
                }

                else -> false
            }
        }

        loadWallet()
    }

    private fun loadWallet() {
        val importWalletButton = findViewById<ExtendedFloatingActionButton>(R.id.import_wallet_button)
        importWalletButton.setOnClickListener {
            getImportWalletDialog().show()
        }

        importWalletButton.visibility = importWalletVisibility

        val walletInformation = WalletDatabaseManager.getWalletInformation(this)
        if (walletInformation.has(WalletDatabaseManager.walletIdKey)) {
            val walletId = walletInformation.getString(WalletDatabaseManager.walletIdKey)
            if (walletId.isNotEmpty()) {
                val walletKit = walletManager.setupWallet(this, walletId, "")
                if (walletKit != null) {
                    setImportWalletVisibility(View.GONE)
                }
            }
        }
    }

    /*
      * @returns an AlertDialog that allows to restore a wallet using a
      * recovery phrase (mnemonic)
      */
    private fun getImportWalletDialog(): AlertDialog {
        val importForm = layoutInflater.inflate(R.layout.import_wallet_form, null)

        return DialogBuilder.buildDialog(
            this,
            { _, _ ->
                run {
                    val mnemonicTextBox = importForm.findViewById<EditText>(R.id.mnemonic_text_box)
                    val mnemonic = mnemonicTextBox.text.toString()
                    val walletKit = walletManager.setupWallet(this, "", mnemonic)
                    if (walletKit != null) {
                        setImportWalletVisibility(View.GONE)
                    }
                }
            }, { _, _ -> }, importForm, false, R.string.import_wallet_title, R.string.import_wallet_message
        )
    }

    private fun setImportWalletVisibility(visibility: Int) {
        val importWalletButton = findViewById<ExtendedFloatingActionButton>(R.id.import_wallet_button)
        importWalletButton.visibility = visibility
        importWalletVisibility = visibility
    }

    override fun onBackPressed() {
        if (Global.allowBackPress) {
            super.onBackPressed()
        }
    }
}
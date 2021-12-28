package com.danielml.materialwallet

import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.danielml.materialwallet.fragments.SettingsFragment
import com.danielml.materialwallet.managers.WalletDatabaseManager
import com.danielml.materialwallet.managers.WalletManager
import com.danielml.materialwallet.utils.DialogBuilder
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.navigation.NavigationBarView
import org.bitcoinj.params.TestNet3Params
import java.util.*

class MainActivity : AppCompatActivity() {
    companion object {
        private var setupWalletVisibility = View.VISIBLE
    }

    private val settingsFragment = SettingsFragment()

    private var walletManager = WalletManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSetupWalletVisibility(setupWalletVisibility)

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

        importWalletButton.visibility = setupWalletVisibility

        val createWalletButton = findViewById<ExtendedFloatingActionButton>(R.id.create_wallet_button)
        createWalletButton.setOnClickListener {
            val walletKit = walletManager.setupWallet(this, Global.sha256(Date().time.toString()), null)
            if (walletKit != null) {
                setSetupWalletVisibility(View.GONE)
            }
        }

        val walletInformation = WalletDatabaseManager.getWalletInformation(this)
        if (walletInformation.has(WalletDatabaseManager.walletIdKey)) {
            val walletId = walletInformation.getString(WalletDatabaseManager.walletIdKey)
            if (walletId.isNotEmpty()) {
                val walletKit = walletManager.setupWallet(this, walletId, "")
                if (walletKit != null) {
                    setSetupWalletVisibility(View.GONE)
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
                        setSetupWalletVisibility(View.GONE)
                    }
                }
            }, { _, _ -> }, importForm, false, R.string.import_wallet_title, R.string.import_wallet_message
        )
    }

    private fun setSetupWalletVisibility(visibility: Int) {
        val importWalletButton = findViewById<ExtendedFloatingActionButton>(R.id.import_wallet_button)
        val createWalletButton = findViewById<ExtendedFloatingActionButton>(R.id.create_wallet_button)
        importWalletButton.visibility = visibility
        createWalletButton.visibility = visibility
        setupWalletVisibility = visibility
    }

    override fun onBackPressed() {
        if (Global.allowBackPress) {
            super.onBackPressed()
        }
    }
}
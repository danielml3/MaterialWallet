package com.danielml.openwallet.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.danielml.openwallet.R
import com.danielml.openwallet.managers.WalletDatabaseManager
import com.danielml.openwallet.managers.WalletManager
import com.danielml.openwallet.utils.DialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainScreenFragment : Fragment() {
    private var walletManager: WalletManager? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.main_screen_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (walletManager == null) {
            walletManager = WalletManager()
        }

        val importWalletButton = view.findViewById<FloatingActionButton>(R.id.import_wallet_button)
        importWalletButton.setOnClickListener {
            getImportWalletDialog().show()
        }

        val walletInformation = WalletDatabaseManager.getWalletInformation(context!!)
        if (walletInformation.has(WalletDatabaseManager.walletIdKey)) {
            val walletId = walletInformation.getString(WalletDatabaseManager.walletIdKey)
            if (walletId.isNotEmpty()) {
                val walletKit = walletManager!!.setupWallet(context!!, walletId, "")
                if (walletKit != null) {
                    importWalletButton.isEnabled = false
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
        val importWalletButton = view!!.findViewById<FloatingActionButton>(R.id.import_wallet_button)

        return DialogBuilder.buildDialog(
            context!!,
            { _, _ ->
                run {
                    val mnemonicTextBox = importForm.findViewById<EditText>(R.id.mnemonic_text_box)
                    val mnemonic = mnemonicTextBox.text.toString()
                    val walletKit = walletManager!!.setupWallet(context!!, "", mnemonic)
                    if (walletKit != null) {
                        importWalletButton.isEnabled = false
                    }
                }
            }, { _, _ -> }, importForm, false, R.string.import_wallet_title, R.string.import_wallet_message
        )
    }
}
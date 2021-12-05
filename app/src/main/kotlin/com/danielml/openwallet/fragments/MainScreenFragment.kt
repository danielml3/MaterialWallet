package com.danielml.openwallet.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.danielml.openwallet.R
import com.danielml.openwallet.managers.MnemonicManager
import com.danielml.openwallet.managers.WalletManager
import com.danielml.openwallet.utils.DialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainScreenFragment : Fragment() {
    private var inflatedLayout: View? = null
    private var firstInitialization = false
    private var walletManager: WalletManager? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        /*
         * Only inflate the layout once
         * This way, even if we change the fragment, the same content will be kept after
         * returning to the main fragment from other fragment
         */
        if (inflatedLayout == null) {
            inflatedLayout = inflater.inflate(R.layout.main_screen_fragment, container, false)
            firstInitialization = true
        }

        return inflatedLayout
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

        val container = view.findViewById<LinearLayout>(R.id.wallet_container)
        if (firstInitialization) {
            val mnemonicList = MnemonicManager.getMnemonicList(context!!)
            for (mnemonic: String in mnemonicList) {
                if (mnemonic.isNotEmpty()) {
                    walletManager!!.createWallet(context!!, mnemonic, container)
                }
            }

            firstInitialization = false
        }
    }

    /*
     * @returns an AlertDialog that allows to restore a wallet using a
     * recovery phrase (mnemonic)
     */
    private fun getImportWalletDialog(): AlertDialog {
        val importForm = layoutInflater.inflate(R.layout.import_wallet_form, null)
        val container = view!!.findViewById<LinearLayout>(R.id.wallet_container)

        return DialogBuilder.buildDialog(
            context!!,
            { _, _ ->
                run {
                    val mnemonicTextBox = importForm.findViewById<EditText>(R.id.mnemonic_text_box)
                    val mnemonic = mnemonicTextBox.text.toString()
                    walletManager!!.createWallet(context!!, mnemonic, container)
                }
            }, { _, _ -> }, importForm, false, R.string.import_wallet_title, R.string.import_wallet_message
        )
    }

    /*
     * Stop all wallets if main fragment gets destroyed
     */
    override fun onDestroy() {
        super.onDestroy()
        walletManager!!.stopAllWallets()
    }
}
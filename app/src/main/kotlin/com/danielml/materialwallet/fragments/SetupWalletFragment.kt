package com.danielml.materialwallet.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.danielml.materialwallet.Global
import com.danielml.materialwallet.R
import com.danielml.materialwallet.managers.WalletManager
import com.danielml.materialwallet.utils.DialogBuilder
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import java.util.*

class SetupWalletFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.setup_wallet_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Global.lastWalletBackStack = Global.SETUP_BACKSTACK
        val importWalletButton = view.findViewById<ExtendedFloatingActionButton>(R.id.import_wallet_button)
        importWalletButton.setOnClickListener {
            getImportWalletDialog().show()
        }

        val createWalletButton = view.findViewById<ExtendedFloatingActionButton>(R.id.create_wallet_button)
        createWalletButton.setOnClickListener {
            val walletKit = WalletManager.setupWallet(context!!, Global.sha256(Date().time.toString()), null)
            if (walletKit != null) {
                detachSetupFragment(context!!, this)
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
            context!!,
            { _, _ ->
                run {
                    val mnemonicTextBox = importForm.findViewById<EditText>(R.id.mnemonic_text_box)
                    val mnemonic = mnemonicTextBox.text.toString()
                    val walletKit = WalletManager.setupWallet(context!!, "", mnemonic)
                    if (walletKit != null) {
                        detachSetupFragment(context!!, this)
                    }
                }
            }, { _, _ -> }, importForm, false, R.string.import_wallet_title, R.string.import_wallet_message
        )
    }

    companion object {
        fun detachSetupFragment(context: Context, fragment: SetupWalletFragment) {
            Global.setupFinished = true
            (context as FragmentActivity).supportFragmentManager
                .beginTransaction()
                .remove(fragment)
                .commit()
        }
    }
}
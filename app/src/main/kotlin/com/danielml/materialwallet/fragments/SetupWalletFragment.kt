package com.danielml.materialwallet.fragments

import android.app.DatePickerDialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.danielml.materialwallet.Global
import com.danielml.materialwallet.R
import com.danielml.materialwallet.coins.AbstractCoin
import com.danielml.materialwallet.coins.Bitcoin
import com.danielml.materialwallet.managers.CoinManager
import com.danielml.materialwallet.managers.WalletManager
import com.danielml.materialwallet.utils.CurrencyUtils
import com.danielml.materialwallet.utils.DialogBuilder
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import org.bitcoinj.core.Coin
import java.util.*

class SetupWalletFragment : Fragment() {
    var selectedCoin = CoinManager.getDefaultCoin()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.setup_wallet_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Global.lastWalletBackStack = Global.SETUP_BACKSTACK
        val importWalletButton = view.findViewById<MaterialButton>(R.id.import_wallet_button)
        importWalletButton.setOnClickListener {
            getImportWalletDialog().show()
        }

        val createWalletButton = view.findViewById<MaterialButton>(R.id.create_wallet_button)
        createWalletButton.setOnClickListener {
            val walletKit = WalletManager.setupWallet(context!!, Global.sha256(Date().time.toString()), null, selectedCoin)
            if (walletKit != null) {
                detachSetupFragment(context!!, this)
            }
        }

        val selectCoinCard = view.findViewById<MaterialCardView>(R.id.select_coin_card)
        val selectedCoinText = view.findViewById<TextView>(R.id.selected_coin_text)
        val selectedCoinBaseTest = getString(R.string.selected_coin)
        selectedCoinText.text = String.format(selectedCoinBaseTest, CoinManager.getDefaultCoin().getDisplayName())

        selectCoinCard.setOnClickListener {
            val supportedCoins = CoinManager.getSupportedCoins()
            val supportedCoinsNames = supportedCoins.map {
                it.getDisplayName()
            }.toTypedArray()
            val builder: AlertDialog.Builder = AlertDialog.Builder(context!!)
            builder.setTitle(R.string.select_coin)
            builder.setItems(
                supportedCoinsNames
            ) { _, item ->
                selectedCoin = supportedCoins[item]
                selectedCoinText.text = String.format(selectedCoinBaseTest, selectedCoin.getDisplayName())
            }
            val alert: AlertDialog = builder.create()
            alert.show()
        }
    }

    /*
    * @returns an AlertDialog that allows to restore a wallet using a
    * recovery phrase (mnemonic)
    */
    private fun getImportWalletDialog(): AlertDialog {
        val importForm = layoutInflater.inflate(R.layout.import_wallet_form, null)
        val dateEditText = importForm.findViewById<EditText>(R.id.sync_from_date)
        var syncTimestamp = WalletManager.walletCreationDate

        dateEditText.setOnClickListener {
            val datePickerDialog = DatePickerDialog(context!!, 0, null, 2015, 11, 21) // 2015-12-21
            datePickerDialog.datePicker.minDate = 1230940800000 // 2009-01-03 (first block date)
            datePickerDialog.datePicker.maxDate = Date().time

            datePickerDialog.setOnDateSetListener { _, year, month, dayOfMonth ->
                val calendar = Calendar.getInstance()
                calendar[year, month] = dayOfMonth
                syncTimestamp = calendar.time.time / 1000

                dateEditText.setText(DateFormat.format("yyyy-MM-dd", calendar.time))
            }

            datePickerDialog.show()
        }

        return DialogBuilder.buildDialog(
            context!!,
            { _, _ ->
                run {
                    val mnemonicTextBox = importForm.findViewById<EditText>(R.id.mnemonic_text_box)
                    val mnemonic = mnemonicTextBox.text.toString()
                    val walletKit = WalletManager.setupWallet(context!!, "", mnemonic, selectedCoin, syncTimestamp)
                    if (walletKit != null) {
                        detachSetupFragment(context!!, this)
                    }
                }
            }, { _, _ -> }, null, importForm, false, R.string.import_wallet_title, R.string.import_wallet_message
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
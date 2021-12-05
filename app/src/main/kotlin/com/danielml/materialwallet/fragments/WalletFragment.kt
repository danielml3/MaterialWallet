package com.danielml.materialwallet.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.danielml.materialwallet.Global
import com.danielml.materialwallet.R
import com.danielml.materialwallet.utils.ClipboardUtils
import com.danielml.materialwallet.utils.DialogBuilder
import com.google.android.material.button.MaterialButton
import org.bitcoinj.core.Coin
import org.bitcoinj.core.Transaction
import org.bitcoinj.wallet.Wallet
import org.bitcoinj.wallet.listeners.WalletCoinsReceivedEventListener
import org.bitcoinj.wallet.listeners.WalletCoinsSentEventListener

class WalletFragment : Fragment(), WalletCoinsSentEventListener, WalletCoinsReceivedEventListener {
    private var handler = Handler(Looper.getMainLooper())

    private var walletKit = Global.globalWalletKit!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.wallet_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        walletKit.wallet().addCoinsSentEventListener(this)
        walletKit.wallet().addCoinsReceivedEventListener(this)
        walletKit.peerGroup().addBlocksDownloadedEventListener { _, block, _, _ ->
            handler.post {
                getLastBlockDateView().text = block?.time?.toString() ?: ""
                syncBalance()
            }
        }

        syncBalance()
        getLastBlockDateView().text = walletKit.wallet().lastBlockSeenTime.toString()

        /*
         * Triggered when the "Send" button on a wallet is clicked
         */
        view.findViewById<Button>(R.id.send_coins_button).setOnClickListener {
            val fragmentManager = (context as FragmentActivity).supportFragmentManager
            val sendCoinsFragment = SendCoinsFragment(walletKit)

            fragmentManager.beginTransaction()
                .setCustomAnimations(
                    android.R.anim.fade_in,
                    android.R.anim.fade_out,
                    android.R.anim.fade_in,
                    android.R.anim.fade_out
                )
                .add(R.id.main_fragment_container, sendCoinsFragment)
                .addToBackStack(null)
                .commit()
        }

        /*
         * Triggered when the "Receive" button on a wallet is clicked
         */
        view.findViewById<Button>(R.id.receive_coins_button).setOnClickListener {
            val address = walletKit.wallet().currentReceiveAddress().toString()
            val copyAddressButton = MaterialButton(context!!)
            copyAddressButton.setText(R.string.copy_address)
            copyAddressButton.setOnClickListener {
                ClipboardUtils.copyToClipboard(context!!, address)
            }

            DialogBuilder.buildDialog(
                context!!,
                null,
                null,
                copyAddressButton,
                true,
                context!!.getString(R.string.receive_coins),
                address
            ).show()
        }
    }

    /*
     * @returns the view that shall contain the wallet balance
     */
    private fun getWalletBalanceView(): TextView {
        return view!!.findViewById(R.id.wallet_balance)
    }

    /*
     * @returns the view that shall contain the last block date
     */
    private fun getLastBlockDateView(): TextView {
        return view!!.findViewById(R.id.last_block_date)
    }

    /*
     * Updates the balance view with the latest balance
     */
    private fun syncBalance() {
        handler.post {
            getWalletBalanceView().text = walletKit.wallet().getBalance(Wallet.BalanceType.ESTIMATED).toString()
        }
    }

    override fun onCoinsSent(wallet: Wallet?, tx: Transaction?, prevBalance: Coin?, newBalance: Coin?) {
        syncBalance()
    }

    override fun onCoinsReceived(wallet: Wallet?, tx: Transaction?, prevBalance: Coin?, newBalance: Coin?) {
        syncBalance()
    }
}
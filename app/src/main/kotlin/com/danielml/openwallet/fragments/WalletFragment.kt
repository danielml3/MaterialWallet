package com.danielml.openwallet.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.danielml.openwallet.Global
import com.danielml.openwallet.Global.Companion.TAG
import com.danielml.openwallet.R
import com.danielml.openwallet.utils.ClipboardUtils
import com.danielml.openwallet.utils.DialogBuilder
import com.google.android.material.button.MaterialButton
import org.bitcoinj.core.Coin
import org.bitcoinj.core.Transaction
import org.bitcoinj.core.listeners.BlocksDownloadedEventListener
import org.bitcoinj.wallet.Wallet
import org.bitcoinj.wallet.listeners.WalletCoinsReceivedEventListener
import org.bitcoinj.wallet.listeners.WalletCoinsSentEventListener
import java.util.*

class WalletFragment : Fragment(), WalletCoinsReceivedEventListener, WalletCoinsSentEventListener {
    private val handler = Handler(Looper.getMainLooper())

    private val walletKit = Global.globalWalletKit!!

    private var lastBlockFetchDate: Long = 0

    private var blockDateUpdateThresholdMs = 1000

    private var blocksDownloadedListener: BlocksDownloadedEventListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.wallet_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Global.allowBackPress = false
        Global.lastWalletBackStack = Global.WALLET_BACKSTACK

        walletKit.wallet().addCoinsSentEventListener(this)
        walletKit.wallet().addCoinsReceivedEventListener(this)

        blocksDownloadedListener = BlocksDownloadedEventListener { _, block, _, blocksLeft ->
            val newBlockFetchDate = Date().time
            if (newBlockFetchDate - lastBlockFetchDate > blockDateUpdateThresholdMs || blocksLeft <= 1) {
                handler.post {
                    setLastBlockDate(block.time)
                    syncBalance()
                }

                lastBlockFetchDate = newBlockFetchDate
            }
        }

        walletKit.peerGroup().addBlocksDownloadedEventListener(blocksDownloadedListener!!)

        val lastBlockDate = walletKit.wallet()?.lastBlockSeenTime
        if (lastBlockDate != null) {
            setLastBlockDate(lastBlockDate)
        }
        syncBalance()

        /*
         * Triggered when the "Send" button on a wallet is clicked
         */
        view.findViewById<Button>(R.id.send_coins_button).setOnClickListener {
            val fragmentManager = (context as FragmentActivity).supportFragmentManager
            val sendCoinsFragment = SendCoinsFragment(walletKit)

            fragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.scale_up, R.anim.scale_down, R.anim.scale_up, R.anim.scale_down)
                .replace(R.id.main_fragment_container, sendCoinsFragment)
                .addToBackStack(Global.SEND_COINS_BACKSTACK)
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

            copyAddressButton.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                setMargins(20, 20, 20, 20)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        walletKit.wallet().removeCoinsReceivedEventListener(this)
        walletKit.wallet().removeCoinsReceivedEventListener(this)
        walletKit.peerGroup().removeBlocksDownloadedEventListener(blocksDownloadedListener)
    }

    /*
     * @returns the view that shall contain the wallet balance
     */
    private fun getWalletBalanceView(): TextView? {
        return view?.findViewById(R.id.wallet_balance)
    }

    /*
     * @returns the view that shall contain the pending wallet balance
     */
    private fun getWalletPendingBalanceView(): TextView? {
        return view?.findViewById(R.id.pending_wallet_balance)
    }

    /*
     * @returns the view that shall contain the last block date
     */
    private fun getLastBlockDateView(): TextView? {
        return view?.findViewById(R.id.last_block_date)
    }

    /*
     * Updates the balance view with the latest balance
     */
    private fun syncBalance() {
        handler.post {
            if (context != null) {
                val availableBalance = walletKit.wallet().getBalance(Wallet.BalanceType.AVAILABLE)
                val estimatedBalance = walletKit.wallet().getBalance(Wallet.BalanceType.ESTIMATED)
                getWalletBalanceView()?.text = availableBalance.toString()
                getWalletPendingBalanceView()?.text = String.format(context!!.getString(R.string.pending_balance), (estimatedBalance - availableBalance))
            }
        }
    }

    override fun onCoinsSent(wallet: Wallet?, tx: Transaction?, prevBalance: Coin?, newBalance: Coin?) {
        syncBalance()
    }

    override fun onCoinsReceived(wallet: Wallet?, tx: Transaction?, prevBalance: Coin?, newBalance: Coin?) {
        syncBalance()
    }

    private fun setLastBlockDate(date: Date) {
        if (context != null) {
            val formattedDate = DateFormat.format("dd, MMMM yyyy - hh:mm:ss", date).toString()
            val formattedText = String.format(context!!.getText(R.string.last_block_date).toString(), formattedDate)
            getLastBlockDateView()?.text = formattedText
        }
    }
}
package com.danielml.openwallet.layouts

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import com.danielml.openwallet.R
import com.danielml.openwallet.fragments.SendCoinsFragment
import com.danielml.openwallet.utils.ClipboardUtils
import com.danielml.openwallet.utils.DialogBuilder
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import org.bitcoinj.core.*
import org.bitcoinj.core.listeners.DownloadProgressTracker
import org.bitcoinj.kits.WalletAppKit
import org.bitcoinj.wallet.Wallet
import org.bitcoinj.wallet.listeners.WalletCoinsReceivedEventListener
import org.bitcoinj.wallet.listeners.WalletCoinsSentEventListener
import java.util.*

class WalletCard(context: Context, private val walletKit: WalletAppKit, walletName: String, container: LinearLayout) :
    DownloadProgressTracker(), WalletCoinsSentEventListener, WalletCoinsReceivedEventListener {
    private var cardView: MaterialCardView

    private var progressBar: ProgressBar

    private var activity = context as Activity

    private var handler = Handler(Looper.getMainLooper())

    init {
        cardView = activity.layoutInflater.inflate(R.layout.wallet_card, container, false) as MaterialCardView
        progressBar = cardView.findViewById(R.id.sync_progress_bar)

        container.addView(cardView)
        getWalletNameView().text = walletName

        walletKit.setDownloadListener(this)
        walletKit.wallet().addCoinsSentEventListener(this)
        walletKit.wallet().addCoinsReceivedEventListener(this)
        syncBalance()

        /*
         * Triggered when the "Send" button on a wallet is clicked
         */
        cardView.findViewById<Button>(R.id.send_coins_button).setOnClickListener {
            val sendCoinsFragment = SendCoinsFragment(walletKit)
            (context as FragmentActivity).supportFragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out)
                .replace(R.id.main_fragment_container, sendCoinsFragment)
                .addToBackStack(null)
                .commit()
        }

        /*
         * Triggered when the "Receive" button on a wallet is clicked
         */
        cardView.findViewById<Button>(R.id.receive_coins_button).setOnClickListener {
            val address = walletKit.wallet().currentReceiveAddress().toString()
            val copyAddressButton = MaterialButton(context)
            copyAddressButton.setText(R.string.copy_address)
            copyAddressButton.setOnClickListener {
                ClipboardUtils.copyToClipboard(context, address)
            }

            DialogBuilder.buildDialog(
                context,
                null,
                null,
                copyAddressButton,
                true,
                context.getString(R.string.receive_coins),
                address
            ).show()
        }
    }

    /*
     * @returns the view that shall contain the wallet name
     */
    private fun getWalletNameView(): TextView {
        return cardView.findViewById(R.id.wallet_name)
    }

    /*
     * @returns the view that shall contain the wallet balance
     */
    private fun getWalletBalanceView(): TextView {
        return cardView.findViewById(R.id.wallet_balance)
    }

    /*
     * @returns the view that shall contain the last block date
     */
    private fun getLastBlockDateView(): TextView {
        return cardView.findViewById(R.id.last_block_date)
    }

    /*
     * Updates the balance view with the latest balance
     */
    private fun syncBalance() {
        handler.post {
            getWalletBalanceView().text = walletKit.wallet().getBalance(Wallet.BalanceType.ESTIMATED).toString()
        }
    }

    /*
     * Sets the progress or a progress bar indicating the blockchain
     * synchronization progress
     */
    private fun setProgress(progress: Int) {
        progressBar.isIndeterminate = false
        progressBar.progress = progress
    }

    /*
     * Triggered when the block download has started
     */
    override fun startDownload(blocks: Int) {
        super.startDownload(blocks)
        handler.post {
            syncBalance()
        }
    }

    /*
     * Triggered when the block download has finished
     */
    override fun doneDownload() {
        super.doneDownload()
        handler.post {
            syncBalance()
            setProgress(100)
        }
    }

    /*
     * Triggered when the download progress has changed
     */
    override fun progress(pct: Double, blocksSoFar: Int, date: Date?) {
        super.progress(pct, blocksSoFar, date)
        setProgress(pct.toInt())
    }

    /*
     * Triggered when a new block is downloaded
     */
    override fun onBlocksDownloaded(
        peer: Peer?,
        block: Block?,
        filteredBlock: FilteredBlock?,
        blocksLeft: Int
    ) {
        super.onBlocksDownloaded(peer, block, filteredBlock, blocksLeft)
        handler.post {
            getLastBlockDateView().text = block?.time?.toString() ?: ""
            syncBalance()
        }
    }

    override fun onCoinsSent(wallet: Wallet?, tx: Transaction?, prevBalance: Coin?, newBalance: Coin?) {
        syncBalance()
    }

    override fun onCoinsReceived(wallet: Wallet?, tx: Transaction?, prevBalance: Coin?, newBalance: Coin?) {
        syncBalance()
    }
}
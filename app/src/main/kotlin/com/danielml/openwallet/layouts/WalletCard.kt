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
import com.danielml.openwallet.BitcoinWallet
import com.danielml.openwallet.Global
import com.danielml.openwallet.R
import com.danielml.openwallet.fragments.SendCoinsFragment
import com.danielml.openwallet.utils.ClipboardUtils
import com.danielml.openwallet.utils.DialogBuilder
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import io.horizontalsystems.bitcoincore.BitcoinCore
import io.horizontalsystems.bitcoincore.models.BalanceInfo
import io.horizontalsystems.bitcoincore.models.BlockInfo
import io.horizontalsystems.bitcoinkit.BitcoinKit

class WalletCard(context: Context, private val wallet: BitcoinWallet, private val container: LinearLayout) :
    BitcoinKit.Listener {
    private var cardView: MaterialCardView
    private var progressBar: ProgressBar
    private var activity = context as Activity
    private var handler = Handler(Looper.getMainLooper())

    init {
        cardView = activity.layoutInflater.inflate(R.layout.wallet_card, container, false) as MaterialCardView
        progressBar = cardView.findViewById(R.id.sync_progress_bar)

        container.addView(cardView)
        getWalletNameView().text = wallet.getWalletName()

        syncBalance()
        wallet.setListener(this)

        /*
         * Triggered when the "Send" button on a wallet is clicked
         */
        cardView.findViewById<Button>(R.id.send_coins_button).setOnClickListener {
            val sendCoinsFragment = SendCoinsFragment(wallet)
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
            val address = wallet.getWalletKit().receiveAddress()
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
            getWalletBalanceView().text = wallet.getWalletKit().balance.spendable.toString()
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
     * Triggered when the balance of the wallet gets updated
     */
    override fun onBalanceUpdate(balance: BalanceInfo) {
        super.onBalanceUpdate(balance)
        syncBalance()
    }

    /*
     * Triggered when the wallet kit changes its state
     */
    override fun onKitStateUpdate(state: BitcoinCore.KitState) {
        super.onKitStateUpdate(state)
        when (state) {
            is BitcoinCore.KitState.Synced -> {
                syncBalance()
                setProgress(100)

                val blockInfo = wallet.getWalletKit().lastBlockInfo
                if (blockInfo != null) {
                    onLastBlockInfoUpdate(blockInfo)
                }
            }

            is BitcoinCore.KitState.Syncing -> {
                setProgress((state.progress * 100).toInt())
            }
        }
    }

    /*
     * Triggered when the last synced block has changed
     */
    override fun onLastBlockInfoUpdate(blockInfo: BlockInfo) {
        super.onLastBlockInfoUpdate(blockInfo)
        handler.post {
            getLastBlockDateView().text = Global.timestampToDate(blockInfo.timestamp)
        }
    }
}
package com.danielml.openwallet.layouts

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import com.danielml.openwallet.BitcoinWallet
import com.danielml.openwallet.Global
import com.danielml.openwallet.R
import com.danielml.openwallet.fragments.ReceiveCoinsFragment
import com.danielml.openwallet.fragments.SendCoinsFragment
import com.danielml.openwallet.fragments.SpecificWalletFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import io.horizontalsystems.bitcoincore.BitcoinCore
import io.horizontalsystems.bitcoincore.core.Bip
import io.horizontalsystems.bitcoincore.models.BalanceInfo
import io.horizontalsystems.bitcoincore.models.BlockInfo
import io.horizontalsystems.bitcoincore.utils.AddressConverterChain
import io.horizontalsystems.bitcoincore.utils.SegwitAddressConverter
import io.horizontalsystems.bitcoinkit.BitcoinKit

class WalletCard(var context: Context, private val wallet: BitcoinWallet, container: LinearLayout) :
    BitcoinKit.Listener {
    private var cardView: MaterialCardView?
    private var handler = Handler(Looper.getMainLooper())
    private var activeWalletText: TextView = (context as Activity).findViewById(R.id.active_wallet_name)

    private val sendCoinsButton: MaterialButton

    init {
        cardView =
            (context as Activity).layoutInflater.inflate(R.layout.wallet_card, container, false) as MaterialCardView

        container.addView(cardView)
        getWalletNameView().text = wallet.getWalletName()

        syncBalance()
        wallet.setListener(this)

        sendCoinsButton = cardView!!.findViewById(R.id.send_coins_button)
        sendCoinsButton.isEnabled = false

        /*
         * Triggered when the "Send" button on a wallet is clicked
         */
        sendCoinsButton.setOnClickListener {
            val sendCoinsFragment = SendCoinsFragment(wallet)
            (context as FragmentActivity).supportFragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out)
                .replace(R.id.secondary_fragment_container, sendCoinsFragment)
                .commit()

            activeWalletText.text = wallet.getWalletName()
            Global.getDraggableWalletContainer(context).shrinkAnimated()
        }

        /*
         * Triggered when the "Receive" button on a wallet is clicked
         */
        cardView!!.findViewById<Button>(R.id.receive_coins_button).setOnClickListener {
            val addressConverter = AddressConverterChain()
            val network = Global.getNetworkFromType(Global.getNetworkType(context))
            addressConverter.prependConverter(SegwitAddressConverter(network.addressSegwitHrp))
            val address = addressConverter.convert(
                wallet.getWalletKit().getPublicKeyByPath(Global.BIP84_FIRST_ADDRESS_PATH),
                Bip.BIP84.scriptType
            ).string
            val receiveCoinsFragment = ReceiveCoinsFragment(address)

            (context as FragmentActivity).supportFragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out)
                .replace(R.id.secondary_fragment_container, receiveCoinsFragment)
                .commit()

            activeWalletText.text = wallet.getWalletName()
            Global.getDraggableWalletContainer(context).shrinkAnimated()
        }

        cardView!!.setOnClickListener {
            val transactionsFragment = SpecificWalletFragment(wallet)
            (context as FragmentActivity).supportFragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out)
                .replace(R.id.secondary_fragment_container, transactionsFragment)
                .commit()

            activeWalletText.text = wallet.getWalletName()
            Global.getDraggableWalletContainer(context).shrinkAnimated()
        }
    }

    /*
     * Reattaches the CardView to the given container, and updates the context to
     * the container's one
     *
     * This should be called by the WalletManager after the main fragment is recreated
     */
    fun reattachToContainer(container: LinearLayout) {
        val cardViewParent = cardView?.parent
        if (cardViewParent != null) {
            (cardViewParent as LinearLayout).removeView(cardView)
        }

        container.addView(cardView)
        context = container.context
        activeWalletText = (context as Activity).findViewById(R.id.active_wallet_name)
    }

    /*
     * @returns the view that shall contain the wallet name
     */
    private fun getWalletNameView(): TextView {
        return cardView!!.findViewById(R.id.wallet_name)
    }

    /*
     * @returns the view that shall contain the wallet balance
     */
    private fun getWalletBalanceView(): TextView {
        return cardView!!.findViewById(R.id.wallet_balance)
    }

    /*
     * @returns the view that shall contain the last block date
     */
    private fun getLastBlockDateView(): TextView {
        return cardView!!.findViewById(R.id.last_block_date)
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
                onSynchronized()
            }

            else -> {
                onDesynchronized()
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

    /*
     * Called when the wallet gets synced with the peers
     */
    private fun onSynchronized() {
        syncBalance()

        val blockInfo = wallet.getWalletKit().lastBlockInfo
        if (blockInfo != null) {
            onLastBlockInfoUpdate(blockInfo)
        }

        handler.post {
            sendCoinsButton.isEnabled = true
        }
    }

    /*
     * Called when the wallet is not synced with the peers
     */
    private fun onDesynchronized() {
        handler.post {
            sendCoinsButton.isEnabled = false
        }
    }
}
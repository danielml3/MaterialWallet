package com.danielml.materialwallet.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.danielml.materialwallet.Global
import com.danielml.materialwallet.R
import com.danielml.materialwallet.utils.CurrencyUtils
import com.danielml.materialwallet.utils.WalletUtils
import org.bitcoinj.core.*
import org.bitcoinj.core.listeners.BlocksDownloadedEventListener
import org.bitcoinj.core.listeners.PeerConnectedEventListener
import org.bitcoinj.core.listeners.PeerDisconnectedEventListener
import org.bitcoinj.wallet.Wallet
import org.bitcoinj.wallet.listeners.WalletCoinsReceivedEventListener
import org.bitcoinj.wallet.listeners.WalletCoinsSentEventListener
import java.util.*

class WalletFragment : Fragment(), WalletCoinsReceivedEventListener, WalletCoinsSentEventListener,
    BlocksDownloadedEventListener,
    PeerConnectedEventListener, PeerDisconnectedEventListener {
    private val handler = Handler(Looper.getMainLooper())

    private val walletKit = Global.globalWalletKit!!

    private var lastBlockFetchDate: Long = 0

    private var blockDateUpdateThresholdMs = 1000

    private val transactionIdList: ArrayList<String> = ArrayList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.wallet_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Global.allowBackPress = false
        Global.lastWalletBackStack = Global.WALLET_BACKSTACK
        transactionIdList.clear()

        walletKit.wallet().addCoinsSentEventListener(this)
        walletKit.wallet().addCoinsReceivedEventListener(this)
        walletKit.peerGroup().addBlocksDownloadedEventListener(this)
        walletKit.peerGroup().addConnectedEventListener(this)
        walletKit.peerGroup().addDisconnectedEventListener(this)

        val sendCoinsButton = view.findViewById<Button>(R.id.send_coins_button)

        // Enable the send coins button if no blocks are pending
        val connectedPeers = walletKit.peerGroup().connectedPeers
        if (connectedPeers != null && connectedPeers.isNotEmpty()) {
            var bestBlockHeightDifference = Int.MAX_VALUE

            for (peer: Peer in connectedPeers) {
                val heightDifference = peer.peerBlockHeightDifference
                if (heightDifference in 0 until bestBlockHeightDifference) {
                    bestBlockHeightDifference = heightDifference
                }
            }

            sendCoinsButton.isEnabled = (bestBlockHeightDifference == 0)
        } else {
            sendCoinsButton.isEnabled = false
        }

        val lastBlockDate = walletKit.wallet()?.lastBlockSeenTime
        if (lastBlockDate != null) {
            setLastBlockDate(lastBlockDate)
        }
        syncBalance()

        /*
         * Triggered when the "Send" button on a wallet is clicked
         */
        sendCoinsButton.setOnClickListener {
            val fragmentManager = (context as FragmentActivity).supportFragmentManager
            val sendCoinsFragment = SendCoinsFragment()

            fragmentManager.beginTransaction()
                .setCustomAnimations(
                    R.anim.scale_up,
                    R.anim.scale_down,
                    R.anim.scale_up,
                    R.anim.scale_down
                )
                .replace(R.id.main_fragment_container, sendCoinsFragment)
                .addToBackStack(Global.SEND_COINS_BACKSTACK)
                .commit()
        }

        /*
         * Triggered when the "Receive" button on a wallet is clicked
         */
        view.findViewById<Button>(R.id.receive_coins_button).setOnClickListener {
            val fragmentManager = (context as FragmentActivity).supportFragmentManager
            val receiveCoinsFragment = ReceiveCoinsFragment()

            fragmentManager.beginTransaction()
                .setCustomAnimations(
                    R.anim.scale_up,
                    R.anim.scale_down,
                    R.anim.scale_up,
                    R.anim.scale_down
                )
                .replace(R.id.main_fragment_container, receiveCoinsFragment)
                .addToBackStack(Global.RECEIVE_COINS_BACKSTACK)
                .commit()
        }

        setupTransactionsList()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        walletKit.wallet().removeCoinsReceivedEventListener(this)
        walletKit.wallet().removeCoinsReceivedEventListener(this)
        walletKit.peerGroup().removeBlocksDownloadedEventListener(this)
        walletKit.peerGroup().removeConnectedEventListener(this)
        walletKit.peerGroup().removeDisconnectedEventListener(this)
    }

    /*
     * @returns the view that shall contain the wallet balance
     */
    private fun getWalletBalanceView(): TextView? {
        return view?.findViewById(R.id.wallet_balance)
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
    @SuppressLint("SetTextI18n")
    private fun syncBalance() {
        handler.post {
            if (context != null) {
                val estimatedBalance = walletKit.wallet().getBalance(Wallet.BalanceType.ESTIMATED)
                getWalletBalanceView()?.text = CurrencyUtils.toString(estimatedBalance)

                setupTransactionsList()
            }
        }
    }

    override fun onCoinsSent(wallet: Wallet?, tx: Transaction?, prevBalance: Coin?, newBalance: Coin?) {
        syncBalance()

        if (tx != null) {
            val container = view!!.findViewById<LinearLayout>(R.id.transaction_container)
            createTransactionCard(tx, container)
        }
    }

    override fun onCoinsReceived(wallet: Wallet?, tx: Transaction?, prevBalance: Coin?, newBalance: Coin?) {
        syncBalance()

        if (tx != null) {
            val container = view!!.findViewById<LinearLayout>(R.id.transaction_container)
            createTransactionCard(tx, container)
        }
    }

    override fun onPeerConnected(peer: Peer?, peerCount: Int) {
        val sendCoinsButton = view?.findViewById<Button>(R.id.send_coins_button)
        handler.post {
            // Enable the send coins button if no blocks are pending
            if (sendCoinsButton?.isEnabled == false) {
                sendCoinsButton.isEnabled = (peer?.peerBlockHeightDifference == 0)
            }
        }
    }

    override fun onPeerDisconnected(peer: Peer?, peerCount: Int) {
        val sendCoinsButton = view?.findViewById<Button>(R.id.send_coins_button)
        handler.post {
            if (peerCount < 1) {
                sendCoinsButton?.isEnabled = false
            }
        }
    }

    override fun onBlocksDownloaded(peer: Peer?, block: Block?, filteredBlock: FilteredBlock?, blocksLeft: Int) {
        val sendCoinsButton = view?.findViewById<Button>(R.id.send_coins_button)
        val newBlockFetchDate = Date().time
        if (newBlockFetchDate - lastBlockFetchDate > blockDateUpdateThresholdMs || blocksLeft <= 1) {
            handler.post {
                if (block != null) {
                    setLastBlockDate(block.time)
                }
                syncBalance()

                // Enable the send coins button if no blocks are pending
                sendCoinsButton?.isEnabled = (blocksLeft == 0)
            }

            lastBlockFetchDate = newBlockFetchDate
        }
    }

    private fun setLastBlockDate(date: Date) {
        if (context != null) {
            val formattedDate = DateFormat.format("dd, MMMM yyyy - HH:mm:ss", date).toString()
            val formattedText = String.format(context!!.getText(R.string.last_block_date).toString(), formattedDate)
            getLastBlockDateView()?.text = formattedText
        }
    }

    /*
     * Sorts the transactions and creates a card for each one
     */
    private fun setupTransactionsList() {
        Thread {
            val container = view?.findViewById<LinearLayout>(R.id.transaction_container)

            for (transaction: Transaction in walletKit.wallet().getTransactions(false)
                .sortedWith { transaction1, transaction2 ->
                    if ((transaction1?.updateTime?.time ?: 0) > (transaction2?.updateTime?.time ?: 0)) {
                        -1
                    } else {
                        1
                    }
                }) {

                if (context == null) {
                    break
                }

                if (!transactionIdList.contains(transaction.txId.toString()) && container != null) {
                    transactionIdList.add(transaction.txId.toString())
                    createTransactionCard(transaction, container)
                }
            }
        }.start()
    }

    /*
     * Creates a transaction card from the given transaction and attaches it to the
     * given container
     */
    private fun createTransactionCard(transaction: Transaction, container: LinearLayout) {
        val cardView = layoutInflater.inflate(R.layout.transaction_card, container, false)
        val dateTextView = cardView.findViewById<TextView>(R.id.transaction_date)
        val valueTextView = cardView.findViewById<TextView>(R.id.transaction_value)
        val feeTextView = cardView.findViewById<TextView>(R.id.transaction_fee)
        val transactionIcon = cardView.findViewById<ImageView>(R.id.transaction_icon)

        val formattedDate = DateFormat.format("dd/MM/yyyy - HH:mm:ss", transaction.updateTime).toString()
        var isIncoming = true

        for (input: TransactionInput in transaction.inputs) {
            val address = input.connectedOutput?.scriptPubKey?.getToAddress(Global.NETWORK_PARAMS)
            if (address != null && walletKit.wallet().isAddressMine(address)) {
                isIncoming = false
            }
        }

        if (isIncoming) {
            transactionIcon.setImageResource(R.drawable.south_west_arrow)
        } else {
            transactionIcon.setImageResource(R.drawable.north_east_arrow)
        }

        if (transaction.fee != null) {
            feeTextView.text =
                String.format(context!!.getString(R.string.transaction_fee), CurrencyUtils.toString(transaction.fee))
        } else {
            feeTextView.visibility = View.GONE
        }

        valueTextView.text =
            CurrencyUtils.toString(WalletUtils.calculateTransactionValue(walletKit, transaction, isIncoming))
        dateTextView.text = formattedDate

        handler.post {
            container.addView(cardView)
        }
    }
}
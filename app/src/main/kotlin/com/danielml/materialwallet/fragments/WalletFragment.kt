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
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.danielml.materialwallet.Global
import com.danielml.materialwallet.R
import com.danielml.materialwallet.layouts.TransactionCard
import com.danielml.materialwallet.priceprovider.CoinbasePriceProvider
import com.danielml.materialwallet.priceprovider.PriceChangeListener
import com.danielml.materialwallet.utils.CurrencyUtils
import com.google.android.material.button.MaterialButton
import org.bitcoinj.core.*
import org.bitcoinj.core.listeners.BlocksDownloadedEventListener
import org.bitcoinj.wallet.Wallet
import org.bitcoinj.wallet.listeners.WalletCoinsReceivedEventListener
import org.bitcoinj.wallet.listeners.WalletCoinsSentEventListener
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*

class WalletFragment : Fragment(), WalletCoinsReceivedEventListener, WalletCoinsSentEventListener,
    BlocksDownloadedEventListener, PriceChangeListener {
    private val handler = Handler(Looper.getMainLooper())

    private val walletKit = Global.globalWalletKit!!

    private var lastBlockFetchDate: Long = 0

    private var blockDateUpdateThresholdMs = 1000

    private val transactionIdList: ArrayList<String> = ArrayList()

    private var transactionThread: Thread? = null

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

        val sendCoinsButton = view.findViewById<Button>(R.id.send_coins_button)

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

        val refreshTransactionsButton = view.findViewById<MaterialButton>(R.id.refresh_transactions)
        refreshTransactionsButton.setOnClickListener {
            recreateTransactionsList()
        }

        setupTransactionsList()

        Global.globalPriceProvider.addOnPriceChangeListener(this)
    }

    override fun onDestroyView() {
        super.onDestroyView()

        walletKit.wallet().removeCoinsReceivedEventListener(this)
        walletKit.wallet().removeCoinsReceivedEventListener(this)
        walletKit.peerGroup().removeBlocksDownloadedEventListener(this)

        Global.globalPriceProvider.removeOnPriceChangeListener(this)
    }

    /*
     * @returns the view that shall contain the wallet balance
     */
    private fun getWalletBalanceView(): TextView? {
        return view?.findViewById(R.id.wallet_balance)
    }

    /*
     * @returns the view that shall contain the wallet balance in a fiat currency
     */
    private fun getCurrentBalanceView(): TextView? {
        return view?.findViewById(R.id.current_balance_text)
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

                val priceDecimal = BigDecimal.valueOf(Global.globalPriceProvider.getPrice().toDouble())
                var fiatPrice = estimatedBalance.toBtc().multiply(priceDecimal)
                fiatPrice = fiatPrice.setScale(2, RoundingMode.HALF_EVEN)

                val fiatString = "$fiatPrice ${CoinbasePriceProvider.FIAT_CURRENCY}"
                getCurrentBalanceView()?.text = resources.getString(R.string.current_balance, fiatString)
            }
        }
    }

    override fun onCoinsSent(wallet: Wallet?, tx: Transaction?, prevBalance: Coin?, newBalance: Coin?) {
        syncBalance()
        val container = view?.findViewById<LinearLayout>(R.id.transaction_container)

        if (tx != null && container != null) {
            createTransactionCard(tx, container, true)
        }
    }

    override fun onCoinsReceived(wallet: Wallet?, tx: Transaction?, prevBalance: Coin?, newBalance: Coin?) {
        syncBalance()
        val container = view?.findViewById<LinearLayout>(R.id.transaction_container)

        if (tx != null && container != null) {
            createTransactionCard(tx, container, true)
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
            }

            lastBlockFetchDate = newBlockFetchDate
        }
    }

    private fun setLastBlockDate(date: Date) {
        if (context != null) {
            val formattedDate = DateFormat.format("dd, MMMM yyyy - HH:mm:ss", date).toString()
            val formattedText =
                String.format(context?.getText(R.string.last_block_date)?.toString() ?: "", formattedDate)
            getLastBlockDateView()?.text = formattedText
        }
    }

    /*
     * Destroys the existing transaction cards and creates them again
     */
    private fun recreateTransactionsList() {
        if (transactionThread?.isAlive == false) {
            handler.post {
                val container = view?.findViewById<LinearLayout>(R.id.transaction_container)
                container?.removeAllViews()

                transactionIdList.clear()
                setupTransactionsList()
            }
        }
    }

    /*
     * Sorts the transactions and creates a card for each one
     */
    private fun setupTransactionsList() {
        transactionThread = Thread {
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

                if (container != null) {
                    createTransactionCard(transaction, container)
                }
            }
        }

        transactionThread?.start()
    }

    /*
     * Creates a transaction card from the given transaction and attaches it to the
     * given container
     */
    private fun createTransactionCard(transaction: Transaction, container: LinearLayout, goesOnTop: Boolean = false) {
        if (transactionIdList.contains(transaction.txId.toString())) {
            return
        }

        val cardView = TransactionCard(context!!, transaction, container).getView()
        transactionIdList.add(transaction.txId.toString())

        handler.post {
            if (goesOnTop) {
                container.addView(cardView, 0)
            } else {
                container.addView(cardView)
            }
        }
    }

    override fun onPriceChange(price: Float, fiat: String) {
        syncBalance()
    }
}
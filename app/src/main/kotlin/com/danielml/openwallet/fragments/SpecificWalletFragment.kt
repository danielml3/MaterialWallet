package com.danielml.openwallet.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.danielml.openwallet.BitcoinWallet
import com.danielml.openwallet.Global
import com.danielml.openwallet.R
import com.danielml.openwallet.managers.TransactionManager
import com.danielml.openwallet.utils.VibratorUtils
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView

@SuppressLint("ClickableViewAccessibility")
class SpecificWalletFragment(var wallet: BitcoinWallet?) : Fragment() {
    constructor() : this(null)

    companion object {
        var lastWallet: BitcoinWallet? = null
    }

    private var mnemonicText: TextView? = null

    private var showMnemonicRunnable = Runnable {
        VibratorUtils.vibrate(context!!, 100)
        mnemonicText?.visibility = View.VISIBLE
    }

    private var deleteWalletRunnable = Runnable {
        if (lastWallet != null) {
            VibratorUtils.vibrate(context!!, 100)
            Global.walletManager.removeWallet(context!!, lastWallet!!)
            Global.getDraggableWalletContainer(context!!).expandAnimated()
        }

        (context as FragmentActivity).supportFragmentManager.beginTransaction().remove(this).commit()
    }

    private var handler = Handler(Looper.getMainLooper())

    init {
        /*
         * Store the last wallet that used this fragment, since Android will initialize
         * this fragment again but with a null wallet after a configuration change
         */
        if (wallet == null) {
            wallet = lastWallet
        } else {
            lastWallet = wallet
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.specific_wallet_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val walletContainer = view.findViewById<LinearLayout>(R.id.transactions_container)
        TransactionManager.generateTransactionCards(layoutInflater, walletContainer, lastWallet?.getWalletKit()?.transactions()?.blockingGet() ?: listOf())

        setupMnemonicCard()
        setupRemoveWalletButton()
    }

    private fun setupMnemonicCard() {
        val mnemonicCard = view!!.findViewById<MaterialCardView>(R.id.recovery_phrase_card)
        mnemonicText = view!!.findViewById(R.id.recovery_phrase)

        mnemonicText!!.text = wallet?.getMnemonic()
        mnemonicText!!.visibility = View.INVISIBLE

        mnemonicCard.setOnTouchListener { v, event ->
            when (event?.action) {
                MotionEvent.ACTION_DOWN -> {
                    handler.removeCallbacks(showMnemonicRunnable)
                    handler.postDelayed(showMnemonicRunnable, 5000)
                }

                MotionEvent.ACTION_UP -> {
                    handler.removeCallbacks(showMnemonicRunnable)
                    mnemonicText!!.visibility = View.INVISIBLE
                }
            }

            v?.onTouchEvent(event) ?: true
        }
    }

    private fun setupRemoveWalletButton() {
        val button = view!!.findViewById<MaterialButton>(R.id.remove_wallet_button)
        button.setOnTouchListener { v, event ->
            when (event?.action) {
                MotionEvent.ACTION_DOWN -> {
                    handler.removeCallbacks(deleteWalletRunnable)
                    handler.postDelayed(deleteWalletRunnable, 5000)
                }

                MotionEvent.ACTION_UP -> {
                    handler.removeCallbacks(deleteWalletRunnable)
                }
            }

            v?.onTouchEvent(event) ?: true
        }
    }
}
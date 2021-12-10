package com.danielml.openwallet.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentContainerView
import com.danielml.openwallet.Global
import com.danielml.openwallet.R
import com.danielml.openwallet.managers.WalletDatabaseManager
import com.google.android.material.button.MaterialButton
import org.json.JSONException

class MainScreenFragment : Fragment() {
    private var inflatedLayout: View? = null
    private var firstInitialization = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        /*
         * Only inflate the layout once
         * This way, even if we change the fragment, the same content will be kept after
         * returning to the main fragment from other fragment
         */
        if (inflatedLayout == null) {
            inflatedLayout = inflater.inflate(R.layout.main_screen_fragment, container, false)
            firstInitialization = true
        }

        return inflatedLayout
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val container = view.findViewById<LinearLayout>(R.id.wallet_container)

        view.findViewById<MaterialButton>(R.id.import_wallet_button).apply {
            setOnClickListener {
                val importWalletFragment = ImportWalletFragment(container)
                (context as FragmentActivity).supportFragmentManager.beginTransaction()
                    .setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out)
                    .replace(R.id.secondary_fragment_container, importWalletFragment)
                    .commit()

                Global.getDraggableWalletContainer(context).shrinkAnimated()
            }
        }

        view.findViewById<MaterialButton>(R.id.create_wallet_button).apply {
            setOnClickListener {
                val createWalletFragment = CreateWalletFragment(container)
                (context as FragmentActivity).supportFragmentManager.beginTransaction()
                    .setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out)
                    .replace(R.id.secondary_fragment_container, createWalletFragment)
                    .commit()

                Global.getDraggableWalletContainer(context).shrinkAnimated()
            }
        }

        Global.walletManager.reattachAllWallets(container)

        val handleHeight = Global.getDraggableWalletContainer(context!!).getHandleHeight()
        val secondaryFragment = view.findViewById<FragmentContainerView>(R.id.secondary_fragment_container)
        secondaryFragment?.setPadding(0, 0, 0, handleHeight)
    }

    override fun onStart() {
        super.onStart()
        val container = view!!.findViewById<LinearLayout>(R.id.wallet_container)

        if (firstInitialization) {
            val mnemonicList = WalletDatabaseManager.getWalletInformationList(context!!)
            for (i: Int in 0 until mnemonicList.length()) {
                val mnemonicJSONObject = mnemonicList.getJSONObject(i)
                val mnemonic = mnemonicJSONObject.getString(WalletDatabaseManager.mnemonicKey)
                val walletName = try {
                    mnemonicJSONObject.getString(WalletDatabaseManager.walletNameKey)
                } catch (e: JSONException) {
                    ""
                }
                if (mnemonic.isNotEmpty()) {
                    Global.walletManager.createWallet(context!!, mnemonic, walletName, container)
                }
            }

            firstInitialization = false
        }
    }
}
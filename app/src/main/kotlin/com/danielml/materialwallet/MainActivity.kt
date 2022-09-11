package com.danielml.materialwallet

import android.content.pm.ApplicationInfo
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.danielml.materialwallet.fragments.SecurityFragment
import com.danielml.materialwallet.fragments.SettingsFragment
import com.danielml.materialwallet.fragments.SetupWalletFragment
import com.danielml.materialwallet.managers.WalletDatabaseManager
import com.danielml.materialwallet.managers.WalletManager
import com.google.android.material.color.DynamicColors
import com.google.android.material.color.DynamicColorsOptions
import com.google.android.material.color.MaterialColors
import com.google.android.material.elevation.SurfaceColors
import com.google.android.material.navigation.NavigationBarView
import org.bitcoinj.params.TestNet3Params

class MainActivity : AppCompatActivity() {
    private val settingsFragment = SettingsFragment()
    private val setupWalletFragment = SetupWalletFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        if (Global.globalWalletKit == null) {
            super.onCreate(null)
        } else {
            super.onCreate(savedInstanceState)
        }
        setContentView(R.layout.activity_main)

        val isDebuggable = (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0)
        if (isDebuggable) {
            Global.NETWORK_PARAMS = TestNet3Params.get()
        }

        val navigationBarView = findViewById<NavigationBarView>(R.id.bottom_navigation)
        navigationBarView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.wallet_page -> {
                    if (Global.lastWalletBackStack.isNotEmpty()) {
                        supportFragmentManager.popBackStack(Global.lastWalletBackStack, 0)
                    } else {
                        supportFragmentManager
                            .beginTransaction()
                            .hide(settingsFragment)
                            .commit()
                    }

                    true
                }

                R.id.settings_page -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_fragment_container, settingsFragment)
                        .show(settingsFragment)
                        .addToBackStack(Global.SETTINGS_BACKSTACK)
                        .commit()
                    true
                }

                R.id.security_page -> {
                    if (Global.walletSetupFinished) {
                        val securityFragment = SecurityFragment()
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.main_fragment_container, securityFragment)
                            .show(securityFragment)
                            .addToBackStack(Global.SECURITY_BACKSTACK)
                            .commit()
                        true
                    } else {
                        false
                    }
                }

                else -> false
            }
        }

        setupColorScheme()

        loadWallet()
        Global.globalPriceProvider.start()
    }

    private fun loadWallet() {
        val walletInformation = WalletDatabaseManager.getWalletInformation(this)
        if (walletInformation.has(WalletDatabaseManager.walletIdKey)) {
            val walletId = walletInformation.getString(WalletDatabaseManager.walletIdKey)
            if (walletId.isNotEmpty()) {
                val walletKit = WalletManager.setupWallet(this, walletId, "")
                if (walletKit != null) {
                    Global.setupFinished = true

                    SetupWalletFragment.detachSetupFragment(this, setupWalletFragment)
                }
            }
        }

        if (!Global.setupFinished) {
            supportFragmentManager
                .beginTransaction()
                .add(R.id.main_fragment_container, setupWalletFragment)
                .addToBackStack(Global.SETUP_BACKSTACK)
                .commit()
        }
    }

    fun setupColorScheme() {
        if (DynamicColors.isDynamicColorAvailable()) {
            DynamicColors.applyIfAvailable(this)
        } else {

        }

        window.statusBarColor = SurfaceColors.SURFACE_0.getColor(this)
        window.navigationBarColor = SurfaceColors.SURFACE_2.getColor(this)
    }

    override fun onBackPressed() {
        if (Global.allowBackPress) {
            super.onBackPressed()
        } else {
            moveTaskToBack(true);
        }
    }
}
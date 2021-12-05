package com.danielml.openwallet

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.danielml.openwallet.fragments.MainScreenFragment
import java.util.logging.Level
import java.util.logging.Logger


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Logger.getLogger("").level = Level.OFF

        // Attach the main fragment to its container
        val mainFragment = MainScreenFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.main_fragment_container, mainFragment)
            .commit()
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 1) {
            supportFragmentManager.popBackStackImmediate()
        } else {
            super.onBackPressed()
        }
    }
}
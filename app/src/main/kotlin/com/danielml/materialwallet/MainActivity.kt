package com.danielml.materialwallet

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.danielml.materialwallet.fragments.MainScreenFragment


class MainActivity : AppCompatActivity() {
    private val mainFragment = MainScreenFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            // Attach the main fragment to its container
            supportFragmentManager.beginTransaction()
                .add(R.id.main_fragment_container, mainFragment)
                .commit()
        }
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 1) {
            supportFragmentManager.popBackStackImmediate()
        } else {
            super.onBackPressed()
        }
    }
}
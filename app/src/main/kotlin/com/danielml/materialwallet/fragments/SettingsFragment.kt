package com.danielml.materialwallet.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.preference.PreferenceFragmentCompat
import com.danielml.materialwallet.Global
import com.danielml.materialwallet.R

class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        Global.allowBackPress = false
        setPreferencesFromResource(R.xml.settings_preferences, null)
    }
}
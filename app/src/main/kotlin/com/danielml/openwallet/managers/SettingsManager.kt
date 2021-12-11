package com.danielml.openwallet.managers

import android.content.Context

class SettingsManager {
    companion object {
        private const val SETTINGS_SHARED_PREFS = "settings"

        const val NETWORK_TYPE_SETTING = "network_type"
        const val MAIN_NET_SETTING_VALUE = "1"
        const val TEST_NET_SETTING_VALUE = "2"
        
        /*
         * Stores the given setting information
         */
        fun setSetting(context: Context, settingName: String, settingValue: String) {
            val sharedPreferences = context.getSharedPreferences(SETTINGS_SHARED_PREFS, 0)
            val editor = sharedPreferences.edit()
            editor.putString(settingName, settingValue)
            editor.apply()
        }

        /*
         * @returns the given setting information
         */
        fun getSetting(context: Context, settingName: String, defaultValue: String = "") : String {
            val sharedPreferences = context.getSharedPreferences(SETTINGS_SHARED_PREFS, 0)
            return sharedPreferences.getString(settingName, defaultValue) ?: defaultValue
        }
    }
}
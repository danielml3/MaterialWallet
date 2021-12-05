package com.danielml.openwallet.managers

import android.content.Context
import com.danielml.openwallet.Global

class MnemonicManager {
    companion object {
        /*
         * @returns the whole mnemonic list from the storage
         */
        fun getMnemonicList(context: Context): HashSet<String> {
            val sharedPreferences = context.getSharedPreferences(Global.SHARED_PREFS_MNEMONICS, 0)
            return HashSet(sharedPreferences.getStringSet(Global.SHARED_PREFS_MNEMONICS_LIST, listOf("").toSet())!!)
        }

        /*
         * Saves an mnemonic to a local database
         */
        fun storeMnemonic(context: Context, mnemonic: List<String>) {
            val sharedPreferences = context.getSharedPreferences(Global.SHARED_PREFS_MNEMONICS, 0)
            val mnemonicList = getMnemonicList(context)
            mnemonicList.add(mnemonic.joinToString(" "))

            val editor = sharedPreferences.edit()
            editor.putStringSet(Global.SHARED_PREFS_MNEMONICS_LIST, mnemonicList)
            editor.apply()
        }
    }
}
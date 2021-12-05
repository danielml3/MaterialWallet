package com.danielml.materialwallet.managers

import android.content.Context
import org.json.JSONException
import org.json.JSONObject
import java.io.File

class WalletDatabaseManager {
    companion object {
        private const val databaseFilename = "wallet-database.json"

        const val mnemonicKey = "mnemonic"
        const val walletIdKey = "walletId"

        /*
         * @returns a JSON array containing the wallets' information
         */
        fun getWalletInformation(context: Context): JSONObject {
            return try {
                JSONObject(getDatabase(context).readText())
            } catch (e: JSONException) {
                JSONObject()
            }
        }

        /*
         * Saves the given wallet information to the local database
         */
        fun storeWalletInformation(context: Context, mnemonic: String, walletId: String) {
            val walletJSONObject = getWalletInformation(context)
            walletJSONObject.put(mnemonicKey, mnemonic)
            walletJSONObject.put(walletIdKey, walletId)

            getDatabase(context).writeText(walletJSONObject.toString())
        }

        /*
         * @returns the full database file object
         */
        private fun getDatabase(context: Context): File {
            val dataDir = context.applicationContext.dataDir
            val databaseFile = File("$dataDir/$databaseFilename")
            if (!databaseFile.exists()) {
                databaseFile.writeText("")
            }

            return databaseFile
        }
    }
}
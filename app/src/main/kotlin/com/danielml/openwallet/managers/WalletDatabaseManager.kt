package com.danielml.openwallet.managers

import android.content.Context
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.File

class WalletDatabaseManager {
    companion object {
        private val databaseFilename = "wallet-database.json"

        val mnemonicKey = "mnemonic"
        val walletNameKey = "wallet_name"

        /*
         * @returns a JSON array containing the wallets' information
         */
        fun getWalletInformationList(context: Context): JSONArray {
            return try {
                JSONArray(getDatabase(context).readText())
            } catch (e: JSONException) {
                JSONArray()
            }
        }

        /*
         * Saves the given wallet information to the local database
         */
        fun storeWalletInformation(context: Context, mnemonic: List<String>, walletName: String) {
            val mnemonicString = mnemonic.joinToString(" ")

            val walletInformationList = deleteWallet(context, mnemonicString)
            val walletJSONObject = JSONObject()
            walletJSONObject.put(mnemonicKey, mnemonicString)
            walletJSONObject.put(walletNameKey, walletName)

            walletInformationList.put(walletJSONObject)
            getDatabase(context).writeText(walletInformationList.toString())
        }

        /*
         * Deletes a wallet from the database
         */
        fun deleteWallet(context: Context, mnemonic: String) : JSONArray {
            val clearedWalletList = JSONArray()
            val walletInformationList = getWalletInformationList(context)

            // Clear previous entries of the same wallet, as they will be overwritten now
            for (i in 0 until walletInformationList.length()) {
                try {
                    val walletJSONObject = walletInformationList.getJSONObject(i)
                    if (walletJSONObject.getString(mnemonicKey) != mnemonic) {
                        clearedWalletList.put(walletJSONObject)
                    }
                } catch (e: JSONException) {
                    break
                }
            }

            getDatabase(context).writeText(clearedWalletList.toString())
            return clearedWalletList
        }

        /*
         * @returns the full database file
         */
        private fun getDatabase(context: Context) : File {
            val dataDir = context.applicationContext.dataDir
            val databaseFile = File("$dataDir/$databaseFilename")
            if (!databaseFile.exists()) {
                databaseFile.writeText("")
            }

            return databaseFile
        }
    }
}
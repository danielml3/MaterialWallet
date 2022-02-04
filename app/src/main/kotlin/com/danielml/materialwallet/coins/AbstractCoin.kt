package com.danielml.materialwallet.coins

import com.danielml.materialwallet.Global
import org.bitcoinj.core.NetworkParameters
import org.bitcoinj.params.MainNetParams
import org.bitcoinj.script.Script

abstract class AbstractCoin {
    /*
     * @returns the coin name (for example, "bitcoin")
     */
    abstract fun getName() : String

    /*
     * @returns the coin name that may be shown to the user
     */
    abstract fun getDisplayName() : String

    /*
     * @returns the network parameters used by the coin
     *
     * This should use isTestNet in order to return MainNet or TestNet parameters
     */
    abstract fun getNetworkParameters() : NetworkParameters

    /*
     * @returns the unit used to display the balance, for example "BTC"
     */
    abstract fun getUnitString() : String

    /*
     * @returns the script type used by the coin
     */
    abstract fun getScriptType() : Script.ScriptType

    /*
     * @returns true if the coin shall return TestNet parameters
     */
    fun isTestNet() : Boolean {
        return Global.isDebuggable
    }
}
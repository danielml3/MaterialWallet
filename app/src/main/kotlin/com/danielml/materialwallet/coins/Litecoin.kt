package com.danielml.materialwallet.coins

import org.bitcoinj.core.NetworkParameters
import org.bitcoinj.params.LitecoinMainNetParams
import org.bitcoinj.params.LitecoinTestNet3Params
import org.bitcoinj.script.Script

class Litecoin : AbstractCoin() {
    override fun getName() : String {
        return "litecoin"
    }

    override fun getDisplayName(): String {
        return "Litecoin"
    }

    override fun getNetworkParameters() : NetworkParameters {
        return LitecoinMainNetParams.get()
    }

    override fun getUnitString(): String {
        return "LTC"
    }

    override fun getScriptType(): Script.ScriptType {
        return Script.ScriptType.P2PKH
    }

    companion object {
        var instance: Litecoin? = null
        fun get(): AbstractCoin {
            if (instance == null) {
                instance = Litecoin()
            }

            return instance!!
        }
    }
}
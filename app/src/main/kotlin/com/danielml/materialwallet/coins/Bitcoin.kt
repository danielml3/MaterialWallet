package com.danielml.materialwallet.coins

import org.bitcoinj.core.NetworkParameters
import org.bitcoinj.params.MainNetParams
import org.bitcoinj.params.TestNet3Params
import org.bitcoinj.script.Script

class Bitcoin : AbstractCoin() {
    override fun getName() : String {
        return "bitcoin"
    }

    override fun getDisplayName(): String {
        return "Bitcoin"
    }

    override fun getNetworkParameters() : NetworkParameters {
        return if (isTestNet()) {
            TestNet3Params.get()
        } else {
            MainNetParams.get()
        }
    }

    override fun getUnitString(): String {
        return "BTC"
    }

    override fun getScriptType(): Script.ScriptType {
        return Script.ScriptType.P2WPKH
    }

    companion object {
        var instance: Bitcoin? = null
        fun get(): AbstractCoin {
            if (instance == null) {
                instance = Bitcoin()
            }

            return instance!!
        }
    }
}
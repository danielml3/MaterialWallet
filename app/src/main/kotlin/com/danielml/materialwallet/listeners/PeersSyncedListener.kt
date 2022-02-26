package com.danielml.materialwallet.listeners

import org.bitcoinj.core.Peer
import org.bitcoinj.core.listeners.PeerConnectedEventListener
import org.bitcoinj.core.listeners.PeerDisconnectedEventListener
import org.bitcoinj.kits.WalletAppKit

/*
 * This class allows keeping track of the peer sync status
 *
 * The onPeersSyncStatusChanged will only be executed after the sync status
 * has really changed, not instantly after the listener registration
 */
abstract class PeersSyncedListener : PeerConnectedEventListener, PeerDisconnectedEventListener {
    private var walletAppKit: WalletAppKit? = null

    /*
     * This function will be executed once the sync status has changed
     */
    abstract fun onPeersSyncStatusChanged(synced: Boolean)

    fun register(walletKit: WalletAppKit) {
        walletAppKit = walletKit
        walletAppKit?.peerGroup()?.addConnectedEventListener(this)
        walletAppKit?.peerGroup()?.addDisconnectedEventListener(this)

        onPeersSyncStatusChanged(isSynced())
    }

    fun unregister() {
        walletAppKit?.peerGroup()?.removeConnectedEventListener(this)
        walletAppKit?.peerGroup()?.removeDisconnectedEventListener(this)
    }

    private fun isSynced(): Boolean {
        val connectedPeers = walletAppKit?.peerGroup()?.connectedPeers
        return if (connectedPeers != null && connectedPeers.isNotEmpty()) {
            var bestBlockHeightDifference = Int.MAX_VALUE

            for (peer: Peer in connectedPeers) {
                val heightDifference = peer.peerBlockHeightDifference
                if (heightDifference in 0 until bestBlockHeightDifference) {
                    bestBlockHeightDifference = heightDifference
                }
            }

            (bestBlockHeightDifference == 0)
        } else {
            false
        }
    }

    override fun onPeerConnected(peer: Peer?, peerCount: Int) {
        onPeersSyncStatusChanged(isSynced())
    }

    override fun onPeerDisconnected(peer: Peer?, peerCount: Int) {
        onPeersSyncStatusChanged(isSynced())
    }
}
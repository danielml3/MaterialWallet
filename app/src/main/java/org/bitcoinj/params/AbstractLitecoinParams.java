/*
 * Copyright 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.bitcoinj.params;

import org.bitcoinj.core.*;
import org.bitcoinj.store.BlockStore;
import org.bitcoinj.utils.MonetaryFormat;
import org.bitcoinj.core.LitecoinSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.bitcoinj.core.Coin.COIN;

/**
 * Common parameters for Litecoin networks.
 */
public abstract class AbstractLitecoinParams extends NetworkParameters {
    protected Block genesisBlock;
    /** Standard format for the LITE denomination. */
    public static final MonetaryFormat LITE;
    /** Standard format for the mLITE denomination. */
    public static final MonetaryFormat MLITE;
    /** Standard format for the Liteoshi denomination. */
    public static final MonetaryFormat LITEOSHI;

    public static final int LITE_TARGET_TIMESPAN = (int) (3.5 * 24 * 60 * 60); // 3.5 days
    public static final int LITE_TARGET_SPACING = (int) (2.5 * 60); // 2.5 minutes
    public static final int LITE_INTERVAL = LITE_TARGET_TIMESPAN / LITE_TARGET_SPACING;

    /**
     * The maximum number of coins to be generated
     */
    public static final long MAX_LITECOINS = 21000000; // TODO: Needs to be 840000000

    /**
     * The maximum money to be generated
     */
    public static final Coin MAX_LITECOIN_MONEY = COIN.multiply(MAX_LITECOINS);

    /** Currency code for base 1 Litecoin. */
    public static final String CODE_LITE = "LITE";
    /** Currency code for base 1/1,000 Litecoin. */
    public static final String CODE_MLITE = "mLITE";
    /** Currency code for base 1/100,000,000 Litecoin. */
    public static final String CODE_LITEOSHI = "Liteoshi";

    static {
        LITE = MonetaryFormat.BTC.noCode()
            .code(0, CODE_LITE)
            .code(3, CODE_MLITE)
            .code(7, CODE_LITEOSHI);
        MLITE = LITE.shift(3).minDecimals(2).optionalDecimals(2);
        LITEOSHI = LITE.shift(7).minDecimals(0).optionalDecimals(2);
    }

    /** The string returned by getId() for the main, production network where people trade things. */
    public static final String ID_LITE_MAINNET = "org.litecoin.production";
    /** The string returned by getId() for the testnet. */
    public static final String ID_LITE_TESTNET = "org.litecoin.test";

    public static final int LITECOIN_PROTOCOL_VERSION_MINIMUM = 70002;
    public static final int LITECOIN_PROTOCOL_VERSION_CURRENT = 70012;

    protected Logger log = LoggerFactory.getLogger(AbstractLitecoinParams.class);

    public AbstractLitecoinParams() {
        super();
        interval = LITE_INTERVAL;
        targetTimespan = LITE_TARGET_TIMESPAN;
        maxTarget = Utils.decodeCompactBits(0x1e0fffffL);

        packetMagic = 0xfbc0b6db;
        bip32HeaderP2PKHpub = 0x0488C42E; //The 4 byte header that serializes in base58 to "xpub". (?)
        bip32HeaderP2PKHpriv = 0x0488E1F4; //The 4 byte header that serializes in base58 to "xprv" (?)
    }

    public MonetaryFormat getMonetaryFormat() {
        return LITE;
    }

    @Override
    public Coin getMaxMoney() {
        return MAX_LITECOIN_MONEY;
    }

    @Override
    public Coin getMinNonDustOutput() {
        return Coin.COIN;
    }

    @Override
    public String getUriScheme() {
        return "litecoin";
    }

    @Override
    public boolean hasMaxMoney() {
        return true;
    }


    @Override
    public void checkDifficultyTransitions(StoredBlock storedPrev, Block nextBlock, BlockStore blockStore) {}

    @Override
    public LitecoinSerializer getSerializer(boolean parseRetain) {
        return new LitecoinSerializer(this, parseRetain);
    }

    @Override
    public int getProtocolVersionNum(final ProtocolVersion version) {
        switch (version) {
            case PONG:
            case BLOOM_FILTER:
                return version.getBitcoinProtocolVersion();
            case CURRENT:
                return LITECOIN_PROTOCOL_VERSION_CURRENT;
            case MINIMUM:
            default:
                return LITECOIN_PROTOCOL_VERSION_MINIMUM;
        }
    }

    private static class CheckpointEncounteredException extends Exception {}
}

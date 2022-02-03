package org.bitcoinj.core;

/**
 * @author jrn
 */
public class LitecoinSerializer extends BitcoinSerializer {
    public LitecoinSerializer(NetworkParameters params, boolean parseRetain) {
        super(params, parseRetain);
    }

    @Override
    public Block makeBlock(final byte[] payloadBytes, final int offset, final int length) throws ProtocolException {
        return new LitecoinBlock(getParameters(), payloadBytes, offset, this, length);
    }
}

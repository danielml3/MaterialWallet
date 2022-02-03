package org.bitcoinj.core;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public class LitecoinBlock extends Block {
    public LitecoinBlock(NetworkParameters params, long setVersion) {
        super(params, setVersion);
    }

    public LitecoinBlock(NetworkParameters params, byte[] payloadBytes, MessageSerializer serializer, int length) throws ProtocolException {
        super(params, payloadBytes, serializer, length);
    }

    public LitecoinBlock(NetworkParameters params, byte[] payloadBytes, int offset, MessageSerializer serializer, int length) throws ProtocolException {
        super(params, payloadBytes, offset, serializer, length);
    }

    public LitecoinBlock(NetworkParameters params, byte[] payloadBytes, int offset, @Nullable Message parent, MessageSerializer serializer, int length) throws ProtocolException {
        super(params, payloadBytes, offset, parent, serializer, length);
    }

    public LitecoinBlock(NetworkParameters params, long version, Sha256Hash prevBlockHash, Sha256Hash merkleRoot, long time, long difficultyTarget, long nonce, List<Transaction> transactions) {
        super(params, version, prevBlockHash, merkleRoot, time, difficultyTarget, nonce, transactions);
    }

    @Override
    protected boolean checkProofOfWork(boolean throwException) throws VerificationException {
        return true;
    }
}

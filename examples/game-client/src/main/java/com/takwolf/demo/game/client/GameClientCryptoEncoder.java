package com.takwolf.demo.game.client;

import com.takwolf.demo.game.common.Aes256GcmUtils;
import com.takwolf.demo.game.common.GameDefine;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.util.concurrent.atomic.AtomicLong;

public class GameClientCryptoEncoder extends MessageToByteEncoder<ByteBuf> {
    private final int conversationId;
    private final SecretKeySpec key;
    private final AtomicLong sequenceSeed;

    public GameClientCryptoEncoder(int conversationId, SecretKeySpec key, AtomicLong sequenceSeed) {
        if (sequenceSeed.get() < 0) {
            throw new IllegalArgumentException("sequenceSeed must start from positive");
        }
        this.conversationId = conversationId;
        this.key = key;
        this.sequenceSeed = sequenceSeed;
    }

    @Override
    protected void encode(ChannelHandlerContext context, ByteBuf in, ByteBuf out) throws GeneralSecurityException {
        long sequence = sequenceSeed.getAndIncrement();
        if (sequence < 0) {
            throw new IllegalStateException("sequence overflow, key must be renewed");
        }

        ByteBuffer nonceBuffer = ByteBuffer.allocate(12);
        nonceBuffer.put(GameDefine.NONCE_CLIENT_PREFIX);
        nonceBuffer.putLong(sequence);
        GCMParameterSpec nonce = Aes256GcmUtils.createNonce(nonceBuffer.array());

        ByteBuffer aadBuffer = ByteBuffer.allocate(12);
        aadBuffer.putInt(conversationId);
        aadBuffer.putLong(sequence);
        byte[] aad = aadBuffer.array();

        byte[] plaintext = new byte[in.readableBytes()];
        in.readBytes(plaintext);

        byte[] ciphertext = Aes256GcmUtils.encrypt(key, nonce, aad, plaintext);

        out.writeInt(conversationId);
        out.writeLong(sequence);
        out.writeBytes(ciphertext);
    }
}

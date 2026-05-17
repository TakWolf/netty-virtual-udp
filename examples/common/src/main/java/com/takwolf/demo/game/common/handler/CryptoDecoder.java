package com.takwolf.demo.game.common.handler;

import com.takwolf.demo.game.common.util.Aes256GcmUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.util.List;

@Slf4j
public class CryptoDecoder extends MessageToMessageDecoder<ByteBuf> {
    private final int conversationId;
    private final SecretKeySpec key;
    private final byte[] noncePrefix;

    public CryptoDecoder(int conversationId, SecretKeySpec key, byte[] noncePrefix) {
        if (noncePrefix.length != 4) {
            throw new IllegalArgumentException("noncePrefix must be 4 bytes");
        }
        this.conversationId = conversationId;
        this.key = key;
        this.noncePrefix = noncePrefix;
    }

    @Override
    protected void decode(ChannelHandlerContext context, ByteBuf in, List<Object> out) {
        if (in.readableBytes() < 4 + 8 + Aes256GcmUtils.TAG_BYTES_LENGTH) {
            log.warn("Illegal data length: {}", in.readableBytes());
            return;
        }

        int conversationId = in.readInt();
        if (conversationId != this.conversationId) {
            log.warn("Illegal conversationId: {}", conversationId);
            return;
        }

        long sequence = in.readLong();
        if (sequence < 0) {
            log.warn("Sequence overflow: {}", sequence);
            return;
        }

        ByteBuffer nonceBuffer = ByteBuffer.allocate(12);
        nonceBuffer.put(noncePrefix);
        nonceBuffer.putLong(sequence);
        GCMParameterSpec nonce = Aes256GcmUtils.createNonce(nonceBuffer.array());

        ByteBuffer aadBuffer = ByteBuffer.allocate(12);
        aadBuffer.putInt(conversationId);
        aadBuffer.putLong(sequence);
        byte[] aad = aadBuffer.array();

        byte[] ciphertext = new byte[in.readableBytes()];
        in.readBytes(ciphertext);

        try {
            byte[] plaintext = Aes256GcmUtils.decrypt(key, nonce, aad, ciphertext);
            out.add(Unpooled.wrappedBuffer(plaintext));
        } catch (GeneralSecurityException ignored) {
            log.warn("Illegal ciphertext.");
        }
    }
}

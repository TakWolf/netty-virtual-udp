package com.takwolf.demo.game.client;

import com.takwolf.demo.game.common.Aes256GcmUtils;
import com.takwolf.demo.game.common.GameDefine;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;

import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.util.List;

@Slf4j
@NullMarked
public class GameClientCryptoDecoder extends MessageToMessageDecoder<ByteBuf> {
    private final int conversationId;
    private final SecretKeySpec key;

    public GameClientCryptoDecoder(int conversationId, SecretKeySpec key) {
        this.conversationId = conversationId;
        this.key = key;
    }

    @Override
    protected void decode(ChannelHandlerContext context, ByteBuf in, List<Object> out) {
        if (in.readableBytes() < 4 + 8 + Aes256GcmUtils.TAG_BYTES_LENGTH) {
            log.warn("Illegal data length {}, ignore", in.readableBytes());
            return;
        }

        int conversationId = in.readInt();
        if (conversationId != this.conversationId) {
            log.warn("Illegal conversationId {}, ignore", conversationId);
            return;
        }

        long sequence = in.readLong();

        ByteBuffer nonceBuffer = ByteBuffer.allocate(12);
        nonceBuffer.put(GameDefine.NONCE_SERVER_PREFIX);
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
            log.warn("Illegal ciphertext, ignore");
        }
    }
}

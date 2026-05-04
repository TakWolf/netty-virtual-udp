package com.takwolf.demo.game.server;

import com.takwolf.demo.game.common.Aes256GcmUtils;
import com.takwolf.demo.game.common.GameCrypto;
import com.takwolf.demo.game.common.GameDefine;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.jspecify.annotations.NullMarked;

import javax.crypto.spec.GCMParameterSpec;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;

@NullMarked
public class GameServerCryptoEncoder extends MessageToByteEncoder<ByteBuf> {
    @Override
    protected void encode(ChannelHandlerContext context, ByteBuf in, ByteBuf out) throws GeneralSecurityException {
        Channel channel = context.channel();
        GameCrypto gameCrypto = channel.attr(GameServerDefine.ATTR_GAME_CRYPTO).get();

        long sequence = gameCrypto.getSequenceSeed().getAndIncrement();
        if (sequence < 0) {
            throw new IllegalStateException("sequence overflow, key must be renewed");
        }

        ByteBuffer nonceBuffer = ByteBuffer.allocate(12);
        nonceBuffer.put(GameDefine.NONCE_SERVER_PREFIX);
        nonceBuffer.putLong(sequence);
        GCMParameterSpec nonce = Aes256GcmUtils.createNonce(nonceBuffer.array());

        ByteBuffer aadBuffer = ByteBuffer.allocate(12);
        aadBuffer.putInt(gameCrypto.getConversationId());
        aadBuffer.putLong(sequence);
        byte[] aad = aadBuffer.array();

        byte[] plaintext = new byte[in.readableBytes()];
        in.readBytes(plaintext);

        byte[] ciphertext = Aes256GcmUtils.encrypt(gameCrypto.getServerKey(), nonce, aad, plaintext);

        out.writeInt(gameCrypto.getConversationId());
        out.writeLong(sequence);
        out.writeBytes(ciphertext);
    }
}

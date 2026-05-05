package com.takwolf.demo.game.server;

import com.takwolf.demo.game.common.Aes256GcmUtils;
import com.takwolf.demo.game.common.GameCrypto;
import com.takwolf.demo.game.common.GameDefine;
import com.takwolf.demo.game.common.UserService;
import com.takwolf.netty.vudp.channel.ChildVirtualChannel;
import com.takwolf.netty.vudp.router.RouteResult;
import com.takwolf.netty.vudp.router.VirtualChannelRouter;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.socket.DatagramPacket;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.spec.GCMParameterSpec;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.util.Optional;

@Slf4j
public class GameServerChannelRouter implements VirtualChannelRouter<Integer, GameCrypto, DatagramPacket> {
    private final UserService userService = new UserService();

    @Override
    public Optional<Integer> parseKey(DatagramPacket packet) {
        ByteBuf in = packet.content();

        if (in.readableBytes() < 4 + 8 + Aes256GcmUtils.TAG_BYTES_LENGTH) {
            log.warn("Illegal data length {}, ignore", in.readableBytes());
            return Optional.empty();
        }

        int conversationId = in.readInt();
        return Optional.of(conversationId);
    }

    @Override
    public RouteResult<GameCrypto> newContext(Integer conversationId) {
        UserService.LoginInfo loginInfo = userService.getLoginInfo(conversationId).orElse(null);
        if (loginInfo == null) {
            log.warn("Illegal conversationId {}, ignore", conversationId);
            return RouteResult.none();
        }
        GameCrypto gameCrypto = GameCrypto.fromLoginInfo(loginInfo, 0);
        return RouteResult.of(gameCrypto);
    }

    @Override
    public GameCrypto existingContext(Integer conversationId, ChildVirtualChannel channel) {
        return channel.attr(GameServerDefine.ATTR_GAME_CRYPTO).get();
    }

    @Override
    public void attachContext(Integer conversationId, GameCrypto gameCrypto, ChildVirtualChannel channel) {
        channel.attr(GameServerDefine.ATTR_GAME_CRYPTO).set(gameCrypto);
    }

    @Override
    public RouteResult<DatagramPacket> routeMessage(Integer conversationId, GameCrypto gameCrypto, DatagramPacket packet) {
        ByteBuf in = packet.content();
        long sequence = in.readLong();

        ByteBuffer nonceBuffer = ByteBuffer.allocate(12);
        nonceBuffer.put(GameDefine.NONCE_CLIENT_PREFIX);
        nonceBuffer.putLong(sequence);
        GCMParameterSpec nonce = Aes256GcmUtils.createNonce(nonceBuffer.array());

        ByteBuffer aadBuffer = ByteBuffer.allocate(12);
        aadBuffer.putInt(conversationId);
        aadBuffer.putLong(sequence);
        byte[] aad = aadBuffer.array();

        byte[] ciphertext = new byte[in.readableBytes()];
        in.readBytes(ciphertext);

        byte[] plaintext;
        try {
            plaintext = Aes256GcmUtils.decrypt(gameCrypto.getClientKey(), nonce, aad, ciphertext);
        } catch (GeneralSecurityException ignored) {
            log.warn("Illegal ciphertext, ignore");
            return RouteResult.none();
        }

        return RouteResult.of(packet.replace(Unpooled.wrappedBuffer(plaintext)));
    }
}

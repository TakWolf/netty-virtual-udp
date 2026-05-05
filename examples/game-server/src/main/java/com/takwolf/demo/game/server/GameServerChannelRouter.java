package com.takwolf.demo.game.server;

import com.takwolf.demo.game.common.service.GameCrypto;
import com.takwolf.demo.game.common.service.UserService;
import com.takwolf.demo.game.common.util.Aes256GcmUtils;
import com.takwolf.netty.vudp.channel.ChildVirtualChannel;
import com.takwolf.netty.vudp.router.RouteResult;
import com.takwolf.netty.vudp.router.VirtualChannelRouter;
import io.netty.buffer.ByteBuf;
import io.netty.channel.socket.DatagramPacket;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.spec.GCMParameterSpec;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class GameServerChannelRouter implements VirtualChannelRouter<Integer, GameCrypto, GameServerChannelRouter.RouteMessage> {
    private final UserService userService;

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
        return channel.attr(GameCrypto.ATTR).get();
    }

    @Override
    public void attachContext(Integer conversationId, GameCrypto gameCrypto, ChildVirtualChannel channel) {
        channel.attr(GameCrypto.ATTR).set(gameCrypto);
    }

    @Override
    public RouteResult<RouteMessage> routeMessage(Integer conversationId, GameCrypto gameCrypto, DatagramPacket packet) {
        ByteBuf in = packet.content();
        long sequence = in.readLong();

        ByteBuffer nonceBuffer = ByteBuffer.allocate(12);
        nonceBuffer.put(GameCrypto.NONCE_PREFIX_CLIENT);
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

        return RouteResult.of(new RouteMessage(packet.sender(), sequence, plaintext));
    }

    @Getter
    @RequiredArgsConstructor
    public static final class RouteMessage {
        private final InetSocketAddress remoteAddress;
        private final long sequence;
        private final byte[] data;
    }
}

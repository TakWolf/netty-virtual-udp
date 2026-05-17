package com.takwolf.demo.server.handler;

import com.takwolf.demo.common.domain.CryptoInfo;
import com.takwolf.demo.common.domain.LoginInfo;
import com.takwolf.demo.common.service.UserService;
import com.takwolf.demo.common.util.Aes256GcmUtils;
import com.takwolf.demo.server.domain.RouteMessage;
import com.takwolf.netty.vudp.channel.ChildVirtualChannel;
import com.takwolf.netty.vudp.router.RouteKeyChannelId;
import com.takwolf.netty.vudp.router.RouteResult;
import com.takwolf.netty.vudp.router.VirtualChannelRouter;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelId;
import io.netty.channel.socket.DatagramPacket;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.spec.GCMParameterSpec;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class ServerChannelRouter implements VirtualChannelRouter<Integer, CryptoInfo, RouteMessage> {
    private final UserService userService;

    @Override
    public Optional<Integer> routeKey(DatagramPacket packet) {
        ByteBuf in = packet.content();

        if (in.readableBytes() < 4 + 8 + Aes256GcmUtils.TAG_BYTES_LENGTH) {
            log.warn("Illegal data length: {}", in.readableBytes());
            return Optional.empty();
        }

        int conversationId = in.readInt();
        return Optional.of(conversationId);
    }

    @Override
    public ChannelId channelId(Integer conversationId) {
        return new RouteKeyChannelId(conversationId);
    }

    @Override
    public RouteResult<CryptoInfo> newContext(Integer conversationId) {
        LoginInfo loginInfo = userService.getLoginInfo(conversationId).orElse(null);
        if (loginInfo == null) {
            log.warn("Illegal conversationId: {}", conversationId);
            return RouteResult.none();
        }
        CryptoInfo cryptoInfo = CryptoInfo.fromLoginInfo(loginInfo, 0);
        return RouteResult.of(cryptoInfo);
    }

    @Override
    public CryptoInfo existingContext(Integer conversationId, ChildVirtualChannel channel) {
        return channel.attr(CryptoInfo.ATTR).get();
    }

    @Override
    public void attachContext(Integer conversationId, CryptoInfo cryptoInfo, ChildVirtualChannel channel) {
        channel.attr(CryptoInfo.ATTR).set(cryptoInfo);
    }

    @Override
    public RouteResult<RouteMessage> routeMessage(Integer conversationId, CryptoInfo cryptoInfo, DatagramPacket packet) {
        ByteBuf in = packet.content();

        long sequence = in.readLong();
        if (sequence < 0) {
            log.warn("Sequence overflow: {}", sequence);
            return RouteResult.none();
        }

        ByteBuffer nonceBuffer = ByteBuffer.allocate(12);
        nonceBuffer.put(CryptoInfo.NONCE_PREFIX_CLIENT);
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
            plaintext = Aes256GcmUtils.decrypt(cryptoInfo.getClientKey(), nonce, aad, ciphertext);
        } catch (GeneralSecurityException ignored) {
            log.warn("Illegal ciphertext.");
            return RouteResult.none();
        }

        return RouteResult.of(new RouteMessage(packet.sender(), sequence, Unpooled.wrappedBuffer(plaintext)));
    }
}

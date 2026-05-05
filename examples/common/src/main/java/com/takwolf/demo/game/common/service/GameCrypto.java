package com.takwolf.demo.game.common.service;

import com.takwolf.demo.game.common.util.Aes256GcmUtils;
import io.netty.util.AttributeKey;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.codec.binary.Hex;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicLong;

@Getter
@RequiredArgsConstructor
public final class GameCrypto {
    public static final AttributeKey<GameCrypto> ATTR = AttributeKey.valueOf("gameCrypto");

    public static final byte[] NONCE_PREFIX_SERVER = "S2C0".getBytes(StandardCharsets.UTF_8);
    public static final byte[] NONCE_PREFIX_CLIENT = "C2S0".getBytes(StandardCharsets.UTF_8);

    @SneakyThrows
    public static GameCrypto fromLoginInfo(UserService.LoginInfo loginInfo, long sequenceInitialValue) {
        SecretKeySpec serverKey = Aes256GcmUtils.createKey(Hex.decodeHex(loginInfo.getServerKey()));
        SecretKeySpec clientKey = Aes256GcmUtils.createKey(Hex.decodeHex(loginInfo.getClientKey()));
        return new GameCrypto(loginInfo.getConversationId(), serverKey, clientKey, new AtomicLong(sequenceInitialValue));
    }

    private final int conversationId;
    private final SecretKeySpec serverKey;
    private final SecretKeySpec clientKey;
    private final AtomicLong sequenceSeed;
}

package com.takwolf.demo.game.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.codec.binary.Hex;

import javax.crypto.spec.SecretKeySpec;
import java.util.concurrent.atomic.AtomicLong;

@Getter
@RequiredArgsConstructor
public final class GameCrypto {
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

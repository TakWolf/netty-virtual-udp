package com.takwolf.demo.game.common;

import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.Test;

import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class Aes256GcmUtilsTests {
    @Test
    public void testAes256Gcm() throws GeneralSecurityException {
        SecretKeySpec key = Aes256GcmUtils.createKey(RandomUtils.secureStrong().randomBytes(Aes256GcmUtils.KEY_BYTES_LENGTH));
        GCMParameterSpec nonce = Aes256GcmUtils.createNonce(RandomUtils.secureStrong().randomBytes(Aes256GcmUtils.NONCE_BYTES_LENGTH));
        byte[] aad = "This is Additional Authenticated Data".getBytes(StandardCharsets.UTF_8);
        byte[] data = RandomUtils.secureStrong().randomBytes(100);
        byte[] encryptedData = Aes256GcmUtils.encrypt(key, nonce, aad, data);
        byte[] decryptedData = Aes256GcmUtils.decrypt(key, nonce, aad, encryptedData);
        assertArrayEquals(data, decryptedData);
    }
}

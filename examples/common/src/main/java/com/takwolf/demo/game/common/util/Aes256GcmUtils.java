package com.takwolf.demo.game.common.util;

import org.jspecify.annotations.Nullable;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.GeneralSecurityException;

/**
 * AES-256-GCM 加解密工具。
 *
 * <p>基于 256 位（32 字节）密钥的 AES 加密算法，GCM 模式在提供机密性的同时，通过认证标签（16 字节）保证数据完整性。
 *
 * <p><b>输出格式</b>
 *
 * <p>{@link #encrypt} 的输出格式为 {@code 密文 + 认证标签}，因此密文长度会比输入的明文多 16 字节。
 * <p>{@link #decrypt} 要求输入相同格式的数据，即 {@code 密文 + 认证标签}。
 *
 * <p><b>安全约束</b>
 *
 * <p>同一密钥下 Nonce 绝不能重用，每次加密都使用全新的随机值。
 * <p>如果使用 AAD（关联认证数据），加密和解密时必须传入完全相同的 AAD 值，否则解密会失败。
 */
public final class Aes256GcmUtils {
    private Aes256GcmUtils() {}

    private static final String ALGORITHM = "AES";
    private static final String ALGORITHM_GCM = ALGORITHM + "/GCM/NoPadding";
    private static final int TAG_BITS_LENGTH = 128;

    public static final int KEY_BYTES_LENGTH = 32;
    public static final int NONCE_BYTES_LENGTH = 12;
    public static final int TAG_BYTES_LENGTH = TAG_BITS_LENGTH / 8;

    /**
     * 创建 AES-256-GCM 密钥规范对象。
     *
     * @param key 原始密钥字节，长度必须为 32 字节
     * @return 可复用的 {@link SecretKeySpec}（线程安全）密钥规范对象
     * @throws IllegalArgumentException 密钥长度不是 32 字节
     */
    public static SecretKeySpec createKey(byte[] key) {
        if (key.length != KEY_BYTES_LENGTH) {
            throw new IllegalArgumentException("key must be " + KEY_BYTES_LENGTH + " bytes");
        }
        return new SecretKeySpec(key, ALGORITHM);
    }

    /**
     * 创建 AES-GCM Nonce 参数规范对象。
     *
     * <p><b>重要：</b>同一密钥下，每次加密都必须使用全新的随机 Nonce 值。Nonce 本身只需要保证随机性和唯一性，不需要保密。
     *
     * @param nonce 原始 Nonce 字节，长度必须为 12 字节
     * @return {@link GCMParameterSpec} 参数规范对象
     * @throws IllegalArgumentException Nonce 长度不是 12 字节
     */
    public static GCMParameterSpec createNonce(byte[] nonce) {
        if (nonce.length != NONCE_BYTES_LENGTH) {
            throw new IllegalArgumentException("nonce must be " + NONCE_BYTES_LENGTH + " bytes");
        }
        return new GCMParameterSpec(TAG_BITS_LENGTH, nonce);
    }

    /**
     * 使用 AES-256-GCM 加密数据。
     *
     * <p>输出格式为 {@code 密文 + 认证标签}（总长度 = 明文长度 + 16 字节）。
     *
     * @param key AES 密钥，通过 {@link #createKey} 创建
     * @param nonce Nonce 参数，通过 {@link #createNonce} 创建，必须是 12 字节随机值
     * @param aad 关联认证数据（可选），加密和解密时必须完全一致；传入 {@code null} 表示不使用
     * @param plaintext 明文数据
     * @return 密文数据，格式为 {@code 密文 + 认证标签}
     * @throws GeneralSecurityException 加密失败
     */
    public static byte[] encrypt(SecretKeySpec key, GCMParameterSpec nonce, byte @Nullable [] aad, byte[] plaintext) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance(ALGORITHM_GCM);
        cipher.init(Cipher.ENCRYPT_MODE, key, nonce);
        if (aad != null && aad.length > 0) {
            cipher.updateAAD(aad);
        }
        return cipher.doFinal(plaintext);
    }

    /**
     * 使用 AES-256-GCM 解密数据。
     *
     * <p>输入格式必须为 {@code 密文 + 认证标签}（与 {@link #encrypt} 输出格式相同）。
     *
     * @param key AES 密钥，通过 {@link #createKey} 创建
     * @param nonce Nonce 参数，通过 {@link #createNonce} 创建，必须与加密时使用的 Nonce 完全相同
     * @param aad 关联认证数据（可选），必须与加密时传入的 AAD 完全一致；传入 {@code null} 表示不使用
     * @param ciphertext 密文数据，格式必须为 {@code 密文 + 认证标签}
     * @return 明文数据
     * @throws GeneralSecurityException 解密失败（包括认证标签不匹配）
     */
    public static byte[] decrypt(SecretKeySpec key, GCMParameterSpec nonce, byte @Nullable [] aad, byte[] ciphertext) throws GeneralSecurityException {
        if (ciphertext.length < TAG_BYTES_LENGTH) {
            throw new IllegalArgumentException("ciphertext must be longer than " + TAG_BYTES_LENGTH + " bytes, ensuring that it includes a tag");
        }
        Cipher cipher = Cipher.getInstance(ALGORITHM_GCM);
        cipher.init(Cipher.DECRYPT_MODE, key, nonce);
        if (aad != null && aad.length > 0) {
            cipher.updateAAD(aad);
        }
        return cipher.doFinal(ciphertext);
    }
}

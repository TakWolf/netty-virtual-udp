package com.takwolf.demo.game.common;

import java.nio.charset.StandardCharsets;

public interface GameDefine {
     byte[] NONCE_SERVER_PREFIX = "S2C0".getBytes(StandardCharsets.US_ASCII);
     byte[] NONCE_CLIENT_PREFIX = "C2S0".getBytes(StandardCharsets.US_ASCII);
}

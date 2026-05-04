package com.takwolf.demo.game.server;

import com.takwolf.demo.game.common.GameCrypto;
import io.netty.util.AttributeKey;

public interface GameServerDefine {
     AttributeKey<GameCrypto> ATTR_GAME_CRYPTO = AttributeKey.valueOf("gameCrypto");
}

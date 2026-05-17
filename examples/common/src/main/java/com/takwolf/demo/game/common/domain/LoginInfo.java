package com.takwolf.demo.game.common.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public final class LoginInfo {
    private final Integer conversationId;
    private final String serverKey;
    private final String clientKey;
}

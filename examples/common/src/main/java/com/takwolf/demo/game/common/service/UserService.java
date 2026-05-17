package com.takwolf.demo.game.common.service;

import com.takwolf.demo.game.common.domain.LoginInfo;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class UserService {
    private final Map<Integer, LoginInfo> loginInfos = Stream.of(
            new LoginInfo(1, "4b497153436a347044684f6754325671646f6b43767676684d5741736b325174", "42735935584f643733314f6c6d66737276716e6f765664336f4d636b32704d39"),
            new LoginInfo(2, "6c49744e794d4e794b766866536d314a447054635a6465583571387772687462", "647150705a45415969563330764a47734a3153427866546d70344e6972376836")
    ).collect(Collectors.toUnmodifiableMap(LoginInfo::getConversationId, Function.identity()));

    public Optional<LoginInfo> getLoginInfo(Integer conversationId) {
        return Optional.ofNullable(loginInfos.get(conversationId));
    }
}

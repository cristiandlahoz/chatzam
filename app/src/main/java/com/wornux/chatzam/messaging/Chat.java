package com.wornux.chatzam.messaging;

import com.wornux.chatzam.enums.ChatType;

import java.util.List;

public class Chat {
    private String chatId;
    private List<String> participants;
    private ChatType chatType;
    private Message lastMessage;

}

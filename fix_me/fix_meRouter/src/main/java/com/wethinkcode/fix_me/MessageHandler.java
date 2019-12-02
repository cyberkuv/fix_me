package com.wethinkcode.fix_me;

import java.util.ArrayList;
import java.util.List;

public class MessageHandler {
    private static List<MessageModel> messageList = new ArrayList<MessageModel>();
    public static List<MessageModel> getMessageList(){
        return messageList;
    }
}
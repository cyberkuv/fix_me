package com.wethinkcode.fix_me;

import java.nio.channels.SocketChannel;

public class MessageModel {
    private static int counter = 2000;
    private int id;
    private int messageFrom = 0;
    private SocketChannel socketChannel;
    private boolean isBroker;
    private String message = "";

    public MessageModel(SocketChannel socketChannel, boolean isBroker){
        this.id = nextId();
        this.isBroker = isBroker;
        this.socketChannel = socketChannel;
    }

    public boolean getIsBroker(){
        return this.isBroker;
    }

    public int getMessageFrom(){
        return this.messageFrom;
    }

    public void setMessageFeom(int massageFrom){
        this.messageFrom = massageFrom;
    }

    public String getMessage(){
        return this.message;
    }

    public void setMessage(String message){
        this.message = message;
    }

    private static int nextId(){
        return counter++;
    }

    public int getId(){
        return this.id;
    }

    public SocketChannel getSockectChannel(){
        return this.socketChannel;
    }

    public boolean isSamePort(SocketChannel socketChannel) throws Exception{
        return this.socketChannel.getRemoteAddress().equals(socketChannel.getRemoteAddress());
    }
}
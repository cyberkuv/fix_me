package com.wethinkcode.fix_me;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class Router {
    public static void main(String[] args) throws IOException {
        System.out.println("___Router Awaits Your Every Command___");
        Router router = new Router();
    }
    public Router() throws IOException {
        Selector selector = Selector.open();
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        InetAddress ip = InetAddress.getByName("localhost");
        serverSocketChannel.bind(new InetSocketAddress(ip, 5000));
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        SelectionKey key = null;
        while(true) {
            if(selector.select() <= 0)
                continue ;
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectedKeys.iterator();
            while(iterator.hasNext()) {
                key = (SelectionKey)iterator.next();
                iterator.remove();
                if(key.isAcceptable()) {
                    SocketChannel socketChannel = serverSocketChannel.accept();
                    socketChannel.configureBlocking(false);
                    socketChannel.register(selector, SelectionKey.OP_READ);
                    System.out.println("___Connection Accepted___: " + socketChannel.getLocalAddress());
                }
                if(key.isReadable()) {
                    SocketChannel socketChannel = (SocketChannel)key.channel();
                    ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
                    socketChannel.read(byteBuffer);
                    String result = new String(byteBuffer.array()).trim();
                    System.out.println("___Recieved Message___: " + result + ". ___Message Lenght___: " + result.length());
                    if(result.length() <= 0) {
                        socketChannel.close();
                        System.out.println("___Connection Closed___");
                        System.out.println("___Server Awaiting New Connection___");
                    }
                }
            }
        }
    }
}

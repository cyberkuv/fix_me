package com.wethinkcode.fix_me;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Random;

public class Router {
    public static void main(String[] args) throws IOException {
        System.out.println("___Router Awaits Your Every Command___");

        Selector selector = Selector.open();
        // Array of Ports
        int[] ports = new int[] { 5000, 5001 };
        // Random 6 digit value
        Random random = new Random();
        int broker = random.nextInt(999999);
        int market = random.nextInt(888888);
        // Looping through each port and binding it to the serversocketchannel
        for(int port : ports) {
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.socket().bind(new InetSocketAddress(port));
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        }
        // ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        // serverSocketChannel.configureBlocking(false);
        // InetAddress ip = InetAddress.getByName("localhost");
        // serverSocketChannel.bind(new InetSocketAddress(ip, 5000));
        // serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        //Selection Key Handling
        while(true) {
            selector.select();

            Iterator<SelectionKey> selectedKeys = selector.selectedKeys().iterator();
            while(selectedKeys.hasNext()) {
                SelectionKey selectedKey = selectedKeys.next();
                if(selectedKey.isAcceptable()) {
                    SocketChannel socketChannel = ((ServerSocketChannel)selectedKey.channel()).accept();
                    socketChannel.configureBlocking(false);
                    switch (socketChannel.socket().getLocalPort()) {
                        case 5000:
                            socketChannel.register(selector, SelectionKey.OP_READ);
                            System.out.println("___brokerId___: " + String.format("%06d", broker) + " | ___Broker Connected___@" + socketChannel.getLocalAddress());
                            break ;
                        case 5001:
                            socketChannel.register(selector, SelectionKey.OP_READ);
                            System.out.println("___marketId___: " + String.format("%06d", market) + " | ___Market Connected___@" + socketChannel.getLocalAddress());
                            break ;
                    }
                    System.out.println("");
                }
                if(selectedKey.isReadable()) {
                    SocketChannel socketChannel = (SocketChannel)selectedKey.channel();
                    ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
                    socketChannel.read(byteBuffer);
                    String result = new String(byteBuffer.array()).trim();
                    switch(socketChannel.socket().getLocalPort()) {
                        case 5000:
                            System.out.println("___Broker@" + String.format("%06d", broker) + " : "  + result);
                            if(result.length() <= 0) {
                                socketChannel.close();
                                System.out.println("___Connection Closed___");
                                System.out.println("___Server Awaiting New Connection___");
                            }
                            break ;
                        case 5001:
                            System.out.println("___Market@" + String.format("%06d", market) + " : " + result);
                            if(result.length() <= 0) {
                                socketChannel.close();
                                System.out.println("___Connection Closed___");
                                System.out.println("___Server Awaiting New Connection___");
                            }
                            break ;
                    }
                }
            }
        }

        // SelectionKey key = null;
        // while(true) {
        //     if(selector.select() <= 0)
        //         continue ;
        //     Set<SelectionKey> selectedKeys = selector.selectedKeys();
        //     Iterator<SelectionKey> iterator = selectedKeys.iterator();
        //     while(iterator.hasNext()) {
        //         key = (SelectionKey)iterator.next();
        //         iterator.remove();
        //         if(key.isAcceptable()) {
        //             SocketChannel socketChannel = serverSocketChannel.accept();
        //             socketChannel.configureBlocking(false);
        //             socketChannel.register(selector, SelectionKey.OP_READ);
        //             System.out.println("___Connection Accepted___: " + socketChannel.getRemoteAddress());
        //         }
        //         if(key.isReadable()) {
        //             SocketChannel socketChannel = (SocketChannel)key.channel();
        //             ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        //             socketChannel.read(byteBuffer);
        //             String result = new String(byteBuffer.array()).trim();
        //             System.out.println("___Recieved Message___: " + result + ". ___Message Lenght___: " + result.length());
        //             if(result.length() <= 0) {
        //                 socketChannel.close();
        //                 System.out.println("___Connection Closed___");
        //                 System.out.println("___Server Awaiting New Connection___");
        //             }
        //         }
        //     }
        // }
    }
}

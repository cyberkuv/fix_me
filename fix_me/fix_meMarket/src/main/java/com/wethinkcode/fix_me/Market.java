package com.wethinkcode.fix_me;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class Market {
    private static BufferedReader input = null;
    public static void main( String[] args ) throws Exception {
        System.out.println("___Market Sends Her greetings___");
        InetSocketAddress socketAddress = new InetSocketAddress(InetAddress.getByName("localhost"), 5001);
        Selector selector = Selector.open();
        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(false);
        socketChannel.connect(socketAddress);
        socketChannel.register(selector, SelectionKey.OP_CONNECT | SelectionKey.OP_READ | SelectionKey.OP_WRITE);
        input = new BufferedReader(new InputStreamReader(System.in));
        while(true) {
            if(selector.select() > 0) {
                Boolean doneStatus = processReadySet(selector.selectedKeys());
                if(doneStatus) {
                    break ;
                }
            }
        }
        socketChannel.close();
    }

    public static Boolean processReadySet(Set readySet) throws Exception {
        SelectionKey key = null;
        Iterator iterator = null;
        iterator = readySet.iterator();
        while(iterator.hasNext()) {
            key = (SelectionKey)iterator.next();
            iterator.remove();
        }
        if(key.isConnectable()) {
            Boolean connected = processConnect(key);
            if(!connected) {
                return true;
            }
        }
        if(key.isReadable()) {
            SocketChannel socketChannel = (SocketChannel)key.channel();
            ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
            socketChannel.read(byteBuffer);
            String result = new String(byteBuffer.array()).trim();
            System.out.println("___Server Responded___: " + result + "___Message Length___: " + result.length());
        }
        if(key.isWritable()) {
            System.out.print("___Enter message___: (type 'exit' to stop): ");
            String msg = input.readLine();
            if(msg.equalsIgnoreCase("exit")) {
                return true;
            }
            SocketChannel socketChannel = (SocketChannel)key.channel();
            ByteBuffer byteBuffer = ByteBuffer.wrap(msg.getBytes());
            socketChannel.write(byteBuffer);
        }
        return false;
    }

    public static Boolean processConnect(SelectionKey key) {
        SocketChannel socketChannel = (SocketChannel)key.channel();
        try {
            while(socketChannel.isConnectionPending()) {
                socketChannel.finishConnect();
            }
        } catch (IOException e) {
            key.cancel();
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
